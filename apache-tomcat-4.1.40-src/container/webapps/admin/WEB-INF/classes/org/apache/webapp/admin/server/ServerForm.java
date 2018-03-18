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

package org.apache.webapp.admin.server;


import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import java.util.List;

/**
 * Form bean for the server form page.  
 * @author Patrick Luby
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class ServerForm extends ActionForm {
    
    // ----------------------------------------------------- Instance Variables
    
    /**
     * The text for the node label.
     */
    private String nodeLabel = null;
    
    /**
     * The text for the port number.
     */    
    private String portNumberText = "8080";
    
    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";

    /**
     * The text for the shutdown text.
     */    
    private String shutdownText = null;
    
    private List debugLvlVals = null;
    
    // ------------------------------------------------------------- Properties
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
     * Return the portNumberText.
     */
    public String getPortNumberText() {
        
        return this.portNumberText;
        
    }
    
    /**
     * Set the portNumberText.
     */
    public void setPortNumberText(String portNumberText) {
        
        this.portNumberText = portNumberText;
        
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
     * Return the Shutdown Text.
     */
    public String getShutdownText() {
        
        return this.shutdownText;
        
    }
    
    /**
     * Set the Shut down  Text.
     */
    public void setShutdownText(String shutdownText) {
        
        this.shutdownText = shutdownText;
        
    }
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        
        this.portNumberText = null;
        this.debugLvl = "0";
        this.shutdownText = null;
        
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
        if (submit != null) {
            
            // check for portNumber -- must not be blank, must be in
            // the range 1 to 65535.
            
            if ((portNumberText == null) || (portNumberText.length() < 1)) {
                errors.add("portNumberText",
                new ActionMessage("error.portNumber.required"));
            } else {
                try {
                    int port = Integer.parseInt(portNumberText);
                    if ((port <= 0) || (port >65535 ))
                        errors.add("portNumberText", 
                            new ActionMessage("error.portNumber.range"));
                } catch (NumberFormatException e) {
                    errors.add("portNumberText", 
                        new ActionMessage("error.portNumber.format"));
                }
            }
        
            // shutdown text can be any non-empty string of atleast 6 characters.
            
            if ((shutdownText == null) || (shutdownText.length() < 7))
                errors.add("shutdownText",
                new ActionMessage("error.shutdownText.length"));
            
        }
        
        return errors;
        
    }
    
}
