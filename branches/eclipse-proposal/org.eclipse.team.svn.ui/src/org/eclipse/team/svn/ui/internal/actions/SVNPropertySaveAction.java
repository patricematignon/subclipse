/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.eclipse.team.svn.ui.internal.actions;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.subversion.client.ISVNProperty;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.dialogs.SaveAsDialog;

/**
 * action to save a property
 */
public class SVNPropertySaveAction extends SVNPropertyAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action)
		throws InvocationTargetException, InterruptedException {
		ISVNProperty svnProperty = getSelectedSvnProperties()[0];

		SaveAsDialog dialog = new SaveAsDialog(getShell());

		if (dialog.open() != SaveAsDialog.OK)
			return;

		IFile file =
			ResourcesPlugin.getWorkspace().getRoot().getFile(
				dialog.getResult());
		try {
			ByteArrayInputStream is =
				new ByteArrayInputStream(svnProperty.getData());
			file.create(is, true, null);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedSvnProperties().length == 1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("SVNPropertySaveAction.save"); //$NON-NLS-1$
	}

}