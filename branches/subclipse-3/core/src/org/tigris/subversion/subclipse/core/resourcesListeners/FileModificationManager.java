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
package org.tigris.subversion.subclipse.core.resourcesListeners;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

/**
 * This class performs several functions related to determining the modified
 * status of files under Subversion control. First, it listens for change delta's for
 * files and brodcasts them to all listeners. It also registers as a save
 * participant so that deltas generated before the plugin are loaded are not
 * missed. 
 */
public class FileModificationManager implements IResourceChangeListener, ISaveParticipant {
	
	
	
	private Set modifiedResources = new HashSet();

	// consider the following changes types and ignore the others (e.g. marker and description changes are ignored)
	protected int INTERESTING_CHANGES = IResourceDelta.CONTENT | 
	                                    IResourceDelta.MOVED_FROM | 
										IResourceDelta.MOVED_TO |
										IResourceDelta.OPEN | 
										IResourceDelta.REPLACED |
										IResourceDelta.TYPE;

	/**
	 * Listen for file modifications and fire modification state changes
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					IResource resource = delta.getResource();
					
					if (resource.getType()==IResource.PROJECT) {
						IProject project = (IProject)resource;
						if (!project.isAccessible()) {
							return false;
						}
						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
							return false;
						} 
						if (RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId()) == null) {
							return false; // not a svn handled project
						}
					}
					
					if (resource.getType()==IResource.FILE && delta.getKind() == IResourceDelta.CHANGED && resource.exists()) {
						int flags = delta.getFlags();
						if((flags & INTERESTING_CHANGES) != 0) {
                            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                            modifiedResources.add(resource);
						}
					} else if (delta.getKind() == IResourceDelta.ADDED) {
                        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                        modifiedResources.add(resource);                        
					} else if (delta.getKind() == IResourceDelta.REMOVED) {
						// provide notifications for deletions since they may not have been managed
						// The move/delete hook would have updated the parent counts properly
						modifiedResources.add(resource);
					}
					return true;
				}
			});
            
            // we refresh all changed resources and broadcast the changes to all listeners (ex : SVNLightwrightDecorator)
			if (!modifiedResources.isEmpty()) {
                IResource[] resources = (IResource[])modifiedResources.toArray(new IResource[modifiedResources.size()]);
				refreshStatus(resources);
                SVNProviderPlugin.broadcastModificationStateChanges(resources);
				modifiedResources.clear();
			}
		} catch (CoreException e) {
			SVNProviderPlugin.log(e.getStatus());
		}

	}

	/**
	 * refresh (reset) the status of all the given resources
     */
    private void refreshStatus(IResource[] resources) {
        for (int i = 0; i < resources.length;i++) {
    		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[i]);
    		try {
				svnResource.refreshStatus();
			} catch (SVNException e) {
				e.printStackTrace();
			}
        }
    }
    
    
	/**
	 * We register a save participant so we can get the delta from workbench
	 * startup to plugin startup.
	 * @throws CoreException
	 */
	public void registerSaveParticipant() throws CoreException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISavedState ss = ws.addSaveParticipant(SVNProviderPlugin.getPlugin(), this);
		if (ss != null) {
			ss.processResourceChangeEvents(this);
		}
		ws.removeSaveParticipant(SVNProviderPlugin.getPlugin());
	}
	
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) {
	}


}

