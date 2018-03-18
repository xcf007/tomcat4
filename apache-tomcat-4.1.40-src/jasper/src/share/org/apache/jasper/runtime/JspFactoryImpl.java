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

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.PageContext;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.jasper.util.SimplePool;
import org.apache.jasper.logging.Logger;

/**
 * Implementation of JspFactory from the spec. Helps create
 * PageContext and other animals.  
 *
 * @author Anil K. Vijendran
 */
public class JspFactoryImpl extends JspFactory {

    protected class PrivilegedGetPageContext implements PrivilegedAction {
        private JspFactoryImpl factory;
        private Servlet servlet;
        private ServletRequest request;
        private ServletResponse response;
        private String errorPageURL;
        private boolean needsSession;
        private int bufferSize;
        private boolean autoflush;

        PrivilegedGetPageContext(JspFactoryImpl factory,
            Servlet servlet,
            ServletRequest request,
            ServletResponse response,
            String errorPageURL,
            boolean needsSession, int bufferSize,
            boolean autoflush)
        {
            this.factory = factory;
            this.servlet = servlet;
            this.request = request;
            this.response = response;
            this.errorPageURL = errorPageURL;
            this.needsSession = needsSession;
            this.bufferSize = bufferSize;
            this.autoflush = autoflush;
        }
 
        public Object run() {
            return factory.internalGetPageContext(servlet,request,
                response,errorPageURL,
                needsSession,bufferSize,autoflush);
        }
    }

    protected class PrivilegedReleasePageContext implements PrivilegedAction {
        private JspFactoryImpl factory;
        private PageContext pageContext;

        PrivilegedReleasePageContext(JspFactoryImpl factory,
            PageContext pageContext)
        {
            this.factory = factory;
            this.pageContext = pageContext;
        }

        public Object run() {
            factory.internalReleasePageContext(pageContext);
            return null;
        }
    }

    private SimplePool pool=new SimplePool( 100 );
    private static final boolean usePool=true;

    Logger.Helper loghelper = new Logger.Helper("JASPER_LOG", "JspFactoryImpl");
    
    public PageContext getPageContext(Servlet servlet, ServletRequest request,
                                      ServletResponse response,
                                      String errorPageURL,                    
                                      boolean needsSession, int bufferSize,
                                      boolean autoflush)
    {
        if( System.getSecurityManager() != null ) {
            PrivilegedGetPageContext dp = new PrivilegedGetPageContext(
                (JspFactoryImpl)this,servlet,request,response,errorPageURL,
                needsSession,bufferSize,autoflush);
            return (PageContext)AccessController.doPrivileged(dp);
        }
        return internalGetPageContext(servlet,request,response,errorPageURL,
                                      needsSession,bufferSize,autoflush);

    }

    protected PageContext internalGetPageContext(Servlet servlet, ServletRequest request,
                                      ServletResponse response, 
                                      String errorPageURL, 
                                      boolean needsSession, int bufferSize, 
                                      boolean autoflush) 
    {
        try {
            PageContext pc;
            if( usePool ) {
                pc=(PageContextImpl)pool.get();
                if( pc == null ) {
                    pc= new PageContextImpl(this);
                }
            } else {
                pc =  new PageContextImpl(this);
            }
            pc.initialize(servlet, request, response, errorPageURL, 
                          needsSession, bufferSize, autoflush);
            return pc;
        } catch (Throwable ex) {
            /* FIXME: need to do something reasonable here!! */
            loghelper.log("Exception initializing page context", ex);
            return null;
        }
    }

    public void releasePageContext(PageContext pc) {
        if( pc == null )
            return;
        if( System.getSecurityManager() != null ) {
            PrivilegedReleasePageContext dp = new PrivilegedReleasePageContext(
                (JspFactoryImpl)this,pc);
            AccessController.doPrivileged(dp);
        } else {
            internalReleasePageContext(pc);
        }
    }

    private void internalReleasePageContext(PageContext pc) {
        pc.release();
        if( usePool) {
            pool.put( pc );
        }
    }

    static class SunJspEngineInfo extends JspEngineInfo {

        final static String SpecificationVersion = "1.2";

        public String getSpecificationVersion() {
            return SpecificationVersion;
        }
    }
    
    static JspEngineInfo info = new SunJspEngineInfo();

    public JspEngineInfo getEngineInfo() {
        return info;
    }
}
