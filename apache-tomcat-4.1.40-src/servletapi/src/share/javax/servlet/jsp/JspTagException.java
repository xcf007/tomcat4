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
package javax.servlet.jsp;

/**
 * Exception to be used by a Tag Handler to indicate some unrecoverable
 * error.
 * This error is to be caught by the top level of the JSP page and will result
 * in an error page.
 */

public class JspTagException extends JspException {
    /**
     * Constructor with a message.
     */
    public JspTagException(String msg) {
	super(msg);
    }

    /**
     * No message
     */
    public JspTagException() {
	super();
    }
}
