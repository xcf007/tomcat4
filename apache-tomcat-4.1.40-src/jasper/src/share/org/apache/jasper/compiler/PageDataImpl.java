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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.util.ListIterator;
import javax.servlet.jsp.tagext.PageData;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.JasperException;

/**
 * An implementation of <tt>javax.servlet.jsp.tagext.PageData</tt> which
 * builds the XML view of a given page.
 *
 * The XML view is built in two passes:
 *
 * During the first pass, the FirstPassVisitor collects the attributes of the
 * top-level jsp:root and those of the jsp:root elements of any included
 * pages, and adds them to the jsp:root element of the XML view.
 * In addition, any taglib directives are converted into xmlns: attributes and
 * added to the jsp:root element of the XML view.
 * This pass ignores any nodes other than JspRoot and TaglibDirective.
 *
 * During the second pass, the SecondPassVisitor produces the XML view, using
 * the combined jsp:root attributes determined in the first pass and any
 * remaining pages nodes (this pass ignores any JspRoot and TaglibDirective
 * nodes).
 *
 * @author Jan Luehe
 */

public class PageDataImpl extends PageData implements TagConstants {

    private static final String JSP_NAMESPACE = "http://java.sun.com/JSP/Page";
    private static final String JSP_VERSION = "1.2";
    private static final String CDATA_START_SECTION = "<![CDATA[\n";
    private static final String CDATA_END_SECTION = "]]>\n";

    // default "xmlns:jsp" and "version" attributes of jsp:root element
    private static AttributesImpl defaultJspRootAttrs;

    // string buffer used to build XML view
    private StringBuffer buf;

    /*
     * Static initializer which sets the "xmlns:jsp" and "version" 
     * attributes of the jsp:root element to their default values.
     */
    static {
        defaultJspRootAttrs = new AttributesImpl();
        defaultJspRootAttrs.addAttribute("", "", "xmlns:jsp", "CDATA",
                                         JSP_NAMESPACE);
        defaultJspRootAttrs.addAttribute("", "", "version", "CDATA",
                                         JSP_VERSION);
    }

    /**
     * Constructor.
     *
     * @param page the page nodes from which to generate the XML view
     */
    public PageDataImpl(Node.Nodes page) throws JasperException {

        // First pass
        FirstPassVisitor firstPassVisitor
            = new FirstPassVisitor(page.getRoot());
        page.visit(firstPassVisitor);

        // Second pass
        buf = new StringBuffer();
        SecondPassVisitor secondPassVisitor
            = new SecondPassVisitor(page.getRoot(), buf);
        page.visit(secondPassVisitor);
    }

    /**
     * Returns the input stream of the XML view.
     *
     * @return the input stream of the XML view
     */
    public InputStream getInputStream() {
        // Turn StringBuffer into InputStream
        try {
            return new ByteArrayInputStream(buf.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            // should never happen
            throw new RuntimeException(uee.toString());
        }
    }

    /*
     * First-pass Visitor for JspRoot nodes (representing jsp:root elements)
     * and TablibDirective nodes, ignoring any other nodes.
     *
     * The purpose of this Visitor is to collect the attributes of the
     * top-level jsp:root and those of the jsp:root elements of any included
     * pages, and add them to the jsp:root element of the XML view.
     * In addition, this Visitor converts any taglib directives into xmlns:
     * attributes and adds them to the jsp:root element of the XML view.
     */
    static class FirstPassVisitor extends Node.Visitor {

        private Node.Root root;
        private AttributesImpl rootAttrs;

        /*
         * Constructor
         */
        public FirstPassVisitor(Node.Root root) {
            this.root = root;
            this.rootAttrs = new AttributesImpl(defaultJspRootAttrs);
        }

        public void visit(Node.Root n) throws JasperException {
            visitBody(n);
            root.setAttributes(rootAttrs);
        }

        public void visit(Node.JspRoot n) throws JasperException {
            Attributes attrs = n.getAttributes();
            if (attrs == null) {
                throw new JasperException("Missing attributes in jsp:root");
            }
            int len = attrs.getLength();
            for (int i=0; i<len; i++) {
                String qName = attrs.getQName(i);
                if ((qName.startsWith("xmlns:jsp")
                     || qName.equals("version"))) {
                    continue;
                }
                rootAttrs.addAttribute(attrs.getURI(i),
                                       attrs.getLocalName(i),
                                       attrs.getQName(i),
                                       attrs.getType(i),
                                       attrs.getValue(i));
            }
            visitBody(n);
            if (n == this.root) {
                // top-level jsp:root element
                root.setAttributes(rootAttrs);
            }
        }

        /*
         * Converts taglib directive into xmlns: attribute of jsp:root element.
         */
        public void visit(Node.TaglibDirective n) throws JasperException {
            Attributes attrs = n.getAttributes();
            if (attrs != null) {
                String qName = "xmlns:" + attrs.getValue("prefix");
                /*
                 * According to javadocs of org.xml.sax.helpers.AttributesImpl,
                 * the addAttribute method does not check to see if the
                 * specified attribute is already contained in the list: This
                 * is the application's responsibility!
                 */
                if (rootAttrs.getIndex(qName) == -1) {
                    rootAttrs.addAttribute("", "", qName, "CDATA",
                                           attrs.getValue("uri"));
                }
            }
        }
    }


    /*
     * Second-pass Visitor responsible for producing XML view and assigning
     * each JSP element a jsp:id attribute.
     */
    static class SecondPassVisitor extends Node.Visitor
                implements TagConstants {

        private Node.Root root;
        private StringBuffer buf;

        // current jsp:id attribute value
        private int jspId;

        /*
         * Constructor
         */
        public SecondPassVisitor(Node.Root root, StringBuffer buf) {
            this.root = root;
            this.buf = buf;
        }

        /*
         * Visits root node of JSP page in JSP syntax.
         */
        public void visit(Node.Root n) throws JasperException {
            appendTag(JSP_ROOT_TAG, n.getAttributes(), n.getBody());
        }

        /*
         * Visits jsp:root element of JSP page in XML syntax.
         *
         * Any nested jsp:root elements (from pages included via an
         * include directive) are ignored.
         */
        public void visit(Node.JspRoot n) throws JasperException {
            if (n == this.root) {
                // top-level jsp:root element
                appendTag(JSP_ROOT_TAG, n.getAttributes(), n.getBody());
            } else {
                visitBody(n);
            }
        }

        public void visit(Node.PageDirective n) throws JasperException {
            appendPageDirective(n);
        }

        public void visit(Node.IncludeDirective n) throws JasperException {
            // expand in place
            visitBody(n);
        }

        public void visit(Node.Comment n) throws JasperException {
            // Comments are ignored in XML view
        }

        public void visit(Node.Declaration n) throws JasperException {
            // jsp:declaration has no attributes, except for jsp:id
            appendTag(JSP_DECLARATION_TAG, n.getAttributes(), n.getText());
        }

        public void visit(Node.Expression n) throws JasperException {
            // jsp:scriptlet has no attributes, except for jsp:id
            appendTag(JSP_EXPRESSION_TAG, n.getAttributes(), n.getText());
        }

        public void visit(Node.Scriptlet n) throws JasperException {
            // jsp:scriptlet has no attributes, except for jsp:id
            appendTag(JSP_SCRIPTLET_TAG, n.getAttributes(), n.getText());
        }

        public void visit(Node.IncludeAction n) throws JasperException {
            appendTag(JSP_INCLUDE_TAG, n.getAttributes(), n.getBody());
        }
    
        public void visit(Node.ForwardAction n) throws JasperException {
            appendTag(JSP_FORWARD_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.GetProperty n) throws JasperException {
            appendTag(JSP_GET_PROPERTY_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.SetProperty n) throws JasperException {
            appendTag(JSP_SET_PROPERTY_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.ParamAction n) throws JasperException {
            appendTag(JSP_PARAM_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.ParamsAction n) throws JasperException {
            appendTag(JSP_PARAMS_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.FallBackAction n) throws JasperException {
            appendTag(JSP_FALLBACK_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.UseBean n) throws JasperException {
            appendTag(JSP_USE_BEAN_TAG, n.getAttributes(), n.getBody());
        }
        
        public void visit(Node.PlugIn n) throws JasperException {
            appendTag(JSP_PLUGIN_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.CustomTag n) throws JasperException {
            appendTag(n.getName(), n.getAttributes(), n.getBody());
        }

        public void visit(Node.UninterpretedTag n) throws JasperException {
            appendTag(n.getName(), n.getAttributes(), n.getBody());
        }

        public void visit(Node.JspText n) throws JasperException {
            // jsp:text has no attributes, except for jsp:id
            appendTag(JSP_TEXT_TAG, n.getAttributes(), n.getBody());
        }

        public void visit(Node.TemplateText n) throws JasperException {
            /*
             * If the template text came from a JSP page written in JSP syntax,
             * create a jsp:text element for it (JSP 5.3.2).
             */
            appendText(n.getText(), !n.isXmlSyntax());
        }

        /*
         * Appends the given tag (including its body, if present) to the XML
         * view.
         */
        private void appendTag(String tag, Attributes attrs, Node.Nodes body)
                    throws JasperException {
            buf.append("<").append(tag);
            buf.append("\n");
            buf.append("  ").append("jsp:id").append("=\"");
            buf.append(jspId++).append("\"\n");
            if (attrs != null) {
                printAttributes(attrs);
            }
            if (body != null) {
                buf.append(">\n");
                body.visit(this);
                buf.append("</" + tag + ">\n");
            } else {
                buf.append("/>\n");
            }
        }

        /*
         * Appends the given tag, including its text body, to the XML view.
         */
        private void appendTag(String tag, Attributes attrs, String text)
                    throws JasperException {
            buf.append("<").append(tag);
            buf.append("\n");
            buf.append("  ").append("jsp:id").append("=\"");
            buf.append(jspId++).append("\"\n");
            if (attrs != null) {
                printAttributes(attrs);
            }
            if (text != null) {
                buf.append(">\n");
                appendText(text, false);
                buf.append("</" + tag + ">\n");
            } else {
                buf.append("/>\n");
            }
        }

        /*
         * Appends the page directive with the given attributes to the XML
         * view.
         *
         * Since the import attribute of the page directive is the only page
         * attribute that is allowed to appear multiple times within the same
         * document, and since XML allows only single-value attributes,
         * the values of multiple import attributes must be combined into one,
         * separated by comma.
         */
        private void appendPageDirective(Node.PageDirective pageDir) {
            Attributes attrs = pageDir.getAttributes();
            buf.append("<").append(JSP_PAGE_DIRECTIVE_TAG);
            buf.append("\n");

            // append jsp:id
            buf.append("  ").append("jsp:id").append("=\"");
            buf.append(jspId++).append("\"\n");

            // append remaining attributes
            int len = attrs.getLength();
            for (int i=0; i<len; i++) {
                String attrName = attrs.getQName(i);
                if ("import".equals(attrName)) {
                    // Ignore page directive's import attribute for now
                    continue;
                }
                String value = attrs.getValue(i);
                buf.append("  ").append(attrName).append("=\"");
                buf.append(JspUtil.getExprInXml(value)).append("\"\n");
            }
            if (pageDir.getImports().size() > 0) {
                // Concatenate names of imported classes/packages
                boolean first = true;
                ListIterator iter = pageDir.getImports().listIterator();
                while (iter.hasNext()) {
                    if (first) {
                        first = false;
                        buf.append("  import=\"");
                    } else {
                        buf.append(",");
                    }
                    buf.append(JspUtil.getExprInXml((String) iter.next()));
                }
                buf.append("\"\n");
            }
            buf.append("/>\n");
        }

        private void appendText(String text, boolean createJspTextElement) {
            if (createJspTextElement) {
                buf.append(JSP_TEXT_TAG_START);
                appendCDATA(text);
                buf.append(JSP_TEXT_TAG_END);
            } else {
                appendCDATA(text);
            }
        }
        
        /*
         * Appends the given text as a CDATA section to the XML view, unless
         * the text has already been marked as CDATA.
         */
        private void appendCDATA(String text) {
            buf.append(CDATA_START_SECTION);
            buf.append(escapeCDATA(text));
            buf.append(CDATA_END_SECTION);
        }

        /*
         * Escapes any occurrences of "]]>" (by replacing them with "]]&gt;")
         * within the given text, so it can be included in a CDATA section.
         */
        private String escapeCDATA(String text) {
            int len = text.length();
            CharArrayWriter result = new CharArrayWriter(len);
            for (int i=0; i<len; i++) {
                if (((i+2) < len)
                        && (text.charAt(i) == ']')
                        && (text.charAt(i+1) == ']')
                        && (text.charAt(i+2) == '>')) {
                    // match found
                    result.write(']');
                    result.write(']');
                    result.write('&');
                    result.write('g');
                    result.write('t');
                    result.write(';');
                    i += 2;
                } else {
                    result.write(text.charAt(i));
                }
            }
            return result.toString();
        }

        /*
         * Appends the given attributes to the XML view.
         */
        private void printAttributes(Attributes attrs) {
            int len = attrs.getLength();
            for (int i=0; i<len; i++) {
                String name = attrs.getQName(i);
                String value = attrs.getValue(i);
                buf.append("  ").append(name).append("=\"");
                buf.append(JspUtil.getExprInXml(value)).append("\"\n");
            }
        }
    }
}

