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
package org.apache.jasper.logging;

import java.io.IOException;
import javax.servlet.ServletContext;

/**
 * Trivial logger that sends all messages to the default sink.  To
 * change default sink, call Logger.setDefaultSink(Writer)
 *
 * @author Alex Chaffee (alex@jguru.com)
 * @since  Tomcat 3.1
 **/
public class DefaultLogger extends Logger {

    static private char[] newline;
    static {
        String separator = System.getProperty("line.separator", "\n");
        newline = separator.toCharArray();
    }
    

    /**
     * Default constructor leaves the debug output going to the
     * default sink.
     */
    public DefaultLogger() {
        super();
    }


    /**
     * The servlet context we are associated with.
     */
    protected ServletContext servletContext;


    /**
     * Construct a logger that writes output to the servlet context log
     * for the current web application.
     *
     * @param servletContext The servlet context for our web application
     */
    public DefaultLogger(ServletContext servletContext) {
        super();
        this.servletContext = servletContext;
    }



    /**
     * Prints log message to default sink
     * 
     * @param        message                the message to log.
     */
    protected void realLog(String message) {
        if (servletContext != null) {
            servletContext.log(message);
            return;
        }
        try {
            defaultSink.write(message);
            defaultSink.write(newline);
            flush();
        }
        catch (IOException e) {
            bad(e, message, null);
        }
    }
    
    /**
     * Prints log message to default sink
     * 
     * @param        message                the message to log.
     * @param        t                the exception that was thrown.
     */
    protected void realLog(String message, Throwable t) {
        if (servletContext != null) {
            servletContext.log(message, t);
            return;
        }
        try {
            defaultSink.write(message);
            defaultSink.write(newline);
            defaultSink.write(throwableToString(t));
            defaultSink.write(newline);
            flush();
        }
        catch (IOException e) {
            bad(e, message, t);
        }
    }

    private void bad(Throwable t1, String message, Throwable t2)
    {
        System.err.println("Default sink is unwritable! Reason:");
        if (t1 != null) t1.printStackTrace();
        if (message != null) System.err.println(message);
        if (t2 != null) t2.printStackTrace();
    }        
    
    /**
     * Flush the log. 
     */
    public void flush() {
        try {
            defaultSink.flush();
        }
        catch (IOException e) {
        }
    }    
}
