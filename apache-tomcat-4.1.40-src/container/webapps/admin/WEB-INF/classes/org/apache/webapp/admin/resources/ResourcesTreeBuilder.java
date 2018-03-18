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

package org.apache.webapp.admin.resources;

import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TreeBuilder;
import org.apache.webapp.admin.TreeControl;
import org.apache.webapp.admin.TreeControlNode;


/**
 * Implementation of <code>TreeBuilder</code> that adds the nodes required
 * for administering the resources (data sources).
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 * @since 4.1
 */

public class ResourcesTreeBuilder implements TreeBuilder {


    // ----------------------------------------------------- Instance Variables


    // ---------------------------------------------------- TreeBuilder Methods


    /**
     * Add the required nodes to the specified <code>treeControl</code>
     * instance.
     *
     * @param treeControl The <code>TreeControl</code> to which we should
     *  add our nodes
     * @param servlet The controller servlet for the admin application
     * @param request The servlet request we are processing
     */
    public void buildTree(TreeControl treeControl,
                          ApplicationServlet servlet,
                          HttpServletRequest request) {

        MessageResources resources = (MessageResources)
            servlet.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        addSubtree(treeControl.getRoot(), resources);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Add the subtree of nodes required for user administration.
     *
     * @param root The root node of our tree control
     * @param resources The MessageResources for our localized messages
     *  messages
     */
    protected void addSubtree(TreeControlNode root,
                              MessageResources resources) {

        TreeControlNode subtree = new TreeControlNode
            ("Global Resource Administration",
             "folder_16_pad.gif",
             resources.getMessage("resources.treeBuilder.subtreeNode"),
             null,
             "content",
             true);        
        TreeControlNode datasources = new TreeControlNode
            ("Globally Administer Data Sources",
             "Datasource.gif",
             resources.getMessage("resources.treeBuilder.datasources"),
             "resources/listDataSources.do?resourcetype=Global&forward=" +
             URLEncoder.encode("DataSources List Setup"),
             "content",
             false);
        TreeControlNode mailsessions = new TreeControlNode
            ("Globally Administer Mail Sessions ",
            "Mailsession.gif",
            resources.getMessage("resources.treeBuilder.mailsessions"),
            "resources/listMailSessions.do?resourcetype=Global&forward=" +
            URLEncoder.encode("MailSessions List Setup"),
            "content",
            false);
        TreeControlNode userdbs = new TreeControlNode
            ("Globally Administer UserDatabase Entries",
             "Realm.gif",
             resources.getMessage("resources.treeBuilder.databases"),
             "resources/listUserDatabases.do?forward=" +
             URLEncoder.encode("UserDatabases List Setup"),
             "content",
             false);
        TreeControlNode envs = new TreeControlNode
            ("Globally Administer Environment Entries",
             "EnvironmentEntries.gif",
            resources.getMessage("resources.env.entries"),
            "resources/listEnvEntries.do?resourcetype=Global&forward=" +
            URLEncoder.encode("EnvEntries List Setup"),
            "content",
            false);
        root.addChild(subtree);
        subtree.addChild(datasources);
        subtree.addChild(mailsessions);
        subtree.addChild(envs);
        subtree.addChild(userdbs);
    }

}
