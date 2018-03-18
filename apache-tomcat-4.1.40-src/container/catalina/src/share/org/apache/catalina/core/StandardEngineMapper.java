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


package org.apache.catalina.core;


import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.Mapper;
import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of <code>Mapper</code> for an <code>Engine</code>,
 * designed to process HTTP requests.  This mapper selects an appropriate
 * <code>Host</code> based on the server name included in the request.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  This Mapper only works with a
 * <code>StandardEngine</code>, because it relies on internal APIs.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class StandardEngineMapper
    implements Mapper {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Mapper is associated.
     */
    private StandardEngine engine = null;


    /**
     * The protocol with which this Mapper is associated.
     */
    private String protocol = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Mapper is associated.
     */
    public Container getContainer() {

        return (engine);

    }


    /**
     * Set the Container with which this Mapper is associated.
     *
     * @param container The newly associated Container
     *
     * @exception IllegalArgumentException if this Container is not
     *  acceptable to this Mapper
     */
    public void setContainer(Container container) {

        if (!(container instanceof StandardEngine))
            throw new IllegalArgumentException
                (sm.getString("httpEngineMapper.container"));
        engine = (StandardEngine) container;

    }


    /**
     * Return the protocol for which this Mapper is responsible.
     */
    public String getProtocol() {

        return (this.protocol);

    }


    /**
     * Set the protocol for which this Mapper is responsible.
     *
     * @param protocol The newly associated protocol
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the child Container that should be used to process this Request,
     * based upon its characteristics.  If no such child Container can be
     * identified, return <code>null</code> instead.
     *
     * @param request Request being processed
     * @param update Update the Request to reflect the mapping selection?
     */
    public Container map(Request request, boolean update) {

        int debug = engine.getDebug();

        // Extract the requested server name
        String server = request.getRequest().getServerName();
        if (server == null) {
            server = engine.getDefaultHost();
            if (update)
                request.setServerName(server);
        }
        if (server == null)
            return (null);
        server = server.toLowerCase();
        if (debug >= 1)
            engine.log("Mapping server name '" + server + "'");

        // Find the matching child Host directly
        if (debug >= 2)
            engine.log(" Trying a direct match");
        Host host = (Host) engine.findChild(server);

        // Find a matching Host by alias.  FIXME - Optimize this!
        if (host == null) {
            if (debug >= 2)
                engine.log(" Trying an alias match");
            Container children[] = engine.findChildren();
            for (int i = 0; i < children.length; i++) {
                String aliases[] = ((Host) children[i]).findAliases();
                for (int j = 0; j < aliases.length; j++) {
                    if (server.equals(aliases[j])) {
                        host = (Host) children[i];
                        break;
                    }
                }
                if (host != null)
                    break;
            }
        }

        // Trying the "default" host if any
        if (host == null) {
            if (debug >= 2)
                engine.log(" Trying the default host");
            host = (Host) engine.findChild(engine.getDefaultHost());
        }

        // Update the Request if requested, and return the selected Host
        ;       // No update to the Request is required
        return (host);

    }


}
