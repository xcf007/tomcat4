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


package org.apache.webapp.admin.host;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.util.List;

/**
 * Form bean for the host page.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class HostForm extends ActionForm {
    
    // ----------------------------------------------------- Instance Variables
        
    /**
     * The administrative action represented by this form.
     */
    private String adminAction = "Edit";

    /**
     * The object name of the Service this bean refers to.
     */
    private String objectName = null;

    /**
     * The text for the node label. This is of the form 'Host(name)'
     * and is picked up from the node of the tree that is clicked on.
     */
    private String nodeLabel = null;
    
    /**
     * The text for the hostName.
     */
    private String hostName = null;
    
    /**
     * The name of the service this host belongs to.
     */
    private String serviceName = null;
    
    /**
     * The directory for the appBase.
     */
    private String appBase = null;
    
    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";

    /**
     * Boolean for autoDeploy.
     */
    private String autoDeploy = "true";

    /**
     * Boolean for deployXML.
     */
    private String deployXML = "true";

    /**
     * Boolean for liveDeploy.
     */
    private String liveDeploy = "true";
    
    /**
     * Boolean for unpack WARs.
     */
    private String unpackWARs = "true";
    
    /**
     * Set of valid values for debug level.
     */
    private List debugLvlVals = null;
    
    /*
     * Represent boolean (true, false) values for unpackWARs etc.
     */
    private List booleanVals = null;
    
    /*
     * Represent aliases as a List.
     */    
    private List aliasVals = null;
   
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
     * Return the object name of the Host this bean refers to.
     */
    public String getObjectName() {

        return this.objectName;

    }


    /**
     * Set the object name of the Host this bean refers to.
     */
    public void setObjectName(String objectName) {

        this.objectName = objectName;

    }

    
    /**
     * Return the object name of the service this host belongs to.
     */
    public String getServiceName() {

        return this.serviceName;

    }


    /**
     * Set the object name of the Service this host belongs to.
     */
    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;

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
     * Return the host name.
     */
    public String getHostName() {
        
        return this.hostName;
        
    }
    
    /**
     * Set the host name.
     */
    public void setHostName(String hostName) {
        
        this.hostName = hostName;
        
    }
    
    /**
     * Return the appBase.
     */
    public String getAppBase() {
        
        return this.appBase;
        
    }
    
    
    /**
     * Set the appBase.
     */
    
    public void setAppBase(String appBase) {
        
        this.appBase = appBase;
        
    }

    /**
     * Return the autoDeploy.
     */
    public String getAutoDeploy() {
        
        return this.autoDeploy;
        
    }
    
    /**
     * Set the autoDeploy.
     */
    
    public void setAutoDeploy(String autoDeploy) {
        
        this.autoDeploy = autoDeploy;
        
    }

    /**
     * Return the deployXML.
     */
    public String getDeployXML() {
        
        return this.deployXML;
        
    }
    
    /**
     * Set the deployXML.
     */
    
    public void setDeployXML(String deployXML) {
        
        this.deployXML = deployXML;
        
    }

    /**
     * Return the liveDeploy.
     */
    public String getLiveDeploy() {
        
        return this.liveDeploy;
        
    }
    
    /**
     * Set the liveDeploy.
     */
    
    public void setLiveDeploy(String liveDeploy) {
        
        this.liveDeploy = liveDeploy;
        
    }
    
    /**
     * Return the unpackWARs.
     */
    public String getUnpackWARs() {
        
        return this.unpackWARs;
        
    }
    
    /**
     * Set the unpackWARs.
     */
    
    public void setUnpackWARs(String unpackWARs) {
        
        this.unpackWARs = unpackWARs;
        
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
     * Return the List of alias Vals.
     */
    public List getAliasVals() {
        
        return this.aliasVals;
        
    }
    
    /**
     * Set the alias Vals.
     */
    public void setAliasVals(List aliasVals) {
        
        this.aliasVals = aliasVals;
        
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
        this.serviceName = null;
        this.hostName = null;
        this.appBase = null;
        this.autoDeploy = "true";
        this.deployXML = "true";
        this.liveDeploy = "true";
        this.debugLvl = "0";
        this.unpackWARs = "true";
        
    }
    
     /**
     * Render this object as a String.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("HostForm[adminAction=");
        sb.append(adminAction);
        sb.append(",debugLvl=");
        sb.append(debugLvl);
        sb.append(",appBase=");
        sb.append(appBase);
        sb.append(",autoDeploy=");
        sb.append(autoDeploy);
        sb.append(",deployXML=");
        sb.append(deployXML);
        sb.append(",liveDeploy=");
        sb.append(liveDeploy);
        sb.append(",unpackWARs=");
        sb.append(unpackWARs);
        sb.append("',objectName='");
        sb.append(objectName);
        sb.append("',hostName=");
        sb.append(hostName);
        sb.append("',serviceName=");
        sb.append(serviceName);
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
        
        // front end validation when save is clicked.
        if (submit != null) {
            
            // hostName cannot be null
            if ((hostName== null) || (hostName.length() < 1)) {
                errors.add("hostName", new ActionMessage("error.hostName.required"));
            }
            
            // appBase cannot be null
            if ((appBase == null) || (appBase.length() < 1)) {
                errors.add("appBase", new ActionMessage("error.appBase.required"));
            }
            
        }        
        return errors;
        
    }
    
}
