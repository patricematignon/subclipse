/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
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
import org.eclipse.team.svn.ui.internal.dialogs.IgnoreResourcesDialog;
import org.eclipse.team.svn.ui.internal.operations.IgnoreOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class IgnoreSynchronizeOperation extends SVNSynchronizeOperation {
	private IResource[] resources;
	private IgnoreResourcesDialog ignoreResourcesDialog;
	private boolean cancel;

	public IgnoreSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, IResource[] resources) {
		super(configuration, elements);
		this.resources = resources;
	}

	protected boolean promptForConflictHandling(Shell shell, SyncInfoSet syncSet) {
		return true;
	}

	protected void run(SVNTeamProvider provider, SyncInfoSet set, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				ignoreResourcesDialog = new IgnoreResourcesDialog(getShell(), resources);
				cancel = ignoreResourcesDialog.open() == IgnoreResourcesDialog.CANCEL;
			}
		});	
		if (cancel) return;
		new IgnoreOperation(getPart(), resources, ignoreResourcesDialog).run();
	}

}
