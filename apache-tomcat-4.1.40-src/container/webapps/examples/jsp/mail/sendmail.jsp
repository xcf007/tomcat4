<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<html>
<head>
<title>Example Mail Sending Form</title>
</head>
<body bgcolor="white">

<p>This page will send an electronic mail message via the
<code>javax.mail.Session</code> resource factory that is configured into
the JNDI context for this web application.  Before it can be used
successfully, you must take note of the following:</p>
<ul>
<li>The default configuration assumes that there is an SMTP server running
    on <strong>localhost</strong>.  If this is not the case, edit your
    <code>conf/server.xml</code> file and change the value for the
    <code>mail.smtp.host</code> parameter to the name of a host that provides
    SMTP service for your network.</li>
<li>The application logic assumes that no user authentication is required
    by your SMTP server before accepting mail messages to be sent.</li>
<li>All of the fields below are required.</li>
</ul>

<form method="POST" action="../../SendMailServlet">
<table>

  <tr>
    <th align="center" colspan="2">
      Enter The Email Message To Be Sent
    </th>
  </tr>

  <tr>
    <th align="right">From:</th>
    <td align="left">
      <input type="text" name="mailfrom" size="60">
    </td>
  </tr>

  <tr>
    <th align="right">To:</th>
    <td align="left">
      <input type="text" name="mailto" size="60">
    </td>
  </tr>

  <tr>
    <th align="right">Subject:</th>
    <td align="left">
      <input type="text" name="mailsubject" size="60">
    </td>
  </tr>

  <tr>
    <td colspan="2">
      <textarea name="mailcontent" rows="10" cols="80">
      </textarea>
    </td> 
  </tr>

  <tr>
    <td align="right">
      <input type="submit" value="Send">
    </td>
    <td align="left">
      <input type="reset" value="Reset">
    </td>
  </tr>

</table>
</form>

</body>
</html>
