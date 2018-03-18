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

package org.apache.jasper;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.jasper.logging.Logger;

import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.xmlparser.ParserUtils;

import java.util.*;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 * @author Pierre Delisle
 */
public final class EmbededServletOptions implements Options {
    private Properties settings=new Properties();
    
    /**
     * Is Jasper being used in development mode?
     */
    public boolean development = true;

    /**
     * Should Ant fork its java compiles of JSP pages.
     */
    public boolean fork = true;

    /**
     * Do you want to keep the generated Java files around?
     */
    public boolean keepGenerated = true;

    /**
     * Determines whether tag handler pooling is enabled.
     */
    public boolean poolingEnabled = true;

    /**
     * Indicates if "mapped" files should be supported. This will generate
     * servlet that has a print statement per line of the JSP file.
     * This seems like a really nice feature to have for debugging.
     */
    public boolean mappedFile = false;
    
    /**
     * Indicates if you want stack traces and such displayed in the client's
     * browser. If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    public boolean sendErrorToClient = false;

    /**
     * Indicates if debugging information should be included in the class file.
     */
    public boolean classDebugInfo = true;

    /**
     * Background compile thread check interval in seconds.
     */
    public int checkInterval = 300;

    /**
     * Indicates status of  JSP reloading.
     */
    public boolean reloading = true;

    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    public File scratchDir;
    
    /**
     * Need to have this as is for versions 4 and 5 of IE. Can be set from
     * the initParams so if it changes in the future all that is needed is
     * to have a jsp initParam of type ieClassId="<value>"
     */
    public String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";

    /**
     * Classpath to use while compiling generated servlets.
     */
    public String classpath = null;
    
    /**
     * Compiler to use.
     */
    public String compiler = null;

    /**
     * Cache for the TLD locations.
     */
    private TldLocationsCache tldLocationsCache = null;

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    private String javaEncoding = "UTF8";

    public String getProperty(String name ) {
        return settings.getProperty( name );
    }

    public void setProperty(String name, String value ) {
        settings.setProperty( name, value );
    }
    
    /**
     * Getter method to see if generated code is kept.
     */
    public boolean getKeepGenerated() {
        return keepGenerated;
    }
    
    public boolean isPoolingEnabled() {
        return poolingEnabled;
    }
    
    /**
     * Getter method to determine HTML mapped servlets support.
     */
    public boolean getMappedFile() {
        return mappedFile;
    }
    
    /**
     * Getter method to determine if errors should be sent to client or thrown
     * into stderr.
     */
    public boolean getSendErrorToClient() {
        return sendErrorToClient;
    }
 
    /**
     * Getter method to determine if class files should be compiled with debug
     * information.
     */
    public boolean getClassDebugInfo() {
        return classDebugInfo;
    }

    /**
     * Background JSP compile thread check interval.
     */
    public int getCheckInterval() {
        return checkInterval;
    }

    /**
     * Getter method to determine if Jasper being used in development mode.
     */
    public boolean getDevelopment() {
        return development;
    }

    /**
     * Getter method for JSP reloading check.
     */
    public boolean getReloading() {
        return reloading;
    }

    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId() {
        return ieClassId;
    }
    
    /**
     * Getter method for scratch dir.
     */
    public File getScratchDir() {
        return scratchDir;
    }

    /**
     * Getter method for classpath used when compiling the servlets
     * generated from JSP files.
     */
    public String getClassPath() {
        return classpath;
    }

    /**
     * Compiler to use.
     */
    public String getCompiler() {
        return compiler;
    }


    public TldLocationsCache getTldLocationsCache() {
        return tldLocationsCache;
    }

    public void setTldLocationsCache( TldLocationsCache tldC ) {
        tldLocationsCache=tldC;
    }

    public String getJavaEncoding() {
        return javaEncoding;
    }

    public boolean getFork() {
        return fork;
    }

    /**
     * Create an EmbededServletOptions object using data available from
     * ServletConfig and ServletContext. 
     */
    public EmbededServletOptions(ServletConfig config,
                                 ServletContext context) {

        Enumeration enumeration=config.getInitParameterNames();
        while( enumeration.hasMoreElements() ) {
            String k=(String)enumeration.nextElement();
            String v=config.getInitParameter( k );

            setProperty( k, v);
        }

        // quick hack
        String validating=config.getInitParameter( "validating");
        if( "false".equals( validating )) ParserUtils.validating=false;
        
        String keepgen = config.getInitParameter("keepgenerated");
        if (keepgen != null) {
            if (keepgen.equalsIgnoreCase("true"))
                this.keepGenerated = true;
            else if (keepgen.equalsIgnoreCase("false"))
                this.keepGenerated = false;
            else Constants.message ("jsp.warning.keepgen", Logger.WARNING);
        }
            
        String mapFile = config.getInitParameter("mappedfile"); 
        if (mapFile != null) {
            if (mapFile.equalsIgnoreCase("true"))
                this.mappedFile = true;
            else if (mapFile.equalsIgnoreCase("false"))
                this.mappedFile = false;
            else Constants.message ("jsp.warning.mappedFile", Logger.WARNING);
        }

        poolingEnabled = true;
        String poolingEnabledParam
            = config.getInitParameter("enablePooling"); 
        if (poolingEnabledParam != null
                  && !poolingEnabledParam.equalsIgnoreCase("true")) {
            if (poolingEnabledParam.equalsIgnoreCase("false"))
                this.poolingEnabled = false;
            else Constants.message("jsp.warning.enablePooling",
                                   Logger.WARNING);
        }
        
        String senderr = config.getInitParameter("sendErrToClient");
        if (senderr != null) {
            if (senderr.equalsIgnoreCase("true"))
                this.sendErrorToClient = true;
            else if (senderr.equalsIgnoreCase("false"))
                this.sendErrorToClient = false;
            else Constants.message ("jsp.warning.sendErrToClient", Logger.WARNING);
        }

        String debugInfo = config.getInitParameter("classdebuginfo");
        if (debugInfo != null) {
            if (debugInfo.equalsIgnoreCase("true"))
                this.classDebugInfo  = true;
            else if (debugInfo.equalsIgnoreCase("false"))
                this.classDebugInfo  = false;
            else Constants.message ("jsp.warning.classDebugInfo", Logger.WARNING);
        }

        String checkInterval = config.getInitParameter("checkInterval");
        if (checkInterval != null) {
            try {
                this.checkInterval = new Integer(checkInterval).intValue();
                if (this.checkInterval == 0) {
                    this.checkInterval = 300;
                    Constants.message("jsp.warning.checkInterval",
                                      Logger.WARNING);
                }
            } catch(NumberFormatException ex) {
                Constants.message ("jsp.warning.checkInterval", Logger.WARNING);
            }
        }

        String development = config.getInitParameter("development");
        if (development != null) {
            if (development.equalsIgnoreCase("true"))
                this.development = true;
            else if (development.equalsIgnoreCase("false"))
                this.development = false;
            else Constants.message ("jsp.warning.development", Logger.WARNING);
        }

        String reloading = config.getInitParameter("reloading");
        if (reloading != null) {
            if (reloading.equalsIgnoreCase("true"))
                this.reloading = true;
            else if (reloading.equalsIgnoreCase("false"))
                this.reloading = false;
            else Constants.message ("jsp.warning.reloading", Logger.WARNING);
        }

        String ieClassId = config.getInitParameter("ieClassId");
        if (ieClassId != null)
            this.ieClassId = ieClassId;

        String classpath = config.getInitParameter("classpath");
        if (classpath != null)
            this.classpath = classpath;

        String dir = config.getInitParameter("scratchdir"); 

        if (dir != null)
            scratchDir = new File(dir);
        else {
            // First we try the Servlet 2.2 javax.servlet.context.tempdir property
            scratchDir = (File) context.getAttribute(Constants.TMP_DIR);
            if (scratchDir == null) {
                // Not running in a Servlet 2.2 container.
                // Try to get the JDK 1.2 java.io.tmpdir property
                dir = System.getProperty("java.io.tmpdir");
                if (dir != null)
                    scratchDir = new File(dir);
            }
        }
                
        if (this.scratchDir == null) {
            Constants.message("jsp.error.no.scratch.dir", Logger.FATAL);
            return;
        }
            
        if (!(scratchDir.exists() && scratchDir.canRead() &&
              scratchDir.canWrite() && scratchDir.isDirectory()))
            Constants.message("jsp.error.bad.scratch.dir",
                              new Object[] {
                                  scratchDir.getAbsolutePath()
                              }, Logger.FATAL);
                                  
        this.compiler = config.getInitParameter("compiler");

        String javaEncoding = config.getInitParameter("javaEncoding");
        if (javaEncoding != null) {
            this.javaEncoding = javaEncoding;
        }

        String fork = config.getInitParameter("fork");
        if (fork != null) {
            if (fork.equalsIgnoreCase("true"))
                this.fork = true;
            else if (fork.equalsIgnoreCase("false"))
                this.fork = false;
            else Constants.message ("jsp.warning.fork", Logger.WARNING);
        }

        // Setup the global Tag Libraries location cache for this
        // web-application.
        tldLocationsCache = new TldLocationsCache(context);

    }

}

