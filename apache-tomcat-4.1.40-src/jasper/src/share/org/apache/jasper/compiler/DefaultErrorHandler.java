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
 * Default implementation of ErrorHandler interface.
 *
 * @author Jan Luehe
 */
class DefaultErrorHandler implements ErrorHandler {

    private ErrorDispatcher err;

    /*
     * Constructor.
     *
     * @param err Error dispatcher for localization support
     */
    DefaultErrorHandler(ErrorDispatcher err) {
        this.err = err;
    }

    /*
     * Processes the given JSP parse error.
     *
     * @param fname Name of the JSP file in which the parse error occurred
     * @param line Parse error line number
     * @param column Parse error column number
     * @param errMsg Parse error message
     * @param exception Parse exception
     */
    public void jspError(String fname, int line, int column, String errMsg,
                         Exception ex) throws JasperException {
        throw new JasperException(fname + "(" + line + "," + column + ")"
                                  + " " + errMsg, ex);
    }

    /*
     * Processes the given JSP parse error.
     *
     * @param errMsg Parse error message
     * @param exception Parse exception
     */
    public void jspError(String errMsg, Exception ex) throws JasperException {
        throw new JasperException(errMsg, ex);
    }

    /*
     * Processes the given javac compilation errors.
     *
     * @param details Array of JavacErrorDetail instances corresponding to the
     * compilation errors
     */
    public void javacError(JavacErrorDetail[] details) throws JasperException {

        Object[] args = null;
        StringBuffer buf = new StringBuffer();
        
        for (int i=0; i<details.length; i++) {
            args = new Object[] {
                new Integer(details[i].getJspBeginLineNumber()), 
                details[i].getJspFileName()
            };
            buf.append(err.getString("jsp.error.single.line.number", args));
            buf.append(err.getString("jsp.error.corresponding.servlet"));
            buf.append(details[i].getErrorMessage());
            buf.append('\n');
        }

        throw new JasperException(err.getString("jsp.error.unable.compile")
                                  + buf);
    }
}
