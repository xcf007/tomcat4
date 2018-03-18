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

package org.apache.webapp.admin.realm;

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
 * Realm </em> transactions, based on the type of Realm.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class EditRealmAction extends Action {


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
    private HttpServletRequest request = null;

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
        this.request = request;
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
        ObjectName rname = null;
        try {
            rname = new ObjectName(request.getParameter("select"));
        } catch (Exception e) {
            String message =
                resources.getMessage("error.realmName.bad",
                                     request.getParameter("select"));
            getServlet().log(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return (null);
        }

       String realmType = null;
       String attribute = null;

       // Find what type of Realm this is
       try {
            attribute = "className";
            String className = (String)
                mBServer.getAttribute(rname, attribute);
            int period = className.lastIndexOf(".");
            if (period >= 0)
                realmType = className.substring(period + 1);
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

        // Forward to the appropriate realm display page

        if ("UserDatabaseRealm".equalsIgnoreCase(realmType)) {
               setUpUserDatabaseRealm(rname, response);
        } else if ("MemoryRealm".equalsIgnoreCase(realmType)) {
               setUpMemoryRealm(rname, response);
        } else if ("JDBCRealm".equalsIgnoreCase(realmType)) {
               setUpJDBCRealm(rname, response);
        } else if ("JNDIRealm".equalsIgnoreCase(realmType)) {
               setUpJNDIRealm(rname, response);
        } else if ("DataSourceRealm".equalsIgnoreCase(realmType)) {
               setUpDataSourceRealm(rname, response);
        }

        return (mapping.findForward(realmType));

    }

    private void setUpUserDatabaseRealm(ObjectName rname,
                                        HttpServletResponse response)
    throws IOException {
        // Fill in the form values for display and editing
        UserDatabaseRealmForm realmFm = new UserDatabaseRealmForm();
        session.setAttribute("userDatabaseRealmForm", realmFm);
        realmFm.setAdminAction("Edit");
        realmFm.setObjectName(rname.toString());
        String realmType = "UserDatabaseRealm";
        StringBuffer sb = new StringBuffer("");
        String host = rname.getKeyProperty("host");
        String context = rname.getKeyProperty("path");
        if (host!=null) {
            sb.append("Host (" + host + ") > ");
        }
        if (context!=null) {
            sb.append("Context (" + context + ") > ");
        }
        sb.append("Realm");
        realmFm.setNodeLabel(sb.toString());
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(Lists.getDebugLevels());
        realmFm.setAllowDeletion(allowDeletion(rname));

        String attribute = null;
        try {

            // Copy scalar properties
            attribute = "debug";
            realmFm.setDebugLvl
                (((Integer) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "resourceName";
            realmFm.setResource
                ((String) mBServer.getAttribute(rname, attribute));

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

    private void setUpMemoryRealm(ObjectName rname,
                                        HttpServletResponse response)
    throws IOException {
        // Fill in the form values for display and editing
        MemoryRealmForm realmFm = new MemoryRealmForm();
        session.setAttribute("memoryRealmForm", realmFm);
        realmFm.setAdminAction("Edit");
        realmFm.setObjectName(rname.toString());
        String realmType = "MemoryRealm";
        StringBuffer sb = new StringBuffer("Realm (");
        sb.append(realmType);
        sb.append(")");
        realmFm.setNodeLabel(sb.toString());
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(Lists.getDebugLevels());
        realmFm.setAllowDeletion(allowDeletion(rname));

        String attribute = null;
        try {

            // Copy scalar properties
            attribute = "debug";
            realmFm.setDebugLvl
                (((Integer) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "pathname";
            realmFm.setPathName
                ((String) mBServer.getAttribute(rname, attribute));

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

    private void setUpJDBCRealm(ObjectName rname,
                                        HttpServletResponse response)
    throws IOException {
        // Fill in the form values for display and editing
        JDBCRealmForm realmFm = new JDBCRealmForm();
        session.setAttribute("jdbcRealmForm", realmFm);
        realmFm.setAdminAction("Edit");
        realmFm.setObjectName(rname.toString());
        String realmType = "JDBCRealm";
        StringBuffer sb = new StringBuffer("Realm (");
        sb.append(realmType);
        sb.append(")");
        realmFm.setNodeLabel(sb.toString());
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(Lists.getDebugLevels());
        realmFm.setAllowDeletion(allowDeletion(rname));

        String attribute = null;
        try {

            // Copy scalar properties
            attribute = "debug";
            realmFm.setDebugLvl
                (((Integer) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "digest";
            realmFm.setDigest
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "driverName";
            realmFm.setDriver
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "roleNameCol";
            realmFm.setRoleNameCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userNameCol";
            realmFm.setUserNameCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userCredCol";
            realmFm.setPasswordCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userTable";
            realmFm.setUserTable
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userRoleTable";
            realmFm.setRoleTable
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionName";
            realmFm.setConnectionName
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionPassword";
            realmFm.setConnectionPassword
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionURL";
            realmFm.setConnectionURL
                ((String) mBServer.getAttribute(rname, attribute));

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

    private void setUpJNDIRealm(ObjectName rname,
                                        HttpServletResponse response)
    throws IOException {
        // Fill in the form values for display and editing
        JNDIRealmForm realmFm = new JNDIRealmForm();
        session.setAttribute("jndiRealmForm", realmFm);
        realmFm.setAdminAction("Edit");
        realmFm.setObjectName(rname.toString());
        String realmType = "JNDIRealm";
        StringBuffer sb = new StringBuffer("Realm (");
        sb.append(realmType);
        sb.append(")");
        realmFm.setNodeLabel(sb.toString());
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(Lists.getDebugLevels());
        realmFm.setSearchVals(Lists.getBooleanValues());
        realmFm.setAllowDeletion(allowDeletion(rname));

        String attribute = null;
        try {

            // Copy scalar properties
            attribute = "debug";
            realmFm.setDebugLvl
                (((Integer) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "digest";
            realmFm.setDigest
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userSubtree";
            realmFm.setUserSubtree
                    (((Boolean) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "roleSubtree";
            realmFm.setRoleSubtree
                    (((Boolean) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "userRoleName";
            realmFm.setUserRoleName
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "roleName";
            realmFm.setRoleName
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "roleBase";
            realmFm.setRoleBase
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "roleSearch";
            realmFm.setRolePattern
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "contextFactory";
            realmFm.setContextFactory
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userBase";
            realmFm.setUserBase
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userPassword";
            realmFm.setUserPassword
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userPattern";
            realmFm.setUserPattern
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userSearch";
            realmFm.setUserSearch
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionName";
            realmFm.setConnectionName
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionPassword";
            realmFm.setConnectionPassword
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "connectionURL";
            realmFm.setConnectionURL
                ((String) mBServer.getAttribute(rname, attribute));

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

    private void setUpDataSourceRealm(ObjectName rname,
                                        HttpServletResponse response)
    throws IOException {
        // Fill in the form values for display and editing
        DataSourceRealmForm realmFm = new DataSourceRealmForm();
        session.setAttribute("dataSourceRealmForm", realmFm);
        realmFm.setAdminAction("Edit");
        realmFm.setObjectName(rname.toString());
        String realmType = "DataSourceRealm";
        StringBuffer sb = new StringBuffer();
        sb.append(resources.getMessage(locale,
                "server.service.treeBuilder.realm"));
        sb.append(" (");
        sb.append(realmType);
        sb.append(")");
        realmFm.setNodeLabel(sb.toString());
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(Lists.getDebugLevels());
        realmFm.setAllowDeletion(allowDeletion(rname));
        realmFm.setBooleanVals(Lists.getBooleanValues());
        
        String attribute = null;
        try {

            // Copy scalar properties
            attribute = "dataSourceName";
            realmFm.setDataSourceName
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "debug";
            realmFm.setDebugLvl
                (((Integer) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "digest";
            realmFm.setDigest
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "localDataSource";
            realmFm.setLocalDataSource
                (((Boolean) mBServer.getAttribute(rname, attribute)).toString());
            attribute = "roleNameCol";
            realmFm.setRoleNameCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userCredCol";
            realmFm.setUserCredCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userNameCol";
            realmFm.setUserNameCol
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userRoleTable";
            realmFm.setUserRoleTable
                ((String) mBServer.getAttribute(rname, attribute));
            attribute = "userTable";
            realmFm.setUserTable
                ((String) mBServer.getAttribute(rname, attribute));

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

    /*
     * Check if "delete this realm" operation should be enabled.
     * this operation is not allowed in case the realm is under service,
     * host or context that the admin app runs on.
     * return "true" if deletion is allowed.
     */

    private String allowDeletion(ObjectName rname) {

     boolean retVal = true;
     try{
        // admin app's values
        String adminService = Lists.getAdminAppService(
                              mBServer, rname.getDomain(),request);
        String adminHost = request.getServerName();
        String adminContext = request.getContextPath();

        String thisService = rname.getKeyProperty("service");
        String thisHost = rname.getKeyProperty("host");
        String thisContext = rname.getKeyProperty("path");

        // realm is under context
        if (thisContext!=null) {
            retVal = !(thisContext.equalsIgnoreCase(adminContext));
        } else if (thisHost != null) {
            // realm is under host
            retVal = !(thisHost.equalsIgnoreCase(adminHost));
        } else {
            // realm is under service
            retVal = !(thisService.equalsIgnoreCase(adminService));
        }

     } catch (Exception e) {
           getServlet().log("Error getting admin service, host or context", e);
     }
        return new Boolean(retVal).toString();
    }
}
