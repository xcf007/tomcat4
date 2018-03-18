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
 
package javax.servlet.jsp.tagext;

import javax.servlet.jsp.tagext.TagInfo;

/**
 * Translation-time information associated with a taglib directive, and its
 * underlying TLD file.
 *
 * Most of the information is directly from the TLD, except for
 * the prefix and the uri values used in the taglib directive
 *
 *
 */

abstract public class TagLibraryInfo {

    /**
     * Constructor.
     *
     * This will invoke the constructors for TagInfo, and TagAttributeInfo
     * after parsing the TLD file.
     *
     * @param prefix the prefix actually used by the taglib directive
     * @param uri the URI actually used by the taglib directive
     */

    protected TagLibraryInfo(String prefix, String uri) {
	this.prefix = prefix;
	this.uri    = uri;
    }


    // ==== methods accessing taglib information =======

    /**
     * The value of the uri attribute from the <%@ taglib directive for this library.
     *
     * @returns the value of the uri attribute
     */
   
    public String getURI() {
        return uri;
    }

    /**
     * The prefix assigned to this taglib from the <%taglib directive
     *
     * @returns the prefix assigned to this taglib from the <%taglib directive
     */

    public String getPrefixString() {
	return prefix;
    }

    // ==== methods using the TLD data =======

    /**
     * The preferred short name (prefix) as indicated in the TLD.
     * This may be used by authoring tools as the preferred prefix
     * to use when creating an include directive for this library.
     *
     * @returns the preferred short name for the library
     */
    public String getShortName() {
        return shortname;
    }

    /**
     * The "reliable" URN indicated in the TLD.
     * This may be used by authoring tools as a global identifier
     * (the uri attribute) to use when creating a taglib directive
     * for this library.
     *
     * @returns a reliable URN to a TLD like this
     */
    public String getReliableURN() {
        return urn;
    }


    /**
     * Information (documentation) for this TLD.
     *
     * @returns the info string for this tag lib
     */
   
    public String getInfoString() {
        return info;
    }


    /**
     * A string describing the required version of the JSP container.
     * 
     * @returns the (minimal) required version of the JSP container.
     * @seealso JspEngineInfo.
     */
   
    public String getRequiredVersion() {
        return jspversion;
    }


    /**
     * An array describing the tags that are defined in this tag library.
     *
     * @returns the tags defined in this tag lib
     */
   
    public TagInfo[] getTags() {
        return tags;
    }


    /**
     * Get the TagInfo for a given tag name, looking through all the
     * tags in this tag library.
     *
     * @param shortname The short name (no prefix) of the tag
     * @returns the TagInfo for that tag. 
     */

    public TagInfo getTag(String shortname) {
        TagInfo tags[] = getTags();

        if (tags == null || tags.length == 0) {
            System.err.println("No tags");
            return null;
        }

        for (int i=0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(shortname)) {
                return tags[i];
            }
        }
        return null;
    }


    // Protected fields

    protected String        prefix;
    protected String        uri;

    protected TagInfo[]     tags;

    // Tag Library Data
    protected String tlibversion; // required
    protected String jspversion;  // optional
    protected String shortname;   // required
    protected String urn;         // required
    protected String info;        // optional
}
