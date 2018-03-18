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

public interface TagConstants {
    public static final String JSP_ROOT_TAG = "jsp:root";
    public static final String JSP_ROOT_TAG_END = "</jsp:root>";
    public static final String JSP_PAGE_DIRECTIVE_TAG = "jsp:directive.page";
    public static final String JSP_INCLUDE_DIRECTIVE_TAG =
            "jsp:directive.include";
    public static final String JSP_DECLARATION_TAG = "jsp:declaration";
    public static final String JSP_DECLARATION_TAG_START = "<jsp:declaration>";
    public static final String JSP_DECLARATION_TAG_END = "</jsp:declaration>";
    public static final String JSP_SCRIPTLET_TAG = "jsp:scriptlet";
    public static final String JSP_SCRIPTLET_TAG_START = "<jsp:scriptlet>";
    public static final String JSP_SCRIPTLET_TAG_END = "</jsp:scriptlet>";
    public static final String JSP_EXPRESSION_TAG = "jsp:expression";
    public static final String JSP_EXPRESSION_TAG_START = "<jsp:expression>";
    public static final String JSP_EXPRESSION_TAG_END = "</jsp:expression>";
    public static final String JSP_USE_BEAN_TAG = "jsp:useBean";
    public static final String JSP_SET_PROPERTY_TAG = "jsp:setProperty";
    public static final String JSP_GET_PROPERTY_TAG = "jsp:getProperty";
    public static final String JSP_INCLUDE_TAG = "jsp:include";
    public static final String JSP_FORWARD_TAG = "jsp:forward";
    public static final String JSP_PARAM_TAG = "jsp:param";
    public static final String JSP_PARAMS_TAG = "jsp:params";
    public static final String JSP_PLUGIN_TAG = "jsp:plugin";
    public static final String JSP_FALLBACK_TAG = "jsp:fallback";
    public static final String JSP_TEXT_TAG = "jsp:text";
    public static final String JSP_TEXT_TAG_START = "<jsp:text>";
    public static final String JSP_TEXT_TAG_END = "</jsp:text>";
}
