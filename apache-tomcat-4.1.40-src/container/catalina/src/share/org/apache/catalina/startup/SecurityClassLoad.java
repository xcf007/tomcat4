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
 * Static class used to preload java classes when using the
 * Java SecurityManager so that the defineClassInPackage
 * RuntimePermission does not trigger an AccessControlException.
 *
 * @author Glenn L. Nielsen
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public final class SecurityClassLoad {

    static void securityClassLoad(ClassLoader loader)
        throws Exception {

        if( System.getSecurityManager() == null )
            return;

        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetNamedDispatcher");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetInitParameter");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetInitParameterNames");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetRequestDispatcher");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetResource");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetResourcePaths");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogMessage");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogException");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogThrowable");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedForward");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedInclude");
        loader.loadClass
            (basePackage +
             "core.ContainerBase$PrivilegedAddChild");
        loader.loadClass
            (basePackage +
             "connector.HttpRequestBase$PrivilegedGetSession");
        loader.loadClass
            (basePackage +
             "connector.HttpResponseBase$PrivilegedFlushBuffer");
        loader.loadClass
            (basePackage +
             "loader.WebappClassLoader$PrivilegedFindResource");
        loader.loadClass
            (basePackage + "session.StandardSession");
        loader.loadClass
            (basePackage + "util.CookieTools");
        loader.loadClass
            (basePackage + "util.URL");
        loader.loadClass(basePackage + "util.Enumerator");
        loader.loadClass("javax.servlet.http.Cookie");
        try {
            loader.loadClass("org.apache.coyote.tomcat4.CoyoteRequest$PrivilegedGetSession");
        } catch(Throwable t) {
        }
        try {
            loader.loadClass("org.apache.coyote.http11.Constants");
        } catch(Throwable t) {
        }

    }
}

