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

package org.apache.catalina;


import org.apache.catalina.cluster.ClusterMemberInfo;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterSender;

/**
 * A <b>Cluster</b> works as a Cluster client/server for the local host
 * Different Cluster implementations can be used to support different
 * ways to communicate within the Cluster. A Cluster implementation is
 * responsible for setting up a way to communicate within the Cluster
 * and also supply "ClientApplications" with <code>ClusterSender</code>
 * used when sending information in the Cluster and
 * <code>ClusterInfo</code> used for receiving information in the Cluster.
 *
 * @author Bip Thelin
 * @version $Revision: 466595 $, $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public interface Cluster {

    // ------------------------------------------------------------- Properties

    /**
     * Return descriptive information about this Cluster implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();

    /**
     * Return the name of the cluster that this Server is currently
     * configured to operate within.
     *
     * @return The name of the cluster associated with this server
     */
    public String getClusterName();

    /**
     * Set the time in seconds that the Cluster waits before
     * checking for changes and replicated data.
     *
     * @param checkInterval The time in seconds to sleep
     */
    public void setCheckInterval(int checkInterval);

    /**
     * Get the time in seconds that this Cluster sleeps.
     *
     * @return The value in seconds
     */
    public int getCheckInterval();

    /**
     * Set the name of the cluster to join, if no cluster with
     * this name is present create one.
     *
     * @param clusterName The clustername to join
     */
    public void setClusterName(String clusterName);

    /**
     * Set the Container associated with our Cluster
     *
     * @param container The Container to use
     */
    public void setContainer(Container container);

    /**
     * Get the Container associated with our Cluster
     *
     * @return The Container associated with our Cluster
     */
    public Container getContainer();

    /**
     * The debug detail level for this Cluster
     *
     * @param debug The debug level
     */
    public void setDebug(int debug);

    /**
     * Returns the debug level for this Cluster
     *
     * @return The debug level
     */
    public int getDebug();

    // --------------------------------------------------------- Public Methods

    /**
     * Returns a collection containing <code>ClusterMemberInfo</code>
     * on the remote members of this Cluster. This method does
     * not include the local host, to retrieve
     * <code>ClusterMemberInfo</code> on the local host
     * use <code>getLocalClusterInfo()</code> instead.
     *
     * @return Collection with all members in the Cluster
     */
    public ClusterMemberInfo[] getRemoteClusterMembers();

    /**
     * Returns a <code>ClusterSender</code> which is the interface
     * to use when sending information in the Cluster. senderId is
     * used as a identifier so that information sent through this
     * instance can only be used with the respectice
     * <code>ClusterReceiver</code>
     *
     * @return The ClusterSender
     */
    public ClusterSender getClusterSender(String senderId);

    /**
     * Returns a <code>ClusterReceiver</code> which is the interface
     * to use when receiving information in the Cluster. senderId is
     * used as a indentifier, only information send through the
     * <code>ClusterSender</code> with the same senderId can be received.
     *
     * @return The ClusterReceiver
     */
    public ClusterReceiver getClusterReceiver(String senderId);

    /**
     * Return cluster information about the local host
     *
     * @return Cluster information
     */
    public ClusterMemberInfo getLocalClusterMember();
}
