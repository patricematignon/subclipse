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
package org.tigris.subversion.subclipse.ui.actions;
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.commands.GetStatusCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResource;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.dialogs.RevertDialog;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Action to restore pristine working copy file 
 */
public class RevertAction extends WorkspaceAction {
    
    private IResource[] resourcesToRevert;
    private String url;
	
	protected void execute(final IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
	    run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InvocationTargetException {
				try {
				    IResource[] modified = getModifiedResources(resources, monitor);
				    if (!confirmRevert(modified)) return;
					for (int i = 0; i < resourcesToRevert.length; i++) {
						
						ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resourcesToRevert[i]);
						if (svnResource instanceof LocalResource) ((LocalResource)svnResource).revert(false);
						else svnResource.revert();
						
						// Revert on a file can also be used to resolve a merge conflict
						if (resourcesToRevert[i].getType() == IResource.FILE) {
							resourcesToRevert[i].getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
						} else {
							resourcesToRevert[i].refreshLocal(IResource.DEPTH_INFINITE, monitor);
						}
					}
					// fix the action enablement
					if (action != null) action.setEnabled(isEnabled());
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	/**
	 * get the modified resources in resources parameter
	 */	
	private IResource[] getModifiedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    final List modified = new ArrayList();
		final SVNException[] exception = new SVNException[] { null };		
	    for (int i = 0; i < resources.length; i++) {
			 IResource resource = resources[i];
			 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			 
			 // if only one resource selected, get url.  Revert dialog displays this.
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
	    return (IResource[]) modified.toArray(new IResource[modified.size()]);
	}
	
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
	 * prompt revert of selected resources.
	 */		
	protected boolean confirmRevert(IResource[] modifiedResources) {
	   if (modifiedResources.length == 0) return false;
	   RevertDialog dialog = new RevertDialog(getShell(), modifiedResources, url);
	   boolean revert = (dialog.open() == RevertDialog.OK);
	   url = null;
	   resourcesToRevert = dialog.getSelectedResources();
	   return revert;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("RevertAction.revert"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForManagedResources()
	 */
	protected boolean isEnabledForManagedResources() {
		return true;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return false;
	}

    /*
     *  (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForInaccessibleResources()
     */
    protected boolean isEnabledForInaccessibleResources() {
        return true;
    }
	
}
