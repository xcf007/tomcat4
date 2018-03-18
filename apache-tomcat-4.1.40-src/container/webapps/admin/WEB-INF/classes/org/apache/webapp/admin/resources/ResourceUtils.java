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


package org.apache.webapp.admin.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ObjectInstance;

/**
 * <p>Shared utility methods for the resource administration module.</p>
 *
 * @author Manveen Kaur
 * @author Amy Roh
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 * @since 4.1
 */

public class ResourceUtils {

    public final static String ENVIRONMENT_TYPE = "Catalina:type=Environment";
    public final static String RESOURCE_TYPE = "Catalina:type=Resource";
    public final static String RESOURCELINK_TYPE = "Catalina:type=ResourceLink";
    public final static String NAMINGRESOURCES_TYPE = "Catalina:type=NamingResources";
    public final static String GLOBAL_TYPE = ",resourcetype=Global";
    public final static String CONTEXT_TYPE = ",resourcetype=Context";
    public final static String SERVICE_DEFAULTCONTEXT_TYPE = 
                                                ",resourcetype=ServiceDefaultContext";
    public final static String HOST_DEFAULTCONTEXT_TYPE = 
                                                ",resourcetype=HostDefaultContext";    
    
    // resource class names
    public final static String USERDB_CLASS = "org.apache.catalina.UserDatabase";
    public final static String DATASOURCE_CLASS = "javax.sql.DataSource";
    public final static String MAILSESSION_CLASS = "javax.mail.Session";

    // --------------------------------------------------------- Public Methods

    /**
     * Construct and return a ResourcesForm identifying all currently defined
     * resources in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static EnvEntriesForm getEnvEntriesForm(MBeanServer mserver, 
        String resourcetype, String path, String host, String service) 
        throws Exception {
                           
        ObjectName ename = null;
        if (resourcetype!=null) {
            if (resourcetype.equals("Global")) {
                ename = new ObjectName( ENVIRONMENT_TYPE + GLOBAL_TYPE + ",*");
            } else if (resourcetype.equals("Context")) {
                ename = new ObjectName(ENVIRONMENT_TYPE + CONTEXT_TYPE + 
                    ",path=" + path + ",host=" + host + ",service=" + 
                    service + ",*");
            } else if (resourcetype.equals("DefaultContext")) {
                if (host.length() > 0) {
                    ename = new ObjectName(ENVIRONMENT_TYPE + 
                        HOST_DEFAULTCONTEXT_TYPE + ",host=" + host + 
                        ",service=" + service + ",*");
                } else {
                    ename = new ObjectName(ENVIRONMENT_TYPE + 
                        SERVICE_DEFAULTCONTEXT_TYPE + ",service=" + service + ",*");
                }
            }
        }
        Iterator iterator = (mserver.queryMBeans(ename, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        
        EnvEntriesForm envEntriesForm = new EnvEntriesForm();
        envEntriesForm.setEnvEntries((String[]) 
                        results.toArray(new String[results.size()]));
        
        if (resourcetype != null) {
            envEntriesForm.setResourcetype(resourcetype);
        } else {
            envEntriesForm.setResourcetype("");
        }
         if (path != null) {
            envEntriesForm.setPath(path);
        } else {
            envEntriesForm.setPath("");
        }        
        if (host != null) {
            envEntriesForm.setHost(host);
        } else {
            envEntriesForm.setHost("");
        }        
        if (service != null) {
            envEntriesForm.setService(service);
        } else {
            envEntriesForm.setService("");
        }     
        
        return (envEntriesForm);

    }

    /**
     * Construct and return a DataSourcesForm identifying all currently defined
     * datasources in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static DataSourcesForm getDataSourcesForm(MBeanServer mserver, 
        String resourcetype, String path, String host, String service) 
        throws Exception {
                            
        ObjectName rname = null;
        if (resourcetype!=null) {
            if (resourcetype.equals("Global")) {
                rname = new ObjectName( RESOURCE_TYPE + GLOBAL_TYPE + 
                                        ",class=" + DATASOURCE_CLASS + ",*");
            } else if (resourcetype.equals("Context")) {
                rname = new ObjectName (RESOURCE_TYPE + CONTEXT_TYPE + 
                    ",path=" + path + ",host=" + host + ",service=" + 
                    service + ",class=" + DATASOURCE_CLASS + ",*");
            } else if (resourcetype.equals("DefaultContext")) {
                if (host.length() > 0) {
                    rname = new ObjectName(RESOURCE_TYPE + 
                        HOST_DEFAULTCONTEXT_TYPE + ",host=" + host + 
                        ",service=" + service + ",class=" + 
                        DATASOURCE_CLASS + ",*");
                } else {
                    rname = new ObjectName(RESOURCE_TYPE + 
                        SERVICE_DEFAULTCONTEXT_TYPE + ",service=" + service + 
                        ",class=" + DATASOURCE_CLASS + ",*");
                }
            }
        }
       
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        DataSourcesForm dataSourcesForm = new DataSourcesForm();
        dataSourcesForm.setDataSources((String[]) 
                        results.toArray(new String[results.size()]));        
        
        if (resourcetype != null) {
            dataSourcesForm.setResourcetype(resourcetype);
        } else {
            dataSourcesForm.setResourcetype("");
        }
         if (path != null) {
            dataSourcesForm.setPath(path);
        } else {
            dataSourcesForm.setPath("");
        }        
        if (host != null) {
            dataSourcesForm.setHost(host);
        } else {
            dataSourcesForm.setHost("");
        }        
        if (service != null) {
            dataSourcesForm.setService(service);
        } else {
            dataSourcesForm.setService("");
        }   
        
        return (dataSourcesForm);

    }
    
    /**
     * Construct and return a MailSessionsForm identifying all currently defined
     * mailsessions in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static MailSessionsForm getMailSessionsForm(MBeanServer mserver, 
        String resourcetype, String path, String host, String service) 
        throws Exception {
                            
        ObjectName rname = null;
        if (resourcetype!=null) {
            if (resourcetype.equals("Global")) {
                rname = new ObjectName( RESOURCE_TYPE + GLOBAL_TYPE + 
                                        ",class=" + MAILSESSION_CLASS + ",*");
            } else if (resourcetype.equals("Context")) {
                rname = new ObjectName (RESOURCE_TYPE + CONTEXT_TYPE + 
                    ",path=" + path + ",host=" + host + ",service=" + 
                    service + ",class=" + MAILSESSION_CLASS + ",*");
            } else if (resourcetype.equals("DefaultContext")) {
                if (host.length() > 0) {
                    rname = new ObjectName(RESOURCE_TYPE + 
                        HOST_DEFAULTCONTEXT_TYPE + ",host=" + host + 
                        ",service=" + service + ",class=" + 
                        MAILSESSION_CLASS + ",*");
                } else {
                    rname = new ObjectName(RESOURCE_TYPE + 
                        SERVICE_DEFAULTCONTEXT_TYPE + ",service=" + service + 
                        ",class=" + MAILSESSION_CLASS + ",*");
                }
            }
        }
       
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        MailSessionsForm mailSessionsForm = new MailSessionsForm();
        mailSessionsForm.setMailSessions((String[]) 
                        results.toArray(new String[results.size()]));        
        
        if (resourcetype != null) {
            mailSessionsForm.setResourcetype(resourcetype);
        } else {
            mailSessionsForm.setResourcetype("");
        }
         if (path != null) {
            mailSessionsForm.setPath(path);
        } else {
            mailSessionsForm.setPath("");
        }        
        if (host != null) {
            mailSessionsForm.setHost(host);
        } else {
            mailSessionsForm.setHost("");
        }        
        if (service != null) {
            mailSessionsForm.setService(service);
        } else {
            mailSessionsForm.setService("");
        }   
        
        return (mailSessionsForm);

    }
    
    /**
     * Construct and return a ResourceLinksForm identifying all currently defined
     * resourcelinks in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static ResourceLinksForm getResourceLinksForm(MBeanServer mserver, 
        String resourcetype, String path, String host, String service) 
        throws Exception {

        ObjectName rname = null;
        if (resourcetype!=null) {
            if (resourcetype.equals("Global")) {
                rname = new ObjectName( RESOURCELINK_TYPE + GLOBAL_TYPE + ",*");
            } else if (resourcetype.equals("Context")) {
                rname = new ObjectName (RESOURCELINK_TYPE + CONTEXT_TYPE + 
                    ",path=" + path + ",host=" + host + ",service=" + 
                    service + ",*");
            } else if (resourcetype.equals("DefaultContext")) {
                if (host.length() > 0) {
                    rname = new ObjectName(RESOURCELINK_TYPE + 
                        HOST_DEFAULTCONTEXT_TYPE + ",host=" + host + 
                        ",service=" + service + ",*");
                } else {
                    rname = new ObjectName(RESOURCELINK_TYPE + 
                        SERVICE_DEFAULTCONTEXT_TYPE + ",service=" + service + ",*");
                }
            }
        }
       
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);        
        ResourceLinksForm resourceLinksForm = new ResourceLinksForm();
        resourceLinksForm.setResourceLinks((String[]) 
                        results.toArray(new String[results.size()]));        
        
        if (resourcetype != null) {
            resourceLinksForm.setResourcetype(resourcetype);
        } else {
            resourceLinksForm.setResourcetype("");
        }
         if (path != null) {
            resourceLinksForm.setPath(path);
        } else {
            resourceLinksForm.setPath("");
        }        
        if (host != null) {
            resourceLinksForm.setHost(host);
        } else {
            resourceLinksForm.setHost("");
        }        
        if (service != null) {
            resourceLinksForm.setService(service);
        } else {
            resourceLinksForm.setService("");
        }   
        
        return (resourceLinksForm);
        
    }
    
    /**
     * Construct and return a UserDatabaseForm identifying all currently defined
     * user databases in the specified resource database.
     *
     * @param mserver MBeanServer to be consulted
     *
     * @exception Exception if an error occurs
     */
    public static UserDatabasesForm getUserDatabasesForm(MBeanServer mserver)
        throws Exception {
            
        ObjectName rname = new ObjectName( RESOURCE_TYPE + GLOBAL_TYPE +
                            ",class=" + USERDB_CLASS + ",*");
        
        Iterator iterator = (mserver.queryMBeans(rname, null).iterator());
        
        ArrayList results = new ArrayList();        
        while (iterator.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iterator.next(); 
            results.add(instance.getObjectName().toString());
        }

        Collections.sort(results);

        UserDatabasesForm userDatabasesForm = new UserDatabasesForm();
        userDatabasesForm.setUserDatabases((String[]) 
                        results.toArray(new String[results.size()]));  
        return (userDatabasesForm);

    }
    
}
