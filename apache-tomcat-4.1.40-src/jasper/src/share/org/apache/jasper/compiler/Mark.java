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

import java.util.Stack;

/**
 * Mark represents a point in the JSP input. 
 *
 * @author Anil K. Vijendran
 */
public final class Mark {
    int cursor, line, col;        // position within current stream
    int fileid;                        // fileid of current stream
    String fileName;            // name of the current file
    String baseDir;                // directory of file for current stream
    char[] stream = null;        // current stream
    Stack includeStack = null;        // stack of stream and stream state of streams
                                //   that have included current stream
    String encoding = null;        // encoding of current file
    private JspReader reader;        // reader that owns this mark 
                                //   (so we can look up fileid's)


    /**
     * Keep track of parser before parsing an included file.
     * This class keeps track of the parser before we switch to parsing an
     * included file. In other words, it's the parser's continuation to be
     * reinstalled after the included file parsing is done.
     */
    class IncludeState {
        int cursor, line, col;
        int fileid;
        String fileName;
        String baseDir;
        String encoding;
        char[] stream = null;

        IncludeState(int inCursor, int inLine, int inCol, int inFileid, 
                     String name, String inBaseDir, String inEncoding,
                     char[] inStream) 
        {
            cursor = inCursor;
            line = inLine;
            col = inCol;
            fileid = inFileid;
            fileName = name;
            baseDir = inBaseDir;
            encoding = inEncoding;
            stream = inStream;
        }
    }


    /**
    * Creates a new mark
    * @param inReader JspReader this mark belongs to
    * @param inStream current stream for this mark
    * @param inFileid id of requested jsp file
    * @param inEncoding encoding of current file
    * @param inBaseDir base directory of requested jsp file
    */
    Mark(JspReader reader, char[] inStream, int fileid, String name,
         String inBaseDir, String inEncoding) 
    {
        this.reader = reader;
        this.stream = inStream;
        this.cursor = this.line = this.col = 0;
        this.fileid = fileid;
        this.fileName = name;
        this.baseDir = inBaseDir;
        this.encoding = inEncoding;
        this.includeStack = new Stack();
    }
        
    Mark(Mark other) {
        this.reader = other.reader;
        this.stream = other.stream;
        this.fileid = other.fileid;
        this.fileName = other.fileName;
        this.cursor = other.cursor;
        this.line = other.line;
        this.col = other.col;
        this.baseDir = other.baseDir;
        this.encoding = other.encoding;

        // clone includeStack without cloning contents
        includeStack = new Stack();
        for ( int i=0; i < other.includeStack.size(); i++ ) {
              includeStack.addElement( other.includeStack.elementAt(i) );
        }
    }
            
    Mark(String filename, int line, int col) {
        //System.out.println("MARK: filename is: " + filename);
        this.reader = null;
        this.stream = null;
        this.cursor = 0;
        this.line = line;
        this.col = col;
        this.fileid = -1;
        this.fileName = filename;
        this.baseDir = "le-basedir";
        this.encoding = "le-endocing";
        this.includeStack = null;
    }

    /** Sets this mark's state to a new stream.
     * It will store the current stream in it's includeStack.
     * @param inStream new stream for mark
     * @param inFileid id of new file from which stream comes from
     * @param inBaseDir directory of file
         * @param inEncoding encoding of new file
     */
    public void pushStream(char[] inStream, int inFileid, String name,
                           String inBaseDir, String inEncoding) 
    {

        // store current state in stack
        includeStack.push(new IncludeState(cursor, line, col, fileid, fileName, baseDir, 
                                           encoding, stream) );

        // set new variables
        cursor = 0;
        line = 0;
        col = 0;
        fileid = inFileid;
        fileName = name;
        baseDir = inBaseDir;
        encoding = inEncoding;
        stream = inStream;
    }



    /** Restores this mark's state to a previously stored stream.
     */
    public boolean popStream() {
        // make sure we have something to pop
        if ( includeStack.size() <= 0 ) return false;

        // get previous state in stack
        IncludeState state = (IncludeState) includeStack.pop( );

        // set new variables
        cursor = state.cursor;
        line = state.line;
        col = state.col;
        fileid = state.fileid;
        fileName = state.fileName;
        baseDir = state.baseDir;
        stream = state.stream;
        return true;
    }

    // -------------------- Locator interface --------------------

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return col;
    }

    public String getSystemId() {
        return getFile();
    }

    public String getPublicId() {
        return null;
    }

    public String toString() {
        return getFile()+"("+line+","+col+")";
    }

    public String getFile() {
        return this.fileName;
    }
    
    public String toShortString() {
        return "("+line+","+col+")";
    }

    public boolean equals(Object other) {
        if (other instanceof Mark) {
            Mark m = (Mark) other;
            return this.reader == m.reader && this.fileid == m.fileid 
                && this.cursor == m.cursor && this.line == m.line 
                && this.col == m.col;
        } 
        return false;
    }
}

