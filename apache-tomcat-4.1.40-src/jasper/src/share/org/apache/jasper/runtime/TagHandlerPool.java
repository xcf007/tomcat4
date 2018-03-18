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

package org.apache.jasper.runtime;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Pool of tag handlers that can be reused.
 *
 * @author Jan Luehe
 */
public class TagHandlerPool {
    
    private static final int MAX_POOL_SIZE = 5;

    private Tag[] handlers;

    // index of next available tag handler
    private int current;

    /**
     * Constructs a tag handler pool with the default capacity.
     */
    public TagHandlerPool() {
        this(MAX_POOL_SIZE);
    }

    /**
     * Constructs a tag handler pool with the given capacity.
     *
     * @param capacity Tag handler pool capacity
     */
    public TagHandlerPool(int capacity) {
        this.handlers = new Tag[capacity];
        this.current = -1;
    }

    /**
     * Gets the next available tag handler from this tag handler pool,
     * instantiating one if this tag handler pool is empty.
     *
     * @param handlerClass Tag handler class
     *
     * @return Reused or newly instantiated tag handler
     *
     * @throws JspException if a tag handler cannot be instantiated
     */
    public synchronized Tag get(Class handlerClass) throws JspException {
        Tag handler = null;

        if (current >= 0) {
            handler = handlers[current--];
        } else {
            try {
                return (Tag) handlerClass.newInstance();
            } catch (Exception e) {
                throw new JspException(e.getMessage(), e);
            }
        }

        return handler;
    }

    /**
     * Adds the given tag handler to this tag handler pool, unless this tag
     * handler pool has already reached its capacity, in which case the tag
     * handler's release() method is called.
     *
     * @param handler Tag handler to add to this tag handler pool
     */
    public synchronized void reuse(Tag handler) {
        if (current < (handlers.length - 1))
            handlers[++current] = handler;
        else
            handler.release();
    }

    /**
     * Calls the release() method of all available tag handlers in this tag
     * handler pool.
     */
    public synchronized void release() {
        for (int i=current; i>=0; i--) {
            handlers[i].release();
        }
    }
}

