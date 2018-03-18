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

import org.apache.jasper.JasperException;

/**
 * Collect info about the page and nodes, and make them availabe through
 * the PageInfo object.
 *
 * @author Kin-man Chung
 */

public class Collector {

    /**
     * A visitor for collection info on the page
     * Info collected so far:
     *   Maximum tag nestings.
     *   Whether a page or a tag element (and its body) contains any scripting
     *       elements.
     */
    static class CollectVisitor extends Node.Visitor {

        private int maxTagNesting = 0;
        private int curTagNesting = 0;
        private boolean scriptingElementSeen = false;
        private boolean usebeanSeen = false;
        private boolean includeActionSeen = false;
        private boolean setPropertySeen = false;
        private boolean hasScriptingVars = false;

        public void visit(Node.ParamAction n) throws JasperException {
            if (n.getValue().isExpression()) {
                scriptingElementSeen = true;
            }
        }

        public void visit(Node.IncludeAction n) throws JasperException {
            if (n.getPage().isExpression()) {
                scriptingElementSeen = true;
            }
            includeActionSeen = true;
            visitBody(n);
        }

        public void visit(Node.ForwardAction n) throws JasperException {
            if (n.getPage().isExpression()) {
                scriptingElementSeen = true;
            }
            visitBody(n);
        }

        public void visit(Node.SetProperty n) throws JasperException {
            if (n.getValue() != null && n.getValue().isExpression()) {
                scriptingElementSeen = true;
            }
            setPropertySeen = true;
        }

        public void visit(Node.UseBean n) throws JasperException {
            if (n.getBeanName() != null && n.getBeanName().isExpression()) {
                scriptingElementSeen = true;
            }
            usebeanSeen = true;
            visitBody(n);
        }

        public void visit(Node.PlugIn n) throws JasperException {
            if (n.getHeight() != null && n.getHeight().isExpression()) {
                scriptingElementSeen = true;
            }
            if (n.getWidth() != null && n.getWidth().isExpression()) {
                scriptingElementSeen = true;
            }
            visitBody(n);
        }

        public void visit(Node.CustomTag n) throws JasperException {

            curTagNesting++;
            if (curTagNesting > maxTagNesting) {
                maxTagNesting = curTagNesting;
            }

            // save values collected so far
            boolean scriptingElementSeenSave = scriptingElementSeen;
            scriptingElementSeen = false;
            boolean usebeanSeenSave = usebeanSeen;
            usebeanSeen = false;
            boolean includeActionSeenSave = includeActionSeen;
            includeActionSeen = false;
            boolean setPropertySeenSave = setPropertySeen;
            setPropertySeen = false;
            boolean hasScriptingVarsSave = hasScriptingVars;
            hasScriptingVars = false;

            // Scan attribute list for expressions
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i].isExpression()) {
                    scriptingElementSeen = true;
                    break;
                }
            }

            visitBody(n);

            if (!hasScriptingVars) {
                // For some reason, varInfos is null when var is not defined
                // in TEI, but tagVarInfos is empty array when var is not
                // defined in tld.
                hasScriptingVars = n.getVariableInfos() != null || 
                        (n.getTagVariableInfos() != null
                         && n.getTagVariableInfos().length > 0);
            }

            // Record if the tag element and its body contains any scriptlet.
            n.setScriptless(! scriptingElementSeen);
            n.setHasUsebean(usebeanSeen);
            n.setHasIncludeAction(includeActionSeen);
            n.setHasSetProperty(setPropertySeen);
            n.setHasScriptingVars(hasScriptingVars);

            // Propagate value of scriptingElementSeen up.
            scriptingElementSeen = scriptingElementSeen || scriptingElementSeenSave;
            usebeanSeen = usebeanSeen || usebeanSeenSave;
            setPropertySeen = setPropertySeen || setPropertySeenSave;
            includeActionSeen = includeActionSeen || includeActionSeenSave;
            hasScriptingVars = hasScriptingVars || hasScriptingVarsSave;

            curTagNesting--;
        }

        public void visit(Node.Declaration n) throws JasperException {
            scriptingElementSeen = true;
        }

        public void visit(Node.Expression n) throws JasperException {
            scriptingElementSeen = true;
        }

        public void visit(Node.Scriptlet n) throws JasperException {
            scriptingElementSeen = true;
        }

        public void updatePageInfo(PageInfo pageInfo) {
            pageInfo.setMaxTagNesting(maxTagNesting);
            pageInfo.setScriptless(! scriptingElementSeen);
        }
    }

    public static void collect(Compiler compiler, Node.Nodes page)
                throws JasperException {

        CollectVisitor collectVisitor = new CollectVisitor();
        page.visit(collectVisitor);
        collectVisitor.updatePageInfo(compiler.getPageInfo());

    }
}

