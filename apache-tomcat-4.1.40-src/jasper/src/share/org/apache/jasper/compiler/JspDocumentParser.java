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

import java.io.*;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

/**
 * Class implementing a parser for a JSP document, that is, a JSP page in XML
 * syntax.
 *
 * @author Jan Luehe
 */

public class JspDocumentParser extends DefaultHandler
            implements LexicalHandler, TagConstants {

    private static final String XMLNS = "xmlns:";
    private static final String XMLNS_JSP = "xmlns:jsp";
    private static final String JSP_VERSION = "version";
    private static final String XMLNS_XSI = "xmlns:xsi";
    private static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
    private static final String URN_JSPTLD = "urn:jsptld:";
    private static final String LEXICAL_HANDLER_PROPERTY
        = "http://xml.org/sax/properties/lexical-handler";

    private ParserController parserController;

    // XXX
    private JspCompilationContext ctxt;
    
    // XML document source
    private InputSource inputSource;

    // XXX
    private String path;

    // Node representing the XML element currently being parsed
    private Node current;

    // Document locator
    private Locator locator;

    // XXX
    private Hashtable taglibs;

    // Flag indicating whether we are inside DTD declarations
    private boolean inDTD;

    private ErrorDispatcher err;

    /*
     * Constructor
     */
    public JspDocumentParser(ParserController pc,
                             String path,
                             InputStream stream) {
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.taglibs = pc.getCompiler().getPageInfo().getTagLibraries();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.path = path;
        this.inputSource = new InputSource(stream);
    }

    /*
     * Parses a JSP document by responding to SAX events.
     *
     * @throws JasperException XXX
     */
    public static Node.Nodes parse(ParserController pc,
                                   String path,
                                   InputStream stream,
                                   Node parent) throws JasperException {
        JspDocumentParser handler = new JspDocumentParser(pc, path, stream);
        handler.current = parent;
        Node.Nodes pageNodes = null;

        try {
            // Use the default (non-validating) parser
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // Configure the parser
            SAXParser saxParser = factory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
            xmlReader.setErrorHandler(handler);

            // Parse the input
            saxParser.parse(handler.inputSource, handler);

            if (parent == null) {
                // Add the jsp:root element to the parse result
                pageNodes = new Node.Nodes((Node.JspRoot) handler.current);
            } else {
                pageNodes = parent.getBody();
            }
        } catch (IOException ioe) {
            handler.err.jspError("jsp.error.data.file.read", path, ioe);
        } catch (Exception e) {
            handler.err.jspError(e);
        }

        return pageNodes;
    }

    /*
     * Receives notification of the start of an element.
     */
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs) throws SAXException {

        Mark start = new Mark(path, locator.getLineNumber(),
                              locator.getColumnNumber());
        Attributes attrsCopy = new AttributesImpl(attrs);

        Node node = null;        
        if (qName.equals(JSP_ROOT_TAG)) {
            node = new Node.JspRoot(attrsCopy, start, current);
            try {
                addCustomTagLibraries(attrsCopy);
            } catch (JasperException je) {
                throw new SAXException(je);
            }
        } else if (qName.equals(JSP_PAGE_DIRECTIVE_TAG)) {
            node = new Node.PageDirective(attrsCopy, start, current);
            String imports = attrs.getValue("import");
            // There can only be one 'import' attribute per page directive
            if (imports != null) {
                ((Node.PageDirective) node).addImport(imports);
            }
        } else if (qName.equals(JSP_INCLUDE_DIRECTIVE_TAG)) {
            node = new Node.IncludeDirective(attrsCopy, start, current);
            String file = attrsCopy.getValue("file");
            try {
                parserController.parse(file, node);
            } catch (FileNotFoundException fnfe) {
                throw new SAXParseException(
                    err.getString("jsp.error.file.not.found", file),
                    locator, fnfe);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        } else if (qName.equals(JSP_DECLARATION_TAG)) {
            node = new Node.Declaration(start, current);
        } else if (qName.equals(JSP_SCRIPTLET_TAG)) {
            node = new Node.Scriptlet(start, current);
        } else if (qName.equals(JSP_EXPRESSION_TAG)) {
            node = new Node.Expression(start, current);
        } else if (qName.equals(JSP_USE_BEAN_TAG)) {
            node = new Node.UseBean(attrsCopy, start, current);
        } else if (qName.equals(JSP_SET_PROPERTY_TAG)) {
            node = new Node.SetProperty(attrsCopy, start, current);
        } else if (qName.equals(JSP_GET_PROPERTY_TAG)) {
            node = new Node.GetProperty(attrsCopy, start, current);
        } else if (qName.equals(JSP_INCLUDE_TAG)) {
            node = new Node.IncludeAction(attrsCopy, start, current);
        } else if (qName.equals(JSP_FORWARD_TAG)) {
            node = new Node.ForwardAction(attrsCopy, start, current);
        } else if (qName.equals(JSP_PARAM_TAG)) {
            node = new Node.ParamAction(attrsCopy, start, current);
        } else if (qName.equals(JSP_PARAMS_TAG)) {
            node = new Node.ParamsAction(start, current);
        } else if (qName.equals(JSP_PLUGIN_TAG)) {
            node = new Node.PlugIn(attrsCopy, start, current);
        } else if (qName.equals(JSP_TEXT_TAG)) {
            node = new Node.JspText(start, current);
        } else if (qName.equals(JSP_FALLBACK_TAG)) {
            node = new Node.FallBackAction(start, current);
        } else {
            node = getCustomTag(qName, attrsCopy, start, current);
            if (node == null) {
                node = new Node.UninterpretedTag(attrsCopy, start, qName,
                                                 current);
            }
        }

        current = node;
    }

    /*
     * Receives notification of character data inside an element.
     *
     * @param buf The characters
     * @param offset The start position in the character array
     * @param len The number of characters to use from the character array
     *
     * @throws SAXException
     */
    public void characters(char[] buf,
                           int offset,
                           int len) throws SAXException {
        /*
         * All textual nodes that have only white space are to be dropped from
         * the document, except for nodes in a jsp:text element, which are 
         * kept verbatim (JSP 5.2.1).
         */
        boolean isAllSpace = true;
        if (!(current instanceof Node.JspText)) {
            for (int i=offset; i<offset+len; i++) {
                if (!Character.isWhitespace(buf[i])) {
                    isAllSpace = false;
                    break;
                }
            }
        }
        if ((current instanceof Node.JspText) || !isAllSpace) {
            Mark start = new Mark(path, locator.getLineNumber(),
                                  locator.getColumnNumber());
            new Node.TemplateText(new String(buf, offset, len), start, current);
        }
    }

    /*
     * Receives notification of the end of an element.
     */
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if (current instanceof Node.ScriptingElement) {
            checkScriptingBody(current.getBody());
        }
        if (current.getParent() != null) {
            current = current.getParent();
        }
    }

    /*
     * Receives the document locator.
     *
     * @param locator the document locator
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void comment(char[] buf, int offset, int len) throws SAXException {
        // ignore comments in the DTD
        if (!inDTD) {
            Mark start = new Mark(path, locator.getLineNumber(),
                                  locator.getColumnNumber());
            new Node.Comment(new String(buf, offset, len), start, current);
        }
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startCDATA() throws SAXException {
        // do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endCDATA() throws SAXException {
        // do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startEntity(String name) throws SAXException {
        // do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endEntity(String name) throws SAXException {
        // do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startDTD(String name, String publicId,
                         String systemId) throws SAXException {   
        inDTD = true;
    }
          
    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endDTD() throws SAXException {
        inDTD = false;
    }

    /*
     * Receives notification of a non-recoverable error.
     */
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    /*
     * Receives notification of a recoverable error.
     */
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    
    //*********************************************************************
    // Private utility methods

    /*
     * Checks if the XML element with the given tag name is a custom action,
     * and returns the corresponding Node object.
     */
    private Node getCustomTag(String qName,
                              Attributes attrs,
                              Mark start,
                              Node parent) throws SAXException {
        int colon = qName.indexOf(':');
        if (colon == -1) {
            return null;
        }

        String prefix = qName.substring(0, colon);
        String shortName = qName.substring(colon + 1);
        if (shortName.length() == 0) {
            return null;
        }

        // Check if this is a user-defined (custom) tag
        TagLibraryInfo tagLibInfo = (TagLibraryInfo) taglibs.get(prefix);
        if (tagLibInfo == null) {
            return null;
        }
        TagInfo tagInfo = tagLibInfo.getTag(shortName);
        if (tagInfo == null) {
            throw new SAXException(err.getString("jsp.error.bad_tag",
                                                 shortName, prefix));
        }
        Class tagHandlerClass = null;
        try {
            tagHandlerClass
                = ctxt.getClassLoader().loadClass(tagInfo.getTagClassName());
        } catch (Exception e) {
            throw new SAXException(err.getString(
                                                 "jsp.error.unable.loadclass",
                                                 shortName, prefix));
        }
       
        return new Node.CustomTag(attrs, start, qName, prefix, shortName,
                                  tagInfo, tagHandlerClass, parent);
    }

    /*
     * Parses the xmlns:prefix attributes from the jsp:root element and adds 
     * the corresponding TagLibraryInfo objects to the set of custom tag
     * libraries.
     */
    private void addCustomTagLibraries(Attributes attrs)
                throws JasperException {
        int len = attrs.getLength();
        for (int i=0; i<len; i++) {
            String qName = attrs.getQName(i);
            if (!qName.startsWith(XMLNS_JSP)
                        && !qName.startsWith(JSP_VERSION)
                        && !qName.startsWith(XMLNS_XSI)
                        && !qName.startsWith(XSI_SCHEMA_LOCATION)) {

                // get the prefix
                String prefix = null;
                try {
                    prefix = qName.substring(XMLNS.length());
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }

                // get the uri
                String uri = attrs.getValue(i);
                if (uri.startsWith(URN_JSPTLD)) {
                    // uri value is of the form "urn:jsptld:path"
                    uri = uri.substring(URN_JSPTLD.length());
                }

                TldLocationsCache cache=ctxt.getOptions().getTldLocationsCache();
                TagLibraryInfo tl=cache.getTagLibraryInfo( uri );
                if( tl==null ) {
                    // get the location
                    String[] location = ctxt.getTldLocation(uri);
                
                    tl = new TagLibraryInfoImpl(ctxt, prefix, uri,
                                                location, err);
                }
                taglibs.put(prefix, tl);
            }
        }
    }

    /*
     * Ensures that the given body only contains nodes that are instances of
     * TemplateText.
     *
     * This check is performed only for the body of a scripting (that is, a
     * declaration, scriptlet, or expression) element, after the end tag of a
     * scripting element has been reached.
     */
    private void checkScriptingBody(Node.Nodes body) throws SAXException {
        if (body != null) {
            int size = body.size();
            for (int i=0; i<size; i++) {
                Node n = body.getNode(i);
                if (!(n instanceof Node.TemplateText)) {
                    String msg = err.getString(
                        "jsp.error.parse.xml.scripting.invalid.body");
                    throw new SAXException(msg);
                }
            }
        }
    }
}
