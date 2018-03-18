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


import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 *
 * @author Remy Maucherat
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class ApplicationContextFacade
    implements ServletContext {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
        super();
        this.context = context;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped application context.
     */
    private ApplicationContext context = null;


    // ------------------------------------------------- ServletContext Methods


    public ServletContext getContext(String uripath) {
        ServletContext theContext = context.getContext(uripath);
        if ((theContext != null) &&
            (theContext instanceof ApplicationContext))
            theContext = ((ApplicationContext) theContext).getFacade();
        return (theContext);
    }


    public int getMajorVersion() {
        return context.getMajorVersion();
    }


    public int getMinorVersion() {
        return context.getMinorVersion();
    }


    public String getMimeType(String file) {
        return context.getMimeType(file);
    }


    public Set getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }


    public URL getResource(String path)
        throws MalformedURLException {
        return context.getResource(path);
    }


    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }


    public RequestDispatcher getRequestDispatcher(String path) {
        return context.getRequestDispatcher(path);
    }


    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }


    public Servlet getServlet(String name)
        throws ServletException {
        return context.getServlet(name);
    }


    public Enumeration getServlets() {
        return context.getServlets();
    }


    public Enumeration getServletNames() {
        return context.getServletNames();
    }


    public void log(String msg) {
        context.log(msg);
    }


    public void log(Exception exception, String msg) {
        context.log(exception, msg);
    }


    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }


    public String getRealPath(String path) {
        return context.getRealPath(path);
    }


    public String getServerInfo() {
        return context.getServerInfo();
    }


    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }


    public Enumeration getInitParameterNames() {
        return context.getInitParameterNames();
    }


    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }


    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }


    public void setAttribute(String name, Object object) {
        context.setAttribute(name, object);
    }


    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }


    public String getServletContextName() {
        return context.getServletContextName();
    }


}
