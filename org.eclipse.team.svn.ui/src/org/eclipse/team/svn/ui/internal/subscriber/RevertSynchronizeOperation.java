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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.ui.internal.dialogs.RevertDialog;
import org.eclipse.team.svn.ui.internal.operations.RevertOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class RevertSynchronizeOperation extends SVNSynchronizeOperation {
	private String url;
	private IResource[] resources;
	private IResource[] resourcesToRevert;
	private boolean revert;
	private boolean prompted;

	public RevertSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, String url, IResource[] resources) {
		super(configuration, elements);
		this.url = url;
		this.resources = resources;
	}

	protected boolean promptForConflictHandling(Shell shell, SyncInfoSet syncSet) {
		return true;
	}

	protected void run(SVNTeamProvider provider, SyncInfoSet set, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (prompted) return;
		prompted = true;
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (resources == null || resources.length == 0) {
					revert = false;
					return;
				}
				RevertDialog dialog = new RevertDialog(getShell(), resources, url);
				revert = (dialog.open() == RevertDialog.OK);
				if (revert) resourcesToRevert = dialog.getSelectedResources();
			}
		});
		if (revert) new RevertOperation(getPart(), resourcesToRevert).run();
	}

}
