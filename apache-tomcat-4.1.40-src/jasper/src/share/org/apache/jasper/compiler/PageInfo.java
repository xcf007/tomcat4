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

import java.util.*;

import org.apache.jasper.Constants;

/**
 * A repository for various info about the page under compilation
 *
 * @author Kin-man Chung
 */

class PageInfo {

    private Vector imports;
    private Vector includes;

    private BeanRepository beanRepository;
    private Hashtable tagLibraries;

    private String language = "java";
    private String xtends = Constants.JSP_SERVLET_BASE;
    private String contentType = null;
    private boolean session = true;
    private int buffer = 8*1024;        // XXX confirm
    private boolean autoFlush = true;
    private boolean threadSafe = true;
    private boolean isErrorPage = false;
    private String errorPage = null;
    private String pageEncoding = null;
    private int maxTagNesting = 0;
    private boolean scriptless = false;

    PageInfo(BeanRepository beanRepository) {
        this.beanRepository = beanRepository;
        this.tagLibraries = new Hashtable();
        this.imports = new Vector();
        this.includes = new Vector();

        // Enter standard imports
        for(int i = 0; i < Constants.STANDARD_IMPORTS.length; i++)
            imports.add(Constants.STANDARD_IMPORTS[i]);
    }

    public void addImports(List imports) {
        this.imports.addAll(imports);
    }

    public List getImports() {
        return imports;
    }

    public void addInclude(String include) {
        this.includes.add(include);
    }
     
    public List getIncludes() {
        return includes;
    }

    public BeanRepository getBeanRepository() {
        return beanRepository;
    }

    public Hashtable getTagLibraries() {
        return tagLibraries;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExtends() {
        return xtends;
    }

    public void setExtends(String xtends) {
        this.xtends = xtends;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public boolean isSession() {
        return session;
    }

    public void setSession(boolean session) {
        this.session = session;
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    public boolean isThreadSafe() {
        return threadSafe;
    }

    public void setThreadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
    }

    public boolean isIsErrorPage() {
        return isErrorPage;
    }

    public void setIsErrorPage(boolean isErrorPage) {
        this.isErrorPage = isErrorPage;
    }

    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public int getMaxTagNesting() {
        return maxTagNesting;
    }

    public void setMaxTagNesting(int maxTagNesting) {
        this.maxTagNesting = maxTagNesting;
    }

    public void setScriptless(boolean s) {
        scriptless = s;
    }

    public boolean isScriptless() {
        return scriptless;
    }

}
