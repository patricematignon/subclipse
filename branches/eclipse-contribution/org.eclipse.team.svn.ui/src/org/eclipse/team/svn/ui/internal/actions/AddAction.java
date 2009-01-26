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
package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * AddAction performs a 'svn add' command on the selected resources. If a
 * container is selected, its children are recursively added.
 */
public class AddAction extends WorkspaceAction {
	
	/*
	 * @see SVNAction#execute()
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		// first we ask the user if he really want to add the selected resources if some of them are marked as ignored
        if (!promptForAddOfIgnored()) { 
            return;
        }
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InvocationTargetException {
				try {
                    // associate the resources with their respective RepositoryProvider					
					Hashtable table = getProviderMapping(getSelectedResources());
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("AddAction.adding")); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1000);
						SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.add(providerResources, IResource.DEPTH_INFINITE, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);

	}
	
	/**
	 * asks the user if he wants to add the resources if some of them are ignored
     * @return false if he answered no
	 */
	private boolean promptForAddOfIgnored() {
		IResource[] resources = getSelectedResources();
		boolean prompt = false;
		for (int i = 0; i < resources.length; i++) {
			ISVNLocalResource resource = SVNWorkspaceRoot.getSVNResourceFor(resources[i]);
			try {
				if (resource.isIgnored()) {
					prompt = true;
					break;
				} 
			} catch (SVNException e) {
				handle(e);
			}
		}
		if (prompt) {
			return MessageDialog.openQuestion(getShell(), Policy.bind("AddAction.addIgnoredTitle"), Policy.bind("AddAction.addIgnoredQuestion")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return true;
	}

	/*
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("AddAction.addFailed"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForManagedResources()
	 */
	protected boolean isEnabledForManagedResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForIgnoredResources()
	 */
	protected boolean isEnabledForIgnoredResources() {
		return true;
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForSVNResource(org.eclipse.team.svn.core.internal.ISVNResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
		// Add to version control should never be enabled for linked resources
		IResource resource = svnResource.getIResource();
		if (resource.isLinked()) return false;
		return super.isEnabledForSVNResource(svnResource);
	}

}