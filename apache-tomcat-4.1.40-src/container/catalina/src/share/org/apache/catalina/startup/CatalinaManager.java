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

import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

/**
 * Implementation of the Catalina JMX MBean as a wrapper of the Catalina class.
 * To be used, the JAR containing this MBean should contain all the classes
 * which are present in bootstrap.jar. The setPath(String path) method should
 * be used to set the correct path where the Tomcat distribution is.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 466595 $
 */

public final class CatalinaManager
    extends NotificationBroadcasterSupport
    implements CatalinaManagerMBean, MBeanRegistration {


    // ----------------------------------------------------- Instance Variables


    /**
     * Status of the Slide domain.
     */
    private int state = STOPPED;


    /**
     * Notification sequence number.
     */
    private long sequenceNumber = 0;


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
     * Path accessor.
     */
    public String getPath() {
        return System.getProperty("catalina.home");
    }


    /**
     * Config file path mutator.
     */
    public void setPath(String path) {
        System.setProperty("catalina.home", path);
    }


    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception {

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

            String[] args = { "start" };
            Bootstrap.main(args);

        } catch (Throwable t) {
            state = STOPPED;
            notification = new AttributeChangeNotification
                (this, sequenceNumber++, System.currentTimeMillis(),
                 "Stopped " + NAME, "State", "java.lang.Integer",
                 new Integer(STARTING), new Integer(STOPPED));
            sendNotification(notification);
        }

        state = STARTED;
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(),
             "Started " + NAME, "State", "java.lang.Integer",
             new Integer(STARTING), new Integer(STARTED));
        sendNotification(notification);

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

            String[] args = { "stop" };
            Bootstrap.main(args);

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


}
