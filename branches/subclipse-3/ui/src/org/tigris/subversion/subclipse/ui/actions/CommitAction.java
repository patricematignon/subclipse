/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRunnable;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.commands.GetStatusCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.dialogs.CommitDialog;
import org.tigris.subversion.subclipse.ui.repository.RepositoryManager;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Action for checking in files to a subversion provider
 * Prompts the user for a release comment, and shows a selection
 * list of added and modified resources, including unversioned resources.
 * If selected, unversioned resources will be added to version control,
 * and committed.
 */
public class CommitAction extends WorkspaceAction {
    private String commitComment;
    private IResource[] resourcesToCommit;
    private String url;
    private boolean unaddedResources;
    private boolean commit;
	
	/*
     * get non added resources and prompts for resources to be added
     * prompts for comments
     * add non added files
     * commit selected files
	 * @see SVNAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		final RepositoryManager manager = SVNUIPlugin.getPlugin().getRepositoryManager();
		final IResource[][] resourcesToBeAdded = new IResource[][] { null };
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
				    // search for modified or added, non-ignored resources in the selection.
				    IResource[] modified = getModifiedResources(resources, monitor);
					
				    // if no changes since last commit, do not show commit dialog.
				    if (modified.length == 0) {
					    MessageDialog.openInformation(getShell(), Policy.bind("CommitDialog.title"), Policy.bind("CommitDialog.noChanges")); //$NON-NLS-1$
					    commit = false;
					} else
					    commit = confirmCommit(modified);

				    // if commit was not canceled, create a list of any
				    // unversioned resources that were selected.
					if (commit) {
					    List toBeAddedList = new ArrayList();
					    for (int i = 0; i < resourcesToCommit.length; i++) {
					        IResource resource = resourcesToCommit[i];
					        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
					        if (!svnResource.isManaged()) toBeAddedList.add(resource);
					    }
					    resourcesToBeAdded[0] = new IResource[toBeAddedList.size()];
					    toBeAddedList.toArray(resourcesToBeAdded[0]);					   
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, true /* cancelable */, PROGRESS_BUSYCURSOR); //$NON-NLS-1$
		
		if (!commit) {
			return; // user canceled
		}
		
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws  InvocationTargetException {
				try {
					// execute the add and commit in a single SVN runnable so sync changes are batched
					SVNProviderPlugin.run(
						new ISVNRunnable() {
							public void run(IProgressMonitor monitor) throws SVNException {
								try {
									int ticks=100;
									monitor.beginTask(null, ticks);
									
									// add unversioned resources.
									if (resourcesToBeAdded[0].length > 0) {
										int addTicks = 20;
										manager.add(resourcesToBeAdded[0], Policy.subMonitorFor(monitor, addTicks));
										ticks-=addTicks;
									}
									
									// commit resources.
									manager.commit(resourcesToCommit, commitComment, Policy.subMonitorFor(monitor,ticks));
									resourcesToCommit = null;
									commitComment = null;
									for (int i = 0; i < resources.length; i++) {
										IResource projectHandle = resources[i].getProject();
										projectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
									}
								} catch (TeamException e) {
									throw SVNException.wrapException(e);
								} catch (CoreException e) {
									throw SVNException.wrapException(e);
								}
							}
						}, monitor);
				} catch (SVNException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	/**
	 * get the unadded resources in resources parameter
	 */
	private IResource[] getUnaddedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
		final List unadded = new ArrayList();
		final SVNException[] exception = new SVNException[] { null };
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
            if (resource.exists()) {
			    // visit each resource deeply
			    try {
				    resource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
						// skip ignored resources and their children
						try {
							if (svnResource.isIgnored())
								return false;
							// visit the children of shared resources
							if (svnResource.isManaged())
								return true;
						} catch (SVNException e) {
							exception[0] = e;
						}
						// file/folder is unshared so record it
						unadded.add(resource);
						return resource.getType() == IResource.FOLDER;
					}
				}, IResource.DEPTH_INFINITE, false /* include phantoms */);
			    } catch (CoreException e) {
				    throw SVNException.wrapException(e);
			    }
			    if (exception[0] != null) throw exception[0];
            }
		}
		unaddedResources = unadded.size() > 0;
		return (IResource[]) unadded.toArray(new IResource[unadded.size()]);
	}	

	/**
	 * get the modified and unadded resources in resources parameter
	 */	
	private IResource[] getModifiedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    final List modified = new ArrayList();
		final SVNException[] exception = new SVNException[] { null };		
	    for (int i = 0; i < resources.length; i++) {
			 IResource resource = resources[i];
			 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			 
			 // if only one resource selected, get url.  Commit dialog displays this.
			 if (resources.length == 1) {
				   SVNUrl svnUrl = svnResource.getStatus().getUrl();
				   if ((svnUrl == null) || (resource.getType() == IResource.FILE)) url = getParentUrl(svnResource);
				   else url = svnResource.getStatus().getUrl().toString();
			 }
			 
			 // get adds, deletes, updates and property updates.
			 GetStatusCommand command = new GetStatusCommand(svnResource);
			 command.run(iProgressMonitor);
			 LocalResourceStatus[] statuses = command.getStatuses();
			 for (int j = 0; j < statuses.length; j++) {
			     if (statuses[j].isTextModified() || statuses[j].isAdded() || statuses[j].isDeleted() || statuses[j].getPropStatus().equals(SVNStatusKind.MODIFIED)) {
			         IResource currentResource = null;
			         currentResource = GetStatusCommand.getResource(statuses[j]);
			         if (currentResource != null)
			             modified.add(currentResource);
			     }
			 }
		}
	    // get unadded resources and add them to the list.
	    IResource[] unaddedResources = getUnaddedResources(resources, iProgressMonitor);
	    for (int i = 0; i < unaddedResources.length; i++)
	        modified.add(unaddedResources[i]);
	    return (IResource[]) modified.toArray(new IResource[modified.size()]);
	}

	/**
	 * for an unadded resource, get url from parent.
	 */	
	private String getParentUrl(ISVNLocalResource svnResource) throws SVNException {
        ISVNLocalFolder parent = svnResource.getParent();
        while (parent != null) {
            SVNUrl url = parent.getStatus().getUrl();
            if (url != null) return url.toString();
            parent = parent.getParent();
        }
        return null;
    }

	/**
	 * prompt commit of selected resources.
	 * @throws SVNException
	 */		
	protected boolean confirmCommit(IResource[] modifiedResources) throws SVNException {
	   if (onTagPath(modifiedResources)) {
	       // Warning - working copy appears to be on a tag path.
	       if (!MessageDialog.openQuestion(getShell(), Policy.bind("CommitDialog.title"), Policy.bind("CommitDialog.tag"))) //$NON-NLS-1$
	           return false;	       
	   }
	   CommitDialog dialog = new CommitDialog(getShell(), modifiedResources, url, unaddedResources);
	   boolean commit = (dialog.open() == CommitDialog.OK);
	   url = null;
	   commitComment = dialog.getComment();
	   resourcesToCommit = dialog.getSelectedResources();
	   return commit;
	}

	private boolean onTagPath(IResource[] modifiedResources) throws SVNException {
	    // Multiple resources selected.
	    if (url == null) {
			 IResource resource = modifiedResources[0];
			 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);	        
             SVNUrl svnUrl = svnResource.getStatus().getUrl();
             String firstUrl;
             if ((svnUrl == null) || (resource.getType() == IResource.FILE)) firstUrl = getParentUrl(svnResource);
             else firstUrl = svnResource.getStatus().getUrl().toString();
             if (firstUrl.indexOf("/tags/") != -1) return true; //$NON-NLS-1$
	    }
	    // One resource selected.
        else if (url.indexOf("/tags/") != -1) return true; //$NON-NLS-1$
        return false;
    }

    /**
	 * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CommitAction.commitFailed"); //$NON-NLS-1$
	}

	/**
	 * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
    
    /*
     *  (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForInaccessibleResources()
     */
    protected boolean isEnabledForInaccessibleResources() {
        return true;
    }
}
