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

import javax.servlet.ServletContext;

/**
 * Simple class to factor out servlet runner dependencies from the JSP
 * engine. There's a few motivations here: 
 *
 *        (a) ability for the JSP engine to be able to run on multiple
 *          servlet engines - 2.1 and 2.2
 *        (b) ability for the JSP engine to take advantage of specific
 *          servlet engines; this is crucial from a J2EE point of
 *          view. 
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class ServletEngine {
    static ServletEngine tomcat;
    static ServletEngine deflt;
    
    /**
     * Get a specific ServletEngine instance for the particular servlet runner
     * we are running on.
     */
    static ServletEngine getServletEngine(String serverInfo) {
        if (serverInfo.startsWith("Tomcat Web Server")) {
            if (tomcat == null) {
                try {
                    tomcat = (ServletEngine)
                        Class.forName("org.apache.jasper.runtime.TomcatServletEngine").newInstance();
                } catch (Exception ex) {
                    return null;
                }
            }
            return tomcat;
        } else {
            if (deflt == null) 
                deflt = new ServletEngine();
            return deflt;
        }
    }
    
    /**
     * Get the class loader for this ServletContext object. 
     */
    public ClassLoader getClassLoader(ServletContext ctx) {
        return null;
    }
}






