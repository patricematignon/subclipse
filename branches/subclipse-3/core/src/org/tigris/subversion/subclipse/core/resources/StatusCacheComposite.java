/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * We use a tree to cache LocalResourceStatus for each resource
 * We could use :
 * - {@link IResource#setSessionProperty} 
 * but it can only be used existing resources
 * - {@link ISynchronizer#setSyncInfo}
 * but it cannot be used in a {@link IResourceChangeListener} because it modified
 * workspace itself 
 */
public class StatusCacheComposite {
	private String segment;
    private LocalResourceStatus status = null;
    private Map children = null;

    /**
     * creates the root of the tree cache
     *
     */
    public StatusCacheComposite() {
    }
    
    private StatusCacheComposite(String segment, LocalResourceStatus status) {
        this.segment = segment;
        this.status = status;
    }
        
    
    /**
     * add a status
     * @param segment
     * @param status
     * @return
     */
    synchronized private StatusCacheComposite addStatus(String segment, LocalResourceStatus status) {
    	if (children == null) {
    		children = new HashMap();
        }
        StatusCacheComposite child = (StatusCacheComposite)children.get(segment);
        if (child == null) {
        	child = new StatusCacheComposite(segment,status); 
        	children.put(segment, child);
        } else {
        	child.status = status;
        }
        return child;
    }
    
    
    /**
     * get the given child or null if this composite has no children with this name
     * @param segment
     * @return
     */
    synchronized private StatusCacheComposite getChild(String segment) {
        if (children == null) {
        	return null;
        }
        return (StatusCacheComposite)children.get(segment);
    }
    
    /**
     * add a status at the given relative path
     * @param path
     * @param status
     */
    synchronized public void addStatus(IPath path, LocalResourceStatus status) {
    	if (path.segmentCount() == 1) {
    		addStatus(path.lastSegment(), status);
        } else {
        	StatusCacheComposite child = getChild(path.segment(0));
            if (child == null) {
            	child = addStatus(path.segment(0),null);
            }
            child.addStatus(path.removeFirstSegments(1), status);
        }
    }
    
    /**
     * get the status at the given relative path
     * @param path
     * @return
     */
    synchronized public LocalResourceStatus getStatus(IPath path) {
        if (path.segmentCount() == 0) {
        	return status;
        }
        StatusCacheComposite child = getChild(path.segment(0));
        if (child == null) {
        	return null;
        } else {
        	return child.getStatus(path.removeFirstSegments(1));
        }
    }
    
    /**
     * remove a child status
     * @param segment
     * @param depth
     */
    synchronized private void removeChildStatus(String segment, int depth) {
    	StatusCacheComposite child = getChild(segment);
        if (child == null) {
        	return;
        }
        if (child.children == null) {
        	children.put(segment,null);
        } else {
        	child.status = null;
        }
        
        if (depth == IResource.DEPTH_ONE) {
        	if (child.children != null) {
        		for (Iterator it = child.children.values().iterator(); it.hasNext(); ) {
        			StatusCacheComposite grandchild = (StatusCacheComposite)it.next();                    
                    grandchild.status = null;
                }
            }
        }
        
        if (depth == IResource.DEPTH_INFINITE)  {
        	child.children = null;
        }
    }
    
    /**
     * removes the status at the given relative path
     * @param path
     * @param depth
     */
    synchronized public void removeStatus(IPath path, int depth) {
    	if (path.segmentCount() > 1) {
            StatusCacheComposite child = getChild(path.segment(0));
            if (child == null) {
            	return;
            }
    		child.removeStatus(path.removeFirstSegments(1),depth);
        } else {
            StatusCacheComposite child = getChild(path.segment(0));
            if (child == null) {
                return;
            } 
            removeChildStatus(path.segment(0),depth);
        }
    }
    
}
