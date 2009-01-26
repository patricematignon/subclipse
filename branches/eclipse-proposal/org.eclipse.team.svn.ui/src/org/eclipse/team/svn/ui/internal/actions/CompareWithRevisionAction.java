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
package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.history.AliasManager;
import org.eclipse.team.svn.core.internal.history.ILogEntry;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.compare.SVNCompareRevisionsInput;
import org.eclipse.team.ui.SaveablePartDialog;

/**
 * Used when you want to compare local resource with remote ones 
 */
public class CompareWithRevisionAction extends WorkspaceAction {
	
	/**
	 * Returns the selected remote file
	 */
	protected ISVNRemoteFile getSelectedRemoteFile() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return null;
		if (!(resources[0] instanceof IFile)) return null;
		IFile file = (IFile)resources[0];
		try {
			return (ISVNRemoteFile)SVNWorkspaceRoot.getBaseResourceFor(file);
		} catch (TeamException e) {
			handle(e, null, null);
			return null;
		}
	}

	/*
	 * @see SVNAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		// Setup holders
		final ISVNRemoteFile[] file = new ISVNRemoteFile[] { null };
		final ILogEntry[][] entries = new ILogEntry[][] { null };
		
		// Get the selected file
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				file[0] = getSelectedRemoteFile();
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		
		if (file[0] == null) {
			// No revisions for selected file
			MessageDialog.openWarning(getShell(), Policy.bind("CompareWithRevisionAction.noRevisions"), Policy.bind("CompareWithRevisionAction.noRevisionsLong")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		// Fetch the log entries
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(Policy.bind("CompareWithRevisionAction.fetching"), 100); //$NON-NLS-1$
					AliasManager tagManager = null;
					IResource[] resources = getSelectedResources();
					if (resources.length == 1) tagManager = new AliasManager(resources[0]);
					entries[0] = file[0].getLogEntries(Policy.subMonitorFor(monitor, 100), SVNRevision.HEAD, SVNRevision.HEAD, new SVNRevision.Number(0), false, 0, tagManager);
					monitor.done();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
		
		if (entries[0] == null) return;
		
		// Show the compare viewer
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SVNCompareRevisionsInput input = new SVNCompareRevisionsInput((IFile)getSelectedResources()[0], entries[0]);
				if(SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_SHOW_COMPARE_REVISION_IN_DIALOG)) {
					// running with a null progress monitor is fine because we have already pre-fetched the log entries above.
					input.run(new NullProgressMonitor());
					SaveablePartDialog cd = createCompareDialog(getShell(), input);
					cd.setBlockOnOpen(true);
					cd.open();
				} else {
					CompareUI.openCompareEditorOnPage(input, getTargetPage());
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	/**
	 * Return the compare dialog to use to show the compare input.
	 */
	protected SaveablePartDialog createCompareDialog(Shell shell, SVNCompareRevisionsInput input) {
		return new SaveablePartDialog(shell, input); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CompareWithRevisionAction.compare"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForSVNResource(org.eclipse.team.svn.core.internal.ISVNResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
		return (!svnResource.isFolder() && super.isEnabledForSVNResource(svnResource));
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

}