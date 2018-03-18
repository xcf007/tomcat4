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

package org.apache.catalina.startup;

/**
 * Catalina MBean interface.
 * To be used, the JAR containing this MBean should contain all the classes
 * which are present in bootstrap.jar. The setPath(String path) method should
 * be used to set the correct path where the Tomcat distribution is.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 466595 $
 */

public interface CatalinaManagerMBean {


    // -------------------------------------------------------------- Constants


    /**
     * Status constants.
     */
    public static final String[] states =
    {"Stopped", "Stopping", "Starting", "Started"};


    public static final int STOPPED  = 0;
    public static final int STOPPING = 1;
    public static final int STARTING = 2;
    public static final int STARTED  = 3;


    /**
     * Component name.
     */
    public static final String NAME = "Catalina servlet container";


    /**
     * Object name.
     */
    public static final String OBJECT_NAME = ":service=Catalina";


    // ------------------------------------------------------ Interface Methods


    /**
     * Retruns the Catalina component name.
     */
    public String getName();


    /**
     * Returns the state.
     */
    public int getState();


    /**
     * Returns a String representation of the state.
     */
    public String getStateString();


    /**
     * Path accessor.
     */
    public String getPath();


    /**
     * Path mutator.
     */
    public void setPath(String Path);


    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception;


    /**
     * Stop the servlet container.
     */
    public void stop();


    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy();


}
