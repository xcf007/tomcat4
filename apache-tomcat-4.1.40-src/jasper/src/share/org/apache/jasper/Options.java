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

import java.io.File;

import org.apache.jasper.compiler.TldLocationsCache;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 * @author Pierre Delisle
 */
public interface Options {


    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated();

    /**
     * Returns true if tag handler pooling is enabled, false otherwise.
     */
    public boolean isPoolingEnabled();

    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile();


    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient();
 

    /**
     * Should we include debug information in compiled class?
     */
    public boolean getClassDebugInfo();


    /**
     * Background compile thread check interval in seconds
     */
    public int getCheckInterval();


    /**
     * Is Jasper being used in development mode?
     */
    public boolean getDevelopment();


    /**
     * JSP reloading check ?
     */
    public boolean getReloading();


    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId();


    /**
     * What is my scratch dir?
     */
    public File getScratchDir();


    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath();


    /**
     * Compiler to use.
     */
    public String getCompiler();


    /**
     * The cache for the location of the TLD's
     * for the various tag libraries 'exposed'
     * by the web application.
     * A tag library is 'exposed' either explicitely in 
     * web.xml or implicitely via the uri tag in the TLD 
     * of a taglib deployed in a jar file (WEB-INF/lib).
     *
     * @return the instance of the TldLocationsCache
     * for the web-application.
     */
    public TldLocationsCache getTldLocationsCache();


    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    public String getJavaEncoding();


    /**
     * boolean flag to tell Ant whether to fork JSP page compilations.
     */
    public boolean getFork();

}
