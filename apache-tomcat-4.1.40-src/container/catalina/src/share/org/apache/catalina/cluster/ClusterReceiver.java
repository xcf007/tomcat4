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

package org.apache.catalina.cluster;

import org.apache.catalina.Logger;

/**
 * This class is responsible for Receiving incoming packets in a Cluster.
 * Different Implementations may use different protocol to
 * communicate within the Cluster.
 *
 * @author Bip Thelin
 * @version $Revision: 466595 $, $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public interface ClusterReceiver extends Runnable {

    // --------------------------------------------------------- Public Methods

    /**
     * The senderId is a identifier used to identify different
     * packages being received in a Cluster. Each package received through
     * the concrete implementation of this interface will have
     * the senderId set at runtime. Usually the senderId is the
     * name of the component that is using this <code>ClusterReceiver</code>
     *
     * @param senderId The senderId to use
     */
    public void setSenderId(String senderId);

    /**
     * get the senderId used to identify messages being received in a Cluster.
     *
     * @return The senderId for this ClusterReceiver
     */
    public String getSenderId();

    /**
     * Set the debug detail level for this component.
     *
     * @param debug The debug level
     */
    public void setDebug(int debug);

    /**
     * Get the debug level for this component
     *
     * @return The debug level
     */
    public int getDebug();

    /**
     * Set the time in seconds for this component to
     * Sleep before it checks for new received data in the Cluster
     *
     * @param checkInterval The time to sleep
     */
    public void setCheckInterval(int checkInterval);

    /**
     * Get the time in seconds this implementation sleeps
     *
     * @return The time in seconds this implementation sleeps
     */
    public int getCheckInterval();

    /**
     * Set the Logger for this component.
     *
     * @param logger The Logger to use with this component.
     */
    public void setLogger(Logger logger);

    /**
     * Get the Logger for this component
     *
     * @return The Logger associated with this component.
     */
    public Logger getLogger();

    /**
     * The log method to use in the implementation
     *
     * @param message The message to be logged.
     */
    public void log(String message);

    /**
     * Get an array of objects that has been received by this component.
     * Only Objects which was received with the same senderId as the
     * one specified for this <code>ClusterReceiver</code> is being returned.
     *
     * @return a value of type 'Object[]'
     */
    public Object[] getObjects();

    /**
     * Start this component, must be called before it can be used.
     */
    public void start();

    /*
     * The background thread.
     */
    public void run();

    /**
     * The stop method for this component, should be called when closing
     * down the Cluster.
     */
    public void stop();
}
