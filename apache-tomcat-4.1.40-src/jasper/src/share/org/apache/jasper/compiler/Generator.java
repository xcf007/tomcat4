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
import java.beans.*;
import java.net.URLEncoder;
import java.lang.reflect.Method;
import javax.servlet.jsp.tagext.*;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.apache.jasper.Constants;

/**
 * Generate Java source from Nodes
 *
 * @author Anil K. Vijendran
 * @author Danno Ferrin
 * @author Mandar Raje
 * @author Rajiv Mordani
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Denis Benoit
 */

public class Generator {

    private ServletWriter out;
    private MethodsBuffer methodsBuffer;
    private ErrorDispatcher err;
    private BeanRepository beanInfo;
    private JspCompilationContext ctxt;
    private boolean breakAtLF;
    private PageInfo pageInfo;
    private int maxTagNesting;
    private Vector tagHandlerPoolNames;

    /**
     * @param s the input string
     * @return quoted and escaped string, per Java rule
     */
    private static String quote(String s) {

        if (s == null)
            return "null";

        return '"' + escape( s ) + '"';
    }

    /**
     * @param s the input string
     * @return escaped string, per Java rule
     */
    private static String escape(String s) {

        if (s == null)
            return "";

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') 
                b.append('\\').append('"');
            else if (c == '\\')
                b.append('\\').append('\\');
            else if (c == '\n')
                b.append('\\').append('n');
            else if (c == '\r')
                b.append('\\').append('r');
            else 
                b.append(c);
        }
        return b.toString();
    }

    /**
     * Generates declarations.  This includes "info" of the page directive,
     * and scriptlet declarations.
     */
    private void generateDeclarations(Node.Nodes page) throws JasperException {

        class DeclarationVisitor extends Node.Visitor {

            public void visit(Node.PageDirective n) throws JasperException {
                String info = n.getAttributeValue("info");
                if (info == null)
                    return;

                out.printil("public String getServletInfo() {");
                out.pushIndent();
                out.printin("return ");
                out.print  (quote(info));
                out.println(";");
                out.popIndent();
                out.print  ('}');
                out.println();
            }

            public void visit(Node.Declaration n) throws JasperException {
                out.printMultiLn(new String(n.getText()));
                out.println();
            }
        }

        out.println();
        page.visit(new DeclarationVisitor());
    }

    /**
     * Compiles list of tag handler pool names.
     */
    private void compileTagHandlerPoolList(Node.Nodes page)
            throws JasperException {

        class TagHandlerPoolVisitor extends Node.Visitor {

            private Vector names;

            /*
             * Constructor
             *
             * @param v Vector of tag handler pool names to populate
             */
            TagHandlerPoolVisitor(Vector v) {
                names = v;
            }

            /*
             * Gets the name of the tag handler pool for the given custom tag
             * and adds it to the list of tag handler pool names unless it is
             * already contained in it.
             */
            public void visit(Node.CustomTag n) throws JasperException {
                
                String name = createTagHandlerPoolName(n.getPrefix(),
                                                       n.getShortName(),
                                                       n.getAttributes(),
                                                       n.getBody() == null);
                n.setTagHandlerPoolName(name);
                if (!names.contains(name)) {
                    names.add(name);
                }

                visitBody(n);
            }

            /*
             * Creates the name of the tag handler pool whose tag handlers may
             * be (re)used to service this action.
             *
             * @return The name of the tag handler pool
             */
            private String createTagHandlerPoolName(String prefix,
                                                    String shortName,
                                                    Attributes attrs,
                                                    boolean hasEmptyBody) {
                String poolName = null;

                if (prefix.indexOf('-') >= 0)
                    prefix = JspUtil.replace(prefix, '-', "$1");
                if (prefix.indexOf('.') >= 0)
                    prefix = JspUtil.replace(prefix, '.', "$2");

                if (shortName.indexOf('-') >= 0)
                    shortName = JspUtil.replace(shortName, '-', "$1");
                if (shortName.indexOf('.') >= 0)
                    shortName = JspUtil.replace(shortName, '.', "$2");
                if (shortName.indexOf(':') >= 0)
                    shortName = JspUtil.replace(shortName, ':', "$3");

                poolName = "_jspx_tagPool_" + prefix + "_" + shortName;
                if (attrs != null) {
                    String[] attrNames = new String[attrs.getLength()];
                    for (int i=0; i<attrNames.length; i++) {
                        attrNames[i] = attrs.getQName(i);
                    }
                    Arrays.sort(attrNames, Collections.reverseOrder());
                    for (int i=0; i<attrNames.length; i++) {
                        poolName = poolName + "_" + attrNames[i];
                    }
                }
                if (hasEmptyBody) {
                    poolName = poolName + "_nobody";
                }
                return poolName;
            }
        }

        page.visit(new TagHandlerPoolVisitor(tagHandlerPoolNames));
    }

    private void declareTemporaryScriptingVars(Node.Nodes page)
            throws JasperException {

        class ScriptingVarVisitor extends Node.Visitor {

            private Vector vars;

            ScriptingVarVisitor() {
                vars = new Vector();
            }

            public void visit(Node.CustomTag n) throws JasperException {

                if (n.getCustomNestingLevel() > 0) {
                    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
                    VariableInfo[] varInfos = n.getVariableInfos();

                    if (varInfos != null) {
                        for (int i=0; i<varInfos.length; i++) {
                            String varName = varInfos[i].getVarName();
                            String tmpVarName = "_jspx_" + varName + "_"
                                + n.getCustomNestingLevel();
                            if (!vars.contains(tmpVarName)) {
                                vars.add(tmpVarName);
                                out.printin(varInfos[i].getClassName());
                                out.print(" ");
                                out.print(tmpVarName);
                                out.print(" = ");
                                out.print(null);
                                out.println(";");
                            }
                        }
                    } else if (tagVarInfos != null) {
                        for (int i=0; i<tagVarInfos.length; i++) {
                            String varName = tagVarInfos[i].getNameGiven();
                            if (varName == null) {
                                varName = n.getTagData().getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
                            }
                            String tmpVarName = "_jspx_" + varName + "_"
                                + n.getCustomNestingLevel();
                            if (!vars.contains(tmpVarName)) {
                                vars.add(tmpVarName);
                                out.printin(tagVarInfos[i].getClassName());
                                out.print(" ");
                                out.print(tmpVarName);
                                out.print(" = ");
                                out.print(null);
                                out.println(";");
                            }
                        }
                    }
                }

                visitBody(n);
            }
        }

        page.visit(new ScriptingVarVisitor());
    }

    /**
     * Generates the destroy() method which is responsible for calling the
     * release() method on every tag handler in any of the tag handler pools.
     */
    private void generateDestroy() {
        if (tagHandlerPoolNames.size() <= 0)
            return;

        out.printil("public void _jspDestroy() {");
        out.pushIndent();
        for (int i=0; i<tagHandlerPoolNames.size(); i++) {
            out.printin((String) tagHandlerPoolNames.elementAt(i));
            out.println(".release();");
        }
        out.popIndent();        
        out.printil("}");
        out.println();
    }

    /**
     * Generates the beginning of the static portion of the servelet.
     */
    private void generatePreamble(Node.Nodes page) throws JasperException {

        String servletPackageName = ctxt.getServletPackageName();
        String servletClassName = ctxt.getServletClassName();
        String serviceMethodName = Constants.SERVICE_METHOD_NAME;

        // First the package name:

        if (! "".equals(servletPackageName) && servletPackageName != null) {
            out.printil("package " + servletPackageName + ";");
            out.println();
        }

        // Generate imports

        Iterator iter = pageInfo.getImports().iterator();
        while (iter.hasNext()) {
            out.printin("import ");
            out.print  ((String)iter.next());
            out.println(";");
        }
        out.println();

        // Generate class declaration

        out.printin("public class ");
        out.print  (servletClassName);
        out.print  (" extends ");
        out.print  (pageInfo.getExtends());
        if (!pageInfo.isThreadSafe()) {
            out.print(" implements SingleThreadModel");
        }
        out.println(" {");
        out.pushIndent();

        // Class body begins here

        generateDeclarations(page);
        out.println();

        // Static initializations here

        // Static data for getIncludes()
        out.printil("private static java.util.Vector _jspx_includes;");
        out.println();
        List includes = pageInfo.getIncludes();
        iter = includes.iterator();
        if( !includes.isEmpty() ) {
            out.printil("static {");
            out.pushIndent();
            out.printin("_jspx_includes = new java.util.Vector(");
            out.print(""+includes.size());
            out.println(");");
            while (iter.hasNext()) {
                out.printin("_jspx_includes.add(\"");
                out.print((String)iter.next());
                out.println("\");");
            }
            out.popIndent();                     
            out.printil("}");
            out.println();
        }

         // Class variable declarations
             
        /*
         * Declare tag handler pools (tags of the same type and with the same
         * attribute set share the same tag handler pool)
         */
        if (ctxt.getOptions().isPoolingEnabled()
                && !tagHandlerPoolNames.isEmpty()) {
            for (int i=0; i<tagHandlerPoolNames.size(); i++) {
                out.printil("private org.apache.jasper.runtime.TagHandlerPool "
                            + tagHandlerPoolNames.elementAt(i) + ";");
            }
            out.println();
        }
 
        // Constructor
        if (ctxt.getOptions().isPoolingEnabled()
                && !tagHandlerPoolNames.isEmpty()) {
            generateServletConstructor(servletClassName);
        }
 
        // Methods here

        // Method used to get compile time include file dependencies
        out.printil("public java.util.List getIncludes() {");
        out.pushIndent();
        out.printil("return _jspx_includes;");
        out.popIndent();
        out.printil("}");
        out.println();

        if (ctxt.getOptions().isPoolingEnabled()
                && !tagHandlerPoolNames.isEmpty()) {
            generateDestroy();
        }
 
        // Now the service method
        out.printin("public void ");
        out.print  (serviceMethodName);
        out.println("(HttpServletRequest request, HttpServletResponse response)");
        out.println("        throws java.io.IOException, ServletException {");

        out.pushIndent();
        out.println();

        // Local variable declarations
        out.printil("JspFactory _jspxFactory = null;");
        out.printil("javax.servlet.jsp.PageContext pageContext = null;");
        if (pageInfo.isSession())
            out.printil("HttpSession session = null;");

        if (pageInfo.isIsErrorPage())
            out.printil("Throwable exception = (Throwable) request.getAttribute(\"javax.servlet.jsp.jspException\");");

        out.printil("ServletContext application = null;");
        out.printil("ServletConfig config = null;");
        out.printil("JspWriter out = null;");
        out.printil("Object page = this;");

             // Number of tag object that need to be popped
        // XXX TODO: use a better criteria
        maxTagNesting = pageInfo.getMaxTagNesting();
/*
        if (maxTagNesting > 0) {
            out.printil("JspxState _jspxState = new JspxState();");
        }
*/
        out.printil("JspWriter _jspx_out = null;");
        out.println();

        declareTemporaryScriptingVars(page);
        out.println();

        out.printil("try {");
        out.pushIndent();

        out.printil("_jspxFactory = JspFactory.getDefaultFactory();");

        out.printin("response.setContentType(");
        out.print  (quote(pageInfo.getContentType()));
        out.println(");");

        out.printil("pageContext = _jspxFactory.getPageContext(this, request, response,");
        out.printin("\t\t\t");
        out.print  (quote(pageInfo.getErrorPage()));
        out.print  (", " + pageInfo.isSession());
        out.print  (", " + pageInfo.getBuffer());
        out.print  (", " + pageInfo.isAutoFlush());
        out.println(");");

        out.printil("application = pageContext.getServletContext();");
        out.printil("config = pageContext.getServletConfig();");

        if (pageInfo.isSession())
            out.printil("session = pageContext.getSession();");
        out.printil("out = pageContext.getOut();");
        out.printil("_jspx_out = out;");
        out.println();
    }

    /*
     * Generates the servlet constructor.
     */
    private void generateServletConstructor(String servletClassName) {
        out.printil("public " + servletClassName + "() {");
        out.pushIndent();
        for (int i=0; i<tagHandlerPoolNames.size(); i++) {
            out.printin((String) tagHandlerPoolNames.elementAt(i));
            out.println(" = new org.apache.jasper.runtime.TagHandlerPool();");
        }
        out.popIndent();
        out.printil("}");
        out.println();
    }

    /**
     * Generate codes defining the classes used in the servlet.
     * 1. Servlet state object, used to pass servlet info round methods.
     */
    private void generateJspState() {
/*
        out.println();
        out.printil("static final class JspxState {");
        out.pushIndent();
        out.printil("public JspWriter out;");
        out.println();
        out.printil("public JspxState() {");
        out.pushIndent();
        out.popIndent();
        out.printil("}");
        out.popIndent();
        out.printil("}");
*/
    }

    /**
     * A visitor that generates codes for the elements in the page.
     */
    class GenerateVisitor extends Node.Visitor {

        /*
         * Hashtable containing introspection information on tag handlers:
         *   <key>: tag prefix
         *   <value>: hashtable containing introspection on tag handlers:
         *              <key>: tag short name
         *              <value>: introspection info of tag handler for 
         *                       <prefix:shortName> tag
         */
        private Hashtable handlerInfos;

        private Hashtable tagVarNumbers;
        private String parent;
        private String pushBodyCountVar;

        private ServletWriter out;
        private MethodsBuffer methodsBuffer;
        private int methodNesting;

        /**
         * Constructor.
         */
        public GenerateVisitor(ServletWriter out,
                               MethodsBuffer methodsBuffer) {
            this.out = out;
            this.methodsBuffer = methodsBuffer;
            methodNesting = 0;
            handlerInfos = new Hashtable();
            tagVarNumbers = new Hashtable();
        }

        /**
         * Returns an attribute value, optionally URL encoded.  If
         * the value is a runtime expression, the result is the string for
         * the expression, otherwise the result is the string literal,
         * quoted and escaped.
         * @param attr An JspAttribute object
         * @param encode true if to be URL encoded
         */
        private String attributeValue(Node.JspAttribute attr, boolean encode) {
            String v = attr.getValue();
            if (v == null)
                return "";

            if (attr.isExpression()) {
                if (encode) {
                    return "java.net.URLEncoder.encode(\"\" + " + v + ")";
                }
                return v;
            } else {
                if (encode) {
                    v = URLEncoder.encode(v);
                }
                return quote(v);
            }
        }

        /**
         * Prints the attribute value specified in the param action, in the
         * form of name=value string.
         *
         * @param n the parent node for the param action nodes.
         */
        private void printParams(Node n, Node.JspAttribute page)
                                                throws JasperException {

            class ParamVisitor extends Node.Visitor {
                String separator;

                ParamVisitor(String separator){
                    this.separator = separator;
                }

                public void visit(Node.ParamAction n) throws JasperException {

                    out.print(" + ");
                    out.print(separator);
                    out.print(" + \"");
                    out.print(n.getAttributeValue("name"));
                    out.print("=\" + ");
                    out.print(attributeValue(n.getValue(), true));

                    // The separator is '&' after the second use
                    separator = "\"&\"";
                }
            }

            String pValue = page.getValue();
            String sep;
            if (page.isExpression()) {
                sep = "((" + pValue + ").indexOf('?')>0? '&': '?')";
            } else {
                sep = pValue.indexOf('?')>0? "\"&\"": "\"?\"";
            }
            if (n.getBody() != null) {
                n.getBody().visit(new ParamVisitor(sep));
            }
        }

        public void visit(Node.Expression n) throws JasperException {
            n.setBeginJavaLine(out.getJavaLine());
            out.printil("out.print(" + new String(n.getText()) + ");");
            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.Scriptlet n) throws JasperException {
            n.setBeginJavaLine(out.getJavaLine());
            out.printMultiLn(n.getText());
            out.println();
            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.IncludeAction n) throws JasperException {

            String flush = n.getAttributeValue("flush");

            boolean isFlush = false;        // default to false;
            if ("true".equals(flush))
                isFlush = true;

            n.setBeginJavaLine(out.getJavaLine());

            out.printin("JspRuntimeLibrary.include(request, response, ");
            out.print(attributeValue(n.getPage(), false));
            printParams(n, n.getPage());
            out.println(", out, " + isFlush + ");");

            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.ForwardAction n) throws JasperException {
            String page = n.getAttributeValue("page");

            n.setBeginJavaLine(out.getJavaLine());

            out.printil("if (true) {");        // So that javac won't complain about
            out.pushIndent();                // codes after "return"
            out.printin("pageContext.forward(");
            out.print(attributeValue(n.getPage(), false));
            printParams(n, n.getPage());
            out.println(");");
            out.printil((methodNesting > 0)? "return true;": "return;");
            out.popIndent();
            out.printil("}");

            n.setEndJavaLine(out.getJavaLine());
            // XXX Not sure if we can eliminate dead codes after this.
        }

        public void visit(Node.GetProperty n) throws JasperException {
            String name = n.getAttributeValue("name");
            String property = n.getAttributeValue("property");

            n.setBeginJavaLine(out.getJavaLine());

            if (beanInfo.checkVariable(name)) {
                // Bean is defined using useBean, introspect at compile time
                Class bean = beanInfo.getBeanType(name);
                String beanName = JspUtil.getCanonicalName(bean);
                java.lang.reflect.Method meth =
                    JspRuntimeLibrary.getReadMethod(bean, property);
                String methodName = meth.getName();
                out.printil("out.print(JspRuntimeLibrary.toString(" +
                            "(((" + beanName + ")pageContext.findAttribute(" +
                            "\"" + name + "\"))." + methodName + "())));");
            } else {
                // The object could be a custom action with an associated
                // VariableInfo entry for this name.
                // Get the class name and then introspect at runtime.
                out.printil("out.print(JspRuntimeLibrary.toString" +
                            "(JspRuntimeLibrary.handleGetProperty" +
                            "(pageContext.findAttribute(\"" +
                            name + "\"), \"" + property + "\")));");
            }

            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.SetProperty n) throws JasperException {
            String name = n.getAttributeValue("name");
            String property = n.getAttributeValue("property");
            String param = n.getAttributeValue("param");
            Node.JspAttribute value = n.getValue();

            n.setBeginJavaLine(out.getJavaLine());

            if ("*".equals(property)){
                out.printil("JspRuntimeLibrary.introspect(" +
                            "pageContext.findAttribute(" +
                            "\"" + name + "\"), request);");
            } else if (value == null) {
                if (param == null)
                    param = property;        // default to same as property
                out.printil("JspRuntimeLibrary.introspecthelper(" +
                            "pageContext.findAttribute(\"" + name + "\"), \"" +
                            property + "\", request.getParameter(\"" + param +
                            "\"), " + "request, \"" + param + "\", false);");
            } else if (value.isExpression()) {
                out.printil("JspRuntimeLibrary.handleSetProperty(" + 
                            "pageContext.findAttribute(\""  + name + "\"), \""
                            + property + "\","); 
                out.print(attributeValue(value, false));
                out.println(");");
            } else {
                out.printil("JspRuntimeLibrary.introspecthelper(" +
                            "pageContext.findAttribute(\"" + name + "\"), \"" +
                            property + "\",");
                out.print(attributeValue(value, false));
                out.println(",null, null, false);");
            }

            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.UseBean n) throws JasperException {

            String name = n.getAttributeValue ("id");
            String scope = n.getAttributeValue ("scope");
            String klass = n.getAttributeValue ("class");
            String type = n.getAttributeValue ("type");
            Node.JspAttribute beanName = n.getBeanName();

            if (type == null)        // if unspecified, use class as type of bean 
                type = klass;

            String scopename = "PageContext.PAGE_SCOPE"; // Default to page
            String lock = "pageContext";

            if ("request".equals(scope)) {
                scopename = "PageContext.REQUEST_SCOPE";
                lock = "request";
            } else if ("session".equals(scope)) {
                scopename = "PageContext.SESSION_SCOPE";
                lock = "session";
            } else if ("application".equals(scope)) {
                scopename = "PageContext.APPLICATION_SCOPE";
                lock = "application";
            }

            n.setBeginJavaLine(out.getJavaLine());

            // Declare bean
            out.printin(type);
            out.print  (' ');
            out.print  (name);
            out.println(" = null;");

            // Lock while getting or creating bean
            out.printin("synchronized (");
            out.print  (lock);
            out.println(") {");
            out.pushIndent();

            // Locate bean from context
            out.printin(name);
            out.print  (" = (");
            out.print  (type);
            out.print  (") pageContext.getAttribute(");
            out.print  (quote(name));
            out.print  (", ");
            out.print  (scopename);
            out.println(");");

            // Create bean
            /*
             * Check if bean is alredy there
             */
            out.printin("if (");
            out.print  (name);
            out.println(" == null){");
            out.pushIndent();
            if (klass == null && beanName == null) {
                /*
                 * If both class name and beanName is not specified, the bean
                 * must be found locally, otherwise it's an error
                 */
                out.printin("throw new java.lang.InstantiationException(\"bean ");
                out.print  (name);
                out.println(" not found within scope\");");
            } else {
                /*
                 * Instantiate bean if not there
                 */
                String className;
                if (beanName != null) {
                    className = attributeValue(beanName, false);
                }
                else {
                    // Implies klass is not null
                    className = quote(klass);
                }
                out.printil("try {");
                out.pushIndent();
                out.printin(name);
                out.print  (" = (");
                out.print  (type);
                out.print  (") java.beans.Beans.instantiate(");
                out.print  ("this.getClass().getClassLoader(), ");
                out.print  (className);
                out.println(");");
                out.popIndent();
                /*
                 * Note: Beans.instantiate throws ClassNotFoundException
                 * if the bean class is abstract.
                 */
                out.printil("} catch (ClassNotFoundException exc) {");
                out.pushIndent();
                out.printil("throw new InstantiationException(exc.getMessage());");
                out.popIndent();
                out.printil("} catch (Exception exc) {");
                out.pushIndent();
                out.printin("throw new ServletException(");
                out.print  ("\"Cannot create bean of class \" + ");
                out.print  (className);
                out.println(", exc);");
                out.popIndent();
                out.printil("}");        // close of try
                /*
                 * Set attribute for bean in the specified scope
                 */
                out.printin("pageContext.setAttribute(");
                out.print  (quote(name));
                out.print  (", ");
                out.print  (name);
                out.print  (", ");
                out.print  (scopename);
                out.println(");");

                // Only visit the body when bean is instantiated
                visitBody(n);
            }
            out.popIndent();
            out.printil("}");

            // End of lock block
            out.popIndent();
            out.printil("}");

            n.setEndJavaLine(out.getJavaLine());
        }
        
        /**
         * @return a string for the form 'attr = "value"'
         */
        private String makeAttr(String attr, String value) {
            if (value == null)
                return "";

            return " " + attr + "=\"" + value + '\"';
        }

        public void visit(Node.PlugIn n) throws JasperException {

            /**
             * A visitor to handle <jsp:param> in a plugin
             */
            class ParamVisitor extends Node.Visitor {

                private boolean ie;

                ParamVisitor(boolean ie) {
                    this.ie = ie;
                }

                public void visit(Node.ParamAction n) throws JasperException {

                    String name = n.getAttributeValue("name");
                    if (name.equalsIgnoreCase("object"))
                        name = "java_object";
                    else if (name.equalsIgnoreCase ("type"))
                        name = "java_type";

                    n.setBeginJavaLine(out.getJavaLine());
                    if( ie ) {
                        // We want something of the form
                        // out.println( "<PARAM name=\"blah\"
                        //     value=\"" + ... + "\">" );
                        out.printil( "out.write( \"<PARAM name=\\\"" +
                            escape( name ) + "\\\" value=\\\"\" + " +
                            attributeValue( n.getValue(), false) +
                            " + \"\\\">\" );" );
                        out.printil("out.write(\"\\n\");");
                    }
                    else {
                        // We want something of the form
                        // out.print( " blah=\"" + ... + "\"" );
                        out.printil( "out.write( \" " + escape( name ) +
                            "=\\\"\" + " +
                            attributeValue( n.getValue(), false) +
                            " + \"\\\"\" );" );
                    }
                    n.setEndJavaLine(out.getJavaLine());
                }
            }

            String type = n.getAttributeValue("type");
            String code = n.getAttributeValue("code");
            String name = n.getAttributeValue("name");
            Node.JspAttribute height = n.getHeight();
            Node.JspAttribute width = n.getWidth();
            String hspace = n.getAttributeValue("hspace");
            String vspace = n.getAttributeValue("vspace");
            String align = n.getAttributeValue("align");
            String iepluginurl = n.getAttributeValue("iepluginurl");
            String nspluginurl = n.getAttributeValue("nspluginurl");
            String codebase = n.getAttributeValue("codebase");
            String archive = n.getAttributeValue("archive");
            String jreversion = n.getAttributeValue("jreversion");

            if (iepluginurl == null)
                iepluginurl = Constants.IE_PLUGIN_URL;
            if (nspluginurl == null)
                nspluginurl = Constants.NS_PLUGIN_URL;


            n.setBeginJavaLine(out.getJavaLine());
            // IE style plugin
            // <OBJECT ...>
            // First compose the runtime output string 
            String s0 = "<OBJECT classid=\"" + ctxt.getOptions().getIeClassId()+
                        "\"" + makeAttr("name", name);
            String s1, s2;
            if (width != null) {
                if (width.isExpression()) {
                    s1 = quote(s0 + " width=\"") + " + " + width.getValue() +
                        " + " + quote("\"");
                } else {
                    s1 = quote(s0 + makeAttr("width", width.getValue()));
                }
            } else {
                s1 = quote(s0);
            }
            if (height != null) {
                if (height.isExpression()) {
                    s2 = quote(" height=\"") + " + " + height.getValue() +
                        " + " + quote("\"");
                } else {
                    s2 = quote(makeAttr("height", height.getValue()));
                }
            } else {
                s2 = "\"\"";
            }
            String s3 = quote(makeAttr("hspace", hspace) +
                                makeAttr("vspace", vspace) +
                                makeAttr("align", align) +
                                makeAttr("codebase", iepluginurl) +
                                '>');
            // Then print the output string to the java file
            out.printil("out.println(" + s1 + " + " + s2 + " + " + s3 + ");");

            // <PARAM > for java_code
            s0 = "<PARAM name=\"java_code\"" + makeAttr("value", code) + '>';
            out.printil("out.println(" + quote(s0) + ");");

            // <PARAM > for java_codebase
            if (codebase != null) {
                s0 = "<PARAM name=\"java_codebase\"" + 
                     makeAttr("value", codebase) +
                     '>';
                out.printil("out.println(" + quote(s0) + ");");
            }

            // <PARAM > for java_archive
            if (archive != null) {
                s0 = "<PARAM name=\"java_archive\"" +
                     makeAttr("value", archive) +
                     '>';
                out.printil("out.println(" + quote(s0) + ");");
            }

            // <PARAM > for type
            s0 = "<PARAM name=\"type\"" +
                 makeAttr("value", "application/x-java-" + type + ";" +
                          ((jreversion==null)? "": "version=" + jreversion)) +
                 '>';
            out.printil("out.println(" + quote(s0) + ");");

            /*
             * generate a <PARAM> for each <jsp:param> in the plugin body
             */
            if (n.getBody() != null)
                n.getBody().visit(new ParamVisitor(true));

            /*
             * Netscape style plugin part
             */
            out.printil("out.println(" + quote("<COMMENT>") + ");");
            s0 = "<EMBED" +
                 makeAttr("type", "application/x-java-" + type + ";" +
                          ((jreversion==null)? "": "version=" + jreversion)) +
                 makeAttr("name", name);

            if (width != null) {
                if (width.isExpression()) {
                    s1 = quote(s0 + " width=\"") + " + " + width.getValue() +
                        " + " + quote("\"");
                } else {
                    s1 = quote(s0 + makeAttr("width", width.getValue()));
                }
            } else {
                s1 = quote(s0);
            }
            if (height != null) {
                if (height.isExpression()) {
                    s2 = quote(" height=\"") + " + " + height.getValue() +
                       " + " + quote("\"");
                } else {
                    s2 = quote(makeAttr("height", height.getValue()));
                }
            } else {
                s2 = "\"\"";
            }

            s3 = quote(makeAttr("hspace", hspace) +
                         makeAttr("vspace", vspace) +
                         makeAttr("align", align) +
                         makeAttr("pluginspage", nspluginurl) +
                         makeAttr("java_code", code) +
                         makeAttr("java_codebase", codebase) +
                         makeAttr("java_archive", archive));
            out.printil("out.println(" + s1 + " + " + s2 + " + " + s3 + ");");
                 
            /*
             * Generate a 'attr = "value"' for each <jsp:param> in plugin body
             */
            if (n.getBody() != null)
                n.getBody().visit(new ParamVisitor(false)); 

            out.printil("out.println(" + quote(">") + ");");

            out.printil("out.println(" + quote("<NOEMBED>") + ");");

            /*
             * Fallback
             */
            if (n.getBody() != null) {
                visitBody(n);
                out.printil("out.write(\"\\n\");");
            }

            out.printil("out.println(" + quote("</NOEMBED></EMBED>") + ");");
            out.printil("out.println(" + quote("</COMMENT>") + ");");
            out.printil("out.println(" + quote("</OBJECT>") + ");");

            n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.CustomTag n) throws JasperException {

            Hashtable handlerInfosByShortName = (Hashtable)
                handlerInfos.get(n.getPrefix());
            if (handlerInfosByShortName == null) {
                handlerInfosByShortName = new Hashtable();
                handlerInfos.put(n.getPrefix(), handlerInfosByShortName);
            }
            TagHandlerInfo handlerInfo = (TagHandlerInfo)
                handlerInfosByShortName.get(n.getShortName());
            if (handlerInfo == null) {
                handlerInfo = new TagHandlerInfo(n,
                                                 n.getTagHandlerClass(),
                                                 err);
                handlerInfosByShortName.put(n.getShortName(), handlerInfo);
            }

            // Create variable names
            String baseVar = createTagVarName(n.getName(), n.getPrefix(),
                                              n.getShortName());
            String tagEvalVar = "_jspx_eval_" + baseVar;
            String tagHandlerVar = "_jspx_th_" + baseVar;
            String tagPushBodyCountVar = "_jspx_push_body_count_" + baseVar;

            // If the tag contains no scripting element, generate its codes
            // to a method.
            ServletWriter outSave = null;
            MethodsBuffer methodsBufferSave = null;
            if (n.isScriptless() && !n.hasScriptingVars()) {
                // The tag handler and its body code can reside in a separate
                // method if it is scriptless and does not have any scripting
                // variable defined.

                String tagMethod = "_jspx_meth_" + baseVar;

                // Generate a call to this method
                out.printin("if (");
                out.print(tagMethod);
                out.print("(");
                if (parent != null) {
                    out.print(parent);
                    out.print(", ");
                }
//                out.println("pageContext, _jspxState)");
                out.print("pageContext");
                if (pushBodyCountVar != null) {
                    out.print(", ");
                    out.print(pushBodyCountVar);
                }
                out.println("))");
                out.pushIndent();
                out.printil((methodNesting > 0)? "return true;": "return;");
                out.popIndent();

                // Set up new buffer for the method
                outSave = out;
                out = methodsBuffer.getOut();
                methodsBufferSave = methodsBuffer;
                methodsBuffer = new MethodsBuffer();

                methodNesting++;
                // Generate code for method declaration
                out.println();
                out.pushIndent();
                out.printin("private boolean ");
                out.print(tagMethod);
                out.print("(");
                if (parent != null) {
                    out.print("javax.servlet.jsp.tagext.Tag ");
                    out.print(parent);
                    out.print(", ");
                }
//                out.println("javax.servlet.jsp.PageContext pageContext, JspxState _jspxState)");
                out.print("javax.servlet.jsp.PageContext pageContext");
                if (pushBodyCountVar != null) {
                    out.print(", int[] ");
                    out.print(pushBodyCountVar);
                }
                out.println(")");
                out.printil("        throws Throwable {");
                out.pushIndent();

                // Initilaize local variables used in this method.
                out.printil("JspWriter out = pageContext.getOut();");
                if (n.isHasUsebean()) {
                    out.println("HttpSession session = pageContext.getSession();");
                    out.println("ServletContext application = pageContext.getServletContext();");
                }
                if (n.isHasUsebean() || n.isHasIncludeAction() || n.isHasSetProperty()) {
                    out.println("HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();");
                }
                if (n.isHasIncludeAction()) {
                    out.println("HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();");
                }
            }

            // Generate code for start tag, body, and end tag
            generateCustomStart(n, handlerInfo, tagHandlerVar, tagEvalVar,
                                tagPushBodyCountVar);

            String tmpParent = parent;
            parent = tagHandlerVar;
            String tmpPushBodyCountVar = null;
            if (n.implementsTryCatchFinally()) {
                tmpPushBodyCountVar = pushBodyCountVar;
                pushBodyCountVar = tagPushBodyCountVar;
            }

            visitBody(n);

            parent = tmpParent;
            if (n.implementsTryCatchFinally()) {
                pushBodyCountVar = tmpPushBodyCountVar;
            }

            generateCustomEnd(n, tagHandlerVar, tagEvalVar,
                              tagPushBodyCountVar);

            if (n.isScriptless() && !n.hasScriptingVars()) {
                // Generate end of method
                if (methodNesting > 0) {
                    out.printil("return false;");
                }
                out.popIndent();
                out.printil("}");
                out.popIndent();

                methodNesting--;

                // Append any methods that got generated in the body to the
                // current buffer
                out.print(methodsBuffer.toString());

                // restore previous buffer
                methodsBuffer = methodsBufferSave;
                out = outSave;
            }
        }

        private static final String SINGLE_QUOTE = "'";
        private static final String DOUBLE_QUOTE = "\\\"";

        public void visit(Node.UninterpretedTag n) throws JasperException {

            /*
             * Write begin tag
             */
            out.printin("out.write(\"<");
            out.print(n.getName());
            Attributes attrs = n.getAttributes();
            if (attrs != null) {
                int attrsLength = attrs.getLength();
                for (int i=0; i<attrsLength; i++) {
                    String quote = DOUBLE_QUOTE;
                    String value = attrs.getValue(i);
                    if (value.indexOf('"') != -1) {
                        quote = SINGLE_QUOTE;
                    }
                    out.print(" ");
                    out.print(attrs.getQName(i));
                    out.print("=");
                    out.print(quote);
                    out.print(value);
                    out.print(quote);
                }
            }

            if (n.getBody() != null) {
                out.println(">\");");
                
                // Visit tag body
                visitBody(n);

                /*
                 * Write end tag
                 */
                out.printin("out.write(\"</");
                out.print(n.getName());
                out.println(">\");");
            } else {
                out.println("/>\");");
            }
        }

        private static final int CHUNKSIZE = 1024;

        public void visit(Node.TemplateText n) throws JasperException {

            String text = n.getText();

            n.setBeginJavaLine(out.getJavaLine());

            out.printin();
            StringBuffer sb = new StringBuffer("out.write(\"");
            int initLength = sb.length();
            int count = CHUNKSIZE;
            for (int i = 0 ; i < text.length() ; i++) {
                char ch = text.charAt(i);
                --count;
                switch(ch) {
                case '"':
                    sb.append('\\').append('\"');
                    break;
                case '\\':
                    sb.append('\\').append('\\');
                    break;
                case '\r':
                    sb.append('\\').append('r');
                    break;
                case '\n':
                    sb.append('\\').append('n');

                    if (breakAtLF || count < 0) {
                        // Generate an out.write() when see a '\n' in template
                        sb.append("\");");
                        out.println(sb.toString());
                        out.printin();
                        sb.setLength(initLength);
                        count = CHUNKSIZE;
                    }
                    break;
                case '\t':        // Not sure we need this
                    sb.append('\\').append('t');
                    break;
                default:
                    sb.append(ch);
                }
            }

            if (sb.length() > initLength) {
                sb.append("\");");
                  out.println(sb.toString());
            }

            n.setEndJavaLine(out.getJavaLine());
        }

        private void generateCustomStart(Node.CustomTag n,
                                         TagHandlerInfo handlerInfo,
                                         String tagHandlerVar,
                                         String tagEvalVar,
                                         String tagPushBodyCountVar)
                            throws JasperException {

            Class tagHandlerClass = handlerInfo.getTagHandlerClass();

            n.setBeginJavaLine(out.getJavaLine());
            out.printin("/* ----  ");
            out.print(n.getName());
            out.println(" ---- */");

            // Declare AT_BEGIN scripting variables
            declareScriptingVars(n, VariableInfo.AT_BEGIN);
            saveScriptingVars(n, VariableInfo.AT_BEGIN);

            String tagHandlerClassName =
                JspUtil.getCanonicalName(tagHandlerClass);
            out.printin(tagHandlerClassName);
            out.print(" ");
            out.print(tagHandlerVar);
            out.print(" = ");
            if (ctxt.getOptions().isPoolingEnabled()) {
                out.print("(");
                out.print(tagHandlerClassName);
                out.print(") ");
                out.print(n.getTagHandlerPoolName());
                out.print(".get(");
                out.print(tagHandlerClassName);
                out.println(".class);");
            } else {
                out.print("new ");
                out.print(tagHandlerClassName);
                out.println("();");
            }

            generateSetters(n, tagHandlerVar, handlerInfo);
            
            if (n.implementsTryCatchFinally()) {
                out.printin("int[] ");
                out.print(tagPushBodyCountVar);
                out.println(" = new int[] { 0 };");
                out.printil("try {");
                out.pushIndent();
            }

            out.printin("int ");
            out.print(tagEvalVar);
            out.print(" = ");
            out.print(tagHandlerVar);
            out.println(".doStartTag();");

            if (!n.implementsBodyTag()) {
                // Synchronize AT_BEGIN scripting variables
                syncScriptingVars(n, VariableInfo.AT_BEGIN);
            }

            if (n.getBody() != null) {
                out.printin("if (");
                out.print(tagEvalVar);
                out.println(" != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {");
                out.pushIndent();
                
                // Declare NESTED scripting variables
                declareScriptingVars(n, VariableInfo.NESTED);
                saveScriptingVars(n, VariableInfo.NESTED);

                if (n.implementsBodyTag()) {
                    out.printin("if (");
                    out.print(tagEvalVar);
                    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {");
                    // Assume EVAL_BODY_BUFFERED
                    out.pushIndent();
                    out.printil("javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();");
                    if (n.implementsTryCatchFinally()) {
                        out.printin(tagPushBodyCountVar);
                        out.println("[0]++;");
                    } else if (pushBodyCountVar != null) {
                        out.printin(pushBodyCountVar);
                        out.println("[0]++;");
                    }
                    out.printil("out = _bc;");

                    out.printin(tagHandlerVar);
                    out.println(".setBodyContent(_bc);");
                    out.printin(tagHandlerVar);
                    out.println(".doInitBody();");

                    out.popIndent();
                    out.printil("}");

                    // Synchronize AT_BEGIN and NESTED scripting variables
                    syncScriptingVars(n, VariableInfo.AT_BEGIN);
                    syncScriptingVars(n, VariableInfo.NESTED);
                        
                } else {
                    // Synchronize NESTED scripting variables
                    syncScriptingVars(n, VariableInfo.NESTED);
                }

                if (n.implementsIterationTag()) {
                    out.printil("do {");
                    out.pushIndent();
                }
            }
        };
        
        private void generateCustomEnd(Node.CustomTag n,
                                       String tagHandlerVar,
                                       String tagEvalVar,
                                       String tagPushBodyCountVar) {

            VariableInfo[] varInfos = n.getVariableInfos();
            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();

            if (n.getBody() != null) {
                if (n.implementsIterationTag()) {
                    out.printin("int evalDoAfterBody = ");
                    out.print(tagHandlerVar);
                    out.println(".doAfterBody();");

                    // Synchronize AT_BEGIN and NESTED scripting variables
                    syncScriptingVars(n, VariableInfo.AT_BEGIN);
                    syncScriptingVars(n, VariableInfo.NESTED);

                    out.printil("if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)");
                    out.pushIndent();
                    out.printil("break;");
                    out.popIndent();

                    out.popIndent();
                    out.printil("} while (true);");
                }
                
                restoreScriptingVars(n, VariableInfo.NESTED);

                if (n.implementsBodyTag()) {
                    out.printin("if (");
                    out.print(tagEvalVar);
                    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)");
                    out.pushIndent();
                    out.printil("out = pageContext.popBody();");
                    if (n.implementsTryCatchFinally()) {
                        out.printin(tagPushBodyCountVar);
                        out.println("[0]--;");
                    } else if (pushBodyCountVar != null) {
                        out.printin(pushBodyCountVar);
                        out.println("[0]--;");
                    }
                    out.popIndent();
                }

                out.popIndent(); // EVAL_BODY
                out.printil("}");
            }

            out.printin("if (");
            out.print(tagHandlerVar);
            out.println(".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)");
            out.pushIndent();
            out.printil((methodNesting > 0)? "return true;": "return;");
            out.popIndent();

            // Synchronize AT_BEGIN scripting variables
            syncScriptingVars(n, VariableInfo.AT_BEGIN);

            // TryCatchFinally
            if (n.implementsTryCatchFinally()) {
                out.popIndent(); // try
                out.printil("} catch (Throwable _jspx_exception) {");
                out.pushIndent();

                out.printin("while (");
                out.print(tagPushBodyCountVar);
                out.println("[0]-- > 0)");
                out.pushIndent();
                out.printil("out = pageContext.popBody();");
                out.popIndent();

                out.printin(tagHandlerVar);
                out.println(".doCatch(_jspx_exception);");
                out.popIndent();
                out.printil("} finally {");
                out.pushIndent();
                out.printin(tagHandlerVar);
                out.println(".doFinally();");
            }

            if (ctxt.getOptions().isPoolingEnabled()) {
                out.printin(n.getTagHandlerPoolName());
                out.print(".reuse(");
                out.print(tagHandlerVar);
                out.println(");");
            }

            if (n.implementsTryCatchFinally()) {
                out.popIndent();
                out.printil("}");
            }

            // Declare and synchronize AT_END scripting variables (must do this
            // outside the try/catch/finally block)
            declareScriptingVars(n, VariableInfo.AT_END);
            syncScriptingVars(n, VariableInfo.AT_END);

            restoreScriptingVars(n, VariableInfo.AT_BEGIN);

            n.setEndJavaLine(out.getJavaLine());
        }

        private void declareScriptingVars(Node.CustomTag n, int scope) {
            
            Vector vec = n.getScriptingVars(scope);
            if (vec != null) {
                for (int i=0; i<vec.size(); i++) {
                    Object elem = vec.elementAt(i);
                    if (elem instanceof VariableInfo) {
                        VariableInfo varInfo = (VariableInfo) elem;
                        out.printin(varInfo.getClassName());
                        out.print(" ");
                        out.print(varInfo.getVarName());
                        out.println(" = null;");
                    } else {
                        TagVariableInfo tagVarInfo = (TagVariableInfo) elem;
                        String varName = tagVarInfo.getNameGiven();
                        if (varName == null) {
                            varName = n.getTagData().getAttributeString(
                                        tagVarInfo.getNameFromAttribute());
                        }
                        out.printin(tagVarInfo.getClassName());
                        out.print(" ");
                        out.print(varName);
                        out.println(" = null;");
                    }
                }
            }
        }

        /*
         * This method is called as part of the custom tag's start element.
         *
         * If the given custom tag has a custom nesting level greater than 0,
         * save the current values of its scripting variables to 
         * temporary variables, so those values may be restored in the tag's
         * end element. This way, the scripting variables may be synchronized
         * by the given tag without affecting their original values.
         */
        private void saveScriptingVars(Node.CustomTag n, int scope) {
            if (n.getCustomNestingLevel() == 0) {
                return;
            }

            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();
            if ((varInfos == null) && (tagVarInfos == null)) {
                return;
            }

            if (varInfos != null) {
                for (int i=0; i<varInfos.length; i++) {
                    if (varInfos[i].getScope() != scope)
                        continue;
                    // If the scripting variable has been declared, skip codes
                    // for saving and restoring it.
                    if (n.getScriptingVars(scope).contains(varInfos[i]))
                        continue;
                    String varName = varInfos[i].getVarName();
                    String tmpVarName = "_jspx_" + varName + "_"
                        + n.getCustomNestingLevel();
                    out.printin(tmpVarName);
                    out.print(" = ");
                    out.print(varName);
                    out.println(";");
                }
            } else {
                for (int i=0; i<tagVarInfos.length; i++) {
                    if (tagVarInfos[i].getScope() != scope)
                        continue;
                    // If the scripting variable has been declared, skip codes
                    // for saving and restoring it.
                    if (n.getScriptingVars(scope).contains(tagVarInfos[i]))
                        continue;
                    String varName = tagVarInfos[i].getNameGiven();
                    if (varName == null) {
                        varName = n.getTagData().getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
                    }
                    String tmpVarName = "_jspx_" + varName + "_"
                        + n.getCustomNestingLevel();
                    out.printin(tmpVarName);
                    out.print(" = ");
                    out.print(varName);
                    out.println(";");
                }
            }
        }

        /*
         * This method is called as part of the custom tag's end element.
         *
         * If the given custom tag has a custom nesting level greater than 0,
         * restore its scripting variables to their original values that were
         * saved in the tag's start element.
         */
        private void restoreScriptingVars(Node.CustomTag n, int scope) {
            if (n.getCustomNestingLevel() == 0) {
                return;
            }

            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();
            if ((varInfos == null) && (tagVarInfos == null)) {
                return;
            }

            if (varInfos != null) {
                for (int i=0; i<varInfos.length; i++) {
                    if (varInfos[i].getScope() != scope)
                        continue;
                    // If the scripting variable has been declared, skip codes
                    // for saving and restoring it.
                    if (n.getScriptingVars(scope).contains(varInfos[i]))
                        continue;
                    String varName = varInfos[i].getVarName();
                    String tmpVarName = "_jspx_" + varName + "_"
                        + n.getCustomNestingLevel();
                    out.printin(varName);
                    out.print(" = ");
                    out.print(tmpVarName);
                    out.println(";");
                }
            } else {
                for (int i=0; i<tagVarInfos.length; i++) {
                    if (tagVarInfos[i].getScope() != scope)
                        continue;
                    // If the scripting variable has been declared, skip codes
                    // for saving and restoring it.
                    if (n.getScriptingVars(scope).contains(tagVarInfos[i]))
                        continue;
                    String varName = tagVarInfos[i].getNameGiven();
                    if (varName == null) {
                        varName = n.getTagData().getAttributeString(
                                tagVarInfos[i].getNameFromAttribute());
                    }
                    String tmpVarName = "_jspx_" + varName + "_"
                        + n.getCustomNestingLevel();
                    out.printin(varName);
                    out.print(" = ");
                    out.print(tmpVarName);
                    out.println(";");
                }
            }
        }

        /*
         * Synchronizes the scripting variables of the given custom tag for
         * the given scope.
         */
        private void syncScriptingVars(Node.CustomTag n, int scope) {
            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();

            if ((varInfos == null) && (tagVarInfos == null)) {
                return;
            }

            if (varInfos != null) {
                for (int i=0; i<varInfos.length; i++) {
                    if (varInfos[i].getScope() == scope) {
                        out.printin(varInfos[i].getVarName());
                        out.print(" = (");
                        out.print(varInfos[i].getClassName());
                        out.print(") pageContext.findAttribute(");
                        out.print(quote(varInfos[i].getVarName()));
                        out.println(");");
                    }
                }
            } else {
                for (int i=0; i<tagVarInfos.length; i++) {
                    if (tagVarInfos[i].getScope() == scope) {
                        String name = tagVarInfos[i].getNameGiven();
                        if (name == null) {
                            name = n.getTagData().getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
                        }
                        out.printin(name);
                        out.print(" = (");
                        out.print(tagVarInfos[i].getClassName());
                        out.print(") pageContext.findAttribute(");
                        out.print(quote(name));
                        out.println(");");
                    }
                }
            }
        }

        /*
         * Creates a tag variable name by concatenating the given prefix and
         * shortName and replacing '-' with "$1", '.' with "$2", and ':' with
         * "$3".
         */
        private String createTagVarName(String fullName, String prefix,
                                        String shortName) {
            if (prefix.indexOf('-') >= 0)
                prefix = JspUtil.replace(prefix, '-', "$1");
            if (prefix.indexOf('.') >= 0)
                prefix = JspUtil.replace(prefix, '.', "$2");

            if (shortName.indexOf('-') >= 0)
                shortName = JspUtil.replace(shortName, '-', "$1");
            if (shortName.indexOf('.') >= 0)
                shortName = JspUtil.replace(shortName, '.', "$2");
            if (shortName.indexOf(':') >= 0)
                shortName = JspUtil.replace(shortName, ':', "$3");

            synchronized (tagVarNumbers) {
                String varName = prefix + "_" + shortName + "_";
                if (tagVarNumbers.get(fullName) != null) {
                    Integer i = (Integer) tagVarNumbers.get(fullName);
                    varName = varName + i.intValue();
                    tagVarNumbers.put(fullName, new Integer(i.intValue() + 1));
                    return varName;
                } else {
                    tagVarNumbers.put(fullName, new Integer(1));
                    return varName + "0";
                }
            }
        }

        private void generateSetters(Node.CustomTag n, String tagHandlerVar,
                                     TagHandlerInfo handlerInfo)
                    throws JasperException {

            out.printin(tagHandlerVar);
            out.println(".setPageContext(pageContext);");
            out.printin(tagHandlerVar);
            out.print(".setParent(");
            out.print(parent);
            out.println(");");

            Node.JspAttribute[] attrs = n.getJspAttributes();
            for (int i=0; i<attrs.length; i++) {
                String attrValue = attrs[i].getValue();
                if (attrValue == null) {
                    continue;
                }
                String attrName = attrs[i].getName();
                Method m = handlerInfo.getSetterMethod(attrName);
                if (m == null) {
                    err.jspError(n, "jsp.error.unable.to_find_method",
                                 attrName);
                }

                Class c[] = m.getParameterTypes();
                // XXX assert(c.length > 0)

                if (!attrs[i].isExpression()) {
                    attrValue = convertString(c[0], attrValue, attrName,
                                              handlerInfo.getPropertyEditorClass(attrName));
                }
                
                out.printin(tagHandlerVar);
                out.print(".");
                out.print(m.getName());
                out.print("(");
                out.print(attrValue);
                out.println(");");
            }
        }

        private String convertString(Class c, String s, String attrName,
                                     Class propEditorClass)
                    throws JasperException {
            
            if (propEditorClass != null) {
                String className = JspUtil.getCanonicalName(c);
                return "(" + className
                    + ")JspRuntimeLibrary.getValueFromBeanInfoPropertyEditor("
                    + className + ".class, \"" + attrName + "\", "
                    + quote(s) + ", "
                    + JspUtil.getCanonicalName(propEditorClass) + ".class)";
            } else if (c == String.class) {
                return quote(s);
            } else if (c == boolean.class) {
                return Boolean.valueOf(s).toString();
            } else if (c == Boolean.class) {
                return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
            } else if (c == byte.class) {
                return "((byte)" + Byte.valueOf(s).toString() + ")";
            } else if (c == Byte.class) {
                return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
            } else if (c == char.class) {
                // non-normative (normative method would fail to compile)
                if (s.length() > 0) {
                    char ch = s.charAt(0);
                    // this trick avoids escaping issues
                    return "((char) " + (int) ch + ")";
                } else {
                    throw new NumberFormatException(
                        err.getString("jsp.error.bad_string_char"));
                }
            } else if (c == Character.class) {
                // non-normative (normative method would fail to compile)
                if (s.length() > 0) {
                    char ch = s.charAt(0);
                    // this trick avoids escaping issues
                    return "new Character((char) " + (int) ch + ")";
                } else {
                    throw new NumberFormatException(
                        err.getString("jsp.error.bad_string_Character"));
                }
            } else if (c == double.class) {
                return Double.valueOf(s).toString();
            } else if (c == Double.class) {
                return "new Double(" + Double.valueOf(s).toString() + ")";
            } else if (c == float.class) {
                return Float.valueOf(s).toString() + "f";
            } else if (c == Float.class) {
                return "new Float(" + Float.valueOf(s).toString() + "f)";
            } else if (c == int.class) {
                return Integer.valueOf(s).toString();
            } else if (c == Integer.class) {
                return "new Integer(" + Integer.valueOf(s).toString() + ")";
            } else if (c == short.class) {
                return "((short) " + Short.valueOf(s).toString() + ")";
            } else if (c == Short.class) {
                return "new Short(" + Short.valueOf(s).toString() + ")";
            } else if (c == long.class) {
                return Long.valueOf(s).toString() + "l";
            } else if (c == Long.class) {
                return "new Long(" + Long.valueOf(s).toString() + "l)";
            } else if (c == Object.class) {
                return "new String(" + quote(s) + ")";
            } else {
                String className = JspUtil.getCanonicalName(c);
                return "(" + className
                    + ")JspRuntimeLibrary.getValueFromPropertyEditorManager("
                    + className + ".class, \"" + attrName + "\", "
                    + quote(s) + ")";
            }
        }   
    }

    /**
     * Generates the ending part of the static portion of the servelet.
     */
    private void generatePostamble(Node.Nodes page) {
        out.popIndent();
        out.printil("} catch (Throwable t) {");
        out.pushIndent();

        out.printil("out = _jspx_out;");
        out.printil("if (out != null && out.getBufferSize() != 0)");
        out.pushIndent();
        out.printil("out.clearBuffer();");
        out.popIndent();

        out.printil("if (pageContext != null) pageContext.handlePageException(t);");

        out.popIndent();
        out.printil("} finally {");
        out.pushIndent();

        out.printil("if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);");

        out.popIndent();
        out.printil("}");

        // Close the service method
        out.popIndent();
        out.printil("}");

        // Append any methods that were generated
        out.print(methodsBuffer.toString());

        // generate class definition for JspxState
        if (maxTagNesting > 0) {
            generateJspState();
        }

        // Close the class definition
        out.popIndent();
        out.printil("}");
    }

    /**
     * Constructor.
     */
    Generator(ServletWriter out, Compiler compiler) {
        this.out = out;
        methodsBuffer = new MethodsBuffer();
        err = compiler.getErrorDispatcher();
        ctxt = compiler.getCompilationContext();
        pageInfo = compiler.getPageInfo();
        beanInfo = pageInfo.getBeanRepository();
        breakAtLF = ctxt.getOptions().getMappedFile();
        if (ctxt.getOptions().isPoolingEnabled()) {
            tagHandlerPoolNames = new Vector();
        }
    }

    /**
     * The main entry for Generator.
     * @param out The servlet output writer
     * @param compiler The compiler
     * @param page The input page
     */
    public static void generate(ServletWriter out, Compiler compiler,
                                Node.Nodes page) throws JasperException {
        Generator gen = new Generator(out, compiler);

        if (gen.ctxt.getOptions().isPoolingEnabled()) {
            gen.compileTagHandlerPoolList(page);
        }
        gen.generatePreamble(page);
        page.visit(gen.new GenerateVisitor(out, gen.methodsBuffer));
        gen.generatePostamble(page);
    }

    /**
     * Class storing the result of introspecting a custom tag handler.
     */
    private static class TagHandlerInfo {

        private Hashtable methodMaps;
        private Hashtable propertyEditorMaps;
        private Class tagHandlerClass;
    
        /**
         * Constructor.
         *
         * @param n The custom tag whose tag handler class is to be
         * introspected
         * @param tagHandlerClass Tag handler class
         * @param err Error dispatcher
         */
        TagHandlerInfo(Node n, Class tagHandlerClass, ErrorDispatcher err)
            throws JasperException
        {
            this.tagHandlerClass = tagHandlerClass;
            this.methodMaps = new Hashtable();
            this.propertyEditorMaps = new Hashtable();

            try {
                BeanInfo tagClassInfo
                    = Introspector.getBeanInfo(tagHandlerClass);
                PropertyDescriptor[] pd
                    = tagClassInfo.getPropertyDescriptors();
                for (int i=0; i<pd.length; i++) {
                    /*
                     * FIXME: should probably be checking for things like
                     *        pageContext, bodyContent, and parent here -akv
                     */
                    if (pd[i].getWriteMethod() != null) {
                        methodMaps.put(pd[i].getName(),
                                       pd[i].getWriteMethod());
                    }
                    if (pd[i].getPropertyEditorClass() != null)
                        propertyEditorMaps.put(pd[i].getName(),
                                               pd[i].getPropertyEditorClass());
                }
            } catch (IntrospectionException ie) {
                err.jspError(n, "jsp.error.introspect.taghandler",
                             tagHandlerClass.getName(), ie);
            }
        }

        /**
         * XXX
         */
        public Method getSetterMethod(String attrName) {
            return (Method) methodMaps.get(attrName);
        }

        /**
         * XXX
         */
        public Class getPropertyEditorClass(String attrName) {
            return (Class) propertyEditorMaps.get(attrName);
        }

        /**
         * XXX
         */
        public Class getTagHandlerClass() {
            return tagHandlerClass;
        }
    }

    private static class MethodsBuffer {

        private java.io.CharArrayWriter charWriter;
        private ServletWriter out;

        MethodsBuffer() {
            charWriter = new java.io.CharArrayWriter();
            out = new ServletWriter(new java.io.PrintWriter(charWriter));
        }

        public ServletWriter getOut() {
            return out;
        }

        public String toString() {
            return charWriter.toString();
        }
    }
}

