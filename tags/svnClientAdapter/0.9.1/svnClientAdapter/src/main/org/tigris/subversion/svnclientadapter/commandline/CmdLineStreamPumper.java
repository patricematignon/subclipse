/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnclientadapter.commandline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Copies all data from an input stream to a String
 * This class is inspired by ant StreamPumper from Robert Field
 *
 */
public class CmdLineStreamPumper implements Runnable {
    private static final String NEWLINE = "\n\r";
    private static final int SLEEP = 5;
    private static final int SIZE = 128;
    private BufferedReader br;
    private boolean finished;
    private StringBuffer sb = new StringBuffer();
    private boolean coaleasceLines = false;

    /**
     * Create a new stream pumper.
     *
     * @param is input stream to read data from
     * @param coaleasceLines if true, it will coaleasce lines
     */
    public CmdLineStreamPumper(InputStream is, boolean coaleasceLines) {
        br = new BufferedReader(new InputStreamReader(is));
        this.coaleasceLines = coaleasceLines;
    }

    /**
     * Create a new stream pumper.
     *
     * @param is input stream to read data from
     */
    public CmdLineStreamPumper(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Copies data from the input stream to the string buffer
     *
     * Terminates as soon as the input stream is closed or an error occurs.
     */
    public void run() {
        synchronized (this) {
            // Just in case this object is reused in the future
            finished = false;
        }

        final byte[] buf = new byte[SIZE];

        int length;
        try {
            String st;
            while((st=br.readLine())!=null) {
                if (coaleasceLines) {
                    sb.append(st);
                } else {
                    sb.append(st+NEWLINE);                    
                }
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {}                
            }
        } catch (Exception e) {
            // ignore
        } finally {
            synchronized (this) {
                finished = true;
                notify();
            }
        }
    }

    public String toString() {
        return sb.toString();
    }

    /**
     * Tells whether the end of the stream has been reached.
     * @return true is the stream has been exhausted.
     **/
    public synchronized boolean isFinished() {
        return finished;
    }

    /**
     * This method blocks until the stream pumper finishes.
     * @see #isFinished()
     **/
    public synchronized void waitFor()
        throws InterruptedException {
        while (!isFinished()) {
            wait();
        }
    }
}
