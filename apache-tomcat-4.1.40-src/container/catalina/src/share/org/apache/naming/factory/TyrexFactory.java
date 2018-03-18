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


package org.apache.naming.factory;

import java.net.URL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import tyrex.tm.TransactionDomain;
import tyrex.tm.DomainConfigurationException;
import tyrex.tm.RecoveryException;

/**
 * Abstract superclass of any factory that creates objects from Tyrex.<br>
 *
 * Subclasses can use getTransactionDomain() to handle the retrieval and
 * creation of the TransactionDomain.
 *
 * Tyrex is an open-source transaction manager, developed by Assaf Arkin and
 * exolab.org. See the <a href="http://tyrex.exolab.org/">Tyrex homepage</a>
 * for more details about Tyrex and downloads.
 *
 * @author David Haraburda
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 */

public abstract class TyrexFactory implements ObjectFactory {


    // ----------------------------------------------------------- Constructors


    // -------------------------------------------------------------- Constants


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------ Protected Methods


    /**
     * Get (and if necessary, create) the active TransactionDomain
     *
     * This class checks to see if there is already a TransactionDomain
     * setup and instantiated.  If so, it is returned, otherwise one is
     * created and initialized using properties obtained from JNDI.
     */
    protected TransactionDomain getTransactionDomain() throws NamingException {
        TransactionDomain domain = null;
        InitialContext initCtx = new InitialContext();
        String config = initCtx.lookup("java:comp/env/" +
            Constants.TYREX_DOMAIN_CONFIG).toString();
        String name = initCtx.lookup("java:comp/env/" +
            Constants.TYREX_DOMAIN_NAME).toString();
        if (config != null && name != null) {
            try {
                domain = TransactionDomain.getDomain(name);
            } catch(Throwable t) {
                // Tyrex throws exceptions if required classes aren't found.
                log("Error loading Tyrex TransactionDomain", t);
                throw new NamingException
                    ("Exception loading TransactionDomain: " + t.getMessage());
            }
            if ((domain == null)
                || (domain.getState() == TransactionDomain.TERMINATED)) {
                URL configURL = Thread.currentThread().getContextClassLoader()
                    .getResource(config);
                if (configURL == null)
                    throw new NamingException
                        ("Could not load Tyrex domain config file");
                try {
                    domain = 
                        TransactionDomain.createDomain(configURL.toString());
                } catch(DomainConfigurationException dce) {
                    throw new NamingException
                        ("Could not create TransactionDomain: " 
                         + dce.getMessage());
                }
            }

        } else {
            throw new NamingException
                ("Specified config file or domain name "
                 + "parameters are invalid.");
        }

        if (domain.getState() == TransactionDomain.READY) {
            try {
                domain.recover();
            } catch( RecoveryException re ) {
                throw new NamingException
                    ("Could not activate TransactionDomain: " 
                     + re.getMessage() );
            }
        }

        return domain;
    }



    // -------------------------------------------------------- Private Methods


    private void log(String message) {
        System.out.print("TyrexFactory:  ");
        System.out.println(message);
    }


    private void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }


}
