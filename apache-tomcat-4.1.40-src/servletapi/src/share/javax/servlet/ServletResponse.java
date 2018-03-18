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

package javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;


/**
 * Defines an object to assist a servlet in sending a response to the client.
 * The servlet container creates a <code>ServletResponse</code> object and
 * passes it as an argument to the servlet's <code>service</code> method.
 *
 * <p>To send binary data in a MIME body response, use
 * the {@link ServletOutputStream} returned by {@link #getOutputStream}.
 * To send character data, use the <code>PrintWriter</code> object 
 * returned by {@link #getWriter}. To mix binary and text data,
 * for example, to create a multipart response, use a
 * <code>ServletOutputStream</code> and manage the character sections
 * manually.
 *
 * <p>The charset for the MIME body response can be specified with 
 * {@link #setContentType}.  For example, "text/html; charset=Shift_JIS".
 * The charset can alternately be set using {@link #setLocale}.
 * If no charset is specified, ISO-8859-1 will be used.  
 * The <code>setContentType</code> or <code>setLocale</code> method 
 * must be called before <code>getWriter</code> for the charset to 
 * affect the construction of the writer.
 * 
 * <p>See the Internet RFCs such as 
 * <a href="http://info.internet.isi.edu/in-notes/rfc/files/rfc2045.txt">
 * RFC 2045</a> for more information on MIME. Protocols such as SMTP
 * and HTTP define profiles of MIME, and those standards
 * are still evolving.
 *
 * @author 	Various
 * @version 	$Version$
 *
 * @see		ServletOutputStream
 *
 */
 
public interface ServletResponse {


    
    /**
     * Returns the name of the charset used for
     * the MIME body sent in this response.
     *
     * <p>If no charset has been assigned, it is implicitly
     * set to <code>ISO-8859-1</code> (<code>Latin-1</code>).
     *
     * <p>See RFC 2047 (http://ds.internic.net/rfc/rfc2045.txt)
     * for more information about character encoding and MIME.
     *
     * @return		a <code>String</code> specifying the
     *			name of the charset, for
     *			example, <code>ISO-8859-1</code>
     *
     */
  
    public String getCharacterEncoding();
    
    

    /**
     * Returns a {@link ServletOutputStream} suitable for writing binary 
     * data in the response. The servlet container does not encode the
     * binary data.  
     
     * <p> Calling flush() on the ServletOutputStream commits the response.
     
     * Either this method or {@link #getWriter} may 
     * be called to write the body, not both.
     *
     * @return				a {@link ServletOutputStream} for writing binary data	
     *
     * @exception IllegalStateException if the <code>getWriter</code> method
     * 					has been called on this response
     *
     * @exception IOException 		if an input or output exception occurred
     *
     * @see 				#getWriter
     *
     */

    public ServletOutputStream getOutputStream() throws IOException;
    
    

    /**
     * Returns a <code>PrintWriter</code> object that 
     * can send character text to the client. 
     * The character encoding used is the one specified 
     * in the <code>charset=</code> property of the
     * {@link #setContentType} method, which must be called
     * <i>before</i> calling this method for the charset to take effect. 
     *
     * <p>If necessary, the MIME type of the response is 
     * modified to reflect the character encoding used.
     *
     * <p> Calling flush() on the PrintWriter commits the response.
     *
     * <p>Either this method or {@link #getOutputStream} may be called
     * to write the body, not both.
     *
     * 
     * @return 				a <code>PrintWriter</code> object that 
     *					can return character data to the client 
     *
     * @exception UnsupportedEncodingException  if the charset specified in
     *						<code>setContentType</code> cannot be
     *						used
     *
     * @exception IllegalStateException    	if the <code>getOutputStream</code>
     * 						method has already been called for this 
     *						response object
     *
     * @exception IOException   		if an input or output exception occurred
     *
     * @see 					#getOutputStream
     * @see 					#setContentType
     *
     */

    public PrintWriter getWriter() throws IOException;
    
    
    
    

    /**
     * Sets the length of the content body in the response
     * In HTTP servlets, this method sets the HTTP Content-Length header.
     *
     *
     * @param len 	an integer specifying the length of the 
     * 			content being returned to the client; sets
     *			the Content-Length header
     *
     */

    public void setContentLength(int len);
    
    

    /**
     * Sets the content type of the response being sent to
     * the client. The content type may include the type of character
     * encoding used, for example, <code>text/html; charset=ISO-8859-4</code>.
     *
     * <p>If obtaining a <code>PrintWriter</code>, this method should be 
     * called first.
     *
     *
     * @param type 	a <code>String</code> specifying the MIME 
     *			type of the content
     *
     * @see 		#getOutputStream
     * @see 		#getWriter
     *
     */

    public void setContentType(String type);
    

    /**
     * Sets the preferred buffer size for the body of the response.  
     * The servlet container will use a buffer at least as large as 
     * the size requested.  The actual buffer size used can be found
     * using <code>getBufferSize</code>.
     *
     * <p>A larger buffer allows more content to be written before anything is
     * actually sent, thus providing the servlet with more time to set
     * appropriate status codes and headers.  A smaller buffer decreases 
     * server memory load and allows the client to start receiving data more
     * quickly.
     *
     * <p>This method must be called before any response body content is
     * written; if content has been written, this method throws an 
     * <code>IllegalStateException</code>.
     *
     * @param size 	the preferred buffer size
     *
     * @exception  IllegalStateException  	if this method is called after
     *						content has been written
     *
     * @see 		#getBufferSize
     * @see 		#flushBuffer
     * @see 		#isCommitted
     * @see 		#reset
     *
     */

    public void setBufferSize(int size);
    
    

    /**
     * Returns the actual buffer size used for the response.  If no buffering
     * is used, this method returns 0.
     *
     * @return	 	the actual buffer size used
     *
     * @see 		#setBufferSize
     * @see 		#flushBuffer
     * @see 		#isCommitted
     * @see 		#reset
     *
     */

    public int getBufferSize();
    
    

    /**
     * Forces any content in the buffer to be written to the client.  A call
     * to this method automatically commits the response, meaning the status 
     * code and headers will be written.
     *
     * @see 		#setBufferSize
     * @see 		#getBufferSize
     * @see 		#isCommitted
     * @see 		#reset
     *
     */

    public void flushBuffer() throws IOException;
    
    
    
    /**
     * Clears the content of the underlying buffer in the response without
     * clearing headers or status code. If the 
     * response has been committed, this method throws an 
     * <code>IllegalStateException</code>.
     *
     * @see 		#setBufferSize
     * @see 		#getBufferSize
     * @see 		#isCommitted
     * @see 		#reset
     *
     * @since 2.3
     */

    public void resetBuffer();
    

    /**
     * Returns a boolean indicating if the response has been
     * committed.  A commited response has already had its status 
     * code and headers written.
     *
     * @return		a boolean indicating if the response has been
     *  		committed
     *
     * @see 		#setBufferSize
     * @see 		#getBufferSize
     * @see 		#flushBuffer
     * @see 		#reset
     *
     */

    public boolean isCommitted();
    
    

    /**
     * Clears any data that exists in the buffer as well as the status code and
     * headers.  If the response has been committed, this method throws an 
     * <code>IllegalStateException</code>.
     *
     * @exception IllegalStateException  if the response has already been
     *                                   committed
     *
     * @see 		#setBufferSize
     * @see 		#getBufferSize
     * @see 		#flushBuffer
     * @see 		#isCommitted
     *
     */

    public void reset();
    
    

    /**
     * Sets the locale of the response, setting the headers (including the
     * Content-Type's charset) as appropriate.  This method should be called
     * before a call to {@link #getWriter}.  By default, the response locale
     * is the default locale for the server.
     * 
     * @param loc  the locale of the response
     *
     * @see 		#getLocale
     *
     */

    public void setLocale(Locale loc);
    
    

    /**
     * Returns the locale assigned to the response.
     * 
     * 
     * @see 		#setLocale
     *
     */

    public Locale getLocale();



}





