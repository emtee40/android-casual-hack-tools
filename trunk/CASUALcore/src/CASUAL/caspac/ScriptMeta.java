/*
 * Copyright (C) 2015 adamoutler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/ .
 */
package CASUAL.caspac;

import CASUAL.CASUALSettings;
import CASUAL.CASUALSettings.MonitorMode;
import CASUAL.FileOperations;
import CASUAL.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Meta provides a holding area and parsing for the metadata in the script.
 */
public class ScriptMeta {

    /**
     * Minimum Build Number revision required for script.
     */
    private String minBuildNumber = "";
    /**
     * The revision of this script (used to determine update required status).
     */
    private String scriptRevision = "";
    /**
     * Unique script identification string.
     */
    private String uniqueIdentifier = "";
    /**
     * URL for support of this Script.
     */
    private String supportURL = "";
    /**
     * Message to be shown during script update.
     */
    private String updateMessage = "";
    /**
     * Message to be shown if killswitch is thrown on script.
     */
    private String killSwitchMessage = "";
    /**
     * Properties extracted from meta.properties.
     */
    private Properties metaProp;
    /**
     * List of expected MD5s for all files in the script (except meta).
     */
    private List<String> md5s = new ArrayList<String>();
    private Script script;

    private CASUALSettings.MonitorMode monitorMode = MonitorMode.ADB;

    /**
     * constructor for new Meta.
     *
     * @param script script this meta belongs to
     */
    public ScriptMeta(final Script script) {
        this.script = script;
        metaProp = new Properties();
    }

    /**
     * Constructor for meta if properties file is available.
     *
     * @param prop properties file to load
     * @param script script this meta belongs to
     */
    public ScriptMeta(Properties prop, final Script script) {
        this.script = script;
        metaProp = prop;
        setVariablesFromProperties(prop);
    }

    /**
     * Constructor for meta if inputstrem properties is available.
     *
     * @param prop properties as inputstream.
     * @param script script this meta belongs to
     * @throws IOException when permissions problem exists.
     */
    public ScriptMeta(InputStream prop, final Script script) throws IOException {
        this.script = script;
        metaProp.load(prop);
        setVariablesFromProperties(metaProp);
    }

    /**
     * verifies metadata is not empty
     *
     * @return true if filled in
     */
    public boolean verifyMeta() {
        if (minBuildNumber.isEmpty()) {
            Log.level0Error(script.getName() + "minBuildNumber is empty! Cannot continue.");
            return false;
        }
        if (scriptRevision.isEmpty()) {
            Log.level0Error(script.getName() + "scriptRevision is empty! Cannot continue.");
            return false;
        }
        if (uniqueIdentifier.isEmpty()) {
            Log.level0Error(script.getName() + "uniqueIdentifier  is empty! Cannot continue.");
            return false;
        }
        if (supportURL.isEmpty()) {
            Log.level0Error(script.getName() + "supportURL  is empty! Cannot continue.");
            return false;
        }
        if (updateMessage.isEmpty()) {
            Log.level0Error(script.getName() + "updateMessage is empty! Cannot continue.");
            return false;
        }
        if (killSwitchMessage.isEmpty()) {
            Log.level0Error(script.getName() + " killSwitchMessage is empty! Cannot continue.");
            return false;
        }
        return true;
    }

    /**
     * gets the metadata as an inputstream
     *
     * @return metadata as inputstream.
     */
    public InputStream getMetaInputStream() {
        setPropsFromVariables();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            metaProp.store(output, "This properties file was generated by CASUAL");
        } catch (IOException ex) {
            Log.errorHandler(ex);
        }
        return new ByteArrayInputStream(output.toByteArray());
    }

    /**
     * writes meta properties to a file
     *
     * @param output file to write
     * @return true if file was written
     * @throws FileNotFoundException when file cannot be created
     * @throws IOException when permissions problem exists.
     */
    public boolean write(String output) throws FileNotFoundException, IOException {
        File f = new File(output);
        return write(f);
    }

    /**
     * writes meta properties to a file
     *
     * @param output file to write
     * @return true if file was written
     * @throws FileNotFoundException when file cannot be created
     * @throws IOException when permissions problem exists.
     */
    public boolean write(File output) throws FileNotFoundException, IOException {
        setPropsFromVariables();
        FileOutputStream fos = new FileOutputStream(output);
        metaProp.store(fos, "This properties file was generated by CASUAL");
        return new FileOperations().verifyExists(output.toString());
    }

    /**
     * sets the properties object from local variables for writing
     *
     * @return this ScriptMeta
     */
    public ScriptMeta setPropsFromVariables() {
        metaProp.setProperty("CASUAL.minBuildNumber", minBuildNumber);
        metaProp.setProperty("Script.Revision", scriptRevision);
        metaProp.setProperty("Script.ID", uniqueIdentifier);
        metaProp.setProperty("Script.SupportURL", supportURL);
        metaProp.setProperty("Script.UpdateMessage", updateMessage);
        metaProp.setProperty("Script.KillSwitchMessage", killSwitchMessage);
        metaProp.setProperty("Script.MonitorMode", this.monitorMode.name());
        return this;
    }

    /**
     * sets the variables from properties object for loading
     *
     * @param prop properties file
     */
    private void setVariablesFromProperties(Properties prop) {
        minBuildNumber = prop.getProperty("CASUAL.minBuildNumber", "");
        if (minBuildNumber.isEmpty()){
            minBuildNumber = prop.getProperty("CASUAL.minSVN", "");
        }
        scriptRevision = prop.getProperty("Script.Revision", "");
        uniqueIdentifier = prop.getProperty("Script.ID", "");
        supportURL = prop.getProperty("Script.SupportURL", "");
        updateMessage = prop.getProperty("Script.UpdateMessage", "");
        killSwitchMessage = prop.getProperty("Script.KillSwitchMessage", "");
        md5s = new ArrayList<String>();
        int i = 0;
        monitorMode = MonitorMode.valueOf(prop.getProperty("Script.MonitorMode", "ADB"));
        while (!prop.getProperty("Script.MD5[" + i + "]", "").isEmpty()) {
            md5s.add(prop.getProperty("Script.MD5[" + i + "]"));
            i++;
        }
    }

    /**
     *
     * @param prop properties file to load
     * @return this ScriptMeta
     */
    public ScriptMeta load(Properties prop) {
        this.metaProp = prop;
        setVariablesFromProperties(metaProp);
        return this;
    }

    void load(BufferedInputStream streamFileFromZip) {
        try {
            this.metaProp.load(streamFileFromZip);
        } catch (IOException ex) {
            Log.errorHandler(ex);
        }
        setVariablesFromProperties(metaProp);
    }

    /**
     * Minimum CASUAL minBuildNumber version requied by this script.
     *
     * @return minBuildNumber version required.
     */
    public int minBuildNumber() {
        return Integer.parseInt(minBuildNumber);
    }

    /**
     * @return the minBuildNumber
     */
    public String getMinBuildNumberVersion() {
        return minBuildNumber;
    }

    /**
     * @param minBuildNumberVersion the minBuildNumber to set
     * @return this ScriptMeta
     */
    public ScriptMeta setMinBuildNumberVersion(String minBuildNumberVersion) {
        this.minBuildNumber = minBuildNumberVersion;
        return this;
    }

    /**
     * @return the scriptRevision
     */
    public String getScriptRevision() {
        return scriptRevision;
    }

    /**
     * @param scriptRevision the scriptRevision to set
     * @return this ScriptMeta
     */
    public ScriptMeta setScriptRevision(String scriptRevision) {
        this.scriptRevision = scriptRevision;
        return this;
    }

    /**
     * @return the uniqueIdentifier
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * @param uniqueIdentifier the uniqueIdentifier to set
     * @return this ScriptMeta
     */
    public ScriptMeta setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
        return this;
    }

    /**
     * @return the supportURL
     */
    public String getSupportURL() {
        return supportURL;
    }

    /**
     * @param supportURL the supportURL to set
     * @return this ScriptMeta
     */
    public ScriptMeta setSupportURL(String supportURL) {
        this.supportURL = supportURL;
        return this;
    }

    /**
     * @return the updateMessage
     */
    public String getUpdateMessage() {
        return updateMessage;
    }

    /**
     * @param updateMessage the updateMessage to set
     * @return this ScriptMeta
     */
    public ScriptMeta setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
        return this;
    }

    /**
     * @return the killSwitchMessage
     */
    public String getKillSwitchMessage() {
        return killSwitchMessage;
    }

    /**
     * @param killSwitchMessage the killSwitchMessage to set
     * @return this ScriptMeta
     */
    public ScriptMeta setKillSwitchMessage(String killSwitchMessage) {
        this.killSwitchMessage = killSwitchMessage;
        return this;
    }

    /**
     * @return the metaProp
     */
    public Properties getMetaProp() {
        return metaProp;
    }

    /**
     * @param metaProp the metaProp to set
     * @return this ScriptMeta
     */
    public ScriptMeta setMetaProp(Properties metaProp) {
        this.metaProp = metaProp;
        return this;
    }

    /**
     * @return the md5s
     */
    public List<String> getMd5s() {
        return md5s;
    }

    /**
     * @param md5s the md5s to set
     * @return this ScriptMeta
     */
    public ScriptMeta setMd5s(List<String> md5s) {
        this.md5s = md5s;
        return this;
    }

    /**
     * @return the script
     */
    public Script getScript() {
        return script;
    }

    /**
     * @param script the script to set
     * @return this ScriptMeta
     */
    public ScriptMeta setScript(Script script) {
        this.script = script;
        return this;
    }

    /**
     * @return the monitorMode
     */
    public CASUALSettings.MonitorMode getMonitorMode() {
        return monitorMode;
    }

    /**
     * @param monitorMode the monitorMode to set
     * @return  this ScriptMeta
     */
    public ScriptMeta setMonitorMode(CASUALSettings.MonitorMode monitorMode) {
        this.monitorMode = monitorMode;
        return this;
    }

}
