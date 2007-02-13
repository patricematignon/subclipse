/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
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
    
    /** Node kind absent */
    public static final SVNNodeKind NONE = new SVNNodeKind(none);

    /** Node kind regular file */
    public static final SVNNodeKind FILE = new SVNNodeKind(file);

    /** Node kind Directory */
    public static final SVNNodeKind DIR = new SVNNodeKind(dir);

    /** Node kind unknwon - something's here, but we don't know what */
    public static final SVNNodeKind UNKNOWN = new SVNNodeKind(unknown);
 
    /**
     * Private costructor.
     * @param kind
     */
    private SVNNodeKind(int kind) {
         this.kind = kind;
    }

    /**
     * @return an integer value representation of the nodeKind
     */
    public int toInt() {
    	return kind;
    }
    
    /**
     * Returns the SVNNodeKind corresponding to the given int representation.
     * (As returned by {@link SVNNodeKind#toInt()} method)
     * @param nodeKind
     * @return SVNNodeKind representing the int value
     */
    public static SVNNodeKind fromInt(int nodeKind) {
        switch(nodeKind) 
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
    
    /**
     * Returns the SVNNodeKind corresponding to the given string or null
     * @param nodeKind
     * @return SVNNodeKind representing the string value
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
       	if ("dir".equals(nodeKind)) {
       		return DIR;
       	} else
        if (UNKNOWN.toString().equals(nodeKind)) {
        	return UNKNOWN;  
        } else
        	return null;
    }    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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
