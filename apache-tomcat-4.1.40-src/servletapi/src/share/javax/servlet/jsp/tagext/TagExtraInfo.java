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

/**
 * Optional class provided by the tag library author to describe additional
 * translation-time information not described in the TLD.
 * The TagExtraInfo class is mentioned in the Tag Library Descriptor file (TLD).
 *
 * <p>
 * This class can be used:
 * <ul>
 * <li> to indicate that the tag defines scripting variables
 * <li> to perform translation-time validation of the tag attributes.
 * </ul>
 *
 * <p>
 * It is the responsibility of the JSP translator that the initial value
 * to be returned by calls to getTagInfo() corresponds to a TagInfo
 * object for the tag being translated. If an explicit call to
 * setTagInfo() is done, then the object passed will be returned in
 * subsequent calls to getTagInfo().
 * 
 * <p>
 * The only way to affect the value returned by getTagInfo()
 * is through a setTagInfo() call, and thus, TagExtraInfo.setTagInfo() is
 * to be called by the JSP translator, with a TagInfo object that
 * corresponds to the tag being translated. The call should happen before
 * any invocation on isValid() and before any invocation on
 * getVariableInfo().
 */

public abstract class TagExtraInfo {

    /**
     * information on scripting variables defined by the tag associated with
     * this TagExtraInfo instance.
     * Request-time attributes are indicated as such in the TagData parameter.
     *
     * @param data The TagData instance.
     * @return An array of VariableInfo data.
     */
    public VariableInfo[] getVariableInfo(TagData data) {
	return new VariableInfo[0];
    }

    /**
     * Translation-time validation of the attributes. 
     * Request-time attributes are indicated as such in the TagData parameter.
     *
     * @param data The TagData instance.
     * @return Whether this tag instance is valid.
     */

    public boolean isValid(TagData data) {
	return true;
    }

    /**
     * Set the TagInfo for this class.
     *
     * @param tagInfo The TagInfo this instance is extending
     */
    public final void setTagInfo(TagInfo tagInfo) {
	this.tagInfo = tagInfo;
    }

    /**
     * Get the TagInfo for this class.
     *
     * @return the taginfo instance this instance is extending
     */
    public final TagInfo getTagInfo() {
	return tagInfo;
    }
    
    // private data
    private TagInfo tagInfo;
}

