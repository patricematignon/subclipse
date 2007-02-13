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
 
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends WorkspaceAction {
	
	public void execute(IAction action) {
		
	}
	
	protected IResource[] getResourcesToSync() {
		return getSelectedResources();
	}
	
	
	
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a
	 * SVN folder.
	 * 
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForSVNResource(org.eclipse.team.svn.core.internal.ISVNResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
		return super.isEnabledForSVNResource(svnResource) || svnResource.getParent().isManaged();
	}

}
