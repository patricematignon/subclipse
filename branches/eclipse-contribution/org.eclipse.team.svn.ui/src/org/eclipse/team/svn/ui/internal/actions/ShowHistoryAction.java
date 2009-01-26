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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.ui.history.IHistoryView;

/**
 * Show history for selected remote file
 */
public class ShowHistoryAction extends SVNAction {

		/*
	 * @see SVNAction#executeIAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				ISVNRemoteResource[] resources = getSelectedRemoteResources();
				IHistoryView view = (IHistoryView)showView(ISVNUIConstants.HISTORY_VIEW_ID);
				if (view != null) {
					view.showHistoryFor(resources[0]);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		ISVNRemoteResource[] resources = getSelectedRemoteResources();
		return resources.length == 1;
	}
	/**
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ShowHistoryAction.showHistory"); //$NON-NLS-1$
	}

}