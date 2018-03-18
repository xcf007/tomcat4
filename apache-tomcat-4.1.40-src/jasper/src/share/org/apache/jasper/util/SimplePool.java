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

package org.apache.jasper.util;

/**
 * Simple object pool. Based on ThreadPool and few other classes
 *
 * The pool will ignore overflow and return null if empty.
 *
 * @author Gal Shachor
 * @author Costin
 */
public final class SimplePool  {
    /*
     * Where the threads are held.
     */
    private Object pool[];

    private int max;
    private int current=-1;

    Object lock;
    public static final int DEFAULT_SIZE=16;
    
    public SimplePool() {
        this.max=DEFAULT_SIZE;
        pool=new Object[max];
        lock=new Object();
    }
    
    public SimplePool(int max) {
        this.max=max;
        pool=new Object[max];
        lock=new Object();
    }

    public  void set(Object o) {
        put(o);
    }
    /**
     * Add the object to the pool, silent nothing if the pool is full
     */
    public  void put(Object o) {
        synchronized( lock ) {
            if( current < (max-1) ) {
                current += 1;
                pool[current] = o;
            }
        }
    }

    /**
     * Get an object from the pool, null if the pool is empty.
     */
    public  Object get() {
        Object item = null;
        synchronized( lock ) {
            if( current >= 0 ) {
                item = pool[current];
                current -= 1;
            }
        }
        return item;
    }

    /**
     * Return the size of the pool
     */
    public int getMax() {
        return max;
    }
}
