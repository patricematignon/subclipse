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
package org.tigris.subversion.svnclientadapter;

/**
 * Kind of a node (dir, file) 
 */
public class SVNNodeKind
{
    private int kind;
    
    private static final int none = 0;
    private static final int file = 1;
    private static final int dir = 2;
    private static final int unknown = 3;
    
    /* absent */
    public static final SVNNodeKind NONE = new SVNNodeKind(none);

    /* regular file */
    public static final SVNNodeKind FILE = new SVNNodeKind(file);

    /* directory */
    public static final SVNNodeKind DIR = new SVNNodeKind(dir);

    /* something's here, but we don't know what */
    public static final SVNNodeKind UNKNOWN = new SVNNodeKind(unknown);
 
    private SVNNodeKind(int kind) {
         this.kind = kind;
    }

    public int toInt() {
    	return kind;
    }
    
    public static SVNNodeKind fromInt(int kind) {
        switch(kind) 
        {
            case none: 
                return NONE;
            case file: 
                return FILE;
            case dir: 
                return DIR;
            case unknown: 
                return UNKNOWN;
            default:
                return null;
        }    	
    }
    
    public String toString() {
        switch(kind) 
        {
            case none: 
                return "none";
            case file: 
                return "file";
            case dir: 
                return "directory";
            case unknown: 
                return "unknown";
            default:
                return "";
        }
    }

    /**
     * returns the ScheduleKind corresponding to the given string or null
     * @param scheduleKind
     * @return
     */
    public static SVNNodeKind fromString(String nodeKind) {
    	if (NONE.toString().equals(nodeKind)) {
    		return NONE;
    	} else
        if (FILE.toString().equals(nodeKind)) {
        	return FILE;
        } else    		
        if (DIR.toString().equals(nodeKind)) {
        	return DIR;
        } else
        if (UNKNOWN.toString().equals(nodeKind)) {
        	return UNKNOWN;  
        } else
        	return null;
    }    

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof SVNNodeKind)) {
            return false;
        }
        return ((SVNNodeKind)obj).kind == kind;
    }    

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new Integer(kind).hashCode();
    }    
    
}
