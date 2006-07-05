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
package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.subversion.client.ISVNProperty;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.ui.internal.Policy;

/**
 * action to modify a property
 */
public class SVNPropertyDeleteAction extends SVNPropertyAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action)
		throws InvocationTargetException, InterruptedException {
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
					ISVNProperty[] svnProperties = getSelectedSvnProperties();

					String message;
					if (svnProperties.length == 1) {
						message = Policy.bind("SVNPropertyDeleteAction.confirmSingle",svnProperties[0].getName()); //$NON-NLS-1$
					} else {
						message = Policy.bind("SVNPropertyDeleteAction.confirmMultiple",Integer.toString(svnProperties.length)); //$NON-NLS-1$
					}
										
					if (!MessageDialog.openQuestion(getShell(), Policy.bind("SVNPropertyDeleteAction.title"), message)) { //$NON-NLS-1$
						return; 
					}
					
					for (int i = 0; i < svnProperties.length;i++) {
						ISVNProperty svnProperty = svnProperties[i];  
						ISVNLocalResource svnResource = getSVNLocalResource(svnProperty);
						try {
							svnResource.deleteSvnProperty(svnProperty.getName(),false);
						} catch (SVNException e) {
							throw new InvocationTargetException(e);
						}
						
					}
				} 
			}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedSvnProperties().length > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("SVNPropertyDeleteAction.delete"); //$NON-NLS-1$
	}


}
