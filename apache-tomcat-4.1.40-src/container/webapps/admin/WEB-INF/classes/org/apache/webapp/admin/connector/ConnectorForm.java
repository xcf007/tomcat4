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

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.net.InetAddress;
import java.util.List;

/**
 * Form bean for the connector page.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class ConnectorForm extends ActionForm {
    
    // ----------------------------------------------------- Instance Variables
    
     /**
     * The administrative action represented by this form.
     */
    private String adminAction = "Edit";

    /**
     * The object name of the Connector this bean refers to.
     */
    private String objectName = null;
    
    /**
     * The name of the service this connector belongs to.
     */
    private String serviceName = null;
   
    /**
     * The text for the scheme.
     */
    private String scheme = null;

    /**
     * The value of secure.
     */
    private String secure = null;
    
    /**
     * The text for the connector type. 
     * Specifies if it is a CoyoteConnector or AJP13Connector etc.
     */
    private String connectorType = null;    
    
     /**
     * The text for the node label.
     */
    private String nodeLabel = null;
    
    
    /**
     * The text for the accept Count.
     */
    private String acceptCountText = null;
    
    /**
     * The value of Compressable MIME Type.
     */
    private String compressableMimeType = null;
    
    /**
     * The value of compression
     */
    private String compression = null;
    
    /**
     * The text for the Connection Linger.
     */
    private String connLingerText = null;

    /**
     * The text for the Connection Time Out.
     */
    private String connTimeOutText = null;
    
    
    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";
    
    /**
     * The text for the buffer size.
     */
    private String bufferSizeText = null;
    
    /**
     * The value of enable Lookups.
     */
    private String enableLookups = "false";
    
    /**
     * The text for the address.
     */
    private String address = null;
    
    /**
     * The text for the minProcessors.
     */
    private String minProcessorsText = null;
    
    /**
     * The text for the max Processors.
     */
    private String maxProcessorsText = null;
    
    /**
     * The text for the URIEncoding.
     */
    private String uriEncodingText = null;
    
    /**
     * The value of useBodyEncodingForURI.
     */
    private String useBodyEncodingForURI = "false";
    
    /**
     * The value of allowTrace.
     */
    private String allowTrace = "false";
    
    /**
     * The text for the port.
     */
    private String portText = null;
    
    /**
     * The text for the redirect port.
     */
    private String redirectPortText = null;
    
    /**
     * The text for the proxyName.
     */
    private String proxyName = null;
    
    /**
     * The text for the proxy Port Number.
     */
    private String proxyPortText = null;
    
    
    /**
     * The text for the connectorName.
     */
    private String connectorName = null;
        
    /**
     * Whether client authentication is supported.
     */
    private String clientAuthentication = "false";
        
    /**
     * The keyStore Filename.
     */
    private String keyStoreFileName = null;
        
    /**
     * The keyStore Password.
     */
    private String keyStorePassword = null;
    
    /**
     * The text for disable upload timeout.
     */
    private String disableUploadTimeout = "false";
    
    /**
     * The maximum HTTP header size.
     */
    private String maxHttpHeaderSizeText = null;
    
    /**
     * The maximum keep alive requests.
     */
    private String maxKeepAliveReqsText = null;
    
    /**
     * The maximum spare processors.
     */
    private String maxSpareProcessorsText = null;
    
    /**
     * The comma separated list of regular expressions matching user agents
     * where compression is not to be used.
     */
    private String noCompressionUA = null;

    /**
     * The comma separated list of regular expressions matching user agents
     * where keep alive is not to be used.
     */
    private String restrictedUA = null;

    /**
     * The server header.
     */
    private String server = null;
    
    /**
     * The size of the socket buffer.
     */
    private String socketBufferText = null;
    
    /**
     * The thread pooling strategy.
     */
    private String strategy = null;
    
    /**
     * The setting for TCP_NO_DELAY. 
     */
    private String tcpNoDelay = "false";
    
    /**
     * The setting for tomcatAuthentication. 
     */
    private String tomcatAuthentication = "true";
    
    /**
     * The setting for thread priority.
     */
    private String threadPriorityText = null;
    
    /**
     * The certificate encoding algorithm to use.
     */
    private String algorithm = null;
    
    /**
     * Comma separated list of ciphers to use for HTTPS.
     */
    private String ciphers = null;
    
    /**
     * Type of keystore of use.
     */
    private String keyStoreType = null;
    
    /**
     * Version of SSL protocol to use;
     */
    private String sslProtocol = null;
    
    /**
     * Set of valid values for debug level.
     */
    private List debugLvlVals = null;
    
    /**
     * Represent boolean (true, false) values for enableLookups etc.
     */    
    private List booleanVals = null;

    /**
     * Represent supported connector types.
     */    
    private List connectorTypeVals = null;

    /**
     * Represent supported clientAuth values.
     */
    private List clientAuthVals = null;
    
    /**
     * Represent support thread priority values.
     */
    private List threadPriorityVals = null;
    
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
     * Return the object name of the Connector this bean refers to.
     */
    public String getObjectName() {

        return this.objectName;

    }


    /**
     * Set the object name of the Connector this bean refers to.
     */
    public void setObjectName(String objectName) {

        this.objectName = objectName;

    }
    
      /**
     * Return the object name of the service this connector belongs to.
     */
    public String getServiceName() {

        return this.serviceName;

    }


    /**
     * Set the object name of the Service this connector belongs to.
     */
    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;

    }
    
    /**
     * Return the Scheme.
     */
    public String getScheme() {
        
        return this.scheme;
        
    }
    
    /**
     * Set the Scheme.
     */
    public void setScheme(String scheme) {
        
        this.scheme = scheme;
        
    }
    
    /**
     * Return the secure text.
     */
    public String getSecure() {
        
        return this.secure;
        
    }
    
    /**
     * Set the secure text/
     */
    public void setSecure(String secure) {
        
        this.secure = secure;
    }
    
    /**
     * Return the Connector type.
     */
    public String getConnectorType() {
        
        return this.connectorType;
        
    }
    
    /**
     * Set the Connector type.
     */
    public void setConnectorType(String connectorType) {
        
        this.connectorType = connectorType;
        
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
     * Return the acceptCountText.
     */
    public String getAcceptCountText() {
        
        return this.acceptCountText;
        
    }
    
    
    /**
     * Set the acceptCountText.
     */
    
    public void setAcceptCountText(String acceptCountText) {
        
        this.acceptCountText = acceptCountText;
        
    }
    
    /**
     * Return the compressable MIME types.
     */
    public String getCompressableMimeType() {
        
        return this.compressableMimeType;
        
    }
    
    /**
     * Set the compressable MIME types.
     */
    public void setCompressableMimeType(String compressableMimeType) {
        
        this.compressableMimeType = compressableMimeType;
        
    }
    
    /**
     * Return the compression text.
     */
    public String getCompression() {
        
        return this.compression;
        
    }
    
    /**
     * Set the compression text.
     */
    public void setCompression(String compression) {
        
        this.compression = compression;
        
    }
    
    /**
     * Return the connLingerText.
     */
    public String getConnLingerText() {
        
        return this.connLingerText;
        
    }
    
    /**
     * Set the connLingerText.
     */
    
    public void setConnLingerText(String connLingerText) {
        
        this.connLingerText = connLingerText;
        
    }
       
    /**
     * Return the connTimeOutText.
     */
    public String getConnTimeOutText() {
        
        return this.connTimeOutText;
        
    }
    
    /**
     * Set the connTimeOutText.
     */
    
    public void setConnTimeOutText(String connTimeOutText) {
        
        this.connTimeOutText = connTimeOutText;
        
    }
       
    /**
     * Return the bufferSizeText.
     */
    public String getBufferSizeText() {
        
        return this.bufferSizeText;
        
    }
    
    /**
     * Set the bufferSizeText.
     */
    
    public void setBufferSizeText(String bufferSizeText) {
        
        this.bufferSizeText = bufferSizeText;
        
    }
    
    /**
     * Return the address.
     */
    public String getAddress() {
        
        return this.address;
        
    }
    
    /**
     * Set the connTimeOutText.
     */
    
    public void setAddress(String address) {
        
        this.address = address;
        
    }
    
    
    /**
     * Return the proxy Name.
     */
    public String getProxyName() {
        
        return this.proxyName;
        
    }
    
    /**
     * Set the proxy Name.
     */
    
    public void setProxyName(String proxyName) {
        
        this.proxyName = proxyName;
        
    }
    
    /**
     * Return the proxy Port NumberText.
     */
    public String getProxyPortText() {
        
        return this.proxyPortText;
        
    }
    
    /**
     * Set the proxy Port NumberText.
     */
    
    public void setProxyPortText(String proxyPortText) {
        
        this.proxyPortText = proxyPortText;
        
    }

   /**
     * Return the true/false value of client authentication.
     */
    public String getClientAuthentication() {

        return this.clientAuthentication;

    }


    /**
     * Set whether client authentication is supported or not.
     */
    public void setClientAuthentication(String clientAuthentication) {

        this.clientAuthentication = clientAuthentication;

    }

    /**
     * Return the object name of the service this connector belongs to.
     */
    public String getKeyStoreFileName() {

        return this.keyStoreFileName;

    }


    /**
     * Set the object name of the Service this connector belongs to.
     */
    public void setKeyStoreFileName(String keyStoreFileName) {

        this.keyStoreFileName = keyStoreFileName;

    }

          /**
     * Return the object name of the service this connector belongs to.
     */
    public String getKeyStorePassword() {

        return this.keyStorePassword;

    }


    /**
     * Set the object name of the Service this connector belongs to.
     */
    public void setKeyStorePassword(String keyStorePassword) {

        this.keyStorePassword = keyStorePassword;

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
     * Return the Enable lookup Text.
     */
    
    public String getEnableLookups() {
        
        return this.enableLookups;
        
    }
    
    /**
     * Set the Enable Lookup Text.
     */
    public void setEnableLookups(String enableLookups) {
        
        this.enableLookups = enableLookups;
        
    }
    
    /**
     * Return the booleanVals.
     */
    public List getBooleanVals() {
        
        return this.booleanVals;
        
    }
    
    /**
     * Set the debugVals.
     */
    public void setBooleanVals(List booleanVals) {
        
        this.booleanVals = booleanVals;
        
    }
    
    /**
     * Return the clientAuth values.
     */
    public List getClientAuthVals() {
        return clientAuthVals;
    }

    /**
     * Set the clientAuth vaues.
     */
    public void setClientAuthVals(List clientAuthVals) {
        this.clientAuthVals = clientAuthVals;
    }
    
    /**
     * Return the threadPriorityVals.
     */
    public List getThreadPriorityVals() {
        return threadPriorityVals;
    }


    /**
     * Set the threadPriorityVals.
     */
    public void setThreadPriorityVals(List threadPriorityVals) {
        this.threadPriorityVals = threadPriorityVals;
    }

    /**
     * Return the min Processors Text.
     */
    public String getMinProcessorsText() {
        
        return this.minProcessorsText;
        
    }
    
    /**
     * Set the minProcessors Text.
     */
    public void setMinProcessorsText(String minProcessorsText) {
        
        this.minProcessorsText = minProcessorsText;
        
    }
    
    /**
     * Return the max processors Text.
     */
    public String getMaxProcessorsText() {
        
        return this.maxProcessorsText;
        
    }
    
    /**
     * Set the Max Processors Text.
     */
    public void setMaxProcessorsText(String maxProcessorsText) {
        
        this.maxProcessorsText = maxProcessorsText;
        
    }
    
    /**
     * Return the URIEncoding text.
     */
    public String getURIEncodingText() {
        
        return this.uriEncodingText;
        
    }
    
    /**
     * Set the URIEncoding Text.
     */
    public void setURIEncodingText(String uriEncodingText) {
        
        this.uriEncodingText = uriEncodingText;
        
    }
    
    /**
     * Return the useBodyEncodingForURI Text.
     */
    public String getUseBodyEncodingForURIText() {
        
        return this.useBodyEncodingForURI;
        
    }
    
    /**
     * Set the useBodyEncodingForURI Text.
     */
    public void setUseBodyEncodingForURIText(String useBodyEncodingForURI) {
        
        this.useBodyEncodingForURI = useBodyEncodingForURI;
        
    }    
    
    /**
     * Return the allowTrace Text.
     */
    public String getAllowTraceText() {
        
        return this.allowTrace;
        
    }
    
    /**
     * Set the allowTrace Text.
     */
    public void setAllowTraceText(String allowTrace) {
        
        this.allowTrace = allowTrace;
        
    }    
    
    /**
     * Return the port text.
     */
    public String getPortText() {
        
        return this.portText;
        
    }
    
    /**
     * Set the port Text.
     */
    public void setPortText(String portText) {
        
        this.portText = portText;
        
    }
    
    
    /**
     * Return the port.
     */
    public String getRedirectPortText() {
        
        return this.redirectPortText;
        
    }
    
    /**
     * Set the Redirect Port Text.
     */
    public void setRedirectPortText(String redirectPortText) {
        
        this.redirectPortText = redirectPortText;
        
    }
    
    /**
     * Return the Service Name.
     */
    public String getConnectorName() {
        
        return this.connectorName;
        
    }
    
    /**
     * Set the Service Name.
     */
    public void setConnectorName(String connectorName) {
        
        this.connectorName = connectorName;
        
    }
    
    /**
     * Return the connectorTypeVals.
     */
    public List getConnectorTypeVals() {
        
        return this.connectorTypeVals;
        
    }
    
    /**
     * Set the connectorTypeVals.
     */
    public void setConnectorTypeVals(List connectorTypeVals) {
        
        this.connectorTypeVals = connectorTypeVals;
        
    }
    
    /**
     * Return the disableUploadTimeout.
     */
    public String getDisableUploadTimeout() {
        return disableUploadTimeout;
    }

    /**
     * Set the disableUploadTimeout.
     */
    public void setDisableUploadTimeout(String disableUploadTimeout) {
        this.disableUploadTimeout = disableUploadTimeout;
    }    

    /**
     * Return the algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Set the algorithm.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Return the ciphers.
     */
    public String getCiphers() {
        return ciphers;
    }

    /**
     * Set the ciphers.
     */
    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    /**
     * Return the keyStoreType.
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Set the keyStoreType.
     */
    public void setKeyStoreType(String keystoreType) {
        this.keyStoreType = keystoreType;
    }

    /**
     * Return the maxHttpHeaderSizeText.
     */
    public String getMaxHttpHeaderSizeText() {
        return maxHttpHeaderSizeText;
    }

    /**
     * Set the maxHttpHeaderSizeText.
     */
    public void setMaxHttpHeaderSizeText(String maxHttpHeaderSize) {
        this.maxHttpHeaderSizeText = maxHttpHeaderSize;
    }

    /**
     * Return the maxKeepAliveReqsText.
     */
    public String getMaxKeepAliveReqsText() {
        return maxKeepAliveReqsText;
    }

    /**
     * Set the maxKeepAliveReqsText.
     */
    public void setMaxKeepAliveReqsText(String maxKeepAliveRequests) {
        this.maxKeepAliveReqsText = maxKeepAliveRequests;
    }

    /**
     * Return the maxSpareProcessorsText.
     */
    public String getMaxSpareProcessorsText() {
        return maxSpareProcessorsText;
    }

    /**
     * Set the maxSpareProcessorsText.
     */
    public void setMaxSpareProcessorsText(String maxSpareProcessors) {
        this.maxSpareProcessorsText = maxSpareProcessors;
    }

    /**
     * Return the noCompressionUA.
     */
    public String getNoCompressionUA() {
        return noCompressionUA;
    }

    /**
     * Set the noCompressionUA.
     */
    public void setNoCompressionUA(String noCompressionUserAgents) {
        this.noCompressionUA = noCompressionUserAgents;
    }

    /**
     * Return the restrictedUA.
     */
    public String getRestrictedUA() {
        return restrictedUA;
    }

    /**
     * Set the restrictedUA.
     */
    public void setRestrictedUA(String restrictedUserAgents) {
        this.restrictedUA = restrictedUserAgents;
    }

    /**
     * Return the server.
     */
    public String getServer() {
        return server;
    }

    /**
     * Set the server.
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Return the socketBufferText.
     */
    public String getSocketBufferText() {
        return socketBufferText;
    }

    /**
     * Set the socketBufferText.
     */
    public void setSocketBufferText(String socketBuffer) {
        this.socketBufferText = socketBuffer;
    }

    /**
     * Return the sslProtocol.
     */
    public String getSslProtocol() {
        return sslProtocol;
    }

    /**
     * Set the sslProtocol.
     */
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    /**
     * Return the strategy.
     */
    public String getStrategy() {
        return strategy;
    }

    /**
     * Set the strategy.
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    /**
     * Return the tcpNoDelay.
     */
    public String getTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Set the tcpNoDelay.
     */
    public void setTcpNoDelay(String tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Return the threadPriorityText.
     */
    public String getThreadPriorityText() {
        return threadPriorityText;
    }

    /**
     * Set the threadPriorityText.
     */
    public void setThreadPriorityText(String threadPriority) {
        this.threadPriorityText = threadPriority;
    }

    /**
     * Returns the tomcatAuthentication.
     */
    public String getTomcatAuthentication() {
        return tomcatAuthentication;
    }


    /**
     * Set the tomcatAuthentication.
     */
    public void setTomcatAuthentication(String tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
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
        this.connectorType = null;
        this.portText = null;
        this.acceptCountText = null;
        this.connTimeOutText = null;
        this.bufferSizeText = null;
        this.address = null;
        this.enableLookups = "false";
        this.minProcessorsText = null;
        this.maxProcessorsText = null;
        this.uriEncodingText = null;
        this.useBodyEncodingForURI = "false";
        this.allowTrace = "false";
        this.portText = null;
        this.redirectPortText = null;
        this.proxyName = null;
        this.proxyPortText = null;
        this.keyStoreFileName = null;
        this.keyStorePassword = null;        
        this.clientAuthentication = "false";
        this.secure = "false";
        this.compressableMimeType = null;
        this.compression = null;
        this.connLingerText = null;
        this.disableUploadTimeout = "false";
        this.maxHttpHeaderSizeText = null;
        this.maxKeepAliveReqsText = null;
        this.maxSpareProcessorsText = null;
        this.noCompressionUA = null;
        this.restrictedUA = null;
        this.server = null;
        this.socketBufferText = null;
        this.strategy = null;
        this.tcpNoDelay = "false";
        this.threadPriorityText = null;
        this.algorithm = null;
        this.ciphers = null;
        this.keyStoreType = null;
        this.sslProtocol = null;
        
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
    
    private ActionErrors errors;
    
    public ActionErrors validate(ActionMapping mapping,
    HttpServletRequest request) {
        
        errors = new ActionErrors();
        
        String submit = request.getParameter("submit");
        
        // front end validation when save is clicked.
        if (submit != null) {
            
            /* general */
            numberCheck("acceptCountText", acceptCountText, true, 0, 128);
            numberCheck("connTimeOutText", connTimeOutText, true, -1, 60000);
            numberCheck("bufferSizeText", bufferSizeText, true, 1, 8192);
  
            /* The IP address can also be null -- which means open the
             server socket on *all* IP addresses for this host */
            if ((address.length() > 0) && !address.equalsIgnoreCase(" ")) {
                try {
                    InetAddress.getByName(address);
                } catch (Exception e) {
                    errors.add("address", new ActionMessage("error.address.invalid"));
                }
            } else {
                address = " ";
            }
            
            /* ports */
            numberCheck("portNumber",  portText, true, 1, 65535);
            numberCheck("redirectPortText",  redirectPortText, true, -1, 65535);
            
            /* processors*/
            numberCheck("minProcessorsText",  minProcessorsText, true, 1, 512);
            try {
                // if min is a valid integer, then check that max >= min
                int min = Integer.parseInt(minProcessorsText);
                numberCheck("maxProcessorsText",  maxProcessorsText, true, min, 512);
            } catch (Exception e) {
                // check for the complete range
                numberCheck("maxProcessorsText",  maxProcessorsText, true, 1, 512);
            }
            
            // proxy                  
            if ((proxyName!= null) && (proxyName.length() > 0)) {
                try {
                    InetAddress.getByName(proxyName);
                } catch (Exception e) {
                    errors.add("proxyName", new ActionMessage("error.proxyName.invalid"));
                }
            }   
            
            // supported only by Coyote HTTP and HTTPS connectors
            if (!("AJP".equalsIgnoreCase(connectorType)))
                numberCheck("proxyPortText",  proxyPortText, true, 0, 65535);            
        }
        
        return errors;
    }
    
    /*
     * Helper method to check that it is a required number and
     * is a valid integer within the given range. (min, max).
     *
     * @param  field  The field name in the form for which this error occured.
     * @param  numText  The string representation of the number.
     * @param rangeCheck  Boolean value set to true of reange check should be performed.
     *
     * @param  min  The lower limit of the range
     * @param  max  The upper limit of the range
     *
     */
    
    public void numberCheck(String field, String numText, boolean rangeCheck,
    int min, int max) {
        
        /* Check for 'is required' */
        if ((numText == null) || (numText.length() < 1)) {
            errors.add(field, new ActionMessage("error."+field+".required"));
        } else {
            
        /*check for 'must be a number' in the 'valid range'*/
            try {
                int num = Integer.parseInt(numText);
                // perform range check only if required
                if (rangeCheck) {
                    if ((num < min) || (num > max ))
                        errors.add( field,
                        new ActionMessage("error."+ field +".range"));
                }
            } catch (NumberFormatException e) {
                errors.add(field,
                new ActionMessage("error."+ field + ".format"));
            }
        }
    }

}
