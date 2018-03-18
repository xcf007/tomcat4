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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;


/**
 * This class is responsible for checking for incoming multicast
 * data and determine if the data belongs to us and if so push
 * it onto an internal stack and let it be picked up when needed.
 *
 * @author Bip Thelin
 * @version $Revision: 466595 $, $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class MulticastReceiver
    extends ClusterSessionBase implements ClusterReceiver {

    // ----------------------------------------------------- Instance Variables

    /**
     * The unique message ID
     */
    private static String senderId = null;

    /**
     * The MulticastSocket to use
     */
    private MulticastSocket multicastSocket = null;

    /**
     * Our Thread name
     */
    private String threadName = "MulticastReceiver";

    /**
     * The name of our component, used for logging.
     */
    private String receiverName = "MulticastReceiver";

    /**
     * The stack that keeps incoming requests
     */
    private static Vector stack = new Vector();

    /**
     * Has this component been started?
     */
    private boolean started = false;

    /**
     * The background thread.
     */
    private Thread thread = null;

    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;

    /**
     * The interval for the background thread to sleep
     */
    private int checkInterval = 5;

    // --------------------------------------------------------- Public Methods

    /**
     * Create a new MulticastReceiver.
     *
     * @param senderId The unique senderId
     * @param multicastSocket The MulticastSocket to use
     */
    MulticastReceiver(String senderId, MulticastSocket multicastSocket,
                    InetAddress multicastAddress, int multicastPort) {
        this.multicastSocket = multicastSocket;
        this.senderId = senderId;
    }

    /**
     * Return a <code>String</code> containing the name of this
     * implementation, used for logging
     *
     * @return The name of the implementation
     */
    public String getName() {
        return(this.receiverName);
    }

    /**
     * Set the time in seconds for this component to
     * Sleep before it checks for new received data in the Cluster
     *
     * @param checkInterval The time to sleep
     */
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * Get the time in seconds this Cluster sleeps
     *
     * @return The time in seconds this Cluster sleeps
     */
    public int getCheckInterval() {
        return(this.checkInterval);
    }

    /**
     * Receive the objects currently in our stack and clear
     * if afterwards.
     *
     * @return An array with objects
     */
    public Object[] getObjects() {
        synchronized (stack) {
            Object[] objs = stack.toArray();
            stack.removeAllElements();
            return (objs);
        }
    }

    /**
     * Start our component
     */
    public void start() {
        started = true;

        // Start the background reaper thread
        threadStart();
    }

    /**
     * Stop our component
     */
    public void stop() {
        started = false;

        // Stop the background reaper thread
        threadStop();
    }


    // -------------------------------------------------------- Private Methods

    /**
     * Check our multicast socket for new data and determine if the
     * data matches us(senderId) and if so push it onto the stack,
     */
    private void receive() {
        try {
            byte[] buf = new byte[5000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            ByteArrayInputStream ips = null;
            ObjectInputStream ois = null;

            multicastSocket.receive(recv);
            ips = new ByteArrayInputStream(buf, 0, buf.length);
            ois = new ObjectInputStream(ips);
            ReplicationWrapper obj = (ReplicationWrapper)ois.readObject();

            if(obj.getSenderId().equals(this.senderId))
                stack.add(obj);
        } catch (IOException e) {
            log("An error occurred when trying to replicate: "+
                e.toString());
        } catch (ClassNotFoundException e) {
            log("An error occurred when trying to replicate: "+
                e.toString());
        }
    }

    // ------------------------------------------------------ Background Thread

    /**
     * The background thread.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            receive();
            threadSleep();
        }
    }

    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    private void threadSleep() {
        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
            ;
        }
    }

    /**
     * Start the background thread.
     */
    private void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        threadName = threadName+"["+senderId+"]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stop the background thread.
     */
    private void threadStop() {
        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;
    }
}
