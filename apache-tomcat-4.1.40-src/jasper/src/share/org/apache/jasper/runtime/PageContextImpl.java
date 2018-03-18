/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jasper.runtime;

import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;

import org.apache.jasper.Constants;
import org.apache.jasper.logging.Logger;

/**
 * Implementation of the PageContext class from the JSP spec.
 *
 * @author Anil K. Vijendran
 * @author Larry Cable
 * @author Hans Bergsten
 * @author Pierre Delisle
 */
public class PageContextImpl extends PageContext {

    Logger.Helper loghelper = new Logger.Helper("JASPER_LOG", "PageContextImpl");

    PageContextImpl(JspFactory factory) {
        this.factory = factory;
    }

    public void initialize(Servlet servlet, ServletRequest request,
                           ServletResponse response, String errorPageURL,
                           boolean needsSession, int bufferSize,
                           boolean autoFlush)
        throws IOException, IllegalStateException, IllegalArgumentException
    {
        _initialize(servlet, request, response, errorPageURL, needsSession, bufferSize, autoFlush);
    }

    void _initialize(Servlet servlet, ServletRequest request,
                           ServletResponse response, String errorPageURL,
                           boolean needsSession, int bufferSize,
                           boolean autoFlush)
        throws IOException, IllegalStateException, IllegalArgumentException
    {

        // initialize state

        this.servlet      = servlet;
        this.config          = servlet.getServletConfig();
        this.context          = config.getServletContext();
        this.needsSession = needsSession;
        this.errorPageURL = errorPageURL;
        this.bufferSize   = bufferSize;
        this.autoFlush    = autoFlush;
        this.request      = request;
        this.response     = response;

        // setup session (if required)
        if (request instanceof HttpServletRequest && needsSession)
            this.session = ((HttpServletRequest)request).getSession();

        if (needsSession && session == null)
            throw new IllegalStateException
                ("Page needs a session and none is available");

        // initialize the initial out ...
        depth = -1;
        if (this.baseOut == null) {
            this.baseOut = _createOut(bufferSize, autoFlush);
        } else {
            this.baseOut.init(response, bufferSize, autoFlush);
        }
        this.out = baseOut;

        if (this.out == null)
            throw new IllegalStateException("failed initialize JspWriter");

        // register names/values as per spec

        setAttribute(OUT,         this.out);
        setAttribute(REQUEST,     request);
        setAttribute(RESPONSE,    response);

        if (session != null)
            setAttribute(SESSION, session);

        setAttribute(PAGE,        servlet);
        setAttribute(CONFIG,      config);
        setAttribute(PAGECONTEXT, this);
        setAttribute(APPLICATION,  context);
        
        isIncluded = request.getAttribute(
            "javax.servlet.include.servlet_path") != null;            
    }

    public void release() {
        out = baseOut;
        try {
            if (isIncluded) {
                ((JspWriterImpl)out).flushBuffer();
                // push it into the including jspWriter
            } else {
                // Do not flush the buffer even if we're not included (i.e.
                // we are the main page. The servlet will flush it and close
                // the stream.
                ((JspWriterImpl)out).flushBuffer();
            }
        } catch (IOException ex) {
            loghelper.log("Internal error flushing the buffer in release()");
        }
        servlet      = null;
        config             = null;
        context             = null;
        needsSession = false;
        errorPageURL = null;
        bufferSize   = JspWriter.DEFAULT_BUFFER;
        autoFlush    = true;
        request      = null;
        response     = null;
        depth = -1;
        baseOut.recycle();
        session      = null;

        attributes.clear();
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                return attributes.get(name);

            case REQUEST_SCOPE:
                return request.getAttribute(name);

            case SESSION_SCOPE:
                if (session == null)
                    throw new IllegalArgumentException("can't access SESSION_SCOPE without an HttpSession");
                else
                    return session.getAttribute(name);

            case APPLICATION_SCOPE:
                return context.getAttribute(name);

            default:
                throw new IllegalArgumentException("unidentified scope");
        }
    }


    public void setAttribute(String name, Object attribute) {
        setAttribute(name, attribute, PAGE_SCOPE);
    }

    public void setAttribute(String name, Object o, int scope) {
        if (name == null) {
            throw new NullPointerException("name may not be null");
        }

        if (o == null) {
            throw new NullPointerException("object may not be null");
        }

        switch (scope) {
            case PAGE_SCOPE:
                attributes.put(name, o);
                break;

            case REQUEST_SCOPE:
                request.setAttribute(name, o);
                break;

            case SESSION_SCOPE:
                if (session == null)
                    throw new IllegalArgumentException
                        ("can't access SESSION_SCOPE without an HttpSession");
                else
                    session.setAttribute(name, o);
                break;

            case APPLICATION_SCOPE:
                context.setAttribute(name, o);
                break;

            default:
        }
    }

    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                attributes.remove(name);
            break;

            case REQUEST_SCOPE:
                request.removeAttribute(name);
            break;

            case SESSION_SCOPE:
                if (session == null)
                    throw new IllegalArgumentException("can't access SESSION_SCOPE without an HttpSession");
                else
                    session.removeAttribute(name);
                // was:
                //                    session.removeValue(name);
                // REVISIT Verify this is correct - akv
            break;

            case APPLICATION_SCOPE:
                context.removeAttribute(name);
            break;

            default:
        }
    }

    public int getAttributesScope(String name) {
        if (attributes.get(name) != null) return PAGE_SCOPE;

        if (request.getAttribute(name) != null)
            return REQUEST_SCOPE;

        if (session != null) {
            if (session.getAttribute(name) != null)
                return SESSION_SCOPE;
        }

        if (context.getAttribute(name) != null) return APPLICATION_SCOPE;

        return 0;
    }

    public Object findAttribute(String name) {
        Object o = attributes.get(name);
        if (o != null)
            return o;

        o = request.getAttribute(name);
        if (o != null)
            return o;

        if (session != null) {
            o = session.getAttribute(name);
            if (o != null)
                return o;
        }

        return context.getAttribute(name);
    }


    public Enumeration getAttributeNamesInScope(int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                return attributes.keys();

            case REQUEST_SCOPE:
                return request.getAttributeNames();

            case SESSION_SCOPE:
                if (session != null) {
                    return session.getAttributeNames();
                } else
                    throw new IllegalArgumentException("can't access SESSION_SCOPE without an HttpSession");

            case APPLICATION_SCOPE:
                return context.getAttributeNames();

            default: return new Enumeration() { // empty enumeration
                public boolean hasMoreElements() { return false; }

                public Object nextElement() { throw new NoSuchElementException(); }
            };
        }
    }

    public void removeAttribute(String name) {
        try {
            removeAttribute(name, PAGE_SCOPE);
            removeAttribute(name, REQUEST_SCOPE);
            if(session != null ) {
                removeAttribute(name, SESSION_SCOPE);
            }
            removeAttribute(name, APPLICATION_SCOPE);
        } catch (Exception ex) {
            // we remove as much as we can, and
            // simply ignore possible exceptions
        }
    }

    public JspWriter getOut() {
        return out;
    }

    public HttpSession getSession() { return session; }
    public Servlet getServlet() { return servlet; }
    public ServletConfig getServletConfig() { return config; }
    public ServletContext getServletContext() {
        return config.getServletContext();
    }
    public ServletRequest getRequest() { return request; }
    public ServletResponse getResponse() { return response; }
    public Exception getException() { return (Exception)request.getAttribute(EXCEPTION); }
    public Object getPage() { return servlet; }


    private final String getAbsolutePathRelativeToContext(String relativeUrlPath) {
        String path = relativeUrlPath;

        if (!path.startsWith("/")) {
            String uri = (String) request.getAttribute("javax.servlet.include.servlet_path");
            if (uri == null)
                uri = ((HttpServletRequest) request).getServletPath();
            String baseURI = uri.substring(0, uri.lastIndexOf('/'));
            path = baseURI+'/'+path;
        }

        return path;
    }

    public void include(String relativeUrlPath)
        throws ServletException, IOException
    {
        JspRuntimeLibrary.include((HttpServletRequest) request,
                                  (HttpServletResponse) response,
                                  relativeUrlPath, out, true);
        /*
        String path = getAbsolutePathRelativeToContext(relativeUrlPath);
        context.getRequestDispatcher(path).include(
            request, new ServletResponseWrapperInclude(response, out));
        */
    }

    public void forward(String relativeUrlPath)
        throws ServletException, IOException
    {
        // JSP.4.5 If the buffer was flushed, throw IllegalStateException
        try {
            out.clear();
        } catch (IOException ex) {
            throw new IllegalStateException(Constants.getString(
                        "jsp.error.attempt_to_clear_flushed_buffer"));
        }

        // Make sure that the response object is not the wrapper for include
        while (response instanceof ServletResponseWrapperInclude) {
            response = ((ServletResponseWrapperInclude)response).getResponse();
        }

        String path = getAbsolutePathRelativeToContext(relativeUrlPath);
        String includeUri 
            = (String) request.getAttribute(Constants.INC_SERVLET_PATH);
        if (includeUri != null)
            request.removeAttribute(Constants.INC_SERVLET_PATH);
        try {
            context.getRequestDispatcher(path).forward(request, response);
        } finally {
            if (includeUri != null)
                request.setAttribute(Constants.INC_SERVLET_PATH, includeUri);
            request.setAttribute(Constants.FORWARD_SEEN, "true");
        }
    }

    protected BodyContent[] outs = new BodyContentImpl[0];
    protected int depth = -1;

    public BodyContent pushBody() {
        depth++;
        if (depth >= outs.length) {
            BodyContent[] newOuts = new BodyContentImpl[depth + 1];
            for (int i = 0; i < outs.length; i++) {
                newOuts[i] = outs[i];
            }
            newOuts[depth] = new BodyContentImpl(out);
            outs = newOuts;
        }

        outs[depth].clearBody();
        out = outs[depth];

        return outs[depth];
    }

    public JspWriter popBody() {
        depth--;
        if (depth >= 0) {
            out = outs[depth];
        } else {
            out = baseOut;
        }
        return out;
    }

    public void handlePageException(Exception ex)
        throws IOException, ServletException 
    {
        // Should never be called since handleException() called with a
        // Throwable in the generated servlet.
        handlePageException((Throwable) ex);
    }

    public void handlePageException(Throwable t)
        throws IOException, ServletException 
    {
        if (t == null) throw new NullPointerException("null Throwable");
        // set the request attribute with the Throwable.

        request.setAttribute("javax.servlet.jsp.jspException", t);

        if (errorPageURL != null && !errorPageURL.equals("")) {

            // Set request attributes.
            // Do not set the javax.servlet.error.exception attribute here
            // (instead, set in the generated servlet code for the error page)
            // in order to prevent the ErrorReportValve, which is invoked as
            // part of forwarding the request to the error page, from
            // throwing it if the response has not been committed (the response
            // will have been committed if the error page is a JSP page).
            request.setAttribute("javax.servlet.error.status_code",
                new Integer(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            request.setAttribute("javax.servlet.error.request_uri",
                ((HttpServletRequest) request).getRequestURI());
            request.setAttribute("javax.servlet.error.servlet_name",
                                 config.getServletName());

            try {
                forward(errorPageURL);
            } catch (IllegalStateException ise) {
                include(errorPageURL);
            }

            // The error page could be inside an include.

            Object newException=request.getAttribute("javax.servlet.error.exception");

            // t==null means the attribute was not set.
            if ( (newException!= null) && (newException==t) ) {
                request.removeAttribute("javax.servlet.error.exception");
            }

            // now clear the error code - to prevent double handling.
            request.removeAttribute("javax.servlet.error.status_code");
            request.removeAttribute("javax.servlet.error.request_uri");
            request.removeAttribute("javax.servlet.error.status_code");
            request.removeAttribute("javax.servlet.jsp.jspException");

            } else {
            // Otherwise throw the exception wrapped inside a ServletException.
                // Set the exception as the root cause in the ServletException
                // to get a stack trace for the real problem
                if (t instanceof IOException) throw (IOException)t;
                if (t instanceof ServletException) throw (ServletException)t;
            if (t instanceof RuntimeException) throw (RuntimeException)t;
            if (t instanceof JspException) {
                Throwable rootCause = ((JspException)t).getRootCause();
                if (rootCause != null) {
                    throw new ServletException(t.getClass().getName() + ": " +
                                               t.getMessage(), rootCause);
                } else {
                    throw new ServletException(t);
                }
            }
                throw new ServletException(t);
            }
    }

    protected JspWriterImpl _createOut(int bufferSize, boolean autoFlush)
        throws IOException, IllegalArgumentException {
        try {
            return new JspWriterImpl(response, bufferSize, autoFlush);
        } catch( Throwable t ) {
            loghelper.log("creating out", t);
            return null;
        }
    }

    /*
     * fields
     */

    // per Servlet state

    protected                 Servlet         servlet;
    protected                 ServletConfig   config;
    protected                 ServletContext  context;

    protected                 JspFactory        factory;

    protected                boolean                needsSession;

    protected                String                errorPageURL;

    protected                boolean                autoFlush;
    protected                int                bufferSize;

    // page scope attributes

    protected transient Hashtable        attributes = new Hashtable(16);

    // per request state

    protected transient ServletRequest        request;
    protected transient ServletResponse response;
    protected transient Object          page;

    protected transient HttpSession        session;

    protected boolean isIncluded;

    // initial output stream

    protected transient JspWriter       out;
    protected transient JspWriterImpl   baseOut;

}
