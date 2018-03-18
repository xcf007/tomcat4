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

package org.apache.jasper.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.JspRuntimeContext;

import org.apache.jasper.logging.Logger;

/**
 * The JSP engine (a.k.a Jasper).
 *
 * The servlet container is responsible for providing a
 * URLClassLoader for the web application context Jasper
 * is being used in. Jasper will try get the Tomcat
 * ServletContext attribute for its ServletContext class
 * loader, if that fails, it uses the parent class loader.
 * In either case, it must be a URLClassLoader.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Remy Maucherat
 * @author Kin-man Chung
 * @author Glenn Nielsen
 */

public class JspServletWrapper {

    private Servlet theServlet;
    private String jspUri;
    private Class servletClass;
    private JspCompilationContext ctxt;
    private long available = 0L;
    private ServletConfig config;
    private Options options;
    private boolean firstTime = true;
    private boolean reload = true;

    JspServletWrapper(ServletConfig config, Options options, String jspUri,
                      boolean isErrorPage, JspRuntimeContext rctxt)
            throws JasperException {

        this.config = config;
        this.options = options;
        this.jspUri = jspUri;
        ctxt = new JspCompilationContext( jspUri, isErrorPage,
                                          options,
                                          config.getServletContext(),
                                          this, rctxt);
        ctxt.createOutdir();
    }

    public JspCompilationContext getJspEngineContext() {
        return ctxt;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public Servlet getServlet()
        throws ServletException, IOException, FileNotFoundException
    {
        if (reload) {
            synchronized (this) {
                // Synchronizing on jsw enables simultaneous loading
                // of different pages, but not the same page.
                if (reload) {
                    // This is to maintain the original protocol.
                    destroy();
                    
                    try {
                        servletClass = ctxt.load();
                        theServlet = (Servlet) servletClass.newInstance();
                    } catch( IllegalAccessException ex1 ) {
                        throw new JasperException( ex1 );
                    } catch( InstantiationException ex ) {
                        throw new JasperException( ex );
                    }
                    
                    theServlet.init(config);
                    firstTime = false;
                    reload = false;
                }
            }    
        }
        return theServlet;
    }

    public ServletContext getServletContext() {
        return config.getServletContext();
    }

    public void service(HttpServletRequest request, 
                        HttpServletResponse response,
                        boolean precompile)
            throws ServletException, IOException, FileNotFoundException {
        try {

            if (ctxt.isRemoved()) {
                throw new FileNotFoundException(jspUri);
            }

            if ((available > 0L) && (available < Long.MAX_VALUE)) {
                response.setDateHeader("Retry-After", available);
                response.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     Constants.getString("jsp.error.unavailable"));
            }

            if (options.getDevelopment() || firstTime ) {
                synchronized (this) {
                    ctxt.compile();
                }
            }

            if (reload) {
                getServlet();
            }

            // If a page is to only to be precompiled return.
            if (precompile) {
                return;
            }

            if (theServlet instanceof SingleThreadModel) {
               // sync on the wrapper so that the freshness
               // of the page is determined right before servicing
               synchronized (this) {
                   theServlet.service(request, response);
                }
            } else {
                theServlet.service(request, response);
            }

        } catch (UnavailableException ex) {
            String includeRequestUri = (String)
                request.getAttribute("javax.servlet.include.request_uri");
            if (includeRequestUri != null) {
                // This file was included. Throw an exception as
                // a response.sendError() will be ignored by the
                // servlet engine.
                throw ex;
            } else {
                int unavailableSeconds = ex.getUnavailableSeconds();
                if (unavailableSeconds <= 0) {
                    unavailableSeconds = 60;        // Arbitrary default
                }
                available = System.currentTimeMillis() +
                    (unavailableSeconds * 1000L);
                response.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                     ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            ctxt.incrementRemoved();
            String includeRequestUri = (String)
                request.getAttribute("javax.servlet.include.request_uri");
            if (includeRequestUri != null) {
                // This file was included. Throw an exception as
                // a response.sendError() will be ignored by the
                // servlet engine.
                throw new ServletException(ex);
            } else {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                                      ex.getMessage());
                } catch (IllegalStateException ise) {
                    Constants.jasperLog.log(
                        Constants.getString("jsp.error.file.not.found",
                                            new Object[] { ex.getMessage() }),
                        ex, Logger.ERROR);
                }
            }
        } catch (ServletException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JasperException(ex);
        }
    }

    public void destroy() {
        if (theServlet != null) {
            theServlet.destroy();
        }
    }

}
