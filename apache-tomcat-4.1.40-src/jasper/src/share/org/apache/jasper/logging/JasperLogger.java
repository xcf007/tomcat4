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

import java.io.Writer;
import javax.servlet.ServletContext;

import org.apache.jasper.util.Queue;

/**
 * A real implementation of the Logger abstraction. 
 *
 * @author Anil V (akv@eng.sun.com)
 * @since  Tomcat 3.1
 */
public class JasperLogger extends Logger {
    
    /**
     * This is an entry that is created in response to every
     * Logger.log(...) call.
     */
    class LogEntry {
        String logName;
        long date;
        String message;
        Throwable t;
        
        LogEntry(String message, Throwable t) {
            // avoid expensive system call
            if (JasperLogger.this.timestamp)
                this.date = System.currentTimeMillis();
            this.message = message;
            this.t = t;
        }

        /**
         * Get the writer into which this log entry needs to be
         * written into. 
         */
        Writer getWriter() {
            return JasperLogger.this.sink;
        }

        /**
         * Format the log message nicely into a string.
         */
        public String toString() {
            
            StringBuffer val = new StringBuffer();

            // custom output, now with timestamp
            // does anyone actually use non-custom logs?

            if( !JasperLogger.this.custom ) {
                val.append("<");
                val.append(JasperLogger.this.getName());
                val.append("> ");
            }

            if (JasperLogger.this.timestamp) {
                formatTimestamp( date, val );
                val.append(" - ");
            }

            if (message != null) {
                val.append(message);
            }
            
            if (t != null) {
                val.append(" - ");
                val.append(throwableToString( t ));
            }

            if( !JasperLogger.this.custom ) {
                val.append("</");
                val.append(JasperLogger.this.getName());
                val.append("> ");
            }

            return val.toString();            
        }
    }


    /**
     * Just one daemon and one queue for all Logger instances.. 
     */
    static LogDaemon logDaemon = null;
    static Queue     logQueue  = null;

    ServletContext servletContext = null;

    public JasperLogger(ServletContext servletContext) {
        this.servletContext = servletContext;
        init();
    }

    public JasperLogger() {
        init();
    }

    private void init() {
        if (logDaemon == null || logQueue == null) {
            logQueue = new Queue();
            LogDaemon logDaemon = new LogDaemon(logQueue, servletContext);
            logDaemon.start();               
        }
    }
    
    /**
     * Adds a log message to the queue and returns immediately. The
     * logger daemon thread will pick it up later and actually print
     * it out.
     * 
     * @param        message                the message to log.
     */
    protected void realLog(String message) {
        logQueue.put(new LogEntry(message, null));
    }
    
    /**
     * Adds a log message and stack trace to the queue and returns
     * immediately. The logger daemon thread will pick it up later and
     * actually print it out. 
     *
     * @param        message                the message to log. 
     * @param        t                the exception that was thrown.
     */
    protected void realLog(String message, Throwable t) {
        logQueue.put(new LogEntry(message, t));
    }
    
    /**
     * Flush the log. 
     */
    public void flush() {
        logDaemon.flush();
    }

    public String toString() {
        return "JasperLogger(" + getName() + ", " + getPath() + ")";
    }
    
}

/**
 * The daemon thread that looks in a queue and if it is not empty
 * writes out everything in the queue to the sink.
 */
class LogDaemon extends Thread {
    LogDaemon(Queue logQueue, ServletContext servletContext) {
        this.logQueue = logQueue;
        this.servletContext = servletContext;
        setDaemon(true);
    }

    static char[] newline;
    static String separator;
    static {
        separator = System.getProperty("line.separator", "\n");
        newline = separator.toCharArray();
    }
    
    Runnable flusher = new Runnable() {
        public void run() {
            do {
                JasperLogger.LogEntry logEntry =
                    (JasperLogger.LogEntry) LogDaemon.this.logQueue.pull();
                if (servletContext != null) {
                    servletContext.log(logEntry.toString());
                    servletContext.log(separator);
                } else {
                    Writer writer = logEntry.getWriter();
                    if (writer != null) {
                        try {
                            writer.write(logEntry.toString());
                            writer.write(newline);
                            writer.flush();
                        } catch (Exception ex) { // IOException
                            ex.printStackTrace(); // nowhere else to write it
                        }
                    }
                }
            } while (!LogDaemon.this.logQueue.isEmpty());
        }};

    public void run() {
        while (true)
            flusher.run();
    }

    public void flush() {
        Thread workerThread = new Thread(flusher);
        workerThread.start();
    }

    private Queue logQueue;
    private ServletContext servletContext;
}
