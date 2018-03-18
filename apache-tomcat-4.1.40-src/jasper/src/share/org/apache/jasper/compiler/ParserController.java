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

import java.io.*;
import java.util.*;
import org.xml.sax.Attributes;
import org.apache.jasper.*;

/**
 * Controller for the parsing of a JSP page.
 * <p>
 * A translation unit (JSP source file and any files included via the
 * include directive) may involve the processing of JSP pages
 * written with different syntaxes (currently the original JSP syntax,
 * as well as the XML syntax (as of JSP 1.2)). This class encapsulates 
 * the behavior related to the selection and invocation of 
 * the proper parser.
 *
 * @author Pierre Delisle
 */
public class ParserController {
    /*
     * The compilation context
     */
    private JspCompilationContext ctxt;

    /*
     * The Compiler
     */
    private Compiler compiler;

    /*
     * A stack to keep track of the 'current base directory'
     * for include directives that refer to relative paths.
     */
    private Stack baseDirStack = new Stack();

    /*
     * Document information which tells us what
     * kind of document we are dealing with.
     */
    private boolean isXml;
    
    /*
     * Static information used in the process of figuring out
     * the kind of document we're dealing with.
     */
    private static final String JSP_ROOT_TAG   = "<jsp:root";

    /*
     * Tells if the file being processed is the "top" file
     * in the translation unit.
     */
    private boolean isTopFile = true;

    /*
     * The encoding of the "top" file. This encoding is used
     * for included files by default.
     * Defaults to "ISO-8859-1" per JSP spec.
     */
    private String topFileEncoding = "ISO-8859-1"; 
    
    /*
     * The 'new' encoding required to read a page.
     */
    private String newEncoding;


    //*********************************************************************
    // Constructor

    public ParserController(JspCompilationContext ctxt, Compiler compiler) {
        this.ctxt = ctxt; // @@@ can we assert that ctxt is not null?
        this.compiler = compiler;
    }
   

    public JspCompilationContext getJspCompilationContext () {
        return ctxt;
    }

    public Compiler getCompiler () {
        return compiler;
    }

    //*********************************************************************
    // Parse

    /**
     * Parse the jsp page provided as an argument.
     * This is only invoked by the compiler.
     *
     * @param inFileName The name of the JSP file to be parsed.
     */
    public Node.Nodes parse(String inFileName)
                throws FileNotFoundException, JasperException {
        return parse(inFileName, null);
    }

    /**
     * Parse the JSP page provided as an argument.
     * This is invoked recursively to handle 'include' directives.
     *
     * @param inFileName The name of the jsp file to be parsed.
     * @param parent The node for the 'include' directive.
     */
    public Node.Nodes parse(String inFileName, Node parent)
                throws FileNotFoundException, JasperException {

        Node.Nodes parsedPage = null;
        String absFileName = resolveFileName(inFileName);
        String encoding = topFileEncoding;
        InputStream stream = null;
        InputStreamReader reader = null;
        try {
            // Figure out what type of JSP document we are dealing with
            reader = getReader(absFileName, encoding);
            figureOutJspDocument(absFileName, encoding, reader);
            if (newEncoding != null)
                encoding = newEncoding;
            if (isTopFile) {
                // Set the "top level" file encoding that will be used
                // for all included files where encoding is not defined.
                topFileEncoding = encoding;
                isTopFile = false;
            } else {
                compiler.getPageInfo().addInclude(absFileName);
            }
            try {
                reader.close();
            } catch (IOException ex) {
            }

            // dispatch to the proper parser
            
            if (isXml) {
                stream = getStream(absFileName);
                parsedPage = JspDocumentParser.parse(this, absFileName,
                                                     stream, parent);
            } else {
                reader = getReader(absFileName, encoding);
                JspReader r = new JspReader(ctxt, absFileName, encoding,
                                            reader,
                                            compiler.getErrorDispatcher());
                parsedPage = Parser.parse(this, r, parent);
            }
            baseDirStack.pop();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception any) {
                }
            }
            
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception any) {
                }
            }
        }

        return parsedPage;
    }

    //*********************************************************************
    // Figure out input Document

    private void figureOutJspDocument(String file, 
                                      String encoding,
                                      InputStreamReader reader)
         throws JasperException
    {
        JspReader jspReader;
        try {
            jspReader = new JspReader(ctxt, file, encoding, reader,
                                      compiler.getErrorDispatcher());
        } catch (FileNotFoundException ex) {
            throw new JasperException(ex);
        }
        jspReader.setSingleFile(true);
        Mark startMark = jspReader.mark();

        // Check for the jsp:root tag
        // No check for xml prolog, since nothing prevents a page
        // to output XML and still use JSP syntax.
        jspReader.reset(startMark);
        Mark mark = jspReader.skipUntil(JSP_ROOT_TAG);
        if (mark != null) {
            isXml = true;
        } else {
            isXml = false;
        }

        newEncoding = null;
        String pageEncoding = null;
        
        // Figure out the encoding of the page
        // xml parser will take care of encoding for
        // page in XML syntax since we pass it a stream
        if (!isXml) {
            jspReader.reset(startMark);
            while (jspReader.skipUntil("<%@") != null) {
                jspReader.skipSpaces();
                if (jspReader.matches("page")) {
                    jspReader.skipSpaces();
                    Attributes attrs = Parser.parseAttributes(this, jspReader);
                    pageEncoding = attrs.getValue("pageEncoding");
                    if (pageEncoding == null) {
                        String contentType = attrs.getValue("contentType");
                        if (contentType != null) {
                            int loc = contentType.indexOf("charset=");
                            if (loc != -1) {
                                newEncoding = contentType.substring(loc+8);
                            }
                        }
                    } else {
                        newEncoding = pageEncoding;
                        return;
                    }
                }
            }
        }
    }
    
    //*********************************************************************
    // Utility methods

    /*
     * Resolve the name of the file and update
     * baseDirStack() to keep track ot the current
     * base directory for each included file.
     * The 'root' file is always an 'absolute' path,
     * so no need to put an initial value in the
     * baseDirStack.
     */
    private String resolveFileName(String inFileName) {
        String fileName = inFileName.replace('\\', '/');
        boolean isAbsolute = fileName.startsWith("/");
        fileName = isAbsolute ? fileName 
            : (String) baseDirStack.peek() + fileName;
        String baseDir = 
            fileName.substring(0, fileName.lastIndexOf("/") + 1);
        baseDirStack.push(baseDir);
        return fileName;
    }

    private InputStream getStream(String file)
        throws FileNotFoundException
    {
        InputStream in;
        in = ctxt.getResourceAsStream(file);
        if (in == null) {
            throw new FileNotFoundException(file);
        }
        return in;
    }

    private InputStreamReader getReader(String file, String encoding)
        throws FileNotFoundException, JasperException
    {
        try {
            return new InputStreamReader(getStream(file), encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new JasperException(
                Constants.getString("jsp.error.unsupported.encoding",
                                    new Object[]{encoding}));
        }
    }

    private void p(String s) {
        System.out.println("[ParserController] " + s);
    }

    private void p(String s, Throwable ex) {
        p(s);
        p(ex.getMessage());
        ex.printStackTrace();
    }
}
