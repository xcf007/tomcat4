/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.webapp.admin.logger;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.util.List;

/**
 * Form bean for the logger page.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class LoggerForm extends ActionForm {
    
    // ----------------------------------------------------- Instance Variables
    
    /**
     * The administrative action represented by this form.
     */
    private String adminAction = "Edit";

  /**
     * The object name of the Logger this bean refers to.
     */
    private String objectName = null;
   
    /**
     * The object name of the parent of this Logger.
     */
    private String parentObjectName = null;
   
    /**
     * The text for the logger type. 
     * Specifies if it is a FileLogger, or SysErr or SysOut Logger.
     */
    private String loggerType = null;

    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";
    
    /**
     * The text for the verbosity.
     */
    private String verbosityLvl = null;
    
   /**
     * The text for the directory.
     */
    private String directory = null;
    
    /**
     * The text for the prefix.
     */
    private String prefix = null;
    
    /**
     * The text for the timestamp.
     */
    private String timestamp = null;
    
    /**
     * The text for the suffix.
     */
    private String suffix = null;
    
    /**
     * The text for the node label.
     */
    private String nodeLabel = null;
    
    private List debugLvlVals = null;
    private List verbosityLvlVals = null;
    private List booleanVals = null;
    private List loggerTypeVals = null;

    // ------------------------------------------------------------- Properties
    
   /**
     * Return the administrative action represented by this form.
     */
    public String getAdminAction() {

        return this.adminAction;

    }


    /**
     * Set the administrative action represented by this form.
     */
    public void setAdminAction(String adminAction) {

        this.adminAction = adminAction;

    }

    /**
     * Return the object name of the Logger this bean refers to.
     */
    public String getObjectName() {

        return this.objectName;

    }


    /**
     * Set the object name of the Logger this bean refers to.
     */
    public void setObjectName(String objectName) {

        this.objectName = objectName;

    }
    
    
    /**
     * Return the parent object name of the Logger this bean refers to.
     */
    public String getParentObjectName() {

        return this.parentObjectName;

    }


    /**
     * Set the parent object name of the Logger this bean refers to.
     */
    public void setParentObjectName(String parentObjectName) {

        this.parentObjectName = parentObjectName;

    }
    
    /**
     * Return the Logger type.
     */
    public String getLoggerType() {
        
        return this.loggerType;
        
    }
    
    /**
     * Set the Logger type.
     */
    public void setLoggerType(String loggerType) {
        
        this.loggerType = loggerType;
        
    }
    
    /**
     * Return the verbosityLvl.
     */
    public String getVerbosityLvl() {
        
        return this.verbosityLvl;
        
    }
    
    /**
     * Set the verbosityLvl.
     */
    public void setVerbosityLvl(String verbosityLvl) {
        
        this.verbosityLvl = verbosityLvl;
        
    }
        
    /**
     * Return the Debug Level Text.
     */
    public String getDebugLvl() {
        
        return this.debugLvl;
        
    }
    
    /**
     * Set the Debug Level Text.
     */
    public void setDebugLvl(String debugLvl) {
        
        this.debugLvl = debugLvl;
        
    }
    
    /**
     * Return the directory.
     */
    public String getDirectory() {
        
        return this.directory;
        
    }
    
    /**
     * Set the directory.
     */
    public void setDirectory(String directory) {
        
        this.directory = directory;
        
    }
    
    /**
     * Return the prefix.
     */
    public String getPrefix() {
        
        return this.prefix;
        
    }
    
    /**
     * Set the prefix.
     */
    public void setPrefix(String prefix) {
        
        this.prefix = prefix;
        
    }
    /**
     * Return the suffix.
     */
    public String getSuffix() {
        
        return this.suffix;
        
    }
    
    /**
     * Set the suffix.
     */
    public void setSuffix(String suffix) {
        
        this.suffix = suffix;
        
    }
     
    /**
     * Return the label of the node that was clicked.
     */
    public String getNodeLabel() {
        
        return this.nodeLabel;
        
    }
    
    /**
     * Set the node label.
     */
    public void setNodeLabel(String nodeLabel) {
        
        this.nodeLabel = nodeLabel;
        
    }

    /**
     * Return the timestamp.
     */
    public String getTimestamp() {
        
        return this.timestamp;
        
    }
    
    /**
     * Set the timestamp.
     */
    public void setTimestamp(String timestamp) {
        
        this.timestamp = timestamp;
        
    }
    
    /**
     * Return the debugVals.
     */
    public List getDebugLvlVals() {
        
        return this.debugLvlVals;
        
    }
    
    /**
     * Set the debugVals.
     */
    public void setDebugLvlVals(List debugLvlVals) {
        
        this.debugLvlVals = debugLvlVals;
        
    }
    
    
    /**
     * Return the verbosity level values.
     */
    public List getVerbosityLvlVals() {
        
        return this.verbosityLvlVals;
        
    }
    
    /**
     * Set the verbosity level values.
     */
    public void setVerbosityLvlVals(List verbosityLvlVals) {
        
        this.verbosityLvlVals = verbosityLvlVals;
        
    }
    
    /**
     * Return the booleanVals.
     */
    public List getBooleanVals() {
        
        return this.booleanVals;
        
    }
    
    /**
     * Set the booleanVals.
     */
    public void setBooleanVals(List booleanVals) {
        
        this.booleanVals = booleanVals;
        
    }
    
    /**
     * Return the loggerTypeVals.
     */
    public List getLoggerTypeVals() {
        
        return this.loggerTypeVals;
        
    }
    
    /**
     * Set the loggerTypeVals.
     */
    public void setLoggerTypeVals(List loggerTypeVals) {
        
        this.loggerTypeVals = loggerTypeVals;
        
    }
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        
        this.objectName = null;
        this.loggerType = null;
        this.parentObjectName = null;
        this.debugLvl = "0";
        this.verbosityLvl = "0";        
        this.directory = null;
        this.prefix = null;
        this.suffix = null;
        this.timestamp = "false";
        
    }
    
    /**
     * Render this object as a String.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("LoggerForm[adminAction=");
        sb.append(adminAction);
        sb.append(",debugLvl=");
        sb.append(debugLvl);
        sb.append(",verbosityLvl=");
        sb.append(verbosityLvl);
        sb.append(",directory=");
        sb.append(directory);
        sb.append(",prefix=");
        sb.append(prefix);
        sb.append(",suffix=");
        sb.append(suffix);
        sb.append(",loggerType=");
        sb.append(loggerType);
        sb.append(",objectName=");
        sb.append(objectName);
        sb.append(",parentObjectName=");
        sb.append(parentObjectName);
        sb.append("]");
        return (sb.toString());

    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    
    public ActionErrors validate(ActionMapping mapping,
    HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        String submit = request.getParameter("submit");        
        String type = request.getParameter("loggerType");
        
        // front end validation when save is clicked.
        // these checks should be done only if it is FileLogger. 
        // No checks needed otherwise        
        if ((submit != null)
           && ("FileLogger").equalsIgnoreCase(type)) {
             
            if ((directory == null) || (directory.length() < 1)) {
                errors.add("directory",
                new ActionMessage("error.directory.required"));
            }
                         
            if ((prefix == null) || (prefix.length() < 1)) {
                errors.add("prefix",
                new ActionMessage("error.prefix.required"));
            }
                         
            if ((suffix == null) || (suffix.length() < 1)) {
                errors.add("suffix",
                new ActionMessage("error.suffix.required"));
            }            
        }
        
        return errors;
    }
}
