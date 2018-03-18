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

package org.apache.webapp.admin.connector;

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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;
import org.apache.webapp.admin.TreeControl;
import org.apache.webapp.admin.TreeControlNode;


/**
 * The <code>Action</code> that completes <em>Add Connector</em> and
 * <em>Edit Connector</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class SaveConnectorAction extends Action {


    // ----------------------------------------------------- Instance Variables

    /**
     * Signature for the <code>createStandardConnector</code> operation.
     */
    private String createStandardConnectorTypes[] =
    { "java.lang.String",    // parent
      "java.lang.String",    // address
      "int"                  // port      
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
        ConnectorForm cform = (ConnectorForm) form;
        String adminAction = cform.getAdminAction();
        String cObjectName = cform.getObjectName();
        String connectorType = cform.getConnectorType();

        // Perform a "Create Connector" transaction (if requested)
        if ("Create".equals(adminAction)) {

            String operation = null;
            Object values[] = null;

            try {
   
                String serviceName = cform.getServiceName();
                
                String address = cform.getAddress();
                if (address.compareTo("") == 0) {
                    address = null;
                }
                
                ObjectName oname =
                    new ObjectName(TomcatTreeBuilder.CONNECTOR_TYPE +
                                   ",service=" + serviceName +
                                   ",port=" + cform.getPortText() +
                                   ",address=" + address);
                                                
                // Ensure that the requested connector name is unique
                if (mBServer.isRegistered(oname)) {
                    ActionMessages errors = new ActionMessages();
                    errors.add("connectorName",
                               new ActionMessage("error.connectorName.exists"));
                    saveErrors(request, errors);
                    return (new ActionForward(mapping.getInput()));
                }

                // Look up our MBeanFactory MBean
                ObjectName fname =
                    new ObjectName(TomcatTreeBuilder.FACTORY_TYPE);

                // Create a new Connector object
                values = new Object[3];                
                values[0] = // parent 
                    TomcatTreeBuilder.SERVICE_TYPE + ",name=" + serviceName;
                values[1] = address;
                values[2] = new Integer(cform.getPortText());

                if ("HTTP".equalsIgnoreCase(connectorType)) {
                        operation = "createHttpConnector"; // HTTP
                } else if ("HTTPS".equalsIgnoreCase(connectorType)) { 
                        operation = "createHttpsConnector";   // HTTPS
                } else {
                        operation = "createAjpConnector";   // AJP(HTTP)                  
                }
                
                cObjectName = (String)
                    mBServer.invoke(fname, operation,
                                    values, createStandardConnectorTypes);
                
                // Add the new Connector to our tree control node
                TreeControl control = (TreeControl)
                    session.getAttribute("treeControlTest");
                if (control != null) {
                    String parentName = 
                          TomcatTreeBuilder.SERVICE_TYPE + ",name=" + serviceName;
                    TreeControlNode parentNode = control.findNode(parentName);
                    if (parentNode != null) {
                        String nodeLabel =
                           "Connector (" + cform.getPortText() + ")";
                        String encodedName =
                            URLEncoder.encode(cObjectName);
                        TreeControlNode childNode =
                            new TreeControlNode(cObjectName,
                                                "Connector.gif",
                                                nodeLabel,
                                                "EditConnector.do?select=" +
                                                encodedName,
                                                "content",
                                                true);
                        // FIXME--the node should be next to the rest of 
                        // the Connector nodes..
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

            ObjectName coname = new ObjectName(cObjectName);

            attribute = "debug";
            int debug = 0;
            try {
                debug = Integer.parseInt(cform.getDebugLvl());
            } catch (Throwable t) {
                debug = 0;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(debug)));
            
            attribute = "acceptCount";
            int acceptCount = 10;
            try {
                acceptCount = Integer.parseInt(cform.getAcceptCountText());
            } catch (Throwable t) {
                acceptCount = 10;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(acceptCount)));
            
            attribute = "connectionTimeout";
            int connectionTimeout = 60000;
            try {
                connectionTimeout = Integer.parseInt(cform.getConnTimeOutText());
            } catch (Throwable t) {
                connectionTimeout = 60000;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(connectionTimeout)));
            
            attribute = "bufferSize";
            int bufferSize = 2048;
            try {
                bufferSize = Integer.parseInt(cform.getBufferSizeText());
            } catch (Throwable t) {
                bufferSize = 2048;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(bufferSize)));            

            attribute = "enableLookups";
            mBServer.setAttribute(coname,
                    new Attribute(attribute,
                                  new Boolean(cform.getEnableLookups())));                        

            attribute = "redirectPort";
            int redirectPort = 443;
            try {
                redirectPort = Integer.parseInt(cform.getRedirectPortText());
            } catch (Throwable t) {
                redirectPort = 443;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(redirectPort))); 

            attribute = "minProcessors";
            int minProcessors = 4;
            try {
                minProcessors = Integer.parseInt(cform.getMinProcessorsText());
            } catch (Throwable t) {
                minProcessors = 4;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(minProcessors))); 

            attribute = "maxProcessors";
            int maxProcessors = 200;
            try {
                maxProcessors = Integer.parseInt(cform.getMaxProcessorsText());
            } catch (Throwable t) {
                maxProcessors = 200;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(maxProcessors)));
            
            attribute = "maxSpareProcessors";
            int maxSpareProcessors = 50;
            try {
                maxSpareProcessors =
                    Integer.parseInt(cform.getMaxSpareProcessorsText());
            } catch (Throwable t) {
                maxSpareProcessors = 50;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(maxSpareProcessors)));
            
            attribute = "URIEncoding";
            String uriEnc = cform.getURIEncodingText();
            if ((uriEnc != null) && (uriEnc.length()==0)) {
                uriEnc = null;
            }
            mBServer.setAttribute(coname, new Attribute(attribute, uriEnc));
            
            attribute = "useBodyEncodingForURI";
            mBServer.setAttribute(coname,
                    new Attribute(attribute,
                            new Boolean(cform.getUseBodyEncodingForURIText())));
            
            attribute = "allowTrace";
            mBServer.setAttribute(coname,
                    new Attribute(attribute,
                            new Boolean(cform.getAllowTraceText())));
      
            attribute = "compressableMimeType";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getCompressableMimeType()));            

            attribute = "compression";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getCompression()));            

            attribute = "connectionLinger";
            int connectionLinger = -1;
            try {
                connectionLinger = Integer.parseInt(cform.getConnLingerText());
            } catch (Throwable t) {
                connectionLinger = -1;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(connectionLinger)));
            
            attribute = "disableUploadTimeout";
            mBServer.setAttribute(coname,
                    new Attribute(attribute,
                            new Boolean(cform.getDisableUploadTimeout())));
      
            attribute = "maxHttpHeaderSize";
            int maxHttpHeaderSize = 4096;
            try {
                maxHttpHeaderSize =
                    Integer.parseInt(cform.getMaxHttpHeaderSizeText());
            } catch (Throwable t) {
                maxHttpHeaderSize = 4096;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(maxHttpHeaderSize)));

            attribute = "maxKeepAliveRequests";
            int maxKeepAliveRequests = 100;
            try {
                maxKeepAliveRequests =
                    Integer.parseInt(cform.getMaxKeepAliveReqsText());
            } catch (Throwable t) {
                maxKeepAliveRequests = 100;
            }
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Integer(maxKeepAliveRequests)));
            
            attribute = "noCompressionUserAgents";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getNoCompressionUA()));            

            attribute = "restrictedUserAgents";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getRestrictedUA()));            

            attribute = "server";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getServer()));            

            attribute = "strategy";              
            mBServer.setAttribute(coname,
                    new Attribute(attribute, cform.getStrategy()));            

            attribute = "tcpNoDelay";
            mBServer.setAttribute(coname,
                    new Attribute(attribute,
                            new Boolean(cform.getTcpNoDelay())));
      
            if (("AJP".equalsIgnoreCase(connectorType))) {
                // tomcatAuthentication exists only for AJP connector
                attribute = "tomcatAuthentication";
                mBServer.setAttribute(coname,
                        new Attribute(attribute,
                                new Boolean(cform.getTomcatAuthentication())));
            } else {
                // proxy name, port, socket buffer and threadPriority do not exist
                // for AJP connector
                attribute = "proxyName";  
                String proxyName = cform.getProxyName();
                if ((proxyName != null) && (proxyName.length()>0)) { 
                    mBServer.setAttribute(coname,
                                  new Attribute(attribute, proxyName));
                }
                
                attribute = "proxyPort";
                int proxyPort = 0;
                try {
                    proxyPort = Integer.parseInt(cform.getProxyPortText());
                } catch (Throwable t) {
                    proxyPort = 0;
                }
                mBServer.setAttribute(coname,
                              new Attribute(attribute, new Integer(proxyPort))); 

                attribute = "socketBuffer";
                int socketBuffer = 9000;
                try {
                    socketBuffer = Integer.parseInt(cform.getSocketBufferText());
                } catch (Throwable t) {
                    socketBuffer = 9000;
                }
                mBServer.setAttribute(coname,
                        new Attribute(attribute, new Integer(socketBuffer)));
                
                attribute = "threadPriority";              
                mBServer.setAttribute(coname,
                        new Attribute(attribute, cform.getThreadPriorityText()));            

            }
            
            // Secure
            attribute = "secure";
            mBServer.setAttribute(coname,
                    new Attribute(attribute, new Boolean(cform.getSecure())));
            
            // HTTPS specific properties
            if("HTTPS".equalsIgnoreCase(connectorType)) {
                attribute = "clientAuth";              
                mBServer.setAttribute(coname,
                        new Attribute(attribute, 
                                cform.getClientAuthentication()));            
                
                attribute = "keystoreFile";
                String keyFile = cform.getKeyStoreFileName();
                if ((keyFile != null) && (keyFile.length()>0)) 
                    mBServer.setAttribute(coname,
                              new Attribute(attribute, keyFile));            
                
                attribute = "keystorePass";
                String keyPass = cform.getKeyStorePassword();
                if ((keyPass != null) && (keyPass.length()>0)) 
                    mBServer.setAttribute(coname,
                              new Attribute(attribute, keyPass));                 
                // request.setAttribute("warning", "connector.keyPass.warning");               

                attribute = "algorithm";
                String algorithm = cform.getAlgorithm();
                if ((algorithm != null) && (algorithm.length()>0)) 
                    mBServer.setAttribute(coname,
                              new Attribute(attribute, algorithm));
                
                attribute = "ciphers";
                String ciphers = cform.getCiphers();
                if ((ciphers != null) && (ciphers.length()>0)) 
                mBServer.setAttribute(coname,
                        new Attribute(attribute, ciphers));

                attribute = "keystoreType";
                String keystoreType = cform.getKeyStoreType();
                if ((keystoreType != null) && (keystoreType.length()>0)) 
                    mBServer.setAttribute(coname,
                              new Attribute(attribute, keystoreType));
                
                attribute = "sslProtocol";
                String sslProtocol = cform.getSslProtocol();
                if ((sslProtocol != null) && (sslProtocol.length()>0)) 
                    mBServer.setAttribute(coname,
                              new Attribute(attribute, sslProtocol));
                
            }
 
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
