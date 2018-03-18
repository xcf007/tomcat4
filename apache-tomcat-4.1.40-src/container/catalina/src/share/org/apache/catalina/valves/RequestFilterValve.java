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


package org.apache.catalina.valves;


import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of a Valve that performs filtering based on comparing the
 * appropriate request property (selected based on which subclass you choose
 * to configure into your Container's pipeline) against a set of regular
 * expressions configured for this Valve.
 * <p>
 * This valve is configured by setting the <code>allow</code> and/or
 * <code>deny</code> properties to a comma-delimited list of regular
 * expressions (in the syntax supported by the jakarta-regexp library) to
 * which the appropriate request property will be compared.  Evaluation
 * proceeds as follows:
 * <ul>
 * <li>The subclass extracts the request property to be filtered, and
 *     calls the common <code>process()</code> method.
 * <li>If there are any deny expressions configured, the property will
 *     be compared to each such expression.  If a match is found, this
 *     request will be rejected with a "Forbidden" HTTP response.</li>
 * <li>If there are any allow expressions configured, the property will
 *     be compared to each such expression.  If a match is found, this
 *     request will be allowed to pass through to the next Valve in the
 *     current pipeline.</li>
 * <li>If one or more deny expressions was specified but no allow expressions,
 *     allow this request to pass through (because none of the deny
 *     expressions matched it).
 * <li>The request will be rejected with a "Forbidden" HTTP response.</li>
 * </ul>
 * <p>
 * This Valve may be attached to any Container, depending on the granularity
 * of the filtering you wish to perform.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public abstract class RequestFilterValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The comma-delimited set of <code>allow</code> expressions.
     */
    protected String allow = null;


    /**
     * The set of <code>allow</code> pre-compiled regular expressions we will
     * evaluate.
     */
    protected REProgram allows[] = new REProgram[0];


    /**
     * The set of <code>deny</code> pre-compiled regular expressions we will
     * evaluate.
     */
    protected REProgram denies[] = new REProgram[0];


    /**
     * The comma-delimited set of <code>deny</code> expressions.
     */
    protected String deny = null;


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.RequestFilterValve/1.0";


    /**
     * The StringManager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return a comma-delimited set of the <code>allow</code> expressions
     * configured for this Valve, if any; otherwise, return <code>null</code>.
     */
    public String getAllow() {

        return (this.allow);

    }


    /**
     * Set the comma-delimited set of the <code>allow</code> expressions
     * configured for this Valve, if any.
     *
     * @param allow The new set of allow expressions
     */
    public void setAllow(String allow) {

        this.allow = allow;
        allows = precalculate(allow);

    }


    /**
     * Return a comma-delimited set of the <code>deny</code> expressions
     * configured for this Valve, if any; otherwise, return <code>null</code>.
     */
    public String getDeny() {

        return (this.deny);

    }


    /**
     * Set the comma-delimited set of the <code>deny</code> expressions
     * configured for this Valve, if any.
     *
     * @param deny The new set of deny expressions
     */
    public void setDeny(String deny) {

        this.deny = deny;
        denies = precalculate(deny);

    }


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Extract the desired request property, and pass it (along with the
     * specified request and response objects) to the protected
     * <code>process()</code> method to perform the actual filtering.
     * This method must be implemented by a concrete subclass.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public abstract void invoke(Request request, Response response,
                                ValveContext context)
        throws IOException, ServletException;


    // ------------------------------------------------------ Protected Methods


    /**
     * Return an array of pre-compiled regular expression objects initialized
     * from the specified argument, which must be <code>null</code> or a
     * comma-delimited list of regular expression patterns.
     *
     * @param list The comma-separated list of patterns
     *
     * @exception IllegalArgumentException if one of the patterns has
     *  invalid syntax
     */
    protected REProgram[] precalculate(String list) {

        if (list == null)
            return (new REProgram[0]);
        list = list.trim();
        if (list.length() < 1)
            return (new REProgram[0]);
        list += ",";

        ArrayList reProgramList = new ArrayList();
        while (list.length() > 0) {
            int comma = list.indexOf(',');
            if (comma < 0)
                break;
            String pattern = list.substring(0, comma).trim();
            try {
                reProgramList.add(new RE(pattern).getProgram());
            } catch (RESyntaxException e) {
                throw new IllegalArgumentException
                    (sm.getString("requestFilterValve.syntax", pattern));
            }
            list = list.substring(comma + 1);
        }

        REProgram reProgramArray[] = new REProgram[reProgramList.size()];
        return ((REProgram[]) reProgramList.toArray(reProgramArray));

    }


    /**
     * Perform the filtering that has been configured for this Valve, matching
     * against the specified request property.
     *
     * @param property The request property on which to filter
     * @param request The servlet request to be processed
     * @param response The servlet response to be processed
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    protected void process(String property,
                           Request request, Response response,
                           ValveContext context)
        throws IOException, ServletException {

        // Default to deny request if property is null
        if (property == null) {
            ServletResponse sres = response.getResponse();
            if (sres instanceof HttpServletResponse) {
                HttpServletResponse hres = (HttpServletResponse) sres;
                hres.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            Exception e = new IllegalArgumentException();
            getContainer().getLogger().log(e,"Request Denied, no property to filter on");
            return;
        }

        
        // Create local RE since RE is not thread safe
        RE re = new RE();
        
        // Check the deny patterns, if any
        for (int i = 0; i < denies.length; i++) {
            re.setProgram(denies[i]);
            if (re.match(property)) {
                ServletResponse sres = response.getResponse();
                if (sres instanceof HttpServletResponse) {
                    HttpServletResponse hres = (HttpServletResponse) sres;
                    hres.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        // Check the allow patterns, if any
        for (int i = 0; i < allows.length; i++) {
            re.setProgram(allows[i]);
            if (re.match(property)) {
                context.invokeNext(request, response);
                return;
            }
        }

        // Allow if denies specified but not allows
        if ((denies.length > 0) && (allows.length == 0)) {
            context.invokeNext(request, response);
            return;
        }

        // Deny this request
        ServletResponse sres = response.getResponse();
        if (sres instanceof HttpServletResponse) {
            HttpServletResponse hres = (HttpServletResponse) sres;
            hres.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

    }


}
