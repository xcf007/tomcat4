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


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>Response</b>
 * object.  By default, all methods are delegated to the wrapped response,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 * @deprecated
 */

public abstract class ResponseWrapper implements Response {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseWrapper(Response response) {

        super();
        this.response = response;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The wrapped response.
     */
    protected Response response = null;


    /**
     * Return the wrapped response.
     */
    public Response getWrappedResponse() {

        return (this.response);

    }


    // ------------------------------------------------------------- Properties


    /**
     * Return the Connector through which this Response is returned.
     */
    public Connector getConnector() {

        return (response.getConnector());

    }


    /**
     * Set the Connector through which this Response is returned.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {

        response.setConnector(connector);

    }


    /**
     * Return the number of bytes actually written to the output stream.
     */
    public int getContentCount() {

        return (response.getContentCount());

    }


    /**
     * Return the Context with which this Response is associated.
     */
    public Context getContext() {

        return (response.getContext());

    }


    /**
     * Set the Context with which this Response is associated.  This should
     * be called as soon as the appropriate Context is identified.
     *
     * @param context The associated Context
     */
    public void setContext(Context context) {

        response.setContext(context);

    }


    /**
     * Return the "processing inside an include" flag.
     */
    public boolean getIncluded() {

        return (response.getIncluded());

    }


    /**
     * Set the "processing inside an include" flag.
     *
     * @param included <code>true</code> if we are currently inside a
     *  RequestDispatcher.include(), else <code>false</code>
     */
    public void setIncluded(boolean included) {

        response.setIncluded(included);

    }


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (response.getInfo());

    }


    /**
     * Return the Request with which this Response is associated.
     */
    public Request getRequest() {

        return (response.getRequest());

    }


    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(Request request) {

        response.setRequest(request);

    }


    /**
     * Return the <code>ServletResponse</code> for which this object
     * is the facade.
     */
    public ServletResponse getResponse() {

        return (response.getResponse());

    }


    /**
     * Return the output stream associated with this Response.
     */
    public OutputStream getStream() {

        return (response.getStream());

    }


    /**
     * Set the output stream associated with this Response.
     *
     * @param stream The new output stream
     */
    public void setStream(OutputStream stream) {

        response.setStream(stream);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream createOutputStream() throws IOException {

        return (response.createOutputStream());

    }


    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishResponse() throws IOException {

        response.finishResponse();

    }


    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength() {

        return (response.getContentLength());

    }


    /**
     * Return the content type that was set or calculated for this response,
     * or <code>null</code> if no content type was set.
     */
    public String getContentType() {

        return (response.getContentType());

    }


    /**
     * Return a PrintWriter that can be used to render error messages,
     * regardless of whether a stream or writer has already been acquired.
     */
    public PrintWriter getReporter() {

        return (response.getReporter());

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        response.recycle();

    }


    /**
     * Reset the data buffer but not any status or header information.
     */
    public void resetBuffer() {

        response.resetBuffer();

    }


}
