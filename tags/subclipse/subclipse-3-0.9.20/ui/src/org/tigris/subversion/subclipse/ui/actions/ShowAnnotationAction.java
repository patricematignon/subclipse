/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.annotations.AnnotateBlocks;
import org.tigris.subversion.subclipse.ui.annotations.AnnotateView;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;

public class ShowAnnotationAction extends WorkspaceAction {

	/**
	 * Action to open a SVN Annotate View
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// Get the selected resource.
		final ISVNRemoteFile cvsResource = getSingleSelectedSVNRemoteFile();
		execute(cvsResource);
	}

	public void execute(final ISVNRemoteFile svnResource) throws InvocationTargetException, InterruptedException {

		if (svnResource == null) {
			return;
		}

		final ISVNAnnotations[] annotations = { null };
		final AnnotateBlocks[] annotateBlocks = { null };
		
		// Run the SVN Annotate action with a progress monitor
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(null, 100);
					annotations[0] = svnResource.getAnnotations(monitor);

				
					annotateBlocks[0] = new AnnotateBlocks(annotations[0]); 
					monitor.done();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
				
			}
		}, false /* cancelable */, PROGRESS_DIALOG);

		
		// Open the view
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			try {
				PlatformUI.getWorkbench().showPerspective("org.tigris.subversion.subclipse.ui.svnPerspective", window); //$NON-NLS-1$
			} catch (WorkbenchException e1) {              
				// If this does not work we will just open the view in the
				// current perspective.
			}
		}

		try {
			AnnotateView view = AnnotateView.openInActivePerspective();
			view.showAnnotations(svnResource, annotateBlocks[0].getAnnotateBlocks(), annotations[0].getInputStream());
		} catch (PartInitException e1) {
			handle(e1);
		}
	}
	
	/**
	 * Ony enabled for single resource selection
	 */
	protected boolean isEnabled() throws TeamException {
		ISVNRemoteFile resource = getSingleSelectedSVNRemoteFile();
		return (resource != null);
	}

	/**
	 * This action is called from one of a Resource Navigator a SVN Resource
	 * Navigator or a History Log Viewer. Return the selected resource as an
	 * ISVNRemoteFile
	 * 
	 * @return ICVSResource
	 * @throws SVNException
	 */
	protected ISVNRemoteFile getSingleSelectedSVNRemoteFile() {
		// Selected from a SVN Resource Navigator or History
		ISVNRemoteFile[] svnResources = this.getSelectedRemoteFiles();
		if (svnResources.length == 1) {
			return svnResources[0];
		}

		// Selected from a Resource Navigator
		IResource[] resources = getSelectedResources();
		if (resources.length == 1) {
			try {
				return (ISVNRemoteFile)SVNWorkspaceRoot.getBaseResourceFor(resources[0]);
			} catch (SVNException e) {
				return null;
			}
		}
		return null;
	}
}
