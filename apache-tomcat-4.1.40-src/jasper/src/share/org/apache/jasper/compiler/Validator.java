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
package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.ValidationMessage;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;

/**
 * Performs validation on the page elements.  Attributes are checked for
 * mandatory presence, entry value validity, and consistency.  As a
 * side effect, some page global value (such as those from page direcitves)
 * are stored, for later use.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 */
public class Validator {

    /**
     * A visitor to validate and extract page directive info
     */
    static class PageDirectiveVisitor extends Node.Visitor {

        private PageInfo pageInfo;
        private ErrorDispatcher err;

        private static final JspUtil.ValidAttribute[] pageDirectiveAttrs = {
            new JspUtil.ValidAttribute("language"),
            new JspUtil.ValidAttribute("extends"),
            new JspUtil.ValidAttribute("import"),
            new JspUtil.ValidAttribute("session"),
            new JspUtil.ValidAttribute("buffer"),
            new JspUtil.ValidAttribute("autoFlush"),
            new JspUtil.ValidAttribute("isThreadSafe"),
            new JspUtil.ValidAttribute("info"),
            new JspUtil.ValidAttribute("errorPage"),
            new JspUtil.ValidAttribute("isErrorPage"),
            new JspUtil.ValidAttribute("contentType"),
            new JspUtil.ValidAttribute("pageEncoding") };

        private boolean languageSeen = false;
        private boolean extendsSeen = false;
        private boolean sessionSeen = false;
         private boolean bufferSeen = false;
        private boolean autoFlushSeen = false;
        private boolean isThreadSafeSeen = false;
        private boolean errorPageSeen = false;
        private boolean isErrorPageSeen = false;
        private boolean contentTypeSeen = false;
        private boolean infoSeen = false;
        private boolean pageEncodingSeen = false;

        /*
         * Constructor
         */
        PageDirectiveVisitor(Compiler compiler) {
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
        }

        public void visit(Node.PageDirective n) throws JasperException {    

            JspUtil.checkAttributes("Page directive", n.getAttributes(),
                                    pageDirectiveAttrs, n.getStart(), err);

            // JSP.2.10.1
            Attributes attrs = n.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                String attr = attrs.getQName(i);
                String value = attrs.getValue(i);

                if ("language".equals(attr)) {
                    if (languageSeen)
                        err.jspError(n, "jsp.error.page.multiple.language");
                    languageSeen = true;
                    if (!"java".equalsIgnoreCase(value))
                        err.jspError(n, "jsp.error.language.nonjava");
                    pageInfo.setLanguage(value);
                } else if ("extends".equals(attr)) {
                    if (extendsSeen)
                        err.jspError(n, "jsp.error.page.multiple.extends");
                    extendsSeen = true;
                    pageInfo.setExtends(value);
                    /*
                     * If page superclass is top level class (i.e. not in a
                     * pkg) explicitly import it. If this is not done, the
                     * compiler will assume the extended class is in the same
                     * pkg as the generated servlet.
                     */
                    if (value.indexOf('.') < 0)
                        n.addImport(value);
                } else if ("contentType".equals(attr)) {
                    if (contentTypeSeen) 
                        err.jspError(n, "jsp.error.page.multiple.contenttypes");
                    contentTypeSeen = true;
                    pageInfo.setContentType(value);
                } else if ("session".equals(attr)) {
                    if (sessionSeen)
                        err.jspError(n, "jsp.error.session.multiple");
                    sessionSeen = true;
                    if ("true".equalsIgnoreCase(value))
                        pageInfo.setSession(true);
                    else if ("false".equalsIgnoreCase(value))
                        pageInfo.setSession(false);
                    else
                        err.jspError(n, "jsp.error.session.invalid");
                } else if ("buffer".equals(attr)) {
                    if (bufferSeen)
                        err.jspError(n, "jsp.error.page.multiple.buffer");
                    bufferSeen = true;

                    if ("none".equalsIgnoreCase(value))
                        pageInfo.setBuffer(0);
                    else {
                        if (value == null || !value.endsWith("kb"))
                            err.jspError(n, "jsp.error.buffer.invalid");

                        try {
                            Integer k = new Integer(
                                value.substring(0, value.length()-2));
                            pageInfo.setBuffer(k.intValue()*1024);
                        } catch (NumberFormatException e) {
                            err.jspError(n, "jsp.error.buffer.invalid");
                        }
                    }
                } else if ("autoFlush".equals(attr)) {
                    if (autoFlushSeen)
                        err.jspError(n, "jsp.error.page.multiple.autoflush");
                    autoFlushSeen = true;
                    if ("true".equalsIgnoreCase(value))
                        pageInfo.setAutoFlush(true);
                    else if ("false".equalsIgnoreCase(value))
                        pageInfo.setAutoFlush(false);
                    else
                        err.jspError(n, "jsp.error.autoFlush.invalid");
                } else if ("isThreadSafe".equals(attr)) {
                    if (isThreadSafeSeen)
                        err.jspError(n, "jsp.error.page.multiple.threadsafe");
                    isThreadSafeSeen = true;
                    if ("true".equalsIgnoreCase(value))
                        pageInfo.setThreadSafe(true);
                    else if ("false".equalsIgnoreCase(value))
                        pageInfo.setThreadSafe(false);
                    else
                        err.jspError(n, "jsp.error.isThreadSafe.invalid");
                } else if ("isErrorPage".equals(attr)) {
                    if (isErrorPageSeen)
                        err.jspError(n, "jsp.error.page.multiple.iserrorpage");
                    isErrorPageSeen = true;
                    if ("true".equalsIgnoreCase(value))
                        pageInfo.setIsErrorPage(true);
                    else if ("false".equalsIgnoreCase(value))
                        pageInfo.setIsErrorPage(false);
                    else
                        err.jspError(n, "jsp.error.isErrorPage.invalid");
                } else if ("errorPage".equals(attr)) {
                    if (errorPageSeen) 
                        err.jspError(n, "jsp.error.page.multiple.errorpage");
                    errorPageSeen = true;
                    pageInfo.setErrorPage(value);
                } else if ("info".equals(attr)) {
                    if (infoSeen) 
                        err.jspError(n, "jsp.error.info.multiple");
                    infoSeen = true;
                } else if ("pageEncoding".equals(attr)) {
                    if (pageEncodingSeen) 
                        err.jspError(n, "jsp.error.page.multiple.pageencoding");
                    pageEncodingSeen = true;
                    pageInfo.setPageEncoding(value);
                }
            }

            // Check for bad combinations
            if (pageInfo.getBuffer() == 0 && !pageInfo.isAutoFlush())
                err.jspError(n, "jsp.error.page.badCombo");

            // Attributes for imports for this node have been processed by
            // the parsers, just add them to pageInfo.
            pageInfo.addImports(n.getImports());
        }
    }

    /**
     * A visitor for validating nodes other than page directives
     */
    static class ValidateVisitor extends Node.Visitor {

        private PageInfo pageInfo;
        private ErrorDispatcher err;

        private static final JspUtil.ValidAttribute[] jspRootAttrs = {
            new JspUtil.ValidAttribute("xsi:schemaLocation"),
            new JspUtil.ValidAttribute("version", true) };

        private static final JspUtil.ValidAttribute[] includeDirectiveAttrs = {
            new JspUtil.ValidAttribute("file", true) };

        private static final JspUtil.ValidAttribute[] taglibDirectiveAttrs = {
            new JspUtil.ValidAttribute("uri", true),
            new JspUtil.ValidAttribute("prefix", true) };

        private static final JspUtil.ValidAttribute[] includeActionAttrs = {
            new JspUtil.ValidAttribute("page", true),
            new JspUtil.ValidAttribute("flush") };

        private static final JspUtil.ValidAttribute[] paramActionAttrs = {
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("value", true) };

        private static final JspUtil.ValidAttribute[] forwardActionAttrs = {
            new JspUtil.ValidAttribute("page", true) };

        private static final JspUtil.ValidAttribute[] getPropertyAttrs = {
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("property", true) };

        private static final JspUtil.ValidAttribute[] setPropertyAttrs = {
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("property", true),
            new JspUtil.ValidAttribute("value"),
            new JspUtil.ValidAttribute("param") };

        private static final JspUtil.ValidAttribute[] useBeanAttrs = {
            new JspUtil.ValidAttribute("id", true),
            new JspUtil.ValidAttribute("scope"),
            new JspUtil.ValidAttribute("class"),
            new JspUtil.ValidAttribute("type"),
            new JspUtil.ValidAttribute("beanName") };

        private static final JspUtil.ValidAttribute[] plugInAttrs = {
            new JspUtil.ValidAttribute("type",true),
            new JspUtil.ValidAttribute("code", true),
            new JspUtil.ValidAttribute("codebase"),
            new JspUtil.ValidAttribute("align"),
            new JspUtil.ValidAttribute("archive"),
            new JspUtil.ValidAttribute("height"),
            new JspUtil.ValidAttribute("hspace"),
            new JspUtil.ValidAttribute("jreversion"),
            new JspUtil.ValidAttribute("name"),
            new JspUtil.ValidAttribute("vspace"),
            new JspUtil.ValidAttribute("width"),
            new JspUtil.ValidAttribute("nspluginurl"),
            new JspUtil.ValidAttribute("iepluginurl") };

        /*
         * Constructor
         */
        ValidateVisitor(Compiler compiler) {
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
        }

        public void visit(Node.JspRoot n) throws JasperException {
            JspUtil.checkAttributes("Jsp:root", n.getAttributes(),
                                    jspRootAttrs, n.getStart(), err);
            visitBody(n);
        }

        public void visit(Node.IncludeDirective n) throws JasperException {
            JspUtil.checkAttributes("Include directive", n.getAttributes(),
                                    includeDirectiveAttrs, n.getStart(), err);
            visitBody(n);
        }

        public void visit(Node.TaglibDirective n) throws JasperException {
            JspUtil.checkAttributes("Taglib directive", n.getAttributes(),
                                    taglibDirectiveAttrs, n.getStart(), err);
        }

        public void visit(Node.ParamAction n) throws JasperException {
            JspUtil.checkAttributes("Param action", n.getAttributes(),
                                    paramActionAttrs, n.getStart(), err);
            n.setValue(getJspAttribute("value", n.getAttributeValue("value"),
                                       n.isXmlSyntax()));
        }

        public void visit(Node.IncludeAction n) throws JasperException {
            JspUtil.checkAttributes("Include action", n.getAttributes(),
                                    includeActionAttrs, n.getStart(), err);
            n.setPage(getJspAttribute("page", n.getAttributeValue("page"),
                                      n.isXmlSyntax()));
            visitBody(n);
        };

        public void visit(Node.ForwardAction n) throws JasperException {
            JspUtil.checkAttributes("Forward", n.getAttributes(),
                                    forwardActionAttrs, n.getStart(), err);
            n.setPage(getJspAttribute("page", n.getAttributeValue("page"),
                                      n.isXmlSyntax()));
            visitBody(n);
        }

        public void visit(Node.GetProperty n) throws JasperException {
            JspUtil.checkAttributes("GetProperty", n.getAttributes(),
                                    getPropertyAttrs, n.getStart(), err);
        }

        public void visit(Node.SetProperty n) throws JasperException {
            JspUtil.checkAttributes("SetProperty", n.getAttributes(),
                                    setPropertyAttrs, n.getStart(), err);
            String name = n.getAttributeValue("name");
            String property = n.getAttributeValue("property");
            String param = n.getAttributeValue("param");
            String value = n.getAttributeValue("value");

            if ("*".equals(property)) { 
                if (param != null || value != null)
                    err.jspError(n, "jsp.error.setProperty.invalid");
                
            } else if (param != null && value != null) {
                err.jspError(n, "jsp.error.setProperty.invalid");
            }
            n.setValue(getJspAttribute("value", value, n.isXmlSyntax()));
        }

        public void visit(Node.UseBean n) throws JasperException {
            JspUtil.checkAttributes("UseBean", n.getAttributes(),
                                    useBeanAttrs, n.getStart(), err);

            String name = n.getAttributeValue ("id");
            String scope = n.getAttributeValue ("scope");
            String className = n.getAttributeValue ("class");
            String type = n.getAttributeValue ("type");
            BeanRepository beanInfo = pageInfo.getBeanRepository();

            if (className == null && type == null)
                err.jspError(n, "jsp.error.useBean.missingType");

            if (beanInfo.checkVariable(name))
                err.jspError(n, "jsp.error.useBean.duplicate");

            if ("session".equals(scope) && !pageInfo.isSession())
                err.jspError(n, "jsp.error.useBean.noSession");

            Node.JspAttribute jattr
                = getJspAttribute("beanName", n.getAttributeValue("beanName"),
                                  n.isXmlSyntax());
            n.setBeanName(jattr);
            if (className != null && jattr != null)
                err.jspError(n, "jsp.error.useBean.notBoth");

            if (className == null)
                className = type;

            if (scope == null || scope.equals("page")) {
                beanInfo.addPageBean(name, className);
            } else if (scope.equals("request")) {
                beanInfo.addRequestBean(name, className);
            } else if (scope.equals("session")) {
                beanInfo.addSessionBean(name,className);
            } else if (scope.equals("application")) {
                beanInfo.addApplicationBean(name,className);
            } else 
                err.jspError(n, "jsp.error.useBean.badScope");

            visitBody(n);
        }

        public void visit(Node.PlugIn n) throws JasperException {
            JspUtil.checkAttributes("Plugin", n.getAttributes(),
                                    plugInAttrs, n.getStart(), err);

            String type = n.getAttributeValue("type");
            if (type == null)
                err.jspError(n, "jsp.error.plugin.notype");
            if (!type.equals("bean") && !type.equals("applet"))
                err.jspError(n, "jsp.error.plugin.badtype");
            if (n.getAttributeValue("code") == null)
                err.jspError(n, "jsp.error.plugin.nocode");

            n.setHeight(getJspAttribute("height", n.getAttributeValue("height"),
                                      n.isXmlSyntax()));
            n.setWidth(getJspAttribute("width", n.getAttributeValue("width"),
                                      n.isXmlSyntax()));
            visitBody(n);
        }

        public void visit(Node.CustomTag n) throws JasperException {
            TagLibraryInfo tagLibInfo = (TagLibraryInfo)
                pageInfo.getTagLibraries().get(n.getPrefix());
            TagInfo tagInfo = tagLibInfo.getTag(n.getShortName());
            if (tagInfo == null) {
                err.jspError(n, "jsp.error.missing.tagInfo", n.getName());
            }

            /*
             * Make sure all required attributes are present
             */
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            Attributes attrs = n.getAttributes();
            for (int i=0; i<tldAttrs.length; i++) {
                if (tldAttrs[i].isRequired()
                    && attrs.getValue(tldAttrs[i].getName()) == null) {
                    err.jspError(n, "jsp.error.missing_attribute",
                                 tldAttrs[i].getName(), n.getShortName());
                }
            }

            /*
             * Make sure there are no invalid attributes
             */
            Hashtable tagDataAttrs = new Hashtable(attrs.getLength());
            Node.JspAttribute[] jspAttrs
                = new Node.JspAttribute[attrs.getLength()];
            for (int i=0; i<attrs.getLength(); i++) {
                boolean found = false;
                for (int j=0; j<tldAttrs.length; j++) {
                    if (attrs.getQName(i).equals(tldAttrs[j].getName())) {
                        if (tldAttrs[j].canBeRequestTime()) {
                            jspAttrs[i]
                                = getJspAttribute(attrs.getQName(i),
                                                  attrs.getValue(i),
                                                  n.isXmlSyntax());
                        } else {
                            jspAttrs[i]
                                = new Node.JspAttribute(attrs.getQName(i),
                                                        attrs.getValue(i),
                                                        false);
                        }
                        if (jspAttrs[i].isExpression()) {
                            tagDataAttrs.put(attrs.getQName(i),
                                             TagData.REQUEST_TIME_VALUE);
                        } else {
                            tagDataAttrs.put(attrs.getQName(i),
                                             attrs.getValue(i));
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    err.jspError(n, "jsp.error.bad_attribute",
                                 attrs.getQName(i));
                }
            }

            TagData tagData = new TagData(tagDataAttrs);
            n.setTagData(tagData);
            n.setJspAttributes(jspAttrs);

            visitBody(n);
        }

        /**
         * Preprocess attributes that can be expressions.  Expression
         * delimiters are stripped.
         */
        private Node.JspAttribute getJspAttribute(String name,
                                                  String value,
                                                  boolean isXml) {
            // XXX Is it an error to see "%=foo%" in non-Xml page?
            // (We won't see "<%=foo%> in xml page because '<' is not a
            // valid attribute value in xml).

            if (value == null)
                return null;

            if (isXml && value.startsWith("%=")) {
                return new Node.JspAttribute(name,
                                             value.substring(2,
                                                             value.length()-1),
                                             true);
            }

            if (!isXml && value.startsWith("<%=")) {
                return new Node.JspAttribute(name,
                                             value.substring(3,
                                                             value.length()-2),
                                             true);
            }

            return new Node.JspAttribute(name, value, false);
        }
    }

    /**
     * A visitor for validating TagExtraInfo classes of all tags
     */
    static class TagExtraInfoVisitor extends Node.Visitor {

        private PageInfo pageInfo;
        private ErrorDispatcher err;

        /*
         * Constructor
         */
        TagExtraInfoVisitor(Compiler compiler) {
            this.pageInfo = compiler.getPageInfo();
            this.err = compiler.getErrorDispatcher();
        }

        public void visit(Node.CustomTag n) throws JasperException {
            TagLibraryInfo tagLibInfo = (TagLibraryInfo)
                pageInfo.getTagLibraries().get(n.getPrefix());
            TagInfo tagInfo = tagLibInfo.getTag(n.getShortName());
            if (tagInfo == null) {
                err.jspError(n, "jsp.error.missing.tagInfo", n.getName());
            }

            if (!tagInfo.isValid(n.getTagData())) {
                err.jspError(n, "jsp.error.invalid.attributes");
            }

            visitBody(n);
        }
    }

    public static void validate(Compiler compiler,
                                Node.Nodes page) throws JasperException {

        /*
         * Visit the page directives first, as they are global to the page
         * and are position independent.
         */
        page.visit(new PageDirectiveVisitor(compiler));

        // Determine the default output content type, per errata_a
        // http://jcp.org/aboutJava/communityprocess/maintenance/jsr053/errata_1_2_a_20020321.html
        PageInfo pageInfo = compiler.getPageInfo();
        String contentType = pageInfo.getContentType();
        if (contentType == null || contentType.indexOf("charset=") < 0) {
            boolean isXml = page.getRoot().isXmlSyntax();
            String defaultType;
            if (contentType == null) {
                defaultType = isXml? "text/xml": "text/html";
            } else {
                defaultType = contentType;
            }
            String charset = pageInfo.getPageEncoding();
            if (charset == null)
                charset = isXml? "UTF-8": "ISO-8859-1";
            pageInfo.setContentType(defaultType + ";charset=" + charset);
        }

        /*
         * Validate all other nodes.
         * This validation step includes checking a custom tag's mandatory and
         * optional attributes against information in the TLD (first validation
         * step for custom tags according to JSP.10.5).
         */
        page.visit(new ValidateVisitor(compiler));

        /*
         * Invoke TagLibraryValidator classes of all imported tags
         * (second validation step for custom tags according to JSP.10.5).
         */
        validateXmlView(new PageDataImpl(page), compiler);

        /*
         * Invoke TagExtraInfo method isValid() for all imported tags 
         * (third validation step for custom tags according to JSP.10.5).
         */
        page.visit(new TagExtraInfoVisitor(compiler));

    }


    //*********************************************************************
    // Private (utility) methods

    /**
     * Validate XML view against the TagLibraryValidator classes of all
     * imported tag libraries.
     */
    private static void validateXmlView(PageData xmlView, Compiler compiler)
                throws JasperException {

        StringBuffer errMsg = null;
        ErrorDispatcher errDisp = compiler.getErrorDispatcher();

        Enumeration enumeration =
            compiler.getPageInfo().getTagLibraries().elements();
        while (enumeration.hasMoreElements()) {
            TagLibraryInfo tli = (TagLibraryInfo) enumeration.nextElement();
            ValidationMessage[] errors
                = ((TagLibraryInfoImpl) tli).validate(xmlView);
            if ((errors != null) && (errors.length != 0)) {
                if (errMsg == null) {
                    errMsg = new StringBuffer();
                }
                errMsg.append("<h3>");
                errMsg.append(errDisp.getString("jsp.error.tlv.invalid.page",
                                                tli.getShortName()));
                errMsg.append("</h3>");
                for (int i=0; i<errors.length; i++) {
                    errMsg.append("<p>");
                    errMsg.append(errors[i].getId());
                    errMsg.append(": ");
                    errMsg.append(errors[i].getMessage());
                    errMsg.append("</p>");
                }
            }
        }

        if (errMsg != null) {
            errDisp.jspError(errMsg.toString());
        }
    }
}

