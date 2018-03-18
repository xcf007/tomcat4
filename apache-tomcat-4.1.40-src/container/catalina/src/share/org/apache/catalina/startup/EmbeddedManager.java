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

package org.apache.catalina.startup;

import java.net.InetAddress;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

/**
 * Implementation of the Catalina JMX MBean as a wrapper of the Catalina class.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 466595 $
 */

public final class EmbeddedManager
    extends NotificationBroadcasterSupport
    implements EmbeddedManagerMBean, MBeanRegistration {


    // ----------------------------------------------------- Instance Variables


    /**
     * Status of the Slide domain.
     */
    private int state = STOPPED;


    /**
     * Notification sequence number.
     */
    private long sequenceNumber = 0;


    /**
     * Embedded Catalina.
     */
    private Embedded embedded = new Embedded();


    // ---------------------------------------------- MBeanRegistration Methods


    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        return new ObjectName(OBJECT_NAME);
    }


    public void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue())
            destroy();
    }


    public void preDeregister()
        throws Exception {
    }


    public void postDeregister() {
        destroy();
    }


    // ----------------------------------------------------- SlideMBean Methods


    /**
     * Retruns the Catalina component name.
     */
    public String getName() {
        return NAME;
    }


    /**
     * Returns the state.
     */
    public int getState() {
        return state;
    }


    /**
     * Returns a String representation of the state.
     */
    public String getStateString() {
        return states[state];
    }


    /**
     * Start the servlet container.
     */
    public void start() {

        Notification notification = null;

        if (state != STOPPED)
            return;

        state = STARTING;

        // Notifying the MBEan server that we're starting

        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(),
             "Starting " + NAME, "State", "java.lang.Integer",
             new Integer(STOPPED), new Integer(STARTING));
        sendNotification(notification);

        try {

            embedded.start();

            state = STARTED;
            notification = new AttributeChangeNotification
                (this, sequenceNumber++, System.currentTimeMillis(),
                 "Started " + NAME, "State", "java.lang.Integer",
                 new Integer(STARTING), new Integer(STARTED));
            sendNotification(notification);

        } catch (Throwable t) {
            state = STOPPED;
            notification = new AttributeChangeNotification
                (this, sequenceNumber++, System.currentTimeMillis(),
                 "Stopped " + NAME, "State", "java.lang.Integer",
                 new Integer(STARTING), new Integer(STOPPED));
            sendNotification(notification);
        }

    }


    /**
     * Stop the servlet container.
     */
    public void stop() {

        Notification notification = null;

        if (state != STARTED)
            return;

        state = STOPPING;

        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(),
             "Stopping " + NAME, "State", "java.lang.Integer",
             new Integer(STARTED), new Integer(STOPPING));
        sendNotification(notification);

        try {

            embedded.stop();

        } catch (Throwable t) {

            // FIXME
            t.printStackTrace();

        }

        state = STOPPED;

        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(),
             "Stopped " + NAME, "State", "java.lang.Integer",
             new Integer(STOPPING), new Integer(STOPPED));
        sendNotification(notification);

    }


    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy() {

        if (getState() != STOPPED)
            stop();

    }


   /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {
        return embedded.getDebug();
    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        embedded.setDebug(debug);
    }


    /**
     * Return true if naming is enabled.
     */
    public boolean isUseNaming() {
        return embedded.isUseNaming();
    }


    /**
     * Enables or disables naming support.
     *
     * @param useNaming The new use naming value
     */
    public void setUseNaming(boolean useNaming) {
        embedded.setUseNaming(useNaming);
    }


    /**
     * Return the Logger for this component.
     */
    public Logger getLogger() {
        return embedded.getLogger();
    }


    /**
     * Set the Logger for this component.
     *
     * @param logger The new logger
     */
    public void setLogger(Logger logger) {
        embedded.setLogger(logger);
    }


    /**
     * Return the default Realm for our Containers.
     */
    public Realm getRealm() {
        return embedded.getRealm();
    }


    /**
     * Set the default Realm for our Containers.
     *
     * @param realm The new default realm
     */
    public void setRealm(Realm realm) {
        embedded.setRealm(realm);
    }


    /**
     * Return the secure socket factory class name.
     */
    public String getSocketFactory() {
        return embedded.getSocketFactory();
    }


    /**
     * Set the secure socket factory class name.
     *
     * @param socketFactory The new secure socket factory class name
     */
    public void setSocketFactory(String socketFactory) {
        embedded.setSocketFactory(socketFactory);
    }


    /**
     * Add a new Connector to the set of defined Connectors.  The newly
     * added Connector will be associated with the most recently added Engine.
     *
     * @param connector The connector to be added
     *
     * @exception IllegalStateException if no engines have been added yet
     */
    public void addConnector(Connector connector) {
        embedded.addConnector(connector);
    }


    /**
     * Add a new Engine to the set of defined Engines.
     *
     * @param engine The engine to be added
     */
    public void addEngine(Engine engine) {
        embedded.addEngine(engine);
    }


    /**
     * Create, configure, and return a new TCP/IP socket connector
     * based on the specified properties.
     *
     * @param address InetAddress to listen to, or <code>null</code>
     *  to listen on all address on this server
     * @param port Port number to listen to
     * @param secure Should this port be SSL-enabled?
     */
    public Connector createConnector(InetAddress address, int port,
                                     boolean secure) {
        return embedded.createConnector(address, port, secure);
    }


    /**
     * Create, configure, and return a Context that will process all
     * HTTP requests received from one of the associated Connectors,
     * and directed to the specified context path on the virtual host
     * to which this Context is connected.
     * <p>
     * After you have customized the properties, listeners, and Valves
     * for this Context, you must attach it to the corresponding Host
     * by calling:
     * <pre>
     *   host.addChild(context);
     * </pre>
     * which will also cause the Context to be started if the Host has
     * already been started.
     *
     * @param path Context path of this application ("" for the default
     *  application for this host, must start with a slash otherwise)
     * @param docBase Absolute pathname to the document base directory
     *  for this web application
     *
     * @exception IllegalArgumentException if an invalid parameter
     *  is specified
     */
    public Context createContext(String path, String docBase) {
        return embedded.createContext(path, docBase);
    }


    /**
     * Create, configure, and return an Engine that will process all
     * HTTP requests received from one of the associated Connectors,
     * based on the specified properties.
     */
    public Engine createEngine() {
        return embedded.createEngine();
    }


    /**
     * Create, configure, and return a Host that will process all
     * HTTP requests received from one of the associated Connectors,
     * and directed to the specified virtual host.
     * <p>
     * After you have customized the properties, listeners, and Valves
     * for this Host, you must attach it to the corresponding Engine
     * by calling:
     * <pre>
     *   engine.addChild(host);
     * </pre>
     * which will also cause the Host to be started if the Engine has
     * already been started.  If this is the default (or only) Host you
     * will be defining, you may also tell the Engine to pass all requests
     * not assigned to another virtual host to this one:
     * <pre>
     *   engine.setDefaultHost(host.getName());
     * </pre>
     *
     * @param name Canonical name of this virtual host
     * @param appBase Absolute pathname to the application base directory
     *  for this virtual host
     *
     * @exception IllegalArgumentException if an invalid parameter
     *  is specified
     */
    public Host createHost(String name, String appBase) {
        return embedded.createHost(name, appBase);
    }


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return embedded.getInfo();
    }


    /**
     * Remove the specified Connector from the set of defined Connectors.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector) {
        embedded.removeConnector(connector);
    }


    /**
     * Remove the specified Context from the set of defined Contexts for its
     * associated Host.  If this is the last Context for this Host, the Host
     * will also be removed.
     *
     * @param context The Context to be removed
     */
    public void removeContext(Context context) {
        embedded.removeContext(context);
    }


    /**
     * Remove the specified Engine from the set of defined Engines, along with
     * all of its related Hosts and Contexts.  All associated Connectors are
     * also removed.
     *
     * @param engine The Engine to be removed
     */
    public void removeEngine(Engine engine) {
        embedded.removeEngine(engine);
    }


    /**
     * Remove the specified Host, along with all of its related Contexts,
     * from the set of defined Hosts for its associated Engine.  If this is
     * the last Host for this Engine, the Engine will also be removed.
     *
     * @param host The Host to be removed
     */
    public void removeHost(Host host) {
        embedded.removeHost(host);
    }


}
