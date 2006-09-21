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
package org.tigris.subversion.subclipse.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

public class CopyAction extends WorkspaceAction implements IWorkbenchWindowActionDelegate {

	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}	

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		final IProject project = resources[0].getProject();
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), project, false, Policy.bind("CopyAction.selectionLabel")); //$NON-NLS-1$
		if (dialog.open() == ContainerSelectionDialog.CANCEL) return;
		Object[] result = dialog.getResult();
		if (result == null || result.length == 0) return;
		final Path path = (Path)result[0];
		IProject selectedProject;
		File target = null;
		if (path.segmentCount() == 1) {
			selectedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(path.toString());
			target = selectedProject.getLocation().toFile();
		} else {
			IFile targetFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			selectedProject = targetFile.getProject();
			target = targetFile.getLocation().toFile();
		}
		final IProject targetProject = selectedProject;
		final File destPath = target;
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				try {
					ISVNClientAdapter client = null;
					for (int i = 0; i < resources.length; i++) {
						final IResource resource = resources[i];
						if (client == null)
						    client = SVNWorkspaceRoot.getSVNResourceFor(resources[i]).getRepository().getSVNClient();
						File checkFile = new File(destPath.getPath() + File.separator + resource.getName());
						File srcPath = new File(resource.getLocation().toString());
						File newDestPath = new File(destPath.getPath() + File.separator + resource.getName());
						if (checkFile.exists()) {
							IInputValidator inputValidator = new IInputValidator() {
								public String isValid(String newText) {
									if (newText.equals(resource.getName())) 
										return Policy.bind("CopyAction.nameConflictSame"); //$NON-NLS-1$
									return null;
								}								
							};
							InputDialog inputDialog = new InputDialog(getShell(), Policy.bind("CopyAction.nameConflictTitle"), Policy.bind("CopyAction.nameConflictMessage", resource.getName()), "Copy of " + resource.getName(), inputValidator); //$NON-NLS-1$
							if (inputDialog.open() == InputDialog.CANCEL) return;
							String newName = inputDialog.getValue();
							if (newName == null  || newName.trim().length() == 0) return;
							newDestPath = new File(destPath.getPath() + File.separator + newName);
						}
						if (project.getFullPath().isPrefixOf(path))
							client.copy(srcPath, newDestPath);
						else
							client.doExport(srcPath, newDestPath, true);
					}
					targetProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				} catch (Exception e) {
					MessageDialog.openError(getShell(), Policy.bind("CopyAction.copy"), e.getMessage()); //$NON-NLS-1$
				}
			}			
		});
	}

	protected boolean isEnabled() throws TeamException {
		// Only enabled if all selections are from same project.
		boolean enabled = super.isEnabled();
		if (!enabled) return false;
		IResource[] resources = getSelectedResources();
		IProject project = null;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof IProject) return false;
			if (project != null && !resources[i].getProject().equals(project)) return false;
			project = resources[i].getProject();
		}
		return true;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
	}
	
}
