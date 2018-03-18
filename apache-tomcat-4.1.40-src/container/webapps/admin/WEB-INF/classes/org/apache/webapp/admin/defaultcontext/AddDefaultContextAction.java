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

package org.apache.webapp.admin.defaultcontext;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.Lists;
import org.apache.webapp.admin.TomcatTreeBuilder;
/**
 * The <code>Action</code> that sets up <em>Add DefaultContext</em> transactions.
 *
 * @author Amy Roh
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class AddDefaultContextAction extends Action {
    
    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;
    
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException, ServletException {
        
        // Acquire the resources that we need
        HttpSession session = request.getSession();
        if (resources == null) {
            resources = getResources(request);
        }
        
        // Fill in the form values for display and editing
        DefaultContextForm defaultContextFm = new DefaultContextForm();
        session.setAttribute("defaultContextForm", defaultContextFm);
        defaultContextFm.setAdminAction("Create");
        defaultContextFm.setObjectName("");
        String service = request.getParameter("serviceName");
        String parent = request.getParameter("parent");
        String defaultContext = null;
        if (service != null) {
            defaultContext = TomcatTreeBuilder.DEFAULTCONTEXT_TYPE +
                            ",service=" + service;
            parent = TomcatTreeBuilder.SERVICE_TYPE + ",name=" + service;
            defaultContextFm.setParentObjectName(parent);
        } else if (parent != null) {
            defaultContextFm.setParentObjectName(parent);
            int position = parent.indexOf(",");
            defaultContext = TomcatTreeBuilder.DEFAULTCONTEXT_TYPE +
                            parent.substring(position, parent.length());
        }
        defaultContextFm.setObjectName(defaultContext);                        
        int position = defaultContext.indexOf(",");
        String loader = TomcatTreeBuilder.LOADER_TYPE + 
                defaultContext.substring(position, defaultContext.length());
        String manager = TomcatTreeBuilder.MANAGER_TYPE + 
                defaultContext.substring(position, defaultContext.length());
        defaultContextFm.setLoaderObjectName(loader);
        defaultContextFm.setManagerObjectName(manager); 
        defaultContextFm.setNodeLabel("");
        defaultContextFm.setCookies("true");
        defaultContextFm.setCrossContext("true");
        defaultContextFm.setReloadable("false");
        defaultContextFm.setSwallowOutput("false");
        defaultContextFm.setUseNaming("true");
        //loader initialization
        defaultContextFm.setLdrCheckInterval("15");
        defaultContextFm.setLdrDebugLvl("0");
        defaultContextFm.setLdrReloadable("false");
        //manager initialization
        defaultContextFm.setMgrCheckInterval("60");
        defaultContextFm.setMgrDebugLvl("0");
        defaultContextFm.setMgrMaxSessions("-1");
        defaultContextFm.setMgrSessionIDInit("");
        
        defaultContextFm.setDebugLvlVals(Lists.getDebugLevels());
        defaultContextFm.setBooleanVals(Lists.getBooleanValues());        
        
        // Forward to the context display page
        return (mapping.findForward("DefaultContext"));
        
    }    
}
