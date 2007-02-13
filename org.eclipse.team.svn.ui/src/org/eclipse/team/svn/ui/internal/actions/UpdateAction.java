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
 *     Panagiotis Korros (panagiotis.korros@gmail.com) - added operations support
 *******************************************************************************/
package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.operations.UpdateOperation;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * UpdateAction performs a 'svn update' command on the selected resources.
 * If conflicts are present (file has been changed both remotely and locally),
 * the changes will be merged into the local file such that the user must
 * resolve the conflicts. 
 */
public class UpdateAction extends WorkspaceAction implements IWorkbenchWindowActionDelegate {

	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
        if (action != null && !action.isEnabled()) { 
        	action.setEnabled(true);
        } 
        else {
	    	    new UpdateOperation(getTargetPart(), getSelectedResources(), SVNRevision.HEAD, true).run();
        } 		
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("UpdateAction.updateerror"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
	}    

}
