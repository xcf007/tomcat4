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

package org.apache.webapp.admin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.management.MBeanServer;
import javax.management.ObjectName;


/**
 * General purpose utility methods to create lists of objects that are
 * commonly required in building the user interface.  In all cases, if there
 * are no matching elements, a zero-length list (rather than <code>null</code>)
 * is returned.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class Lists {


    // ----------------------------------------------------------- Constructors


    /**
     * Protected constructor to prevent instantiation.
     */
    protected Lists() { }


    // ------------------------------------------------------- Static Variables


    /**
     * Precomputed list of debug level labels and values.
     */
    private static List debugLevels = new ArrayList();

    static {
        debugLevels.add(new LabelValueBean("0", "0"));
        debugLevels.add(new LabelValueBean("1", "1"));
        debugLevels.add(new LabelValueBean("2", "2"));
        debugLevels.add(new LabelValueBean("3", "3"));
        debugLevels.add(new LabelValueBean("4", "4"));
        debugLevels.add(new LabelValueBean("5", "5"));
        debugLevels.add(new LabelValueBean("6", "6"));
        debugLevels.add(new LabelValueBean("7", "7"));
        debugLevels.add(new LabelValueBean("8", "8"));
        debugLevels.add(new LabelValueBean("9", "9"));
    }

    /**
     * Precomputed list of verbosity level labels and values.
     */
    private static List verbosityLevels = new ArrayList();

    static {
        verbosityLevels.add(new LabelValueBean("0", "0"));
        verbosityLevels.add(new LabelValueBean("1", "1"));
        verbosityLevels.add(new LabelValueBean("2", "2"));
        verbosityLevels.add(new LabelValueBean("3", "3"));
        verbosityLevels.add(new LabelValueBean("4", "4"));
    }

    /**
     * Precomputed list of (true,false) labels and values.
     */
    private static List booleanValues = new ArrayList();

    static {
            booleanValues.add(new LabelValueBean("True", "true"));
            booleanValues.add(new LabelValueBean("False", "false"));
    }
    
    /**
     * Precomputed list of clientAuth labels and values.
     */
    private static List clientAuthValues = new ArrayList();

    static {
            clientAuthValues.add(new LabelValueBean("True","true"));
            clientAuthValues.add(new LabelValueBean("False","false"));
            clientAuthValues.add(new LabelValueBean("Want","want"));
    }
    
    /**
     * Precomputed list of thread priority labels and values.
     */
    private static List threadPriorityValues = new ArrayList();
    
    static {
        threadPriorityValues.add(
                new LabelValueBean("Min", "" + Thread.MIN_PRIORITY));
        threadPriorityValues.add(
                new LabelValueBean("Norm", "" + Thread.NORM_PRIORITY));
        threadPriorityValues.add(
                new LabelValueBean("Max", "" + Thread.MAX_PRIORITY));
    }
    // --------------------------------------------------------- Public Methods


    /**
     * Return a <code>List</code> of {@link LabelValueBean}s for the legal
     * settings for <code>debug</code> properties.
     */
    public static List getDebugLevels() {

        return (debugLevels);

    }
    
    /**
     * Return a <code>List</code> of {@link LabelValueBean}s for the legal
     * settings for <code>verbosity</code> properties.
     */
    public static List getVerbosityLevels() {

        return (verbosityLevels);

    }

    /**
     * Return a <code>List</code> of {@link LabelValueBean}s for the legal
     * settings for <code>boolean</code> properties.
     */
    public static List getBooleanValues() {

        return (booleanValues);

    }

    /**
     * Return a <code>List</code> of {@link LabelValueBean}s for the legal
     * settings for <code>clientAuth</code> properties.
     */
    public static List getClientAuthValues() {

        return (clientAuthValues);

    }

    /**
     * Return a <code>List</code> of {@link LabelValueBean}s for the legal
     * settings for <code>threadPriority</code> properties.
     */
    public static List getThreadPriorityValues() {

        return (threadPriorityValues);

    }

    /**
     * Return a list of <code>Connector</code> object name strings
     * for the specified <code>Service</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param service Object name of the service for which to select connectors
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getConnectors(MBeanServer mbserver, ObjectName service)
        throws Exception {

        StringBuffer sb = new StringBuffer(service.getDomain());
        sb.append(":type=Connector,service=");
        sb.append(service.getKeyProperty("name"));
        sb.append(",*");
        ObjectName search = new ObjectName(sb.toString());
        ArrayList connectors = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            connectors.add(names.next().toString());
        }
        Collections.sort(connectors);
        return (connectors);

    }


    /**
     * Return a list of <code>Connector</code> object name strings
     * for the specified <code>Service</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param service Object name of the service for which to select connectors
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getConnectors(MBeanServer mbserver, String service)
        throws Exception {

        return (getConnectors(mbserver, new ObjectName(service)));

    }


    /**
     * Return a list of <code>Context</code> object name strings
     * for the specified <code>Host</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param host Object name of the host for which to select contexts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getContexts(MBeanServer mbserver, ObjectName host)
        throws Exception {

        StringBuffer sb = new StringBuffer(host.getDomain());
        sb.append(":type=Context,host=");
        sb.append(host.getKeyProperty("host"));
        sb.append(",service=");
        sb.append(host.getKeyProperty("service"));
        sb.append(",*");
        ObjectName search = new ObjectName(sb.toString());
        ArrayList contexts = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            contexts.add(names.next().toString());
        }
        Collections.sort(contexts);
        return (contexts);

    }


    /**
     * Return a list of <code>DefaultContext</code> object name strings
     * for the specified <code>Host</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select default
     * contexts
     * @param containerType The type of the container for which to select 
     * default contexts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getDefaultContexts(MBeanServer mbserver, String 
        container) throws Exception {

        return (getDefaultContexts(mbserver, new ObjectName(container)));

    }
    
    
    /**
     * Return a list of <code>DefaultContext</code> object name strings
     * for the specified <code>Host</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select default
     * contexts
     * @param containerType The type of the container for which to select 
     * default contexts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getDefaultContexts(MBeanServer mbserver, ObjectName 
        container) throws Exception {
        
        StringBuffer sb = new StringBuffer(container.getDomain());
        sb.append(":type=DefaultContext");
        String type = container.getKeyProperty("type");
        String host = container.getKeyProperty("host");
        if ("Host".equals(type)) {
            host = container.getKeyProperty("host");
        }
        if (host != null) {
            sb.append(",host=");
            sb.append(host);
        }
        String service = container.getKeyProperty("service");
        if ("Service".equals(type)) {
            service = container.getKeyProperty("name");
        }
        if (service != null) {
            sb.append(",service=");
            sb.append(service);
        }
        ObjectName search = new ObjectName(sb.toString());
        ArrayList defaultContexts = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            String name = names.next().toString();
            defaultContexts.add(name);
        }
        Collections.sort(defaultContexts);
        return (defaultContexts);

    }


    /**
     * Return a list of <code>Context</code> object name strings
     * for the specified <code>Host</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param host Object name of the host for which to select contexts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getContexts(MBeanServer mbserver, String host)
        throws Exception {

        return (getContexts(mbserver, new ObjectName(host)));

    }

    /**
     * Return a list of <code>Host</code> object name strings
     * for the specified <code>Service</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param service Object name of the service for which to select hosts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getHosts(MBeanServer mbserver, ObjectName service)
        throws Exception {

        StringBuffer sb = new StringBuffer(service.getDomain());
        sb.append(":type=Host,service=");
        sb.append(service.getKeyProperty("name"));
        sb.append(",*");
        ObjectName search = new ObjectName(sb.toString());
        ArrayList hosts = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            hosts.add(names.next().toString());
        }
        Collections.sort(hosts);
        return (hosts);

    }


    /**
     * Return a list of <code>Host</code> object name strings
     * for the specified <code>Service</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param service Object name of the service for which to select hosts
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getHosts(MBeanServer mbserver, String service)
        throws Exception {

        return (getHosts(mbserver, new ObjectName(service)));

    }


    /**
     * Return a list of <code>Logger</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  loggers
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getLoggers(MBeanServer mbserver, ObjectName container)
        throws Exception {

        StringBuffer sb = new StringBuffer(container.getDomain());
        sb.append(":type=Logger");
        String type = container.getKeyProperty("type");
        String path = container.getKeyProperty("path");
        if (path != null) {
            sb.append(",path=");
            sb.append(path);
        }
        String host = container.getKeyProperty("host");
        if ("Host".equals(type)) {
            host = container.getKeyProperty("host");
        }
        if (host != null) {
            sb.append(",host=");
            sb.append(host);
        }
        String service = container.getKeyProperty("service");
        if ("Service".equals(type)) {
            service = container.getKeyProperty("name");
        }
        if (service != null) {
            sb.append(",service=");
            sb.append(service);
        }
        ObjectName search = new ObjectName(sb.toString());
        ArrayList loggers = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            loggers.add(names.next().toString());
        }
        Collections.sort(loggers);
        return (loggers);

    }


    /**
     * Return a list of <code>Logger</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  loggers
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getLoggers(MBeanServer mbserver, String container)
        throws Exception {

        return (getLoggers(mbserver, new ObjectName(container)));

    }


    /**
     * Return a list of <code>Realm</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  realms
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getRealms(MBeanServer mbserver, ObjectName container)
        throws Exception {

        StringBuffer sb = new StringBuffer(container.getDomain());
        sb.append(":type=Realm");
        String type = container.getKeyProperty("type");
        String path = container.getKeyProperty("path");
        if (path != null) {
            sb.append(",path=");
            sb.append(path);
        }
        String host = container.getKeyProperty("host");
        if ("Host".equals(type)) {
            host = container.getKeyProperty("host");
        }
        if (host != null) {
            sb.append(",host=");
            sb.append(host);
        }
        String service = container.getKeyProperty("service");
        if ("Service".equals(type)) {
            service = container.getKeyProperty("name");
        }
        if (service != null) {
            sb.append(",service=");
            sb.append(service);
        }
        ObjectName search = new ObjectName(sb.toString());
        ArrayList realms = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            realms.add(names.next().toString());
        }
        Collections.sort(realms);
        return (realms);

    }


    /**
     * Return a list of <code>Realm</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  realms
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getRealms(MBeanServer mbserver, String container)
        throws Exception {

        return (getRealms(mbserver, new ObjectName(container)));

    }

    /**
     * Return a list of <code>Valve</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  Valves
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getValves(MBeanServer mbserver, ObjectName container)
        throws Exception {

        StringBuffer sb = new StringBuffer(container.getDomain());
        sb.append(":type=Valve");
        String type = container.getKeyProperty("type");
        sb.append(TomcatTreeBuilder.WILDCARD);
        
        String service = container.getKeyProperty("service");
        String host = container.getKeyProperty("host");
        String path = container.getKeyProperty("path");
        
        if ("Service".equals(type)) {
            service = container.getKeyProperty("name");
        }
        if (service != null) {
            sb.append(",service=");
            sb.append(service);
        }
        
        ObjectName search = new ObjectName(sb.toString());        
        ArrayList valves = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            ObjectName valve = (ObjectName) names.next();
            String vpath = valve.getKeyProperty("path");            
            String vhost = valve.getKeyProperty("host");
            
            String valveType = null;
            String className = (String) 
                    mbserver.getAttribute(valve, "className");
            int period = className.lastIndexOf(".");
            if (period >= 0)
                valveType = className.substring(period + 1);

           // Return only user-configurable valves.
           if ("AccessLogValve".equalsIgnoreCase(valveType) ||
               "RemoteAddrValve".equalsIgnoreCase(valveType) ||
               "RemoteHostValve".equalsIgnoreCase(valveType) || 
               "RequestDumperValve".equalsIgnoreCase(valveType) ||
               "SingleSignOn".equalsIgnoreCase(valveType)) {
            // if service is the container, then the valve name
            // should not contain path or host                   
            if ("Service".equalsIgnoreCase(type)) {
                if ((vpath == null) && (vhost == null)) {
                    valves.add(valve.toString());
                }
            } 
            
            if ("Host".equalsIgnoreCase(type)) {
                if ((vpath == null) && (host.equalsIgnoreCase(vhost))) { 
                    valves.add(valve.toString());      
                }
            }
            
            if ("Context".equalsIgnoreCase(type)) {
                if ((path.equalsIgnoreCase(vpath)) && (host.equalsIgnoreCase(vhost))) {
                    valves.add(valve.toString());      
                }
            }
           }
        }        
        Collections.sort(valves);
        return (valves);
    }

    
    /**
     * Return a list of <code>Valve</code> object name strings
     * for the specified container (service, host, or context) object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param container Object name of the container for which to select
     *                  valves
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getValves(MBeanServer mbserver, String container)
        throws Exception {

        return (getValves(mbserver, new ObjectName(container)));

    }
    
    /**
     * Return a list of <code>Server</code> object name strings.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getServers(MBeanServer mbserver)
        throws Exception {

        ObjectName search = new ObjectName("Catalina:type=Server,*");
        ArrayList servers = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            servers.add(names.next().toString());
        }
        Collections.sort(servers);
        return (servers);

    }


    /**
     * Return a list of <code>Service</code> object name strings
     * for the specified <code>Server</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param server Object name of the server for which to select services
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getServices(MBeanServer mbserver, ObjectName server)
        throws Exception {

        StringBuffer sb = new StringBuffer(server.getDomain());
        sb.append(":type=Service,*");
        ObjectName search = new ObjectName(sb.toString());
        ArrayList services = new ArrayList();
        Iterator names = mbserver.queryNames(search, null).iterator();
        while (names.hasNext()) {
            services.add(names.next().toString());
        }
        Collections.sort(services);
        return (services);

    }


    /**
     * Return a list of <code>Service</code> object name strings
     * for the specified <code>Server</code> object name.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param server Object name of the server for which to select services
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static List getServices(MBeanServer mbserver, String server)
        throws Exception {

        return (getServices(mbserver, new ObjectName(server)));

    }


    /**
     * Return the  <code>Service</code> object name string
     * that the admin app belongs to.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param request Http request
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static String getAdminAppService
        (MBeanServer mbserver, String domain, HttpServletRequest request)
        throws Exception {

        // Get the admin app's service name
        StringBuffer sb = new StringBuffer(domain);
        sb.append(":type=Context");
        sb.append(",path=");
        sb.append(request.getContextPath());
        sb.append(",*");
        ObjectName search = new ObjectName(sb.toString());
        Iterator names = mbserver.queryNames(search, null).iterator();
        String service = null;
        while (names.hasNext()) {
            service = ((ObjectName)names.next()).getKeyProperty("service");
        }
        return service;

    }


    /**
     * Return the  <code>Host</code> object name string
     * that the admin app belongs to.
     *
     * @param mbserver MBeanServer from which to retrieve the list
     * @param request Http request
     *
     * @exception Exception if thrown while retrieving the list
     */
    public static String getAdminAppHost
        (MBeanServer mbserver, String domain, HttpServletRequest request)
        throws Exception {

        // Get the admin app's host name
        StringBuffer sb = new StringBuffer(domain);
        sb.append(":type=Context");
        sb.append(",path=");
        sb.append(request.getContextPath());
        sb.append(",*");
        ObjectName search = new ObjectName(sb.toString());
        Iterator names = mbserver.queryNames(search, null).iterator();
        String host = null;
        while (names.hasNext()) {
            host = ((ObjectName)names.next()).getKeyProperty("host");
        }
        return host;

    }

}
