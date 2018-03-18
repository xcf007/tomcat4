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
package org.apache.jasper.compiler;


import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import java.beans.Beans;

import org.apache.jasper.JasperException;

/**
 * Holds instances of {session, application, page}-scoped beans 
 *
 * @author Mandar Raje
 */
public class BeanRepository {

    Vector sessionBeans;
    Vector pageBeans;
    Vector appBeans;
    Vector requestBeans;
    Hashtable beanTypes;
    ClassLoader loader;
    
    public BeanRepository (ClassLoader loader) {
        sessionBeans = new Vector(11);
        pageBeans    = new Vector(11);
        appBeans = new Vector(11);
        requestBeans    = new Vector(11);
        beanTypes    = new Hashtable ();
        this.loader = loader;
    }
    
    public boolean checkSessionBean (String s) {
        return sessionBeans.contains (s);
    }
    
    public void addSessionBean (String s, String type) throws JasperException {
        sessionBeans.addElement (s);
        putBeanType (s, type);
    }
    
    public boolean hasSessionBeans () {
        return !sessionBeans.isEmpty ();
    }
    
    public Enumeration getSessionBeans () {
        return sessionBeans.elements ();
    }
    
    public boolean checkApplicationBean (String s) {
        return appBeans.contains (s);
    }
    
    public void addApplicationBean (String s, String type) throws JasperException 
    {
        appBeans.addElement (s);
        putBeanType (s, type);
    }
    
    public boolean hasApplicationBeans () {
        return !appBeans.isEmpty ();
    }
    
    public Enumeration getApplicationBeans () {
        return appBeans.elements ();
    }
    
    public boolean checkRequestBean (String s) {
        return requestBeans.contains (s);
    }
    
    public void addRequestBean (String s, String type) throws JasperException {
        requestBeans.addElement (s);
        putBeanType (s, type);
    }
    
    public boolean hasRequestBeans () {
        return !requestBeans.isEmpty ();
    }
    
    public Enumeration getRequestBeans () {
        return requestBeans.elements ();
    }
    
    public boolean checkPageBean (String s) {
        return pageBeans.contains (s);
    }
    
    public void addPageBean (String s, String type) throws JasperException {
        pageBeans.addElement (s);
        putBeanType (s, type);
    }
    
    public boolean hasPageBeans () {
        return !pageBeans.isEmpty ();
    }
    
    public Enumeration getPageBeans () {
        return pageBeans.elements ();
    }

    public boolean ClassFound (String clsname)
            throws ClassNotFoundException {
        Class cls = null;
        //try {
            cls = loader.loadClass (clsname);
            //} catch (ClassNotFoundException ex) {
            //return false;
            //}
        return !(cls == null);        
    }
    
    public Class getBeanType (String bean) throws JasperException {
        Class cls = null;
        try {
            cls = loader.loadClass ((String)beanTypes.get(bean));
        } catch (ClassNotFoundException ex) {
            throw new JasperException (ex);
        }
        return cls;
    }
  
    public void putBeanType (String bean, String type) throws JasperException {
        try {
            beanTypes.put (bean, type);
        } catch (Exception ex) {
            throw new JasperException (ex);
        }
    }
    
    //public void putBeanType (String bean, Class type) {
    //beanTypes.put (bean, type);
    //}
    
    public void removeBeanType (String bean) {
        beanTypes.remove (bean);
    }
    
    // Not sure if this is the correct way.
    // After pageContext is finalised this will change.
    public boolean checkVariable (String bean) {
        return (checkPageBean(bean) || checkSessionBean(bean) ||
                checkRequestBean(bean) || checkApplicationBean(bean));
    }

    // Ideally this method should belong to the utils.
    public Class getClass (String clsname)
            throws ClassNotFoundException {
        Class cls = null;
        if (clsname != null) {
            cls = loader.loadClass (clsname);
        }
        return cls;
    }

    public boolean beanFound (String beanName)
            throws ClassNotFoundException {
        try {
            Beans.instantiate(Thread.currentThread().getContextClassLoader(), beanName);
            return true;
        } catch (java.io.IOException ex) {
            // Ignore it for the time being.
            return false;
        }
    }
}




