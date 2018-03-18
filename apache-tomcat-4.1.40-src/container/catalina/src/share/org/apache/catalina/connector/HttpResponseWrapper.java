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


package org.apache.catalina.connector;


import org.apache.catalina.HttpResponse;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>HttpResponse</b>
 * object.  By default, all methods are delegated to the wrapped response,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 * @deprecated
 */

public abstract class HttpResponseWrapper
    extends ResponseWrapper
    implements HttpResponse {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public HttpResponseWrapper(HttpResponse response) {

        super(response);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the value for the specified header, or <code>null</code> if this
     * header has not been set.  If more than one value was added for this
     * name, only the first is returned; use getHeaderValues() to retrieve all
     * of them.
     *
     * @param name Header name to look up
     */
    public String getHeader(String name) {

        return (((HttpResponse) response).getHeader(name));

    }


    /**
     * Return an array of all the header names set for this response, or
     * a zero-length array if no headers have been set.
     */
    public String[] getHeaderNames() {

        return (((HttpResponse) response).getHeaderNames());

    }


    /**
     * Return an array of all the header values associated with the
     * specified header name, or an zero-length array if there are no such
     * header values.
     *
     * @param name Header name to look up
     */
    public String[] getHeaderValues(String name) {

        return (((HttpResponse) response).getHeaderValues(name));

    }


    /**
     * Return the error message that was set with <code>sendError()</code>
     * for this response.
     */
    public String getMessage() {

        return (((HttpResponse) response).getMessage());

    }


    /**
     * Return the HTTP status code associated with this Response.
     */
    public int getStatus() {

        return (((HttpResponse) response).getStatus());

    }


    /**
     * Reset this response, and specify the values for the HTTP status code
     * and corresponding message.
     *
     * @exception IllegalStateException if this response has already been
     *  committed
     */
    public void reset(int status, String message) {

        ((HttpResponse) response).reset(status, message);

    }


}
