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

package org.apache.webapp.admin.context;


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
 * The <code>Action</code> that completes <em>Add Context</em> and
 * <em>Edit Context</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class SaveContextAction extends Action {


    // ----------------------------------------------------- Instance Variables

    /**
     * Signature for the <code>createStandardContext</code> operation.
     */
    private String createStandardContextTypes[] =
    { "java.lang.String",     // parent
      "java.lang.String",     // path
      "java.lang.String",     // docBase
    };

   /**
     * Signature for the <code>createStandardLoader</code> operation.
     */
    private String createStandardLoaderTypes[] =
    { "java.lang.String",     // parent
    };

   /**
     * Signature for the <code>createStandardManager</code> operation.
     */
    private String createStandardManagerTypes[] =
    { "java.lang.String",     // parent
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
        ContextForm cform = (ContextForm) form;
        String adminAction = cform.getAdminAction();
        String cObjectName = cform.getObjectName();
        String lObjectName = cform.getLoaderObjectName();
        String mObjectName = cform.getManagerObjectName();
        String oNamePath = "";
        if ((cform.getPath() == null) || (cform.getPath().length()<1)) {
            oNamePath = ("/");
        } else {
            oNamePath = cform.getPath();
        }

        // Perform a "Create Context" transaction (if requested)
        if ("Create".equals(adminAction)) {

            String operation = null;
            Object values[] = null;
            
            try {
                // get the parent host name
                String parentName = cform.getParentObjectName();
                ObjectName honame = new ObjectName(parentName);
                
                // Ensure that the requested context name is unique
                ObjectName oname =
                    new ObjectName(TomcatTreeBuilder.CONTEXT_TYPE +
                                   ",path=" + oNamePath +
                                   ",host=" + honame.getKeyProperty("host") +
                                   ",service=" + honame.getKeyProperty("service"));
                
                if (mBServer.isRegistered(oname)) {
                    ActionMessages errors = new ActionMessages();
                    errors.add("contextName",
                               new ActionMessage("error.contextName.exists"));
                    saveErrors(request, errors);
                    return (new ActionForward(mapping.getInput()));
                }
                
                // Look up our MBeanFactory MBean
                ObjectName fname =
                    new ObjectName(TomcatTreeBuilder.FACTORY_TYPE);

                // Create a new StandardContext object
                values = new Object[3];
                values[0] = parentName;
                values[1] = oNamePath;
                values[2] = cform.getDocBase();

                operation = "createStandardContext";    
                
                cObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardContextTypes);
                
                // Create a new Loader object
                values = new String[1];
                // parent of loader is the newly created context
                values[0] = cObjectName.toString();
                operation = "createWebappLoader";
                lObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardLoaderTypes);                
                
                // Create a new StandardManager object
                values = new String[1];
                // parent of manager is the newly created Context
                values[0] = cObjectName.toString();
                operation = "createStandardManager";
                mObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardManagerTypes);
                
                // Add the new Context to our tree control node
                addToTreeControlNode(oname, cObjectName, parentName, 
                                    resources, session);

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

            ObjectName coname = new ObjectName(cObjectName);
            ObjectName loname = new ObjectName(lObjectName);
            ObjectName moname = new ObjectName(mObjectName);

            attribute = "debug";
            int debug = 0;
            try {
                debug = Integer.parseInt(cform.getDebugLvl());
            } catch (Throwable t) {
                debug = 0;
            }            
            mBServer.setAttribute(coname,
                                  new Attribute("debug", new Integer(debug)));

            attribute = "path";
            String path = "";
            try {
                path = cform.getPath();
            } catch (Throwable t) {
                path = "";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("path", path));
            
            attribute = "workDir";
            String workDir = "";
            workDir = cform.getWorkDir();
            if ((workDir!=null) && (workDir.length()>=1)) {
                mBServer.setAttribute(coname,
                                  new Attribute("workDir", workDir));
            }
 
            attribute = "cookies";
            String cookies = "false";
            try {
                cookies = cform.getCookies();
            } catch (Throwable t) {
                cookies = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("cookies", new Boolean(cookies)));

            attribute = "crossContext";
            String crossContext = "false";
            try {
                crossContext = cform.getCrossContext();
            } catch (Throwable t) {
                crossContext = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("crossContext", new Boolean(crossContext)));

            attribute = "privileged";
            String privileged = "false";
            try {
                privileged = cform.getPrivileged();
            } catch (Throwable t) {
                privileged = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("privileged", new Boolean(privileged)));

            attribute = "override";
            String override = "false";
            try {
                override = cform.getOverride();
            } catch (Throwable t) {
                override = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("override", new Boolean(override)));

            attribute = "reloadable";
            String reloadable = "false";
            try {
                reloadable = cform.getReloadable();
            } catch (Throwable t) {
                reloadable = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("reloadable", new Boolean(reloadable)));

            attribute = "swallowOutput";
            String swallowOutput = "false";
            try {
                swallowOutput = cform.getSwallowOutput();
            } catch (Throwable t) {
                swallowOutput = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("swallowOutput", new Boolean(swallowOutput)));

            attribute = "useNaming";
            String useNaming = "false";
            try {
                useNaming = cform.getUseNaming();
            } catch (Throwable t) {
                useNaming = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("useNaming", new Boolean(useNaming)));

            attribute = "antiJARLocking";
            String antiJarLocking = "false";
            try {
                antiJarLocking = cform.getAntiJarLocking();
            } catch (Throwable t) {
                antiJarLocking = "false";
            }
            mBServer.setAttribute(coname,
                                  new Attribute("antiJARLocking", new Boolean(antiJarLocking)));

            // Loader properties            
            attribute = "reloadable";
            try {
                reloadable = cform.getLdrReloadable();
            } catch (Throwable t) {
                reloadable = "false";
            }
            mBServer.setAttribute(loname,
                                  new Attribute("reloadable", new Boolean(reloadable)));
            
            attribute = "debug";
            try {
                debug = Integer.parseInt(cform.getLdrDebugLvl());
            } catch (Throwable t) {
                debug = 0;
            }
            mBServer.setAttribute(loname,
                                  new Attribute("debug", new Integer(debug)));
            
            attribute = "checkInterval";
            int checkInterval = 15;
            try {
                checkInterval = Integer.parseInt(cform.getLdrCheckInterval());
            } catch (Throwable t) {
                checkInterval = 15;
            }
            mBServer.setAttribute(loname,
                                  new Attribute("checkInterval", new Integer(checkInterval)));

            // Manager properties            
            attribute = "entropy";
            String entropy = cform.getMgrSessionIDInit();
            if ((entropy!=null) && (entropy.length()>=1)) {
                mBServer.setAttribute(moname,
                                  new Attribute("entropy",entropy));
            }
            
            attribute = "debug";
            try {
                debug = Integer.parseInt(cform.getMgrDebugLvl());
            } catch (Throwable t) {
                debug = 0;
            }            
            mBServer.setAttribute(moname,
                                  new Attribute("debug", new Integer(debug)));
            
            attribute = "checkInterval";
            try {
                checkInterval = Integer.parseInt(cform.getMgrCheckInterval());
            } catch (Throwable t) {
                checkInterval = 60;
            }
            mBServer.setAttribute(moname,
                                  new Attribute("checkInterval", new Integer(checkInterval)));
            
            attribute = "maxActiveSessions";
            int maxActiveSessions = -1;
            try {
                maxActiveSessions = Integer.parseInt(cform.getMgrMaxSessions());
            } catch (Throwable t) {
                maxActiveSessions = -1;
            }
            mBServer.setAttribute(moname,
                                  new Attribute("maxActiveSessions", new Integer(maxActiveSessions)));

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
    
    
    /**
     * Append nodes for any define resources for the specified Context.
     *
     * @param containerNode Container node for the tree control
     * @param containerName Object name of the parent container
     * @param resources The MessageResources for our localized messages
     *  messages
     */
    public void addToTreeControlNode(ObjectName oname, String containerName, 
                                    String parentName, MessageResources resources, 
                                    HttpSession session) 
        throws Exception {
                              
        TreeControl control = (TreeControl) session.getAttribute("treeControlTest");
        if (control != null) {
            TreeControlNode parentNode = control.findNode(parentName);
            if (parentNode != null) {
                String path = oname.getKeyProperty("path");
                String nodeLabel = "Context (" + path + ")";
                String encodedName = URLEncoder.encode(oname.toString());
                TreeControlNode childNode = 
                    new TreeControlNode(oname.toString(),
                                        "Context.gif",
                                        nodeLabel,
                                        "EditContext.do?select=" +
                                        encodedName,
                                        "content",
                                        true);
                parentNode.addChild(childNode);
                // FIXME - force a redisplay
                String type = oname.getKeyProperty("type");
                if (type == null) {
                    type = "";
                }
                if (path == null) {
                    path = "";
                }        
                String host = oname.getKeyProperty("host");
                if (host == null) {
                    host = "";
                }        
                String service = oname.getKeyProperty("service");
                TreeControlNode subtree = new TreeControlNode
                    ("Context Resource Administration " + containerName,
                    "folder_16_pad.gif",
                    resources.getMessage("resources.treeBuilder.subtreeNode"),
                    null,
                    "content",
                    true);        
                childNode.addChild(subtree);
                TreeControlNode datasources = new TreeControlNode
                    ("Context Data Sources " + containerName,
                    "Datasource.gif",
                    resources.getMessage("resources.treeBuilder.datasources"),
                    "resources/listDataSources.do?resourcetype=" + 
                    URLEncoder.encode(type) + "&path=" +
                    URLEncoder.encode(path) + "&host=" + 
                    URLEncoder.encode(host) + "&service=" +
                    URLEncoder.encode(service) + "&forward=" +
                    URLEncoder.encode("DataSources List Setup"),
                    "content",
                    false);
                TreeControlNode mailsessions = new TreeControlNode
                    ("Context Mail Sessions " + containerName,
                    "Mailsession.gif",
                    resources.getMessage("resources.treeBuilder.mailsessions"),
                    "resources/listMailSessions.do?resourcetype=" + 
                    URLEncoder.encode(type) + "&path=" +
                    URLEncoder.encode(path) + "&host=" + 
                    URLEncoder.encode(host) + "&service=" +
                    URLEncoder.encode(service) + "&forward=" +
                    URLEncoder.encode("MailSessions List Setup"),
                    "content",
                    false);
                TreeControlNode resourcelinks = new TreeControlNode
                    ("Resource Links " + containerName,
                    "ResourceLink.gif",
                    resources.getMessage("resources.treeBuilder.resourcelinks"),
                    "resources/listResourceLinks.do?resourcetype=" + 
                    URLEncoder.encode(type) + "&path=" +
                    URLEncoder.encode(path) + "&host=" + 
                    URLEncoder.encode(host) + "&service=" +
                    URLEncoder.encode(service) + "&forward=" +
                    URLEncoder.encode("ResourceLinks List Setup"),
                    "content",
                    false);
                TreeControlNode envs = new TreeControlNode
                    ("Context Environment Entries "+ containerName,
                    "EnvironmentEntries.gif",
                    resources.getMessage("resources.env.entries"),
                    "resources/listEnvEntries.do?resourcetype=" + 
                    URLEncoder.encode(type) + "&path=" +
                    URLEncoder.encode(path) + "&host=" + 
                    URLEncoder.encode(host) + "&service=" +
                    URLEncoder.encode(service) + "&forward=" +
                    URLEncoder.encode("EnvEntries List Setup"),
                    "content",
                    false);
                subtree.addChild(datasources);
                subtree.addChild(mailsessions);
                subtree.addChild(resourcelinks);
                subtree.addChild(envs);                    
            } else {
                    getServlet().log
                        ("Cannot find parent node '" + parentName + "'");
            } 
        }else {
            getServlet().log("Cannot find TreeControlNode!");
        }                              
    }    
    
}
