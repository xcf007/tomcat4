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

package org.apache.webapp.admin.service;


import java.net.URLEncoder;
import java.util.Locale;
import java.io.IOException;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;
import org.apache.webapp.admin.TreeControl;
import org.apache.webapp.admin.TreeControlNode;



/**
 * The <code>Action</code> that completes <em>Add Service</em> and
 * <em>Edit Service</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class SaveServiceAction extends Action {


    // ----------------------------------------------------- Instance Variables


    /**
     * Signature for the <code>createStandardEngine</code> operation.
     */
    private String createStandardEngineTypes[] =
    { "java.lang.String",     // parent
      "java.lang.String",     // name
      "java.lang.String",     // defaultHost
    };


    /**
     * Signature for the <code>createStandardService</code> operation.
     */
    private String createStandardServiceTypes[] =
    { "java.lang.String",     // parent
      "java.lang.String",     // name
    };


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
        
        // Identify the requested action
        ServiceForm sform = (ServiceForm) form;
        String adminAction = sform.getAdminAction();
        String sObjectName = sform.getObjectName();
        String eObjectName = sform.getEngineObjectName();

        // Perform a "Create Service" transaction (if requested)
        if ("Create".equals(adminAction)) {

            String operation = null;
            String values[] = null;

            try {

                // Ensure that the requested service name is unique
                ObjectName oname =
                    new ObjectName(TomcatTreeBuilder.SERVICE_TYPE +
                                   ",name=" + sform.getServiceName());
                if (mBServer.isRegistered(oname)) {
                    ActionMessages errors = new ActionMessages();
                    errors.add("serviceName",
                               new ActionMessage("error.serviceName.exists"));
                    saveErrors(request, errors);
                    return (new ActionForward(mapping.getInput()));
                }

                // Look up our MBeanFactory MBean
                ObjectName fname =
                    new ObjectName(TomcatTreeBuilder.FACTORY_TYPE);

                // Create a new StandardService object
                values = new String[2];
                values[0] = TomcatTreeBuilder.SERVER_TYPE;
                values[1] = sform.getServiceName();
                operation = "createStandardService";
                sObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardServiceTypes);

                // Create a new StandardEngine object
                values = new String[3];
                values[0] = sObjectName;
                values[1] = sform.getEngineName();
                values[2] = sform.getDefaultHost();
                if ("".equals(values[2])) {
                    values[2] = null;
                }
                operation = "createStandardEngine";
                eObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardEngineTypes);

                // Add the new Service to our tree control node
                TreeControl control = (TreeControl)
                    session.getAttribute("treeControlTest");
                if (control != null) {
                    String parentName = TomcatTreeBuilder.SERVER_TYPE;
                    TreeControlNode parentNode = control.findNode(parentName);
                    if (parentNode != null) {
                        String nodeLabel =
                            "Service (" + sform.getServiceName() + ")";
                        String encodedName =
                            URLEncoder.encode(sObjectName);
                        TreeControlNode childNode =
                            new TreeControlNode(sObjectName,
                                                "Service.gif",
                                                nodeLabel,
                                                "EditService.do?select=" +
                                                encodedName,
                                                "content",
                                                true);
                        parentNode.addChild(childNode);
                        // FIXME - force a redisplay
                    } else {
                        getServlet().log
                            ("Cannot find parent node '" + parentName + "'");
                    }
                } else {
                    getServlet().log
                        ("Cannot find TreeControlNode!");
                }

            } catch (Exception e) {

                getServlet().log
                    (resources.getMessage(locale, "users.error.invoke",
                                          operation), e);
                response.sendError
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     resources.getMessage(locale, "users.error.invoke",
                                          operation));
                return (null);

            }

        }

        // Perform attribute updates as requested
        String attribute = null;
        try {

            ObjectName soname = new ObjectName(sObjectName);
            ObjectName eoname = new ObjectName(eObjectName);

            attribute = "debug";
            int debug = 0;
            try {
                debug = Integer.parseInt(sform.getDebugLvl());
            } catch (Throwable t) {
                debug = 0;
            }
            mBServer.setAttribute(soname,
                                  new Attribute("debug", new Integer(debug)));
            mBServer.setAttribute(eoname,
                                  new Attribute("debug", new Integer(debug)));

            attribute = "defaultHost";
            String defaultHost = sform.getDefaultHost();
            if ("".equals(defaultHost)) {
                defaultHost = null;
            }
            mBServer.setAttribute(eoname,
                                  new Attribute("defaultHost", defaultHost));

        } catch (Exception e) {

            getServlet().log
                (resources.getMessage(locale, "users.error.attribute.set",
                                      attribute), e);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.attribute.set",
                                      attribute));
            return (null);
        }
        
        // Forward to the success reporting page
        session.removeAttribute(mapping.getAttribute());
        return (mapping.findForward("Save Successful"));
        
    }
    
}
