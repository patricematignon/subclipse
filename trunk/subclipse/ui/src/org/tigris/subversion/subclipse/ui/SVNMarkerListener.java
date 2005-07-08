/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.IResourceStateChangeListener;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

public class SVNMarkerListener implements IResourceStateChangeListener {

    public SVNMarkerListener() {
        super();
        SVNProviderPlugin.addResourceStateChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
     */
    public void resourceSyncInfoChanged(IResource[] changedResources) {
        for (int i = 0; i < changedResources.length; i++) {
    	    try {
    	    	if (changedResources[i].exists())
    	    	{
    	    		changedResources[i].deleteMarkers("org.tigris.subversion.subclipse.ui.conflictMarker", true, IResource.DEPTH_ZERO); //$NON-NLS-1$
    	    		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(changedResources[i]);
    	    		LocalResourceStatus status = svnResource.getStatus();
    	    		if (status.isTextConflicted()) {
    	    			try {
    	    				IMarker marker = changedResources[i].createMarker("org.tigris.subversion.subclipse.ui.conflictMarker"); //$NON-NLS-1$
    	    				marker.setAttribute(IMarker.MESSAGE, Policy.bind("SVNConflicts")); //$NON-NLS-1$
    	    				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    	    			} catch (Exception e) {
    	    				SVNUIPlugin.log(e.getMessage());
    	    			}
    	    		}
    	    	}
    	    } catch (Exception e) {
    	        SVNUIPlugin.log(e.getMessage());
    	    }
        }

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#resourceModified(org.eclipse.core.resources.IResource[])
     */
    public void resourceModified(IResource[] changedResources) {

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
     */
    public void projectConfigured(IProject project) {

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
     */
    public void projectDeconfigured(IProject project) {

    }

}
