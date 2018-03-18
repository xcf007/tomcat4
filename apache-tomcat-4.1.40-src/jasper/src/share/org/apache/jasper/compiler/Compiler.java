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

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.Servlet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.Options;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.util.SystemLogHandler;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.apache.jasper.servlet.JspServletWrapper;

/**
 * Main JSP compiler class. This class uses Ant for compiling.
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Remy Maucherat
 */
public class Compiler {


    // ----------------------------------------------------------------- Static


    static {

        System.setErr(new SystemLogHandler(System.err));

    }

    // Some javac are not thread safe; use a lock to serialize compilation, 
    static Object javacLock = new Object();


    // ----------------------------------------------------- Instance Variables


    protected JspCompilationContext ctxt;

    private ErrorDispatcher errDispatcher;
    private PageInfo pageInfo;
    private JspServletWrapper jsw;
    private JasperAntLogger logger;

    protected Project project=null;

    protected Options options;

    protected Node.Nodes pageNodes;

    // ------------------------------------------------------------ Constructor


    public Compiler(JspCompilationContext ctxt) {
        this(ctxt, null);
    }


    public Compiler(JspCompilationContext ctxt, JspServletWrapper jsw) {
        this.jsw = jsw;
        this.ctxt = ctxt;
        this.options = ctxt.getOptions();
    }

    // Lazy eval - if we don't need to compile we probably don't need the project
    private Project getProject() {

        if( project!=null ) return project;

        // Initializing project
        project = new Project();
        // XXX We should use a specialized logger to redirect to jasperlog
        logger = new JasperAntLogger();
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);

        if( Constants.jasperLog.getVerbosityLevel() >= Logger.DEBUG ) {
            logger.setMessageOutputLevel( Project.MSG_VERBOSE );
        } else {
            logger.setMessageOutputLevel( Project.MSG_INFO );
        }
        project.addBuildListener( logger );
        if (System.getProperty("catalina.home") != null) {
            project.setBasedir( System.getProperty("catalina.home"));
        }

        if( options.getCompiler() != null ) {
            Constants.jasperLog.log("Compiler " + options.getCompiler(), Logger.INFORMATION);
            project.setProperty("build.compiler", options.getCompiler() );
        }
        project.init();
        return project;
    }

    class JasperAntLogger extends DefaultLogger {

        private StringBuffer reportBuf = new StringBuffer();

        protected void printMessage(final String message,
                                    final PrintStream stream,
                                    final int priority) {
        }

        protected void log(String message) {
            reportBuf.append(message);
            reportBuf.append(System.getProperty("line.separator"));
        }

        protected String getReport() {
            String report = reportBuf.toString();
            reportBuf.setLength(0);
            return report;
        }
    }

    // --------------------------------------------------------- Public Methods


    /** 
     * Compile the jsp file from the current engine context
     */
    public void generateJava()
        throws FileNotFoundException, JasperException, Exception
    {
        if (errDispatcher == null) {
            this.errDispatcher = new ErrorDispatcher();
        }

        // Setup page info area
        pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader()));

        String javaFileName = ctxt.getServletJavaFileName();
        ServletWriter writer = null;
        
        try {
            // Setup the ServletWriter
            String javaEncoding = ctxt.getOptions().getJavaEncoding();

            OutputStreamWriter osw = null; 
            try {
                osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
                                             javaEncoding);
            } catch (UnsupportedEncodingException ex) {
                errDispatcher.jspError("jsp.error.needAlternateJavaEncoding", javaEncoding);
            }

            writer = new ServletWriter(new PrintWriter(osw));
            ctxt.setWriter(writer);

            // Parse the file
            ParserController parserCtl = new ParserController(ctxt, this);
            pageNodes = parserCtl.parse(ctxt.getJspFile());

            // Validate and process attributes
            Validator.validate(this, pageNodes);

            // Dump out the page (for debugging)
            // Dumper.dump(pageNodes);

            // Collect page info
            Collector.collect(this, pageNodes);

            // Determine which custom tag needs to declare which scripting vars
            ScriptingVariabler.set(pageNodes);

            // Optimization: concatenate contiguous template texts.
            TextOptimizer.concatenate(this, pageNodes);
            
            // generate servlet .java file
            Generator.generate(writer, this, pageNodes);
            writer.close();
            writer = null;

            // The writer is only used during the compile, dereference
            // it in the JspCompilationContext when done to allow it
            // to be GC'd and save memory.
            ctxt.setWriter(null);
        } catch (Exception e) {
            if (writer != null) {
                try {
                    writer.close();
                    writer = null;
                } catch (Exception e1) {
                    // Do nothing
                }
            }
            // Remove the generated .java file
            new File(javaFileName).delete();
            throw e;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e2) {
                    // Do nothing
                }
            }
        }
    }

    /** 
     * Compile the jsp file from the current engine context
     */
    public void generateClass()
        throws FileNotFoundException, JasperException, Exception {

        if (errDispatcher == null) {
            this.errDispatcher = new ErrorDispatcher();
        }

        String javaEncoding = ctxt.getOptions().getJavaEncoding();
        String javaFileName = ctxt.getServletJavaFileName();
        String classpath = ctxt.getClassPath(); 

        StringBuffer info=new StringBuffer();
        info.append("Compile: javaFileName=" + javaFileName + "\n" );
        info.append("    classpath=" + classpath + "\n" );
        
        String sep = System.getProperty("path.separator");

        StringBuffer errorReport = new StringBuffer();
        boolean success = true;

        // Start capturing the System.err output for this thread
        SystemLogHandler.setThread();

        // Initializing javac task
        getProject();
        Javac javac = (Javac) project.createTask("javac");

        // Initializing classpath
        Path path = new Path(project);
        path.setPath(System.getProperty("java.class.path"));
        StringTokenizer tokenizer = new StringTokenizer(classpath, sep);
        while (tokenizer.hasMoreElements()) {
            String pathElement =
                JspRuntimeLibrary.decode(tokenizer.nextToken());
            File repository = new File(pathElement);
            path.setLocation(repository);
            info.append("     cp=" + repository + "\n");
        }

        // Initializing sourcepath
        Path srcPath = new Path(project);
        srcPath.setLocation(options.getScratchDir());

        info.append("     work dir=" + options.getScratchDir() + "\n");

        // Configure the compiler object
        javac.setEncoding(javaEncoding);
        javac.setClasspath(path);
        javac.setDebug(ctxt.getOptions().getClassDebugInfo());
        javac.setSrcdir(srcPath);
        javac.setTempdir(options.getScratchDir());
        javac.setOptimize(! ctxt.getOptions().getClassDebugInfo() );
        javac.setFork(ctxt.getOptions().getFork());

        info.append("    srcDir=" + srcPath + "\n" );

        // Set the Java compiler to use
        if (options.getCompiler() != null) {
            javac.setCompiler(options.getCompiler());
            info.append("    compiler=" + options.getCompiler() + "\n");
        }

        // Build includes path
        PatternSet.NameEntry includes = javac.createInclude();
        includes.setName(ctxt.getJspPath());
        info.append("    include="+ ctxt.getJspPath() + "\n" );

        BuildException error=null;
        try {
            if (ctxt.getOptions().getFork()) {
                javac.execute();
            } else {
                synchronized(javacLock) {
                    javac.execute();
                }
            }
        } catch (BuildException e) {
            success = false;
            error=e;
            info.append("Exception compiling "  + e.toString() + "\n");
        }

        errorReport.append(logger.getReport());

        // Stop capturing the System.err output for this thread
        String errorCapture = SystemLogHandler.unsetThread();
        if (errorCapture != null) {
            errorReport.append(System.getProperty("line.separator"));
            errorReport.append(errorCapture);
        }

        if (!ctxt.keepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }

        if (!success) {
            Constants.jasperLog.log( "Error compiling file: " + javaFileName + " " + errorReport,
                                     Logger.ERROR);
            Constants.jasperLog.log( "Info: " + info.toString(),
                                     Logger.ERROR);
            if( error != null ) {
                Constants.jasperLog.log( "Exception: ", error );
                error.printStackTrace();
            }
            
            errDispatcher.javacError(errorReport.toString(), javaFileName, pageNodes);
        }

    }

    /** 
     * Compile the jsp file from the current engine context
     */
    public void compile()
        throws FileNotFoundException, JasperException, Exception
    {
        try {
            generateJava();
            generateClass();
        } finally {
            // Make sure these object which are only used during the
            // generation and compilation of the JSP page get
            // dereferenced so that they can be GC'd and reduce the
            // memory footprint.
            errDispatcher = null;
            logger = null;
            project = null;
            pageInfo = null;
            pageNodes = null;
        }
    }

    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    public boolean isOutDated() {
        return isOutDated( true );
    }

    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation.
     * @param checkClass Verify the class file if true, only the .java file if false.
     */
    public boolean isOutDated(boolean checkClass) {

        String jsp = ctxt.getJspFile();

        long jspRealLastModified = 0;
        try {
            URL jspUrl = ctxt.getResource(jsp);
            if (jspUrl == null) {
                ctxt.incrementRemoved();
                return false;
            }

            URLConnection conn = jspUrl.openConnection();        
            jspRealLastModified = conn.getLastModified();
            conn.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        long targetLastModified;
        File targetFile;
        
        if( checkClass ) {
            targetFile = new File(ctxt.getClassFileName());
        } else {
            targetFile = new File( ctxt.getServletJavaFileName());
        }
        
        if (!targetFile.exists()) {
            return true;
        }
        targetLastModified = targetFile.lastModified();
        if (targetLastModified < jspRealLastModified) {
            //System.out.println("Compiler: outdated, " + targetFile + " " + targetLastModified );
            return true;
        }

        // determine if compile time includes have been changed
        if( jsw==null ) {
            return false;
        }
        Servlet servlet=null;
        try {
            servlet = jsw.getServlet();
        } catch( ServletException ex1 ) {
        } catch( IOException ex2 ) {
        }
        if (servlet == null) {
            // System.out.println("Compiler: outdated, no servlet " + targetFile );
            return true;
        }
        List includes = null;
        // If the page contains a page directive with "extends" attribute
        // it may not be an instance of HttpJspBase.
        // For now only track dependencies on included files if this is not
        // the case.  A more complete solution is to generate the servlet
        // to implement (say) JspInlcudes which contains getIncludes method.
        if (servlet instanceof HttpJspBase) {
            includes = ((HttpJspBase)servlet).getIncludes();
        }

        if (includes == null) {
            return false;
        }

        Iterator it = includes.iterator();
        while (it.hasNext()) {
            String include = (String)it.next();
            try {
                URL includeUrl = ctxt.getResource(include);
                if (includeUrl == null) {
                    //System.out.println("Compiler: outdated, no includeUri " + include );
                    return true;
                }

                URLConnection includeUrlConn = includeUrl.openConnection();
                long includeLastModified = includeUrlConn.getLastModified();
                includeUrlConn.getInputStream().close();

                if (includeLastModified > targetLastModified) {
                    //System.out.println("Compiler: outdated, include old " + include );
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        return false;

    }

    
    /**
     * Gets the error dispatcher.
     */
    public ErrorDispatcher getErrorDispatcher() {
        return errDispatcher;
    }


    /**
     * Gets the info about the page under compilation
     */
    public PageInfo getPageInfo() {
        return pageInfo;
    }


    public JspCompilationContext getCompilationContext() {
        return ctxt;
    }


    /**
     * Remove generated files
     */
    public void removeGeneratedFiles() {
        try {
            String classFileName = ctxt.getServletClassName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                classFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
        try {
            String javaFileName = ctxt.getServletJavaFileName();
            if (javaFileName != null) {
                File javaFile = new File(javaFileName);
                javaFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
    }


}
