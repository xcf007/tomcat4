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
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Mapper;
import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of <code>Mapper</code> for a <code>Host</code>,
 * designed to process HTTP requests.  This mapper selects an appropriate
 * <code>Context</code> based on the request URI included in the request.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  This Mapper only works with a
 * <code>StandardHost</code>, because it relies on internal APIs.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class StandardHostMapper
    implements Mapper {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Mapper is associated.
     */
    private StandardHost host = null;


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

        return (host);

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

        if (!(container instanceof StandardHost))
            throw new IllegalArgumentException
                (sm.getString("httpHostMapper.container"));
        host = (StandardHost) container;

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

        // Has this request already been mapped?
        if (update && (request.getContext() != null))
            return (request.getContext());

        // Perform mapping on our request URI
        String uri = ((HttpRequest) request).getDecodedRequestURI();
        Context context = host.map(uri);

        // Update the request (if requested) and return the selected Context
        if (update) {
            request.setContext(context);
            if (context != null)
                ((HttpRequest) request).setContextPath(context.getPath());
            else
                ((HttpRequest) request).setContextPath(null);
        }
        return (context);

    }


}
