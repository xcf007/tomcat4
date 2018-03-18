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
package org.apache.jasper.xmlparser;


import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.logging.Logger;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * XML parsing utilities for processing web application deployment
 * descriptor and tag library descriptor files.  FIXME - make these
 * use a separate class loader for the parser to be used.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 719718 $ $Date: 2008-11-21 21:22:24 +0000 (Fri, 21 Nov 2008) $
 */

public class ParserUtils {
    // ----------------------------------------------------- Instance Variables

    /**
     * The class loader to use for accessing our XML parser.
     */
    protected ClassLoader classLoader = null;


    /**
     * An error handler for use when parsing XML documents.
     */
    static ErrorHandler errorHandler = new MyErrorHandler();


    /**
     * An entity resolver for use when parsing XML documents.
     */
    static EntityResolver entityResolver = new MyEntityResolver();

    public static boolean validating=true;

    // --------------------------------------------------------- Public Methods


    /**
     * Return the class loader used to access our XML parser.
     */
//     public ClassLoader getClassLoader() {

//         return (this.classLoader);

//     }


    /**
     * Parse the specified XML document, and return a <code>TreeNode</code>
     * that corresponds to the root node of the document tree.
     *
     * @param uri URI of the XML document being parsed
     * @param is Input stream containing the deployment descriptor
     *
     * @exception JasperException if an input/output error occurs
     * @exception JasperException if a parsing error occurs
     */
    public TreeNode parseXMLDocument(String uri, InputStream is)
        throws JasperException {

        Document document = null;

        // Perform an XML parse of this document, via JAXP
        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            /*
            Class clazz =
             classLoader.loadClass("javax.xml.parsers.DocumentBuilderFactory");
            System.out.println("Loaded DBF from:");
            System.out.println(clazz.getClassLoader());
            Method method = clazz.getMethod("newInstance", new Class[0]);
            DocumentBuilderFactory factory = (DocumentBuilderFactory)
                method.invoke(null, new Object[0]);
            */
            factory.setNamespaceAware(true);
            factory.setValidating(validating);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            builder.setErrorHandler(errorHandler);
            document = builder.parse(is);
            //System.out.println("Parsing " + uri );
            /*
        } catch (ClassNotFoundException ex) {
            throw new JasperException("CNFE: " + ex); // FIXME
        } catch (IllegalAccessException ex) {
            throw new JasperException("IACE: " + ex); // FIXME
        } catch (IllegalArgumentException ex) {
            throw new JasperException("IARE: " + ex); // FIXME
        } catch (InvocationTargetException ex) {
            throw new JasperException("ITE: " + ex); // FIXME
        } catch (NoSuchMethodException ex) {
            throw new JasperException("NSME: " + ex); // FIXME
            */
        } catch (ParserConfigurationException ex) {
            throw new JasperException
                (Constants.getString("jsp.error.parse.xml",
                                     new Object[]{uri, ex.getMessage()}));
        } catch (SAXParseException ex) {
            throw new JasperException
                (Constants.getString
                 ("jsp.error.parse.xml.line",
                  new Object[]{uri,
                               new Integer(ex.getLineNumber()),
                               new Integer(ex.getColumnNumber()),
                               ex.getMessage()}));
        } catch (SAXException sx) {
            throw new JasperException
                (Constants.getString("jsp.error.parse.xml",
                                     new Object[]{uri, sx.getMessage()}));
        } catch (IOException io) {
            throw new JasperException
                (Constants.getString("jsp.error.parse.xml",
                                     new Object[]{uri, io.toString()}));
        }

        // Convert the resulting document to a graph of TreeNodes
        return (convert(null, document.getDocumentElement()));
        

    }


    /**
     * Set the class loader used to access our XML parser.
     *
     * @param classLoader The new class loader
     */
    public void setClassLoader(ClassLoader classLoader) {

        this.classLoader = classLoader;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Create and return a TreeNode that corresponds to the specified Node,
     * including processing all of the attributes and children nodes.
     *
     * @param parent The parent TreeNode (if any) for the new TreeNode
     * @param node The XML document Node to be converted
     */
    protected TreeNode convert(TreeNode parent, Node node) {

        // Construct a new TreeNode for this node
        TreeNode treeNode = new TreeNode(node.getNodeName(), parent);
        //         System.out.println("PU: " + node.getNodeName());

        // Convert all attributes of this node
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            int n = attributes.getLength();
            for (int i = 0; i < n; i++) {
                Node attribute = attributes.item(i);
                treeNode.addAttribute(attribute.getNodeName(),
                                      attribute.getNodeValue());
                //                 System.out.println("PU: " + 
                //                                    attribute.getNodeName() + "=" + 
                //                                    attribute.getNodeValue());
            }
        }

        // Create and attach all children of this node
        NodeList children = node.getChildNodes();
        if (children != null) {
            int n = children.getLength();
            for (int i = 0; i < n; i++) {
                Node child = children.item(i);
                if (child instanceof Comment)
                    continue;
                if (child instanceof Text) {
                    String body = ((Text) child).getData();
                    if (body != null) {
                        body = body.trim();
                        if (body.length() > 0)
                            treeNode.setBody(body);
                    }
                } else {
                    TreeNode treeChild = convert(treeNode, child);
                }
            }
        }
        
        // Return the completed TreeNode graph
        return (treeNode);

    }


    // ------------------------------------------------------- Static Variables


    /**
     * The special class loaders for each web application's XML parser,
     * keyed by the web application class loader instance.  FIXME - this
     * probably interferes with garbage collection after an application reload.
     */
    //    private static HashMap classLoaders = new HashMap();


    // --------------------------------------------------------- Static Methods


    /**
     * Create (if necessary) and return an instance of ParserUtils that has
     * been loaded by our subordinate class loader (and therefore should have
     * access to the XML parser that is visible to repositories of that
     * class loader).
     *
     * @param parentLoader The web application class loader
     */
    public synchronized static ParserUtils createParserUtils
        (ClassLoader parentLoader) {

        ParserUtils parserUtils = null;
        /*
        try {
            Class clazz = parentLoader.loadClass
                ("org.apache.jasper.xmlparser.ParserUtils");
            parserUtils = (ParserUtils) clazz.newInstance();
            System.out.println("ParserUtils loaded by ClassLoader:\r\n" +
                               parserUtils.getClass().getClassLoader());
            parserUtils.setClassLoader(createClassLoader(parentLoader));
        } catch (ClassNotFoundException e) {
            System.out.println("createParserUtils:  ClassNotFoundException");
            e.printStackTrace(System.out);
        } catch (IllegalAccessException e) {
            System.out.println("createParserUtils:  IllegalAccessException");
            e.printStackTrace(System.out);
        } catch (InstantiationException e) {
            System.out.println("createParserUtils:  InstantiationException");
            e.printStackTrace(System.out);
        } catch (MalformedURLException e) {
            System.out.println("createParserUtils:  MalformedURLException");
            e.printStackTrace(System.out);
        }
        */
        parserUtils = new ParserUtils();
        parserUtils.setClassLoader(parentLoader);
        return (parserUtils);
    }


    /**
     * Construct (if necessary) and return a class loader that has been
     * configured with the specified parent class loader, and repositories
     * as needed to access the required XML parser.
     *
     * @param parentLoader The web application class loader
     *
     * @exception MalformedURLException if we cannot create a valid URL for
     *  one of the required repositories
     */
    /* This  doesn't work.
      
      public synchronized static ClassLoader createClassLoader
        (ClassLoader parentLoader) throws MalformedURLException {

        // Return any existing class loader for this web application
        URLClassLoader classLoader = (URLClassLoader)
            classLoaders.get(parentLoader);
        if (classLoader != null)
            return (classLoader);

        // Construct, cache, and return a new class loader
        URL urls[] = new URL[2]; // FIXME
        urls[0] = new URL("file:///classes/jaxp.jar");
        urls[1] = new URL("file:///classes/crimson.jar");
        classLoader = new URLClassLoader(urls, parentLoader);
        classLoaders.put(parentLoader, classLoader);
        System.out.println("PUF:  createClassLoader:\r\n" + classLoader);
        urls = classLoader.getURLs();
        for (int i = 0; i < urls.length; i++)
            System.out.println("PUF:    url=" + urls[i]);
        return (classLoader);

    }
    */


}


// ------------------------------------------------------------ Private Classes

class MyEntityResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException
    {
        for (int i=0; i<Constants.CACHED_DTD_PUBLIC_IDS.length; i++) {
            String cachedDtdPublicId = Constants.CACHED_DTD_PUBLIC_IDS[i];
            if (cachedDtdPublicId.equals(publicId)) {
                String resourcePath = Constants.CACHED_DTD_RESOURCE_PATHS[i];
                InputStream input =
                    this.getClass().getResourceAsStream(resourcePath);
                if (input == null) {
                    throw new SAXException(
                        Constants.getString("jsp.error.internal.filenotfound", 
                                            new Object[]{resourcePath}));
                }
                InputSource isrc =
                    new InputSource(input);
                //System.out.println("Resolve entity "  + publicId + " " + systemId + " " + resourcePath);
                return isrc;
            }
        }
        System.out.println("Resolve entity failed"  + publicId + " " + systemId );
        Constants.message("jsp.error.parse.xml.invalidPublicId",
                                new Object[]{publicId}, Logger.ERROR);
        return null;
    }
}

class MyErrorHandler implements ErrorHandler {
    public void warning(SAXParseException ex)
        throws SAXException
    {
        System.out.println("ParserUtils: warning " + ex );
        // We ignore warnings
    }

    public void error(SAXParseException ex)
        throws SAXException
    {
        throw ex;
    }

    public void fatalError(SAXParseException ex)
        throws SAXException
    {
        throw ex;
    }
}
