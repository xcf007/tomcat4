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

import java.io.FileNotFoundException;
import java.io.CharArrayWriter;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;

/**
 * This class implements a parser for a JSP page (non-xml view).
 * JSP page grammar is included here for reference.  The token '#'
 * that appears in the production indicates the current input token
 * location in the production.
 * 
 * @author Kin-man Chung
 */

public class Parser {

    private ParserController parserController;
    private JspCompilationContext ctxt;
    private JspReader reader;
    private String currentFile;
    private Mark start;
    private Hashtable taglibs;
    private ErrorDispatcher err;

    /**
     * The constructor
     */
    private Parser(ParserController pc, JspReader reader) {
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.taglibs = pc.getCompiler().getPageInfo().getTagLibraries();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.reader = reader;
        this.currentFile = reader.mark().getFile();
        start = reader.mark();
    }

    /**
     * The main entry for Parser
     * 
     * @param pc The ParseController, use for getting other objects in compiler
     *                 and for parsing included pages
     * @param reader To read the page
     * @param parent The parent node to this page, null for top level page
     * @return list of nodes representing the parsed page
     */
    public static Node.Nodes parse(ParserController pc,
                                   JspReader reader,
                                   Node parent) throws JasperException {
        Parser parser = new Parser(pc, reader);

        Node.Root root = new Node.Root(null, reader.mark(), parent);

        while (reader.hasMoreInput()) {
            parser.parseElements(root);
        }

        Node.Nodes page = new Node.Nodes(root);
        return page;
    }

    /**
     * Attributes ::= (S Attribute)* S?
     */
    Attributes parseAttributes() throws JasperException {
        AttributesImpl attrs = new AttributesImpl();

        reader.skipSpaces();
        while (parseAttribute(attrs))
            reader.skipSpaces();

        return attrs;
    }

    /**
     * Parse Attributes for a reader, provided for external use
     */
    public static Attributes parseAttributes(ParserController pc,
                                             JspReader reader)
                throws JasperException {
        Parser tmpParser = new Parser(pc, reader);
        return tmpParser.parseAttributes();
    }

    /**
     * Attribute ::= Name S? Eq S?
     *               (   '"<%= RTAttributeValueDouble
     *                 | '"' AttributeValueDouble
     *                 | "'<%= RTAttributeValueSingle
     *                 | "'" AttributeValueSingle
     *               }
     * Note: JSP and XML spec does not allow while spaces around Eq.  It is
     * added to be backward compatible with Tomcat, and with other xml parsers.
     */
    private boolean parseAttribute(AttributesImpl attrs) throws JasperException {
        String name = parseName();
        if (name == null)
            return false;

         reader.skipSpaces();
        if (!reader.matches("="))
            err.jspError(reader.mark(), "jsp.error.attribute.noequal");

         reader.skipSpaces();
        char quote = (char) reader.nextChar();
        if (quote != '\'' && quote != '"')
            err.jspError(reader.mark(), "jsp.error.attribute.noquote");

         String watchString = "";
        if (reader.matches("<%="))
            watchString = "%>";
        watchString = watchString + quote;
        
        String attr = parseAttributeValue(watchString);
        attrs.addAttribute("", name, name, "CDATA", attr);
        return true;
    }

    /**
     * Name ::= (Letter | '_' | ':') (Letter | Digit | '.' | '_' | '-' | ':')*
     */
    private String parseName() throws JasperException {
        char ch = (char)reader.peekChar();
        if (Character.isLetter(ch) || ch == '_' || ch == ':') {
            StringBuffer buf = new StringBuffer();
            buf.append(ch);
            reader.nextChar();
            ch = (char)reader.peekChar();
            while (Character.isLetter(ch) || Character.isDigit(ch) ||
                        ch == '.' || ch == '_' || ch == '-' || ch == ':') {
                buf.append(ch);
                reader.nextChar();
                ch = (char) reader.peekChar();
            }
            return buf.toString();
        }
        return null;
    }

    /**
     * AttributeValueDouble ::= (QuotedChar - '"')*
     *                                ('"' | <TRANSLATION_ERROR>)
     * RTAttributeValueDouble ::= ((QuotedChar - '"')* - ((QuotedChar-'"')'%>"')
     *                                  ('%>"' | TRANSLATION_ERROR)
     */
    private String parseAttributeValue(String watch) throws JasperException {
        Mark start = reader.mark();
        Mark stop = reader.skipUntilIgnoreEsc(watch);
        if (stop == null) {
            err.jspError(start, "jsp.error.attribute.unterminated", watch);
        }

        String ret = parseQuoted(reader.getText(start, stop));
        if (watch.length() == 1)        // quote
            return ret;

        // putback delimiter '<%=' and '%>', since they are needed if the
        // attribute does not allow RTexpression.
        return "<%=" + ret + "%>";
    }

    /**
     * QuotedChar ::=   '&apos;'
     *                      | '&quot;'
     *                | '\\'
     *                | '\"'
     *                | "\'"
     *                | '\>'
     *                | Char
     */
    private String parseQuoted(String tx) {
        StringBuffer buf = new StringBuffer();
        int size = tx.length();
        int i = 0;
        while (i < size) {
            char ch =tx.charAt(i);
            if (ch == '&') {
                if (i+5 < size && tx.charAt(i+1) == 'a'
                        && tx.charAt(i+2) == 'p' && tx.charAt(i+3) == 'o'
                        && tx.charAt(i+4) == 's' && tx.charAt(i+5) == ';') {
                    buf.append('\'');
                    i += 6;
                } else if (i+5 < size && tx.charAt(i+1) == 'q'
                        && tx.charAt(i+2) == 'u' && tx.charAt(i+3) == 'o'
                        && tx.charAt(i+4) == 't' && tx.charAt(i+5) == ';') {
                    buf.append('"');
                    i += 6;
                } else {
                    buf.append(ch);
                    ++i;
                }
            } else if (ch == '\\' && i+1 < size) {
                ch = tx.charAt(i+1);
                if (ch == '\\' || ch == '\"' || ch == '\'' || ch == '>') {
                    buf.append(ch);
                    i += 2;
                } else {
                    buf.append('\\');
                    ++i;
                }
            } else {
                buf.append(ch);
                ++i;
            }
        }
        return buf.toString();
    }

    private String parseScriptText(String tx) {
        CharArrayWriter cw = new CharArrayWriter();
        int size = tx.length();
        int i = 0;
        while (i < size) {
            char ch = tx.charAt(i);
            if (i+2 < size && ch == '%' && tx.charAt(i+1) == '\\'
                    && tx.charAt(i+2) == '>') {
                cw.write('%');
                cw.write('>');
                i += 3;
            } else {
                cw.write(ch);
                ++i;
            }
        }
        cw.close();
        return cw.toString();
    }

    /*
     * Invokes parserController to parse the included page
     */
    private void processIncludeDirective(String file, Node parent) 
                throws JasperException {
        if (file == null) {
            return;
        }

        try {
            parserController.parse(file, parent);
        } catch (FileNotFoundException ex) {
            err.jspError(start, "jsp.error.file.not.found", file);
        } catch (Exception ex) {
            err.jspError(start, ex.getMessage());
        }
    }

    /*
     * Parses a page directive with the following syntax:
     *   PageDirective ::= ( S Attribute)*
     */
    private void parsePageDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        Node.PageDirective n = new Node.PageDirective(attrs, start, parent);

        /*
         * A page directive may contain multiple 'import' attributes, each of
         * which consists of a comma-separated list of package names.
         * Store each list with the node, where it is parsed.
         */
        for (int i = 0; i < attrs.getLength(); i++) {
            if ("import".equals(attrs.getQName(i))) {
                n.addImport(attrs.getValue(i));
            }
        }
    }

    /*
     * Parses an include directive with the following syntax:
     *   IncludeDirective ::= ( S Attribute)*
     */
    private void parseIncludeDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();

        // Included file expanded here
        Node includeNode = new Node.IncludeDirective(attrs, start, parent);
        processIncludeDirective(attrs.getValue("file"), includeNode);
    }

    /*
     * Parses a taglib directive with the following syntax:
     *   Directive ::= ( S Attribute)*
     */
    private void parseTaglibDirective(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        String uri = attrs.getValue("uri");
        String prefix = attrs.getValue("prefix");
        if (uri != null && prefix != null) {
            // Errors to be checked in Validator
            String[] location = ctxt.getTldLocation(uri);
            TagLibraryInfo tl = new TagLibraryInfoImpl(ctxt, prefix, uri,
                                                       location, err);
            taglibs.put(prefix, tl);
        }

        new Node.TaglibDirective(attrs, start, parent);
    }

    /*
     * Parses a directive with the following syntax:
     *   Directive ::= S? (   'page' PageDirective
     *                            | 'include' IncludeDirective
     *                            | 'taglib' TagLibDirective)
     *                       S? '%>'
     */
    private void parseDirective(Node parent) throws JasperException {
        reader.skipSpaces();

        String directive = null;
        if (reader.matches("page")) {
            directive = "<%@ page";
            parsePageDirective(parent);
        } else if (reader.matches("include")) {
            directive = "<%@ include";
            parseIncludeDirective(parent);
        } else if (reader.matches("taglib")) {
            directive = "<%@ taglib";
            parseTaglibDirective(parent);
        } else {
            err.jspError(reader.mark(), "jsp.error.invalid.directive");
        }

        reader.skipSpaces();
        if (!reader.matches("%>")) {
            err.jspError(start, "jsp.error.unterminated", directive);
        }
    }
        
    /*
     * JSPCommentBody ::= (Char* - (Char* '--%>')) '--%>'
     */
    private void parseComment(Node parent) throws JasperException {        
        start = reader.mark();
        Mark stop = reader.skipUntil("--%>");
        if (stop == null) {
            err.jspError(start, "jsp.error.unterminated", "&lt;%--");
        }

        new Node.Comment(reader.getText(start, stop), start, parent);
    }

    /*
     * DeclarationBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseDeclaration(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, "jsp.error.unterminated", "&lt;%!");
        }

        new Node.Declaration(parseScriptText(reader.getText(start, stop)),
                                start, parent);
    }

    /*
     * ExpressionBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseExpression(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, "jsp.error.unterminated", "&lt;%=");
        }

        new Node.Expression(parseScriptText(reader.getText(start, stop)),
                                start, parent);
    }
        
    /*
     * Scriptlet ::= (Char* - (char* '%>')) '%>'
     */
    private void parseScriptlet(Node parent) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil("%>");
        if (stop == null) {
            err.jspError(start, "jsp.error.unterminated", "&lt;%");
        }

        new Node.Scriptlet(parseScriptText(reader.getText(start, stop)),
                                start, parent);
    }
        
    /**
     *  Param ::= '<jsp:param' Attributes* '/>'
     */
    private void parseParam(Node parent) throws JasperException {

        if (!reader.matches("<jsp:param")) {
            err.jspError(reader.mark(), "jsp.error.paramexpectedonly");
        }
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (!reader.matches("/>")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:param");
        }

        new Node.ParamAction(attrs, start, parent);
    }

    /**
     *  Params ::= (Param S?)*
     */
    private void parseParams(Node parent, String tag) throws JasperException {

        Mark start = reader.mark();

        while (reader.hasMoreInput()) {
            if (reader.matchesETag(tag)) {
                break;
            }

            parseParam(parent);
            reader.skipSpaces();
        }
    }

    /*
     * IncludeAction ::= Attributes
     *                         (   '/>'
     *                           | '>' S? Params '</jsp:include' S? '>'
     *                         )
     */
    private void parseInclude(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (reader.matches("/>")) {
            // No body
            new Node.IncludeAction(attrs, start, parent);
            return;
        }
            
        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:include");
        }

        reader.skipSpaces();
        Node includeNode = new Node.IncludeAction(attrs, start, parent);
        parseParams(includeNode, "jsp:include");
    }
   
    /*
     * ForwardAction ::= Attributes
     *                         (  '/>'
     *                          | '>' S? Params '<jsp:forward' S? '>'
     *                         )
     */
    private void parseForward(Node parent) throws JasperException {

        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (reader.matches("/>")) {
            // No body
            new Node.ForwardAction(attrs, start, parent);
            return;
        }

        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:forward");
        }

        reader.skipSpaces();
        Node forwardNode = new Node.ForwardAction(attrs, start, parent);
        parseParams(forwardNode, "jsp:forward");
    }

    /*
     * GetProperty ::= (S? Attribute)* S? '/>/
     */
    private void parseGetProperty(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (!reader.matches("/>")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:getProperty");
        }

        new Node.GetProperty(attrs, start, parent);
    }

    /*
     * SetProperty ::= (S Attribute)* S? '/>'
     */
    private void parseSetProperty(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (!reader.matches("/>")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:setProperty");
        }

        new Node.SetProperty(attrs, start, parent);
    }

    /*
     * UseBean ::= (S Attribute)* S?
     *                   ('/>' | ( '>' Body '</jsp:useBean' S? '>' ))
     */
    private void parseUseBean(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (reader.matches("/>")) {
            new Node.UseBean(attrs, start, parent);
            return;
        }

        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                         "&lt;jsp:useBean");
        }

        Node beanNode = new Node.UseBean(attrs, start, parent);
        parseBody(beanNode, "jsp:useBean");
    }

    /*
     * JspParams ::=  S? '>' S? Params+ </jsp:params' S? '>'
     */
    private void parseJspParams(Node parent) throws JasperException {

        reader.skipSpaces();
        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.params.notclosed");
        }

        reader.skipSpaces();
        Node jspParamsNode = new Node.ParamsAction(start, parent);
        parseParams(jspParamsNode, "jsp:params");
    }

    /*
     * FallBack ::=  S? '>' Char* '</jsp:fallback' S? '>'
     */
    private void parseFallBack(Node parent) throws JasperException {

        reader.skipSpaces();
        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.fallback.notclosed");
        }

        Node.FallBackAction fallback = new Node.FallBackAction(start, parent);
        parseBodyText(fallback, "jsp:fallback");
    }

    /*
     * PlugIn ::= Attributes '>' PlugInBody '</jsp:plugin' S? '>'
     * PlugBody ::= S? ('<jsp:params' JspParams S?)?
     *                        ('<jsp:fallback' JspFallack S?)?
     */
    private void parsePlugin(Node parent) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();

        if (!reader.matches(">")) {
            err.jspError(reader.mark(), "jsp.error.plugin.notclosed");
        }

        reader.skipSpaces();
        Node pluginNode = new Node.PlugIn(attrs, start, parent);
        if (reader.matches("<jsp:params")) {
            parseJspParams(pluginNode);
            reader.skipSpaces();
        }

        if (reader.matches("<jsp:fallback")) {
            parseFallBack(pluginNode);
            reader.skipSpaces();
        }

        if (!reader.matchesETag("jsp:plugin")) {
            err.jspError(reader.mark(), "jsp.error.plugin.notclosed");
        }
    }

    /*
     * StandardAction ::=   'include' IncludeAction
     *                          | 'forward' ForwardAction
     *                          | 'getProperty' GetPropertyAction
     *                          | 'setProperty' SetPropertyAction
     *                          | 'useBean' UseBeanAction
     *                          | 'plugin' PlugInAction
     */
    private void parseAction(Node parent) throws JasperException {
        Mark start = reader.mark();

        if (reader.matches("include")) {
            parseInclude(parent);
        } else if (reader.matches("forward")) {
            parseForward(parent);
        } else if (reader.matches("getProperty")) {
            parseGetProperty(parent);
        } else if (reader.matches("setProperty")) {
            parseSetProperty(parent);
        } else if (reader.matches("useBean")) {
            parseUseBean(parent);
        } else if (reader.matches("plugin")) {
            parsePlugin(parent);
        } else {
            err.jspError(start, "jsp.error.badaction");
        }
    }

    /*
     * ActionElement ::= EmptyElemTag | Stag Body Etag
     * EmptyElemTag ::= '<' Name ( S Attribute )* S? '/>'
     * Stag ::= '<' Name ( S Attribute)* S? '>'
     * Etag ::= '</' Name S? '>'
     */
    private boolean parseCustomTag(Node parent) throws JasperException {
        if (reader.peekChar() != '<') {
            return false;
        }

        reader.nextChar();        // skip '<'
        String tagName = reader.parseToken(false);
        int i = tagName.indexOf(':');
        if (i == -1) {
            reader.reset(start);
            return false;
        }

        String prefix = tagName.substring(0, i);
        String shortTagName = tagName.substring(i+1);

        // Check if this is a user-defined tag.
        TagLibraryInfo tagLibInfo = (TagLibraryInfo) taglibs.get(prefix);
        if (tagLibInfo == null) {
            reader.reset(start);
            return false;
        }
        TagInfo tagInfo = tagLibInfo.getTag(shortTagName);
        if (tagInfo == null) {
            err.jspError(start, "jsp.error.bad_tag", shortTagName, prefix);
        }
        Class tagHandlerClass = null;
        try {
            tagHandlerClass
                = ctxt.getClassLoader().loadClass(tagInfo.getTagClassName());
        } catch (Exception e) {
            err.jspError(start, "jsp.error.unable.loadclass", shortTagName,
                         prefix);
        }

        // EmptyElemTag ::= '<' Name ( #S Attribute )* S? '/>'
        // or Stag ::= '<' Name ( #S Attribute)* S? '>'
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        
        if (reader.matches("/>")) {
            // EmptyElemTag ::= '<' Name ( S Attribute )* S? '/>'#
            new Node.CustomTag(attrs, start, tagName, prefix, shortTagName,
                               tagInfo, tagHandlerClass, parent);
            return true;
        }
        
        if (!reader.matches(">")) {
            err.jspError(start, "jsp.error.unterminated.tag");
        }

        // ActionElement ::= Stag #Body Etag
        
        // Looking for a body, it still can be empty; but if there is a
        // a tag body, its syntax would be dependent on the type of
        // body content declared in TLD.
        String bc = tagInfo.getBodyContent();

        Node tagNode = new Node.CustomTag(attrs, start, tagName, prefix,
                                          shortTagName, tagInfo,
                                          tagHandlerClass, parent);
        // There are 3 body content types: empty, jsp, or tag-dependent.
        if (bc.equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY)) {
            if (!reader.matchesETag(tagName)) {
                err.jspError(start, "jasper.error.emptybodycontent.nonempty",
                             tagName);
            }
        } else if (bc.equalsIgnoreCase(TagInfo.BODY_CONTENT_TAG_DEPENDENT)) {
            // parse the body as text
            parseBodyText(tagNode, tagName);
        } else if (bc.equalsIgnoreCase(TagInfo.BODY_CONTENT_JSP)) {
            // parse body as JSP page
            parseBody(tagNode, tagName);
        } else {
            err.jspError(start, "jasper.error.bad.bodycontent.type");
        }

        return true;
    }

    /*
     *
     */
    private void parseTemplateText(Node parent) throws JasperException {
        // Note except for the beginning of a page, the current char is '<'.
        // Quoting in template text is handled here.
        // JSP2.6 "A literal <% is quoted by <\%"
        if (reader.matches("<\\%")) {
            String content = reader.nextContent();
            new Node.TemplateText("<%" + content, start, parent);
        } else {
            new Node.TemplateText(reader.nextContent(), start, parent);
        }
    }
        
    /*
     * BodyElement ::=          '<%--' JSPCommentBody
     *                        | '<%@' DirectiveBody
     *                        | '<%!' DeclarationBody
     *                        | '<%=' ExpressionBody
     *                        | '<%' ScriptletBody
     *                        | '<jsp:' StandardAction
     *                        | '<' CustomAction
     *                        | TemplateText
     */
    private void parseElements(Node parent) throws JasperException {
        start = reader.mark();
        if (reader.matches("<%--")) {
            parseComment(parent);
        } else if (reader.matches("<%@")) {
            parseDirective(parent);
        } else if (reader.matches("<%!")) {
            parseDeclaration(parent);
        } else if (reader.matches("<%=")) {
            parseExpression(parent);
        } else if (reader.matches("<%")) {
            parseScriptlet(parent);
        } else if (reader.matches("<jsp:")) {
            parseAction(parent);
        } else if (!parseCustomTag(parent)) {
            parseTemplateText(parent);
        }
    }

    /*
     *
     */
    private void parseBodyText(Node parent, String tag) throws JasperException{
        Mark bodyStart = reader.mark();
        Mark bodyEnd = reader.skipUntilETag(tag);
        if (bodyEnd == null) {
            err.jspError(start, "jsp.error.unterminated",
                         "&lt;" + tag + "&gt;");
        }
        new Node.TemplateText(reader.getText(bodyStart, bodyEnd), bodyStart,
                              parent);
    }

    /*
     * Parse the body as JSP content.
     * @param tag The name of the tag whose end tag would terminate the body
     */
    private void parseBody(Node parent, String tag) throws JasperException {

        while (reader.hasMoreInput()) {
            if (reader.matchesETag(tag)) {
                return;
            }
            parseElements(parent);
        }
        err.jspError(start, "jsp.error.unterminated", "&lt;" + tag + "&gt;");
    }

    
}

