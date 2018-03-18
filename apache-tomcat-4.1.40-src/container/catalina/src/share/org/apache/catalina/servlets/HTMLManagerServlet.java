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


package org.apache.catalina.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
* Servlet that enables remote management of the web applications installed
* within the same virtual host as this web application is.  Normally, this
* functionality will be protected by a security constraint in the web
* application deployment descriptor.  However, this requirement can be
* relaxed during testing.
* <p>
* The difference between the <code>ManagerServlet</code> and this
* Servlet is that this Servlet prints out a HTML interface which
* makes it easier to administrate.
* <p>
* However if you use a software that parses the output of
* <code>ManagerServlet</code you won't be able to upgrade
* to this Servlet since the output are not in the
* same format ar from <code>ManagerServlet</code>
*
* @author Bip Thelin
* @author Malcolm Edgar
* @author Glenn L. Nielsen
* @version $Revision: 547085 $, $Date: 2007-06-14 03:13:59 +0100 (Thu, 14 Jun 2007) $
* @see ManagerServlet
*/

public final class HTMLManagerServlet extends ManagerServlet {

    // --------------------------------------------------------- Public Methods

    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

        // Identify the request parameters that we need
        String command = request.getPathInfo();

        String path = request.getParameter("path");
        String installPath = request.getParameter("installPath");
        String installConfig = request.getParameter("installConfig");
        String installWar = request.getParameter("installWar");

        // Prepare our output writer to generate the response message
        Locale locale = Locale.getDefault();
        String charset = context.getCharsetMapper().getCharset(locale);
        response.setLocale(locale);
        response.setContentType("text/html; charset=" + charset);

        String message = "";
        // Process the requested command
        if (command == null || command.equals("/")) {
        } else if (command.equals("/install")) {
            message = install(installConfig, installPath, installWar);
        } else if (command.equals("/list")) {
        } else if (command.equals("/reload")) {
            message = reload(path);
        } else if (command.equals("/remove")) {
            message = remove(path);
        } else if (command.equals("/sessions")) {
            message = sessions(path);
        } else if (command.equals("/start")) {
            message = start(path);
        } else if (command.equals("/stop")) {
            message = stop(path);
        } else {
            message =
                sm.getString("managerServlet.unknownCommand",command);
        }

        list(request, response, message);
    }

    /**
     * Process a POST request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

        // Identify the request parameters that we need
        String command = request.getPathInfo();

        if (command == null || !command.equals("/upload")) {
            doGet(request,response);
            return;
        }

        // Prepare our output writer to generate the response message
        Locale locale = Locale.getDefault();
        String charset = context.getCharsetMapper().getCharset(locale);
        response.setLocale(locale);
        response.setContentType("text/html; charset=" + charset);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        boolean uploadFailed = true;
        
        // Create a new file upload handler
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(deployed);
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Set upload parameters
        upload.setSizeMax(-1);
    
        // Parse the request
        String war = null;
        FileItem warUpload = null;
        File xmlFile = null;
        
        // There is a possible race condition here. If liveDeploy is true it
        // means the deployer could start to deploy the app before we do it.
        synchronized(getLock()) { 
            try {
                List items = upload.parseRequest(request);
            
                // Process the uploaded fields
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
            
                    if (!item.isFormField()) {
                        if (item.getFieldName().equals("installWar") &&
                            warUpload == null) {
                            warUpload = item;
                        } else {
                            item.delete();
                        }
                    }
                }
                while(true) {
                    if (warUpload == null) {
                        writer.println(sm.getString
                            ("htmlManagerServlet.installUploadNoFile"));
                        break;
                    }
                    war = warUpload.getName();
                    if (!war.toLowerCase().endsWith(".war")) {
                        writer.println(sm.getString
                            ("htmlManagerServlet.installUploadNotWar",war));
                        break;
                    }
                    // Get the filename if uploaded name includes a path
                    if (war.lastIndexOf('\\') >= 0) {
                        war = war.substring(war.lastIndexOf('\\') + 1);
                    }
                    if (war.lastIndexOf('/') >= 0) {
                        war = war.substring(war.lastIndexOf('/') + 1);
                    }
                    
                    String xmlName = war.substring(0,war.length()-4) + ".xml";
                    
                    // Identify the appBase of the owning Host of this Context
                    // (if any)
                    String appBase = null;
                    File appBaseDir = null;
                    appBase = ((Host) context.getParent()).getAppBase();
                    appBaseDir = new File(appBase);
                    if (!appBaseDir.isAbsolute()) {
                        appBaseDir = new File(System.getProperty("catalina.base"),
                                              appBase);
                    }
                    File file = new File(appBaseDir,war);
                    if (file.exists()) {
                        writer.println(sm.getString
                            ("htmlManagerServlet.installUploadWarExists",war));
                        break;
                    }
                    warUpload.write(file);
                    try {
                        URL url = file.toURL();
                        war = url.toString();
                        war = "jar:" + war + "!/";
                    } catch(MalformedURLException e) {
                        file.delete();
                        throw e;
                    }
                    
                    // Extract the context.xml file, if any
                    xmlFile = new File(appBaseDir, xmlName);
                    extractXml(file, xmlFile);
                    
                    uploadFailed = false;
                    
                    break;
                }
            } catch(Exception e) {
                String message = sm.getString
                    ("htmlManagerServlet.installUploadFail", e.getMessage());
                log(message, e);
                writer.println(message);
            } finally {
                if (warUpload != null) {
                    warUpload.delete();
                }
                warUpload = null;
            }
        
            // Define the context.xml URL if present
            String xmlURL = null;
            if (xmlFile != null && xmlFile.exists()) {
                xmlURL = new String("file:" + xmlFile.getAbsolutePath());
            }
    
            // If there were no errors, install the WAR
            if (!uploadFailed) {
                install(writer, xmlURL, null, war);
            }
        }

        String message = stringWriter.toString();

        list(request, response, message);
    }

    /**
     * Install an application for the specified path from the specified
     * web application archive.
     *
     * @param config URL of the context configuration file to be installed
     * @param path Context path of the application to be installed
     * @param war URL of the web application archive to be installed
     * @return message String
     */
    protected String install(String config, String path, String war) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.install(printWriter, config, path, war);

        return stringWriter.toString();
    }

    /**
     * Render a HTML list of the currently active Contexts in our virtual host,
     * and memory and server status information.
     *
     * @param message a message to display
     */
    public void list(HttpServletRequest request,
                     HttpServletResponse response,
                     String message) throws IOException {

        if (debug >= 1)
            log("list: Listing contexts for virtual host '" +
                deployer.getName() + "'");

        PrintWriter writer = response.getWriter();

        // HTML Header Section
        writer.print(HTML_HEADER_SECTION);

        // Body Header Section
        Object[] args = new Object[2];
        args[0] = request.getContextPath();
        args[1] = sm.getString("htmlManagerServlet.title");
        writer.print(MessageFormat.format(BODY_HEADER_SECTION, args));

        // Message Section
        args = new Object[3];
        args[0] = sm.getString("htmlManagerServlet.messageLabel");
        if (message == null || message.length() == 0) {
            args[1] = "OK";
        } else {
            args[1] = RequestUtil.filter(message);
        }
        writer.print(MessageFormat.format(MESSAGE_SECTION, args));

        // Manager Section
        args = new Object[7];
        args[0] = sm.getString("htmlManagerServlet.manager");
        args[1] = response.encodeURL(request.getContextPath() + "/html/list");
        args[2] = sm.getString("htmlManagerServlet.list");
        args[3] = response.encodeURL
            (request.getContextPath() + "/" +
             sm.getString("htmlManagerServlet.helpHtmlManagerFile"));
        args[4] = sm.getString("htmlManagerServlet.helpHtmlManager");
        args[5] = response.encodeURL
            (request.getContextPath() + "/" +
             sm.getString("htmlManagerServlet.helpManagerFile"));
        args[6] = sm.getString("htmlManagerServlet.helpManager");
        writer.print(MessageFormat.format(MANAGER_SECTION, args));

        // Apps Header Section
        args = new Object[6];
        args[0] = sm.getString("htmlManagerServlet.appsTitle");
        args[1] = sm.getString("htmlManagerServlet.appsPath");
        args[2] = sm.getString("htmlManagerServlet.appsName");
        args[3] = sm.getString("htmlManagerServlet.appsAvailable");
        args[4] = sm.getString("htmlManagerServlet.appsSessions");
        args[5] = sm.getString("htmlManagerServlet.appsTasks");
        writer.print(MessageFormat.format(APPS_HEADER_SECTION, args));

        // Apps Row Section
        // Create sorted map of deployed applications context paths.
        String contextPaths[] = deployer.findDeployedApps();

        TreeMap sortedContextPathsMap = new TreeMap();

        for (int i = 0; i < contextPaths.length; i++) {
            String displayPath = contextPaths[i];
            sortedContextPathsMap.put(displayPath, contextPaths[i]);
        }

        String appsStart = sm.getString("htmlManagerServlet.appsStart");
        String appsStop = sm.getString("htmlManagerServlet.appsStop");
        String appsReload = sm.getString("htmlManagerServlet.appsReload");
        String appsRemove = sm.getString("htmlManagerServlet.appsRemove");

        Iterator iterator = sortedContextPathsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String displayPath = (String) entry.getKey();
            String contextPath = (String) entry.getKey();
            Context context = deployer.findDeployedApp(contextPath);
            if (displayPath.equals("")) {
                displayPath = "/";
            }

            if (context != null ) {
                args = new Object[5];
                args[0] = displayPath;
                args[1] = context.getDisplayName();
                if (args[1] == null) {
                    args[1] = "&nbsp;";
                }
                args[2] = new Boolean(context.getAvailable());
                args[3] = response.encodeURL
                    (request.getContextPath() +
                     "/html/sessions?path=" + displayPath);
                args[4] =
                    new Integer(context.getManager().findSessions().length);
                writer.print
                    (MessageFormat.format(APPS_ROW_DETAILS_SECTION, args));

                args = new Object[8];
                args[0] = response.encodeURL
                    (request.getContextPath() +
                     "/html/start?path=" + displayPath);
                args[1] = appsStart;
                args[2] = response.encodeURL
                    (request.getContextPath() +
                     "/html/stop?path=" + displayPath);
                args[3] = appsStop;
                args[4] = response.encodeURL
                    (request.getContextPath() +
                     "/html/reload?path=" + displayPath);
                args[5] = appsReload;
                args[6] = response.encodeURL
                    (request.getContextPath() +
                     "/html/remove?path=" + displayPath);
                args[7] = appsRemove;
                if (context.getPath().equals(this.context.getPath())) {
                    writer.print(MessageFormat.format(
                        MANAGER_APP_ROW_BUTTON_SECTION, args));
                } else if (context.getAvailable()) {
                    writer.print(MessageFormat.format(
                        STARTED_APPS_ROW_BUTTON_SECTION, args));
                } else {
                    writer.print(MessageFormat.format(
                        STOPPED_APPS_ROW_BUTTON_SECTION, args));
                }

            }
        }

        // Install Section
        args = new Object[7];
        args[0] = sm.getString("htmlManagerServlet.installTitle");
        args[1] = sm.getString("htmlManagerServlet.installServer");
        args[2] = response.encodeURL(request.getContextPath() + "/html/install");
        args[3] = sm.getString("htmlManagerServlet.installPath");
        args[4] = sm.getString("htmlManagerServlet.installConfig");
        args[5] = sm.getString("htmlManagerServlet.installWar");
        args[6] = sm.getString("htmlManagerServlet.installButton");
        writer.print(MessageFormat.format(INSTALL_SECTION, args));

        args = new Object[4];
        args[0] = sm.getString("htmlManagerServlet.installUpload");
        args[1] = response.encodeURL(request.getContextPath() + "/html/upload");
        args[2] = sm.getString("htmlManagerServlet.installUploadFile");
        args[3] = sm.getString("htmlManagerServlet.installButton");
        writer.print(MessageFormat.format(UPLOAD_SECTION, args));

        // Server Header Section
        args = new Object[7];
        args[0] = sm.getString("htmlManagerServlet.serverTitle");
        args[1] = sm.getString("htmlManagerServlet.serverVersion");
        args[2] = sm.getString("htmlManagerServlet.serverJVMVersion");
        args[3] = sm.getString("htmlManagerServlet.serverJVMVendor");
        args[4] = sm.getString("htmlManagerServlet.serverOSName");
        args[5] = sm.getString("htmlManagerServlet.serverOSVersion");
        args[6] = sm.getString("htmlManagerServlet.serverOSArch");
        writer.print(MessageFormat.format(SERVER_HEADER_SECTION, args));

        // Server Row Section
        args = new Object[6];
        args[0] = ServerInfo.getServerInfo();
        args[1] = System.getProperty("java.runtime.version");
        args[2] = System.getProperty("java.vm.vendor");
        args[3] = System.getProperty("os.name");
        args[4] = System.getProperty("os.version");
        args[5] = System.getProperty("os.arch");
        writer.print(MessageFormat.format(SERVER_ROW_SECTION, args));

        // HTML Tail Section
        writer.print(HTML_TAIL_SECTION);

        // Finish up the response
        writer.flush();
        writer.close();
    }

    /**
     * Reload the web application at the specified context path.
     *
     * @see ManagerServlet#reload(PrintWriter, String)
     *
     * @param path Context path of the application to be restarted
     * @return message String
     */
    protected String reload(String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.reload(printWriter, path);

        return stringWriter.toString();
    }

    /**
     * Remove the web application at the specified context path.
     *
     * @see ManagerServlet#remove(PrintWriter, String)
     *
     * @param path Context path of the application to be removed
     * @return message String
     */
    protected String remove(String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.remove(printWriter, path);

        return stringWriter.toString();
    }

    /**
     * Display session information and invoke list.
     *
     * @see ManagerServlet#sessions(PrintWriter, String)
     *
     * @param path Context path of the application to list session information
     * @return message String
     */
    public String sessions(String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.sessions(printWriter, path);

        return stringWriter.toString();
    }

    /**
     * Start the web application at the specified context path.
     *
     * @see ManagerServlet#start(PrintWriter, String)
     *
     * @param path Context path of the application to be started
     * @return message String
     */
    public String start(String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.start(printWriter, path);

        return stringWriter.toString();
    }

    /**
     * Stop the web application at the specified context path.
     *
     * @see ManagerServlet#stop(PrintWriter, String)
     *
     * @param path Context path of the application to be stopped
     * @return message String
     */
    protected String stop(String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.stop(printWriter, path);

        return stringWriter.toString();
    }

    
    // ------------------------------------------------------ Private Constants

    // These HTML sections are broken in relatively small sections, because of
    // limited number of subsitutions MessageFormat can process
    // (maximium of 10).

    private static final String HTML_HEADER_SECTION =
        "<html>\n" +
        "<head>\n" +
        "<style>\n" +
        "  table { width: 100%; }\n" +
        "  td.page-title {\n" +
        "    text-align: center;\n" +
        "    vertical-align: top;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    font-weight: bold;\n" +
        "    background: white;\n" +
        "    color: black;\n" +
        "  }\n" +
        "  td.title {\n" +
        "    text-align: left;\n" +
        "    vertical-align: top;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    font-style:italic;\n" +
        "    font-weight: bold;\n" +
        "    background: #D2A41C;\n" +
        "  }\n" +
        "  td.header-left {\n" +
        "    text-align: left;\n" +
        "    vertical-align: top;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    font-weight: bold;\n" +
        "    background: #FFDC75;\n" +
        "  }\n" +
        "  td.header-center {\n" +
        "    text-align: center;\n" +
        "    vertical-align: top;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    font-weight: bold;\n" +
        "    background: #FFDC75;\n" +
        "  }\n" +
        "  td.row-left {\n" +
        "    text-align: left;\n" +
        "    vertical-align: middle;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    color: black;\n" +
        "    background: white;\n" +
        "  }\n" +
        "  td.row-center {\n" +
        "    text-align: center;\n" +
        "    vertical-align: middle;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    color: black;\n" +
        "    background: white;\n" +
        "  }\n" +
        "  td.row-right {\n" +
        "    text-align: right;\n" +
        "    vertical-align: middle;\n" +
        "    font-family:verdana,sans-serif;\n" +
        "    color: black;\n" +
        "    background: white;\n" +
        "  }\n" +
        "</style>\n";

    private static final String BODY_HEADER_SECTION =
        "<title>{0}</title>\n" +
        "</head>\n" +
        "\n" +
        "<body bgcolor=\"#FFFFFF\">\n" +
        "\n" +
        "<table cellspacing=\"4\" width=\"100%\" border=\"0\">\n" +
        " <tr>\n" +
        "  <td colspan=\"2\">\n" +
        "   <a href=\"http://www.apache.org/\">\n" +
        "    <img border=\"0\" alt=\"The Apache Software Foundation\""+
        "         align=\"left\"\n src=\"{0}/images/asf-logo.gif\">\n" +
        "   </a>\n" +
        "   <a href=\"http://tomcat.apache.org/\">\n" +
        "    <img border=\"0\" alt=\"The Tomcat Servlet/JSP Container\"\n" +
        "         align=\"right\" src=\"{0}/images/tomcat.gif\">\n" +
        "   </a>\n" +
        "  </td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<hr size=\"1\" noshade=\"noshade\">\n" +
        "<table cellspacing=\"4\" width=\"100%\" border=\"0\">\n" +
        " <tr>\n" +
        "  <td class=\"page-title\" bordercolor=\"#000000\" " +
        "align=\"left\" nowrap>\n" +
        "   <font size=\"+2\">{1}</font>\n" +
        "  </td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";

    private static final String MESSAGE_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        " <tr>\n" +
        "  <td class=\"row-left\" width=\"10%\">" +
        "<small><b>{0}</b></small>&nbsp;</td>\n" +
        "  <td class=\"row-left\"><pre>{1}</pre></td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";

    private static final String MANAGER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"3\" class=\"title\">{0}</td>\n" +
        "</tr>\n" +
        " <tr>\n" +
        "  <td class=\"row-left\"><a href=\"{1}\">{2}</a></td>\n" +
        "  <td class=\"row-center\"><a href=\"{3}\">{4}</a></td>\n" +
        "  <td class=\"row-right\"><a href=\"{5}\">{6}</a></td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";

    private static final String APPS_HEADER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"5\" class=\"title\">{0}</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"header-left\"><small>{1}</small></td>\n" +
        " <td class=\"header-left\"><small>{2}</small></td>\n" +
        " <td class=\"header-center\"><small>{3}</small></td>\n" +
        " <td class=\"header-center\"><small>{4}</small></td>\n" +
        " <td class=\"header-center\"><small>{5}</small></td>\n" +
        "</tr>\n";

    private static final String APPS_ROW_DETAILS_SECTION =
        "<tr>\n" +
        " <td class=\"row-left\"><small><a href=\"{0}\">{0}</a>" +
        "</small></td>\n" +
        " <td class=\"row-left\"><small>{1}</small></td>\n" +
        " <td class=\"row-center\"><small>{2}</small></td>\n" +
        " <td class=\"row-center\">" +
        "<small><a href=\"{3}\">{4}</a></small></td>\n";

    private static final String MANAGER_APP_ROW_BUTTON_SECTION =
        " <td class=\"row-left\">\n" +
        "  <small>\n" +
        "  &nbsp;{1}&nbsp;\n" +
        "  &nbsp;{3}&nbsp;\n" +
        "  &nbsp;{5}&nbsp;\n" +
        "  &nbsp;{7}&nbsp;\n" +
        "  </small>\n" +
        " </td>\n" +
        "</tr>\n";

    private static final String STARTED_APPS_ROW_BUTTON_SECTION =
        " <td class=\"row-left\">\n" +
        "  <small>\n" +
        "  &nbsp;{1}&nbsp;\n" +
        "  &nbsp;<a href=\"{2}\">{3}</a>&nbsp;\n" +
        "  &nbsp;<a href=\"{4}\">{5}</a>&nbsp;\n" +
        "  &nbsp;<a href=\"{6}\">{7}</a>&nbsp;\n" +
        "  </small>\n" +
        " </td>\n" +
        "</tr>\n";

    private static final String STOPPED_APPS_ROW_BUTTON_SECTION =
        " <td class=\"row-left\">\n" +
        "  <small>\n" +
        "  &nbsp;<a href=\"{0}\">{1}</a>&nbsp;\n" +
        "  &nbsp;{3}&nbsp;\n" +
        "  &nbsp;{5}&nbsp;\n" +
        "  &nbsp;<a href=\"{6}\">{7}</a>&nbsp;\n" +
        "  </small>\n" +
        " </td>\n" +
        "</tr>\n";

    private static final String INSTALL_SECTION =
        "</table>\n" +
        "<br>\n" +
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"2\" class=\"title\">{0}</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td colspan=\"2\" class=\"header-left\"><small>{1}</small></td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td colspan=\"2\">\n" +
        "<form method=\"get\" action=\"{2}\">\n" +
        "<table cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  <small>{3}</small>\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"text\" name=\"installPath\" size=\"20\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  <small>{4}</small>\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"text\" name=\"installConfig\" size=\"20\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  <small>{5}</small>\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"text\" name=\"installWar\" size=\"40\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  &nbsp;\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"submit\" value=\"{6}\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "</table>\n" +
        "</form>\n" +
        "</td>\n" +
        "</tr>\n";

    private static final String UPLOAD_SECTION =
        "<tr>\n" +
        " <td colspan=\"2\" class=\"header-left\"><small>{0}</small></td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td colspan=\"2\">\n" +
        "<form action=\"{1}\" method=\"post\" " +
        "enctype=\"multipart/form-data\">\n" +
        "<table cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  <small>{2}</small>\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"file\" name=\"installWar\" size=\"40\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  &nbsp;\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"submit\" value=\"{3}\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "</table>\n" +
        "</form>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";

    private static final String SERVER_HEADER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"6\" class=\"title\">{0}</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"header-center\"><small>{1}</small></td>\n" +
        " <td class=\"header-center\"><small>{2}</small></td>\n" +
        " <td class=\"header-center\"><small>{3}</small></td>\n" +
        " <td class=\"header-center\"><small>{4}</small></td>\n" +
        " <td class=\"header-center\"><small>{5}</small></td>\n" +
        " <td class=\"header-center\"><small>{6}</small></td>\n" +
        "</tr>\n";

    private static final String SERVER_ROW_SECTION =
        "<tr>\n" +
        " <td class=\"row-center\"><small>{0}</small></td>\n" +
        " <td class=\"row-center\"><small>{1}</small></td>\n" +
        " <td class=\"row-center\"><small>{2}</small></td>\n" +
        " <td class=\"row-center\"><small>{3}</small></td>\n" +
        " <td class=\"row-center\"><small>{4}</small></td>\n" +
        " <td class=\"row-center\"><small>{5}</small></td>\n" +
        "</tr>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";

    private static final String HTML_TAIL_SECTION =
        "<hr size=\"1\" noshade=\"noshade\">\n" +
        "<center><font size=\"-1\" color=\"#525D76\">\n" +
        " <em>Copyright &copy; 1999-2002, Apache Software Foundation</em>" +
        "</font></center>\n" +
        "\n" +
        "</body>\n" +
        "</html>";
}
