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
package org.tigris.subversion.subclipse.ui.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.svnclientadapter.utils.Command;

/**
 * Action to edit conflicts
 */
public class EditConflictsAction extends WorkspaceAction {

    protected void execute(final IAction action)
            throws InvocationTargetException, InterruptedException {

        run(new WorkspaceModifyOperation() {
            public void execute(IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    IPreferenceStore preferenceStore = SVNUIPlugin.getPlugin()
                            .getPreferenceStore();
                    String mergeProgramLocation = preferenceStore
                            .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_LOCATION);
                    String mergeProgramParameters = preferenceStore
                            .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_PARAMETERS);

                    if (mergeProgramLocation.equals("")) {
                        throw new SVNException(
                                Policy
                                        .bind("EditConflictsAction.noMergeProgramConfigured")); //$NON-NLS-1$
                    }
                    File mergeProgramFile = new File(mergeProgramLocation);
                    if (!mergeProgramFile.exists()) {
                        throw new SVNException(
                                Policy
                                        .bind("EditConflictsAction.mergeProgramDoesNotExist")); //$NON-NLS-1$
                    }

                    IResource resource = getSelectedResources()[0];
                    ISVNLocalResource svnResource = SVNWorkspaceRoot
                            .getSVNResourceFor(resource);
                    File conflictNewFile = svnResource.getStatus()
                            .getFileConflictNew();
                    File conflictOldFile = svnResource.getStatus()
                            .getFileConflictOld();
                    File conflictWorkingFile = svnResource.getStatus()
                            .getFileConflictWorking();

                    Command command = new Command(mergeProgramLocation);
                    String[] parameters = StringUtils.split(
                            mergeProgramParameters, ' ');
                    for (int i = 0; i < parameters.length; i++) {
                        parameters[i] = StringUtils.replace(parameters[i],
                                "${theirs}", conflictNewFile.getAbsolutePath());
                        parameters[i] = StringUtils.replace(parameters[i],
                                "${yours}", conflictWorkingFile
                                        .getAbsolutePath());
                        parameters[i] = StringUtils.replace(parameters[i],
                                "${base}", conflictOldFile.getAbsolutePath());
                        parameters[i] = StringUtils.replace(parameters[i],
                                "${merged}", svnResource.getFile()
                                        .getAbsolutePath());
                    }
                    command.setParameters(parameters);
                    command.exec();

                    command.waitFor();
                    resource.refreshLocal(IResource.DEPTH_ZERO, null);

                    // fix the action enablement
                    if (action != null)
                        action.setEnabled(isEnabled());
                } catch (TeamException e) {
                    throw new InvocationTargetException(e);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                } catch (InterruptedException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }, false /* cancelable */, PROGRESS_BUSYCURSOR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#getErrorTitle()
     */
    protected String getErrorTitle() {
        return Policy.bind("EditConflictsAction.errorTitle"); //$NON-NLS-1$
    }

    /**
     * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForSVNResource(org.tigris.subversion.subclipse.core.ISVNResource)
     */
    protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) {
        try {
            return svnResource.getStatus().isTextConflicted();
        } catch (SVNException e) {
            return false;
        }
    }

    /**
     * Method isEnabledForAddedResources.
     * 
     * @return boolean
     */
    protected boolean isEnabledForMultipleResources() {
        return false;
    }
    
}