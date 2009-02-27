/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.subscriber;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.CommitDialog;
import org.eclipse.team.svn.ui.internal.operations.CommitOperation;
import org.eclipse.team.svn.ui.internal.settings.ProjectProperties;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Sync view operation for putting file system resources
 */
public class CommitSynchronizeOperation extends SVNSynchronizeOperation {
    private String commitComment;
    private IResource[] resourcesToCommit;
    private String url;
    private boolean commit;
    private boolean keepLocks;
    private String proposedComment;

	protected CommitSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, String url, String proposedComment) {
		super(configuration, elements);
		this.url = url;
		this.proposedComment = proposedComment;
	}
	
	private boolean confirmCommit(SyncInfoSet set) {
	    commit = false;
	    IResource[] modified = set.getResources();
	    if (modified.length > 0) {
	        try {
                ProjectProperties projectProperties = ProjectProperties.getProjectProperties(modified[0]);
                IResource[] unaddedResources = getUnaddedResources(set);
                final CommitDialog dialog = new CommitDialog(getShell(), modified, url, unaddedResources.length > 0, projectProperties);
                dialog.setComment(proposedComment);
        		getShell().getDisplay().syncExec(new Runnable() {
        			public void run() {
        				commit = (dialog.open() == CommitDialog.OK);
        			}
        		});
        	    if (commit) {
        	        resourcesToCommit = dialog.getSelectedResources();
        	        commitComment = dialog.getComment();
        	        keepLocks = dialog.isKeepLocks();
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
		return true;
	}
	
	/**
	 * Prompts the user to determine how conflicting changes should be handled.
	 * Note: This method is designed to be overridden by test cases.
	 * @return 0 to sync conflicts, 1 to sync all non-conflicts, 2 to cancel
	 */
	private int promptForConflicts(Shell shell, SyncInfoSet syncSet) {
		String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL};
		String title = Policy.bind("SyncAction.commit.conflict.title"); //$NON-NLS-1$
		String question = Policy.bind("SyncAction.commit.conflict.question"); //$NON-NLS-1$
		final MessageDialog dialog = new MessageDialog(shell, title, null, question, MessageDialog.QUESTION, buttons, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode();
	}
	
	private IResource[] getUnaddedResources(SyncInfoSet set) {
	    IResource[] resources = set.getResources();
		List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (isAdded(resource)) {
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
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// First, ask the user if they want to include conflicts
		SyncInfoSet syncSet = getSyncInfoSet();
		if (!promptForConflictHandling(getShell(), syncSet)) return;
		// Divide the sync info by project
		final Map projectSyncInfos = getProjectSyncInfoSetMap(syncSet);
		Iterator iter = projectSyncInfos.keySet().iterator();
		final IProject project = (IProject) iter.next();
		SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(project, SVNProviderPlugin.PROVIDER_ID);
		monitor.beginTask(null, projectSyncInfos.size() * 100);
		run(provider, syncSet, Policy.subMonitorFor(monitor,100));
		monitor.done();
	}

    /* (non-Javadoc)
	 * @see org.eclipse.team.examples.filesystem.ui.FileSystemSynchronizeOperation#run(org.eclipse.team.examples.filesystem.FileSystemProvider, org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(SVNTeamProvider provider, SyncInfoSet set, IProgressMonitor monitor) {
		if (set.hasConflicts() || set.hasIncomingChanges()) {
			switch (promptForConflicts(getShell(), set)) {
			case 0:
				// Yes, commit non-conflicts
				set.removeConflictingNodes();
				set.removeIncomingNodes();
				break;
			case 1:
				// No, stop here
				return;
			default:
				return;
			}	
		}
	    if (confirmCommit(set)) {
	        final IResource[][] resourcesToBeAdded = new IResource[][] { null };
	        final IResource[][] resourcesToBeDeleted = new IResource[][] { null };
		    List toBeAddedList = new ArrayList();
		    List toBeDeletedList = new ArrayList();
		    for (int i = 0; i < resourcesToCommit.length; i++) {
		        IResource resource = resourcesToCommit[i];
		        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		        try {
                    if (!svnResource.isManaged()) toBeAddedList.add(resource);
                    if (svnResource.getStatus().isMissing()) toBeDeletedList.add(resource);
                } catch (SVNException e) {
                    e.printStackTrace();
                }
		    }
		    resourcesToBeAdded[0] = new IResource[toBeAddedList.size()];
		    toBeAddedList.toArray(resourcesToBeAdded[0]);
		    resourcesToBeDeleted[0] = new IResource[toBeDeletedList.size()];
		    toBeDeletedList.toArray(resourcesToBeDeleted[0]);
		    try {
                new CommitOperation(getPart(), resourcesToCommit, resourcesToBeAdded[0], resourcesToBeDeleted[0], resourcesToCommit, commitComment, keepLocks).run();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
	    }
	}

}