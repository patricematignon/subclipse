/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.subscriber;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.dialogs.CommitDialog;
import org.tigris.subversion.subclipse.ui.operations.CommitOperation;
import org.tigris.subversion.subclipse.ui.settings.ProjectProperties;

/**
 * Sync view operation for putting file system resources
 */
public class CommitSynchronizeOperation extends SVNSynchronizeOperation {
	private SyncInfoSet syncSet;
    private String commitComment;
    private IResource[] resourcesToCommit;
    private String url;
    private boolean unaddedResources;	
	private boolean commit;

	protected CommitSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, String url) {
		super(configuration, elements);
		this.url = url;
	}
	
	protected SyncInfoSet getSyncInfoSet() {
		if (syncSet == null) {
			syncSet = super.getSyncInfoSet();
			if (!promptForConflictHandling(getShell(), syncSet)) {
				syncSet.clear();
				return syncSet;
			}
			if (!confirmCommit()) {
			    syncSet.clear();
			    return syncSet;
			}
		}
	    return syncSet;
	}
	
	private boolean confirmCommit() {
	    commit = false;
	    IResource[] modified = syncSet.getResources();
	    if (modified.length > 0) {
	        try {
                ProjectProperties projectProperties = ProjectProperties.getProjectProperties(modified[0]);
                IResource[] unaddedResources = getUnaddedResources();
                final CommitDialog dialog = new CommitDialog(getShell(), modified, url, unaddedResources.length > 0, projectProperties);
        		getShell().getDisplay().syncExec(new Runnable() {
        			public void run() {
        				commit = (dialog.open() == CommitDialog.OK);
        			}
        		});
        	    if (commit) {
        	        resourcesToCommit = dialog.getSelectedResources();
        	        commitComment = dialog.getComment();
        	    }
	        } catch (SVNException e) {
                e.printStackTrace();
            }
	    }
		return commit;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.examples.filesystem.ui.FileSystemSynchronizeOperation#promptForConflictHandling(org.eclipse.swt.widgets.Shell, org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected boolean promptForConflictHandling(Shell shell, SyncInfoSet syncSet) {
		// If there is a conflict in the syncSet, we need to prompt the user before proceeding.
		if (syncSet.hasConflicts() || syncSet.hasIncomingChanges()) {
			switch (promptForConflicts(shell, syncSet)) {
			case 0:
				// Yes, synchronize conflicts as well
				break;
			case 1:
				// No, stop here
				syncSet.removeConflictingNodes();
				syncSet.removeIncomingNodes();
				break;
			case 2:
			default:
				// Cancel
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * Prompts the user to determine how conflicting changes should be handled.
	 * Note: This method is designed to be overridden by test cases.
	 * @return 0 to sync conflicts, 1 to sync all non-conflicts, 2 to cancel
	 */
	private int promptForConflicts(Shell shell, SyncInfoSet syncSet) {
		String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
		String title = "Confirm Overwrite"; //$NON-NLS-1$
		String question = "You have changes that conflict with the server. Release those changes?"; //$NON-NLS-1$
		final MessageDialog dialog = new MessageDialog(shell, title, null, question, MessageDialog.QUESTION, buttons, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode();
	}
	
	private IResource[] getUnaddedResources() {
		IResource[] resources = syncSet.getResources();
		List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!isAdded(resource)) {
				result.add(resource);
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	private boolean isAdded(IResource resource) {
	    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
			if (svnResource.isIgnored())
				return false;
			// visit the children of shared resources
			if (svnResource.isManaged())
				return false;
			if ((resource.getType() == IResource.FOLDER) && isSymLink(resource)) // don't traverse into symlink folders
				return false;
		} catch (SVNException e) {
		    e.printStackTrace();
		    return false;
		}
		return true;
    }
	
	private boolean isSymLink(IResource resource) {
		File file = resource.getLocation().toFile();
	    try {
	    	if (!file.exists())
	    		return true;
	    	else {
	    		String cnnpath = file.getCanonicalPath();
	    		String abspath = file.getAbsolutePath();
	    		return !abspath.equals(cnnpath);
	    	}
	    } catch(IOException ex) {
	      return true;
	    }	
	}		

    /* (non-Javadoc)
	 * @see org.eclipse.team.examples.filesystem.ui.FileSystemSynchronizeOperation#run(org.eclipse.team.examples.filesystem.FileSystemProvider, org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(SVNTeamProvider provider, SyncInfoSet set, IProgressMonitor monitor) {
		//SVNOperations.getInstance().checkin(set.getResources(), IResource.DEPTH_INFINITE, true, monitor);
	    if (commit) {
	        final IResource[][] resourcesToBeAdded = new IResource[][] { null };
		    List toBeAddedList = new ArrayList();
		    for (int i = 0; i < resourcesToCommit.length; i++) {
		        IResource resource = resourcesToCommit[i];
		        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		        try {
                    if (!svnResource.isManaged()) toBeAddedList.add(resource);
                } catch (SVNException e) {
                    e.printStackTrace();
                }
		    }
		    resourcesToBeAdded[0] = new IResource[toBeAddedList.size()];
		    toBeAddedList.toArray(resourcesToBeAdded[0]);
		    try {
                new CommitOperation(getPart(), resourcesToCommit, resourcesToBeAdded[0], resourcesToCommit, commitComment).run();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
	    }
	}

}
