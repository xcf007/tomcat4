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

package org.apache.jasper;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

import org.apache.jasper.logging.Logger;

/**
 * Some constants and other global data that are used by the compiler and the runtime.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class Constants {
    
    public static Logger jasperLog = null;

    static {
        jasperLog = Logger.getDefaultLogger();
        jasperLog.setName("JASPER_LOG");
    }

    /**
     * The base class of the generated servlets. 
     */
    public static final String JSP_SERVLET_BASE = "HttpJspBase";

    /**
     * _jspService is the name of the method that is called by 
     * HttpJspBase.service(). This is where most of the code generated
     * from JSPs go.
     */
    public static final String SERVICE_METHOD_NAME = "_jspService";

    /**
     * Default servlet content type.
     */
    public static final String SERVLET_CONTENT_TYPE = "text/html";

    /**
     * These classes/packages are automatically imported by the
     * generated code. 
     */
    public static final String[] STANDARD_IMPORTS = { 
        "javax.servlet.*", 
        "javax.servlet.http.*", 
        "javax.servlet.jsp.*", 
        "org.apache.jasper.runtime.*", 
    };

    /**
     * FIXME
     * ServletContext attribute for classpath. This is tomcat specific. 
     * Other servlet engines can choose to have this attribute if they 
     * want to have this JSP engine running on them. 
     */
    //public static final String SERVLET_CLASSPATH = "org.apache.tomcat.jsp_classpath";
    public static final String SERVLET_CLASSPATH = "org.apache.catalina.jsp_classpath";

    /**
     * FIXME
     * Request attribute for <code>&lt;jsp-file&gt;</code> element of a
     * servlet definition.  If present on a request, this overrides the
     * value returned by <code>request.getServletPath()</code> to select
     * the JSP page to be executed.
     */
    public static final String JSP_FILE = "org.apache.catalina.jsp_file";


    /**
     * FIXME
     * ServletContext attribute for class loader. This is tomcat specific. 
     * Other servlet engines can choose to have this attribute if they 
     * want to have this JSP engine running on them. 
     */
    //public static final String SERVLET_CLASS_LOADER = "org.apache.tomcat.classloader";
    public static final String SERVLET_CLASS_LOADER = "org.apache.catalina.classloader";

    /**
     * Default size of the JSP buffer.
     */
    public static final int K = 1024;
    public static final int DEFAULT_BUFFER_SIZE = 8*K;

    /**
     * Default size for the tag buffers.
     */
    public static final int DEFAULT_TAG_BUFFER_SIZE = 512;

    /**
     * The query parameter that causes the JSP engine to just
     * pregenerated the servlet but not invoke it. 
     */
    public static final String PRECOMPILE = "jsp_precompile";

    /**
     * The default package name for compiled jsp pages.
     */
    public static final String JSP_PACKAGE_NAME = "org.apache.jsp";

    /**
     * Servlet context and request attributes that the JSP engine
     * uses. 
     */
    public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";
    public static final String TMP_DIR = "javax.servlet.context.tempdir";
    public static final String FORWARD_SEEN = "javax.servlet.forward.seen";

    /**
     * Public Id and the Resource path (of the cached copy) 
     * of the DTDs for tag library descriptors. 
     */
    public static final String TAGLIB_DTD_PUBLIC_ID_11 = 
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String TAGLIB_DTD_RESOURCE_PATH_11 = 
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd";
    public static final String TAGLIB_DTD_PUBLIC_ID_12 = 
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String TAGLIB_DTD_RESOURCE_PATH_12 = 
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd";

    /**
     * Public Id and the Resource path (of the cached copy) 
     * of the DTDs for web application deployment descriptors
     */
    public static final String WEBAPP_DTD_PUBLIC_ID_22 = 
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WEBAPP_DTD_RESOURCE_PATH_22 = 
        "/javax/servlet/resources/web-app_2_2.dtd";
    public static final String WEBAPP_DTD_PUBLIC_ID_23 = 
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WEBAPP_DTD_RESOURCE_PATH_23 = 
        "/javax/servlet/resources/web-app_2_3.dtd";

    /**
     * List of the Public IDs that we cache, and their
     * associated location. This is used by 
     * an EntityResolver to return the location of the
     * cached copy of a DTD.
     */
    public static final String[] CACHED_DTD_PUBLIC_IDS = {
        TAGLIB_DTD_PUBLIC_ID_11,
        TAGLIB_DTD_PUBLIC_ID_12,
        WEBAPP_DTD_PUBLIC_ID_22,
        WEBAPP_DTD_PUBLIC_ID_23,
    };
    public static final String[] CACHED_DTD_RESOURCE_PATHS = {
        TAGLIB_DTD_RESOURCE_PATH_11,
        TAGLIB_DTD_RESOURCE_PATH_12,
        WEBAPP_DTD_RESOURCE_PATH_22,
        WEBAPP_DTD_RESOURCE_PATH_23,
    };
    
    /**
     * Default URLs to download the pluging for Netscape and IE.
     */
    public static final String NS_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/";

    public static final String IE_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";

    /**
     * This is where all our error messages and such are stored. 
     */
    private static ResourceBundle resources;
    
    private static void initResources() {
        try {
            resources =
                ResourceBundle.getBundle("org.apache.jasper.resources.messages");
        } catch (MissingResourceException e) {
            throw new Error("Fatal Error: missing resource bundle: "+e.getClassName());
        }
    }

    /**
     * Get hold of a "message" or any string from our resources
     * database. 
     */
    public static final String getString(String key) {
        return getString(key, null);
    }

    /**
     * Format the string that is looked up using "key" using "args". 
     */
    public static final String getString(String key, Object[] args) {
        if (resources == null) 
            initResources();
        
        try {
            String msg = resources.getString(key);
            if (args == null)
                return msg;
            MessageFormat form = new MessageFormat(msg);
            return form.format(args);
        } catch (MissingResourceException ignore) {
            throw new Error("Fatal Error: missing resource: "+ignore.getClassName());
        }
    }

    /** 
     * Print a message into standard error with a certain verbosity
     * level. 
     * 
     * @param key is used to look up the text for the message (using
     *            getString()). 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static final void message(String key, int verbosityLevel) {
        message(key, null, verbosityLevel);
    }


    /**
     * Print a message into standard error with a certain verbosity
     * level after formatting it using "args". 
     *
     * @param key is used to look up the message. 
     * @param args is used to format the message. 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static final void message(String key, Object[] args, int verbosityLevel) {
        if (jasperLog == null) {
            jasperLog = Logger.getLogger("JASPER_LOG");
            if (jasperLog == null) {
                jasperLog = Logger.getDefaultLogger();
            }
        }

        if (jasperLog != null) {
            jasperLog.log(getString(key, args), verbosityLevel);
        }
    }

}

