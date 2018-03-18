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

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.Lists;

/**
 * The <code>Action</code> that sets up <em>Edit Logger</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class EditLoggerAction extends Action {
    

    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mBServer = null;
    

    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;
    

    // --------------------------------------------------------- Public Methods
    
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException, ServletException {
        
        // Acquire the resources that we need
        HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);
        if (resources == null) {
            resources = getResources(request);
        }
        
        // Acquire a reference to the MBeanServer containing our MBeans
        try {
            mBServer = ((ApplicationServlet) getServlet()).getServer();
        } catch (Throwable t) {
            throw new ServletException
            ("Cannot acquire MBeanServer reference", t);
        }
        
        // Set up the object names of the MBeans we are manipulating
        ObjectName lname = null;
        StringBuffer sb = null;
        try {
            lname = new ObjectName(request.getParameter("select"));
        } catch (Exception e) {
            String message =
                resources.getMessage("error.loggerName.bad",
                                     request.getParameter("select"));
            getServlet().log(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return (null);
        }

        // Fill in the form values for display and editing
        LoggerForm loggerFm = new LoggerForm();
        session.setAttribute("loggerForm", loggerFm);
        loggerFm.setAdminAction("Edit");
        loggerFm.setObjectName(lname.toString());
        loggerFm.setParentObjectName("");        
        sb = new StringBuffer("");
        String host = lname.getKeyProperty("host");
        String context = lname.getKeyProperty("path");        
        if (host!=null) {
            sb.append("Host (" + host + ") > ");
        }
        if (context!=null) {
            sb.append("Context (" + context + ") > ");
        }
        sb.append("Logger");
        loggerFm.setNodeLabel(sb.toString());
        loggerFm.setDebugLvlVals(Lists.getDebugLevels());        
        loggerFm.setVerbosityLvlVals(Lists.getVerbosityLevels());        
        loggerFm.setBooleanVals(Lists.getBooleanValues());        
      
        String attribute = null;
        try {

            // Copy scalar properties

            // Calculate the type of logger we are managing
            attribute = "className";
            String className = 
                (String) mBServer.getAttribute(lname, attribute);
            int period = className.lastIndexOf('.');
            String loggerType = className.substring(period + 1);
            loggerFm.setLoggerType(loggerType);            
            attribute = "debug";
            loggerFm.setDebugLvl
                (((Integer) mBServer.getAttribute(lname, attribute)).toString());            
            attribute = "verbosity";
            loggerFm.setVerbosityLvl
                (((Integer) mBServer.getAttribute(lname, attribute)).toString());
            
            if ("FileLogger".equals(loggerType)) {
            // Initialize rest of variables.
                attribute = "directory";
                loggerFm.setDirectory
                    ((String) mBServer.getAttribute(lname, attribute));
                attribute = "prefix";
                loggerFm.setPrefix
                    ((String) mBServer.getAttribute(lname, attribute));
                attribute = "suffix";
                loggerFm.setSuffix
                    ((String) mBServer.getAttribute(lname, attribute));           
                attribute = "timestamp";
                loggerFm.setTimestamp
                    (((Boolean) mBServer.getAttribute(lname, attribute)).toString());
            }

        } catch (Throwable t) {
            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.get",
                                      attribute), t);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.get",
                                      attribute));
            return (null);
        }
        
        // Forward to the logger display page
        return (mapping.findForward("Logger"));
        
    }


}
