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
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.*;
import java.net.JarURLConnection;
import java.net.*;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;

/**
 * A container for all tag libraries that are defined "globally"
 * for the web application.
 * 
 * Tag Libraries can be defined globally in one of two ways:
 *   1. Via <taglib> elements in web.xml:
 *      the uri and location of the tag-library are specified in
 *      the <taglib> element.
 *   2. Via packaged jar files that contain .tld files
 *      within the META-INF directory, or some subdirectory
 *      of it. The taglib is 'global' if it has the <uri>
 *      element defined.
 *
 * A mapping between the taglib URI and its associated TaglibraryInfoImpl
 * is maintained in this container.
 * Actually, that's what we'd like to do. However, because of the
 * way the classes TagLibraryInfo and TagInfo have been defined,
 * it is not currently possible to share an instance of TagLibraryInfo
 * across page invocations. A bug has been submitted to the spec lead.
 * In the mean time, all we do is save the 'location' where the
 * TLD associated with a taglib URI can be found.
 *
 * When a JSP page has a taglib directive, the mappings in this container
 * are first searched (see method getLocation()).
 * If a mapping is found, then the location of the TLD is returned.
 * If no mapping is found, then the uri specified
 * in the taglib directive is to be interpreted as the location for
 * the TLD of this tag library.
 *
 * @author Pierre Delisle
 * @author Jan Luehe
 */

public class TldLocationsCache {

    /**
     * The types of URI one may specify for a tag library
     */
    public static final int ABS_URI = 0;
    public static final int ROOT_REL_URI = 1;
    public static final int NOROOT_REL_URI = 2;

    private static final String WEB_XML = "/WEB-INF/web.xml";
    
    /**
     * The mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    private Hashtable mappings;

    private Hashtable tlds;

    private boolean initialized;
    private ServletContext ctxt;

    //*********************************************************************
    // Constructor and Initilizations
    
    /**
     * Constructor.
     *
     * @param ctxt the servlet context of the web application in which Jasper 
     * is running
     */
    public TldLocationsCache(ServletContext ctxt) {
        this.ctxt = ctxt;
        mappings = new Hashtable();
        tlds = new Hashtable();
        initialized = false;
    }

    private void init() {
        if( initialized ) return;
        try {
            processWebDotXml();
            processJars();
            processTldsInFileSystem("/WEB-INF/");
            initialized = true;
        } catch (JasperException ex) {
            Constants.message("jsp.error.internal.tldinit",
                              new Object[] { ex.getMessage() },
                              Logger.ERROR);
        }
    }

    /*
     * Populates taglib map described in web.xml.
     */    
    private void processWebDotXml() throws JasperException {

        // Acquire an input stream to the web application deployment descriptor
        InputStream is = ctxt.getResourceAsStream(WEB_XML);
        if (is == null) {
            Constants.message("jsp.error.internal.filenotfound",
                              new Object[] {WEB_XML},
                              Logger.WARNING);
            return;
        }

        // Parse the web application deployment descriptor
        TreeNode webtld = new ParserUtils().parseXMLDocument(WEB_XML, is);

        Iterator taglibs = webtld.findChildren("taglib");
        while (taglibs.hasNext()) {

            // Parse the next <taglib> element
            TreeNode taglib = (TreeNode) taglibs.next();
            String tagUri = null;
            String tagLoc = null;
            TreeNode child = taglib.findChild("taglib-uri");
            if (child != null)
                tagUri = child.getBody();
            child = taglib.findChild("taglib-location");
            if (child != null)
                tagLoc = child.getBody();

            // Save this location if appropriate
            if (tagLoc == null)
                continue;
            if (uriType(tagLoc) == NOROOT_REL_URI)
                tagLoc = "/WEB-INF/" + tagLoc;
            String tagLoc2 = null;
            if (tagLoc.endsWith(".jar"))
                tagLoc2 = "META-INF/taglib.tld";
            mappings.put(tagUri, new String[] {tagLoc, tagLoc2});
        }
    }

    /**
     * Processes any JAR files contained in this web application's
     * WEB-INF/lib directory.
     */
    private void processJars() throws JasperException {
        Set libSet = ctxt.getResourcePaths("/WEB-INF/lib");
        if (libSet != null) {
            Iterator it = libSet.iterator();
            while (it.hasNext()) {
                String resourcePath = (String) it.next();
                if (resourcePath.endsWith(".jar")) 
                    processTldsInJar(resourcePath);
            }
        }
    }

    /**
     * Parses any TLD files located in the META-INF directory (or any 
     * subdirectory of it) of the JAR file at the given resource path, and adds
     * an implicit map entry to the taglib map for any TLD that has a <uri>
     * element.
     *
     * @param resourcePath Context-relative resource path
     */
    private void processTldsInJar(String resourcePath) throws JasperException {

        JarFile jarFile = null;
        InputStream stream = null;

        try {
            URL url = ctxt.getResource(resourcePath);
            if (url == null) return;
            url = new URL("jar:" + url.toString() + "!/");
            JarURLConnection conn =
                (JarURLConnection) url.openConnection();
            jarFile = conn.getJarFile();
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("META-INF/")) continue;
                if (!name.endsWith(".tld")) continue;
                stream = jarFile.getInputStream(entry);
                String uri = getUriFromTld(resourcePath, stream);
                // Add implicit map entry only if its uri is not already
                // present in the map
                if (uri != null && mappings.get(uri) == null) {
                    mappings.put(uri, new String[]{ resourcePath, name });
                }
            }

        } catch (Exception ex) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {}
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {}
            }
            throw new JasperException(ex);
        }
    }

    /*
     * Searches the filesystem under /WEB-INF for any TLD files, and adds
     * an implicit map entry to the taglib map for any TLD that has a <uri>
     * element.
     */
    private void processTldsInFileSystem(String startPath)
            throws JasperException {

        Set dirList = ctxt.getResourcePaths(startPath);
        if (dirList != null) {
            Iterator it = dirList.iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                if (path.endsWith("/")) {
                    processTldsInFileSystem(path);
                }
                if (!path.endsWith(".tld")) {
                    continue;
                }
                InputStream stream = ctxt.getResourceAsStream(path);
                String uri = null;
                try {
                    uri = getUriFromTld(path, stream);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable t) {
                            // do nothing
                        }
                    }
                }
                // Add implicit map entry only if its uri is not already
                // present in the map
                if (uri != null && mappings.get(uri) == null) {
                    mappings.put(uri, new String[] { path, null });
                }
            }
        }
    }

    /*
     * Returns the value of the uri element of the given TLD, or null if the
     * given TLD does not contain any such element.
     */
    private String getUriFromTld(String resourcePath, InputStream in) 
        throws JasperException
    {
        // Parse the tag library descriptor at the specified resource path
        TreeNode tld = new ParserUtils().parseXMLDocument(resourcePath, in);
        TreeNode uri = tld.findChild("uri");
        if (uri != null) {
            String body = uri.getBody();
            if (body != null)
                return body;
        }

        return null;
    }

    //*********************************************************************
    // Accessors

    /**
     * Get the 'location' of the TLD associated with 
     * a given taglib 'uri'.
     * 
     * @return An array of two Strings. The first one is
     * real path to the TLD. If the path to the TLD points
     * to a jar file, then the second string is the
     * name of the entry for the TLD in the jar file.
     * Returns null if the uri is not associated to
     * a tag library 'exposed' in the web application.
     * A tag library is 'exposed' either explicitely in 
     * web.xml or implicitely via the uri tag in the TLD 
     * of a taglib deployed in a jar file (WEB-INF/lib).
     */
    public String[] getLocation(String uri) 
        throws JasperException
    {
        if( ! initialized ) init();
        return (String[])mappings.get(uri);
    }

    //*********************************************************************
    // Utility methods

    /** 
     * Returns the type of a URI:
     *     ABS_URI
     *     ROOT_REL_URI
     *     NOROOT_REL_URI
     */
    public static int uriType(String uri) {
        if (uri.indexOf(':') != -1) {
            return ABS_URI;
        } else if (uri.startsWith("/")) {
            return ROOT_REL_URI;
        } else {
            return NOROOT_REL_URI;
        }
    }

    public TagLibraryInfo getTagLibraryInfo(String uri) {
        if (!initialized) init();
        return (TagLibraryInfo) tlds.get(uri);
    }

    public void addTagLibraryInfo(String uri, TagLibraryInfo tld) {
        if (!initialized) init();
        tlds.put(uri, tld);
    }
}
