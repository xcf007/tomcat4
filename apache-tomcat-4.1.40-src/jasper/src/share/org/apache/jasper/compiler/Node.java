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

import java.util.*;
import javax.servlet.jsp.tagext.*;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;

/**
 * An internal data representation of a JSP page or a JSP docuement (XML).
 * Also included here is a visitor class for tranversing nodes.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 */

public abstract class Node {
    
    protected Attributes attrs;
    protected Nodes body;
    protected String text;
    protected Mark startMark;
    protected int beginJavaLine;
    protected int endJavaLine;
    protected Node parent;

    /**
     * Constructor.
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(Mark start, Node parent) {
        this.startMark = start;
        addToParent(parent);
    }

    /**
     * Constructor.
     * @param attrs The attributes for this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(Attributes attrs, Mark start, Node parent) {
        this.attrs = attrs;
        this.startMark = start;
        addToParent(parent);
    }

    /*
     * Constructor.
     * @param text The text associated with this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(String text, Mark start, Node parent) {
        this.text = text;
        this.startMark = start;
        addToParent(parent);
    }

    public Attributes getAttributes() {
        return attrs;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public String getAttributeValue(String name) {
        return (attrs == null) ? null : attrs.getValue(name);
    }

    public Nodes getBody() {
        return body;
    }

    public void setBody(Nodes body) {
        this.body = body;
    }

    public String getText() {
        return text;
    }

    public Mark getStart() {
        return startMark;
    }

    public Node getParent() {
        return parent;
    }

    public int getBeginJavaLine() {
        return beginJavaLine;
    }

    public void setBeginJavaLine(int begin) {
        beginJavaLine = begin;
    }

    public int getEndJavaLine() {
        return endJavaLine;
    }

    public void setEndJavaLine(int end) {
        endJavaLine = end;
    }

    /**
     * @return true if the current page is in xml syntax, false otherwise.
     */
    public boolean isXmlSyntax() {
        Node r = this;
        while (!(r instanceof Node.Root)) {
            r = r.getParent();
            if (r == null)
                return false;
        }

        return r.isXmlSyntax();
    }

    /**
     * Selects and invokes a method in the visitor class based on the node
     * type.  This is abstract and should be overrode by the extending classes.
     * @param v The visitor class
     */
    abstract void accept(Visitor v) throws JasperException;


    //*********************************************************************
    // Private utility methods

    /*
     * Adds this Node to the body of the given parent.
     */
    private void addToParent(Node parent) {
        if (parent != null) {
            this.parent = parent;
            Nodes parentBody = parent.getBody();
            if (parentBody == null) {
                parentBody = new Nodes();
                parent.setBody(parentBody);
            }
            parentBody.add(this);
        }
    }


    /*********************************************************************
     * Child classes
     */
    
    /**
     * Represents the root of a Jsp page or Jsp document
     */
    public static class Root extends Node {

        private Root parentRoot;

        Root(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);

            // Figure out and set the parent root
            Node r = parent;
            while ((r != null) && !(r instanceof Node.Root))
                r = r.getParent();
            parentRoot = (Node.Root) r;
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public boolean isXmlSyntax() {
            return false;
        }

        /**
         * @return The enclosing root to this root.  Usually represents the
         * page that includes this one.
         */
        public Root getParentRoot() {
            return parentRoot;
        }
    }
    
    /**
     * Represents the root of a Jsp document (XML syntax)
     */
    public static class JspRoot extends Root {

        public JspRoot(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public boolean isXmlSyntax() {
            return true;
        }

    }

    /**
     * Represents a page directive
     */
    public static class PageDirective extends Node {

        private Vector imports;

        public PageDirective(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
            imports = new Vector();
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        /**
         * Parses the comma-separated list of class or package names in the
         * given attribute value and adds each component to this
         * PageDirective's vector of imported classes and packages.
         * @param value A comma-separated string of imports.
         */
        public void addImport(String value) {
            int start = 0;
            int index;
            while ((index = value.indexOf(',', start)) != -1) {
                imports.add(value.substring(start, index).trim());
                start = index + 1;
            }
            if (start == 0) {
                // No comma found
                imports.add(value.trim());
            } else {
                imports.add(value.substring(start).trim());
            }
        }

        public List getImports() {
            return imports;
        }
    }

    /**
     * Represents an include directive
     */
    public static class IncludeDirective extends Node {

        public IncludeDirective(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a custom taglib directive
     */
    public static class TaglibDirective extends Node {

        public TaglibDirective(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a Jsp comment
     * Comments are kept for completeness.
     */
    public static class Comment extends Node {

        public Comment(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents an expression, declaration, or scriptlet
     */
    public static abstract class ScriptingElement extends Node {

        public ScriptingElement(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public ScriptingElement(Mark start, Node parent) {
            super(start, parent);
        }

        /**
         * When this node was created from a JSP page in JSP syntax, its text
         * was stored as a String in the "text" field, whereas when this node
         * was created from a JSP document, its text was stored as one or more
         * TemplateText nodes in its body. This method handles either case.
         * @return The text string
         */
        public String getText() {
            String ret = text;
            if ((ret == null) && (body != null)) {
                StringBuffer buf = new StringBuffer();
                for (int i=0; i<body.size(); i++) {
                    buf.append(body.getNode(i).getText());
                }
                ret = buf.toString();
            }
            return ret;
        }
    }

    /**
     * Represents a declaration
     */
    public static class Declaration extends ScriptingElement {

        public Declaration(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public Declaration(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents an expression.  Expressions in attributes are embedded
     * in the attribute string and not here.
     */
    public static class Expression extends ScriptingElement {

        public Expression(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public Expression(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a scriptlet
     */
    public static class Scriptlet extends ScriptingElement {

        public Scriptlet(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public Scriptlet(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a param action
     */
    public static class ParamAction extends Node {

        JspAttribute value;

        public ParamAction(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setValue(JspAttribute value) {
            this.value = value;
        }

        public JspAttribute getValue() {
            return value;
        }
    }

    /**
     * Represents a params action
     */
    public static class ParamsAction extends Node {

        public ParamsAction(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a fallback action
     */
    public static class FallBackAction extends Node {

        public FallBackAction(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents an include action
     */
    public static class IncludeAction extends Node {

        private JspAttribute page;

        public IncludeAction(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setPage(JspAttribute page) {
            this.page = page;
        }

        public JspAttribute getPage() {
            return page;
        }
    }

    /**
     * Represents a forward action
     */
    public static class ForwardAction extends Node {

        private JspAttribute page;

        public ForwardAction(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setPage(JspAttribute page) {
            this.page = page;
        }

        public JspAttribute getPage() {
            return page;
        }
    }

    /**
     * Represents a getProperty action
     */
    public static class GetProperty extends Node {

        public GetProperty(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a setProperty action
     */
    public static class SetProperty extends Node {

        private JspAttribute value;

        public SetProperty(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setValue(JspAttribute value) {
            this.value = value;
        }

        public JspAttribute getValue() {
            return value;
        }
    }

    /**
     * Represents a useBean action
     */
    public static class UseBean extends Node {

        JspAttribute beanName;

        public UseBean(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setBeanName(JspAttribute beanName) {
            this.beanName = beanName;
        }

        public JspAttribute getBeanName() {
            return beanName;
        }
    }

    /**
     * Represents a plugin action
     */
    public static class PlugIn extends Node {

        JspAttribute height;
        JspAttribute width;

        public PlugIn(Attributes attrs, Mark start, Node parent) {
            super(attrs, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public void setHeight(JspAttribute height) {
            this.height = height;
        }

        public void setWidth(JspAttribute width) {
            this.width = width;
        }

        public JspAttribute getHeight() {
            return height;
        }

        public JspAttribute getWidth() {
            return width;
        }
    }

    /**
     * Represents an uninterpreted tag, from a Jsp document
     */
    public static class UninterpretedTag extends Node {
        private String tagName;

        public UninterpretedTag(Attributes attrs, Mark start, String name,
                                Node parent) {
            super(attrs, start, parent);
            tagName = name;
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public String getName() {
            return tagName;
        }
    }

    /**
     * Represents a custom tag
     */
    public static class CustomTag extends Node {
        private String name;
        private String prefix;
        private String shortName;
        private JspAttribute[] jspAttrs;
        private TagData tagData;
        private boolean scriptless;        // true if the tag and its body
                                        // contians no scripting elements.
        private boolean hasUsebean;
        private boolean hasIncludeAction;
        private boolean hasSetProperty;
        private boolean hasScriptingVars;
        private String tagHandlerPoolName;
        private TagInfo tagInfo;
        private Class tagHandlerClass;
        private VariableInfo[] varInfos;
        private int customNestingLevel;
        private boolean implementsIterationTag;
        private boolean implementsBodyTag;
        private boolean implementsTryCatchFinally;
        private Vector atBeginScriptingVars;
        private Vector atEndScriptingVars;
        private Vector nestedScriptingVars;
        private Node.CustomTag customTagParent;
        private Integer numCount;

        public CustomTag(Attributes attrs, Mark start, String name,
                         String prefix, String shortName,
                         TagInfo tagInfo, Class tagHandlerClass, Node parent) {
            super(attrs, start, parent);
            this.name = name;
            this.prefix = prefix;
            this.shortName = shortName;
            this.tagInfo = tagInfo;
            this.customNestingLevel = makeCustomNestingLevel();

            this.tagHandlerClass = tagHandlerClass;
            this.implementsIterationTag = 
                IterationTag.class.isAssignableFrom(tagHandlerClass);
            this.implementsBodyTag =
                BodyTag.class.isAssignableFrom(tagHandlerClass);
            this.implementsTryCatchFinally = 
                TryCatchFinally.class.isAssignableFrom(tagHandlerClass);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        /**
         * @return The full tag name
         */
        public String getName() {
            return name;
        }

        /**
         * @return The tag prefix
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * @return The tag name without prefix
         */
        public String getShortName() {
            return shortName;
        }

        public void setJspAttributes(JspAttribute[] jspAttrs) {
            this.jspAttrs = jspAttrs;
        }

        public JspAttribute[] getJspAttributes() {
            return jspAttrs;
        }
        
        public void setTagData(TagData tagData) {
            this.tagData = tagData;
            this.varInfos = tagInfo.getVariableInfo(tagData);
        }

        public TagData getTagData() {
            return tagData;
        }

        public void setScriptless(boolean s) {
            scriptless = s;
        }

        public boolean isScriptless() {
            return scriptless;
        }

        public void setHasUsebean(boolean u) {
            hasUsebean = u;
        }

        public boolean isHasUsebean() {
            return hasUsebean;
        }

        public void setHasIncludeAction(boolean i) {
            hasIncludeAction = i;
        }

        public boolean isHasIncludeAction() {
            return hasIncludeAction;
        }

        public void setHasSetProperty(boolean s) {
            hasSetProperty = s;
        }

        public boolean isHasSetProperty() {
            return hasSetProperty;
        }

        public void setHasScriptingVars(boolean s) {
            hasScriptingVars = s;
        }

        public boolean hasScriptingVars() {
            return hasScriptingVars;
        }

        public void setTagHandlerPoolName(String s) {
            tagHandlerPoolName = s;
        }

        public String getTagHandlerPoolName() {
            return tagHandlerPoolName;
        }

        public TagInfo getTagInfo() {
            return tagInfo;
        }

        public Class getTagHandlerClass() {
            return tagHandlerClass;
        }

        public boolean implementsIterationTag() {
            return implementsIterationTag;
        }

        public boolean implementsBodyTag() {
            return implementsBodyTag;
        }

        public boolean implementsTryCatchFinally() {
            return implementsTryCatchFinally;
        }

        public TagVariableInfo[] getTagVariableInfos() {
            return tagInfo.getTagVariableInfos();
         }
 
        public VariableInfo[] getVariableInfos() {
            return varInfos;
        }

        public void setCustomTagParent(Node.CustomTag n) {
            this.customTagParent = n;
        }

        public Node.CustomTag getCustomTagParent() {
            return this.customTagParent;
        }

        public void setNumCount(Integer count) {
            this.numCount = count;
        }

        public Integer getNumCount() {
            return this.numCount;
        }

        public void setScriptingVars(Vector vec, int scope) {
            switch (scope) {
            case VariableInfo.AT_BEGIN:
                this.atBeginScriptingVars = vec;
                break;
            case VariableInfo.AT_END:
                this.atEndScriptingVars = vec;
                break;
            case VariableInfo.NESTED:
                this.nestedScriptingVars = vec;
                break;
            }
        }

        /*
         * Gets the scripting variables for the given scope that need to be
         * declared.
         */
        public Vector getScriptingVars(int scope) {
            Vector vec = null;

            switch (scope) {
            case VariableInfo.AT_BEGIN:
                vec = this.atBeginScriptingVars;
                break;
            case VariableInfo.AT_END:
                vec = this.atEndScriptingVars;
                break;
            case VariableInfo.NESTED:
                vec = this.nestedScriptingVars;
                break;
            }

            return vec;
        }

        /*
         * Gets this custom tag's custom nesting level, which is given as
         * the number of times this custom tag is nested inside itself.
         */
        public int getCustomNestingLevel() {
            return customNestingLevel;
        }

        /*
         * Computes this custom tag's custom nesting level, which corresponds
         * to the number of times this custom tag is nested inside itself.
         *
         * Example:
         * 
         *  <g:h>
         *    <a:b> -- nesting level 0
         *      <c:d>
         *        <e:f>
         *          <a:b> -- nesting level 1
         *            <a:b> -- nesting level 2
         *            </a:b>
         *          </a:b>
         *          <a:b> -- nesting level 1
         *          </a:b>
         *        </e:f>
         *      </c:d>
         *    </a:b>
         *  </g:h>
         * 
         * @return Custom tag's nesting level
         */
        private int makeCustomNestingLevel() {
            int n = 0;
            Node p = parent;
            while (p != null) {
                if ((p instanceof Node.CustomTag)
                        && name.equals(((Node.CustomTag) p).name)) {
                    n++;
                }
                p = p.parent;
            }
            return n;
        }
    }

    /**
     * Represents the body of a <jsp:text> element
     */
    public static class JspText extends Node {

        public JspText(Mark start, Node parent) {
            super(start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
    }

    /**
     * Represents a template text string
     */
    public static class TemplateText extends Node {

        public TemplateText(String text, Mark start, Node parent) {
            super(text, start, parent);
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        /**
         * Returns true if this template text contains whitespace only.
         */
        public boolean isAllSpace() {
            boolean isAllSpace = true;
            for (int i=0; i<text.length(); i++) {
                if (!Character.isWhitespace(text.charAt(i))) {
                    isAllSpace = false;
                    break;
                }
            }
            return isAllSpace;
        }

    }

    /*********************************************************************
     * Auxillary classes used in Node
     */

    /**
     * Represents attributes that can be request time expressions.
     */

    public static class JspAttribute {

        private String name;
        private String value;
        private boolean expression;

        JspAttribute(String name, String value, boolean expr) {
            this.name = name;
            this.value = value;
            this.expression = expr;
        }

        /**
          * @return The name of the attribute
         */
        public String getName() {
            return name;
        }

        /**
         * @return the value for the attribute, or the expression string
         *           (stripped of "<%=", "%>", "%=", or "%")
         */
        public String getValue() {
            return value;
        }

        /**
         * @return true is the value represents an expression
         */
        public boolean isExpression() {
            return expression;
        }
    }

    /**
     * An ordered list of Node, used to represent the body of an element, or
     * a jsp page of jsp document.
     */
    public static class Nodes {

        private List list;
        private Node.Root root;                // null if this is not a page

        public Nodes() {
            list = new Vector();
        }

        public Nodes(Node.Root root) {
            this.root = root;
            list = new Vector();
            list.add(root);
        }

        /**
         * Appends a node to the list
         * @param n The node to add
         */
        public void add(Node n) {
            list.add(n);
            root = null;
        }

        /**
         * Visit the nodes in the list with the supplied visitor
         * @param v The visitor used
         */
        public void visit(Visitor v) throws JasperException {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Node n = (Node) iter.next();
                n.accept(v);
            }
        }

        public int size() {
            return list.size();
        }

        public Node getNode(int index) {
            Node n = null;
            try {
                n = (Node) list.get(index);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            return n;
        }
        
        public Node.Root getRoot() {
            return root;
        }
    }

    /**
     * A visitor class for visiting the node.  This class also provides the
     * default action (i.e. nop) for each of the child class of the Node.
     * An actual visitor should extend this class and supply the visit
     * method for the nodes that it cares.
     */
    public static class Visitor {

        /**
         * The method provides a place to put actions that are common to
         * all nodes.  Override this in the child visitor class if need to.
         */
        protected void doVisit(Node n) throws JasperException {
        }

        /**
         * Visit the body of a node, using the current visitor
         */
        protected void visitBody(Node n) throws JasperException {
            if (n.getBody() != null) {
                n.getBody().visit(this);
            }
        }

        public void visit(Root n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(JspRoot n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(PageDirective n) throws JasperException {
            doVisit(n);
        }

        public void visit(IncludeDirective n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(TaglibDirective n) throws JasperException {
            doVisit(n);
        }

        public void visit(Comment n) throws JasperException {
            doVisit(n);
        }

        public void visit(Declaration n) throws JasperException {
            doVisit(n);
        }

        public void visit(Expression n) throws JasperException {
            doVisit(n);
        }

        public void visit(Scriptlet n) throws JasperException {
            doVisit(n);
        }

        public void visit(IncludeAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(ForwardAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(GetProperty n) throws JasperException {
            doVisit(n);
        }

        public void visit(SetProperty n) throws JasperException {
            doVisit(n);
        }

        public void visit(ParamAction n) throws JasperException {
            doVisit(n);
        }

        public void visit(ParamsAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(FallBackAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(UseBean n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(PlugIn n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(CustomTag n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(UninterpretedTag n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(JspText n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(TemplateText n) throws JasperException {
            doVisit(n);
        }
    }
}
