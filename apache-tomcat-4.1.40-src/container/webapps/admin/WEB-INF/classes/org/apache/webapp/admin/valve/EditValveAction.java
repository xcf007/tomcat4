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

package org.apache.webapp.admin.valve;

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
 * A generic <code>Action</code> that sets up <em>Edit 
 * Valve </em> transactions, based on the type of Valve.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class EditValveAction extends Action {
    

    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mBServer = null;
    

    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;
    
    private HttpSession session = null;
    private Locale locale = null;
    private String parent = null;
    
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
        session = request.getSession();
        locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);
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
        ObjectName vname = null;
        try {
            vname = new ObjectName(request.getParameter("select"));
        } catch (Exception e) {
            String message =
                resources.getMessage("error.valveName.bad",
                                     request.getParameter("select"));
            getServlet().log(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return (null);
        }
        
       parent = request.getParameter("parent");
       String valveType = null;
       String attribute = null;
       
       // Find what type of Valve this is
       try {    
            attribute = "className";
            String className = (String) 
                mBServer.getAttribute(vname, attribute);
            int period = className.lastIndexOf(".");
            if (period >= 0)
                valveType = className.substring(period + 1);
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

        // Forward to the appropriate valve display page        
        if ("AccessLogValve".equalsIgnoreCase(valveType)) {
               setUpAccessLogValve(vname, response);
        } else if ("RemoteAddrValve".equalsIgnoreCase(valveType)) {
               setUpRemoteAddrValve(vname, response);
        } else if ("RemoteHostValve".equalsIgnoreCase(valveType)) {
                setUpRemoteHostValve(vname, response);
        } else if ("RequestDumperValve".equalsIgnoreCase(valveType)) {
               setUpRequestDumperValve(vname, response);
        } else if ("SingleSignOn".equalsIgnoreCase(valveType)) {
               setUpSingleSignOnValve(vname, response);
        }
       
        
        return (mapping.findForward(valveType));
                
    }

    private void setUpAccessLogValve(ObjectName vname,
                                        HttpServletResponse response) 
    throws IOException {
        // Fill in the form values for display and editing
        AccessLogValveForm valveFm = new AccessLogValveForm();
        session.setAttribute("accessLogValveForm", valveFm);
        valveFm.setAdminAction("Edit");
        valveFm.setObjectName(vname.toString()); 
        valveFm.setParentObjectName(parent);
        String valveType = "AccessLogValve";
        StringBuffer sb = new StringBuffer("");
        String host = vname.getKeyProperty("host");
        String context = vname.getKeyProperty("path");        
        if (host!=null) {
            sb.append("Host (" + host + ") > ");
        }
        if (context!=null) {
            sb.append("Context (" + context + ") > ");
        }
        sb.append("Valve");
        valveFm.setNodeLabel(sb.toString());
        valveFm.setValveType(valveType);
        valveFm.setDebugLvlVals(Lists.getDebugLevels());
        valveFm.setBooleanVals(Lists.getBooleanValues());
        String attribute = null;
        try {
            
            // Copy scalar properties
            attribute = "debug";
            valveFm.setDebugLvl
                (((Integer) mBServer.getAttribute(vname, attribute)).toString());
            attribute = "directory";
            valveFm.setDirectory
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "pattern";
            valveFm.setPattern
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "prefix";
            valveFm.setPrefix
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "suffix";
            valveFm.setSuffix
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "resolveHosts";
            valveFm.setResolveHosts
                (((Boolean) mBServer.getAttribute(vname, attribute)).toString());
            attribute = "rotatable";
            valveFm.setRotatable
                (((Boolean) mBServer.getAttribute(vname, attribute)).toString());

        } catch (Throwable t) {
            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.get",
                                      attribute), t);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.get",
                                      attribute));
        }     
    }

    private void setUpRequestDumperValve(ObjectName vname,
                                        HttpServletResponse response) 
    throws IOException {
        // Fill in the form values for display and editing
        RequestDumperValveForm valveFm = new RequestDumperValveForm();
        session.setAttribute("requestDumperValveForm", valveFm);
        valveFm.setAdminAction("Edit");
        valveFm.setObjectName(vname.toString()); 
        valveFm.setParentObjectName(parent);
        String valveType = "RequestDumperValve";
        StringBuffer sb = new StringBuffer("Valve (");
        sb.append(valveType);
        sb.append(")");
        valveFm.setNodeLabel(sb.toString());
        valveFm.setValveType(valveType);
    }

    private void setUpSingleSignOnValve(ObjectName vname,
                                        HttpServletResponse response) 
    throws IOException {
        // Fill in the form values for display and editing
        SingleSignOnValveForm valveFm = new SingleSignOnValveForm();
        session.setAttribute("singleSignOnValveForm", valveFm);
        valveFm.setAdminAction("Edit");
        valveFm.setObjectName(vname.toString()); 
        valveFm.setParentObjectName(parent);
        String valveType = "SingleSignOn";
        StringBuffer sb = new StringBuffer("Valve (");
        sb.append(valveType);
        sb.append(")");
        valveFm.setNodeLabel(sb.toString());
        valveFm.setValveType(valveType);
        valveFm.setDebugLvlVals(Lists.getDebugLevels());
        String attribute = null;
        try {
            
            // Copy scalar properties
            attribute = "debug";
            valveFm.setDebugLvl
                (((Integer) mBServer.getAttribute(vname, attribute)).toString());
        } catch (Throwable t) {
            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.get",
                                      attribute), t);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.get",
                                      attribute));
        }     
    }


    private void setUpRemoteAddrValve(ObjectName vname,
                                        HttpServletResponse response) 
    throws IOException {
        // Fill in the form values for display and editing
        RemoteAddrValveForm valveFm = new RemoteAddrValveForm();
        session.setAttribute("remoteAddrValveForm", valveFm);
        valveFm.setAdminAction("Edit");
        valveFm.setObjectName(vname.toString()); 
        valveFm.setParentObjectName(parent);
        String valveType = "RemoteAddrValve";
        StringBuffer sb = new StringBuffer("Valve (");
        sb.append(valveType);
        sb.append(")");
        valveFm.setNodeLabel(sb.toString());
        valveFm.setValveType(valveType);
        String attribute = null;
        try {
            
            // Copy scalar properties
            attribute = "allow";
            valveFm.setAllow
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "deny";
            valveFm.setDeny
                ((String) mBServer.getAttribute(vname, attribute));
                        
        } catch (Throwable t) {
            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.get",
                                      attribute), t);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.get",
                                      attribute));
        }     
    }

    private void setUpRemoteHostValve(ObjectName vname,
                                        HttpServletResponse response) 
    throws IOException {
        // Fill in the form values for display and editing
        RemoteHostValveForm valveFm = new RemoteHostValveForm();
        session.setAttribute("remoteHostValveForm", valveFm);
        valveFm.setAdminAction("Edit");
        valveFm.setObjectName(vname.toString()); 
        valveFm.setParentObjectName(parent);
        String valveType = "RemoteHostValve";
        StringBuffer sb = new StringBuffer("Valve (");
        sb.append(valveType);
        sb.append(")");
        valveFm.setNodeLabel(sb.toString());
        valveFm.setValveType(valveType);
        String attribute = null;
        try {
            
            // Copy scalar properties
            attribute = "allow";
            valveFm.setAllow
                ((String) mBServer.getAttribute(vname, attribute));
            attribute = "deny";
            valveFm.setDeny
                ((String) mBServer.getAttribute(vname, attribute));
                        
        } catch (Throwable t) {
            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.get",
                                      attribute), t);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.get",
                                      attribute));
        }     
    }
    
}
