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

import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.apache.jasper.Constants;

/**
 * This is the subclass of all JSP-generated servlets.
 *
 * @author Anil K. Vijendran
 */
public abstract class HttpJspBase 
    extends HttpServlet 
    implements HttpJspPage 
{
    protected PageContext pageContext;
    static {
        if( JspFactory.getDefaultFactory() == null ) {
            JspFactoryImpl factory = new JspFactoryImpl();
            if( System.getSecurityManager() != null ) {
                String basePackage = "org.apache.jasper.";
                try {
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "runtime.JspFactoryImpl$PrivilegedGetPageContext");
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "runtime.JspFactoryImpl$PrivilegedReleasePageContext");
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "runtime.JspRuntimeLibrary");
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "runtime.JspRuntimeLibrary$PrivilegedIntrospectHelper");
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "runtime.ServletResponseWrapperInclude");
                    factory.getClass().getClassLoader().loadClass( basePackage +
                                                                   "servlet.JspServletWrapper");
                } catch (ClassNotFoundException ex) {
                    System.out.println(
                                       "Jasper JspRuntimeContext preload of class failed: " +
                                       ex.getMessage());
                }
            }
            JspFactory.setDefaultFactory(factory);
        }
    }

    protected HttpJspBase() {
    }

    public final void init(ServletConfig config) 
        throws ServletException 
    {
        super.init(config);
        jspInit();
    }
    
    public String getServletInfo() {
        return Constants.getString ("jsp.engine.info");
    }

    public final void destroy() {
        jspDestroy();
        _jspDestroy();
    }

    /**
     * Entry point into service.
     */
    public final void service(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException 
    {
        _jspService(request, response);
    }
    
    public void jspInit() {
    }
    
    public void jspDestroy() {
    }

    protected void _jspDestroy() {
    }

    /**
     * Get the list of compile time included files used
     * by the JSP file.
     *
     * Overridden by generated JSP java source files.
     *
     * @return List compile time includes
     */
    public abstract List getIncludes();

    public abstract void _jspService(HttpServletRequest request, 
                                     HttpServletResponse response) 
        throws ServletException, IOException;
}
