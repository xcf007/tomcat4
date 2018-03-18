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

package org.apache.webapp.admin.logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.LabelValueBean;
import org.apache.webapp.admin.Lists;

/**
 * The <code>Action</code> that sets up <em>Add Logger</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public class AddLoggerAction extends Action {
        
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
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException, ServletException {

        // Acquire the resources that we need
        HttpSession session = request.getSession();
        if (resources == null) {
            resources = getResources(request);
        }
        
        // Fill in the form values for display and editing
        LoggerForm loggerFm = new LoggerForm();
        session.setAttribute("loggerForm", loggerFm);
        loggerFm.setAdminAction("Create");
        loggerFm.setObjectName("");
        String parent = request.getParameter("parent");
        loggerFm.setParentObjectName(parent);
        String type = request.getParameter("type");
        if (type == null)
            type = "FileLogger";    // default type is FileLogger
        loggerFm.setLoggerType(type);
        loggerFm.setDebugLvl("0");
        loggerFm.setDebugLvlVals(Lists.getDebugLevels());
        loggerFm.setVerbosityLvlVals(Lists.getVerbosityLevels());        
        loggerFm.setBooleanVals(Lists.getBooleanValues());        
      
        String loggerTypes[] = new String[3];
        loggerTypes[0] = "FileLogger";
        loggerTypes[1] = "SystemErrLogger";
        loggerTypes[2] = "SystemOutLogger";
        
        ArrayList types = new ArrayList();    
        // the first element in the select list should be the type selected
        types.add(new LabelValueBean(type,
                "AddLogger.do?parent=" + URLEncoder.encode(parent) 
                + "&type=" + type));        
        for (int i=0; i< loggerTypes.length; i++) {
            if (!type.equalsIgnoreCase(loggerTypes[i])) {
                types.add(new LabelValueBean(loggerTypes[i],
                "AddLogger.do?parent=" + URLEncoder.encode(parent) 
                + "&type=" + loggerTypes[i]));        
            }
        }
        loggerFm.setLoggerTypeVals(types);
        
        // Forward to the logger display page
        return (mapping.findForward("Logger"));
        
    }
    
}
