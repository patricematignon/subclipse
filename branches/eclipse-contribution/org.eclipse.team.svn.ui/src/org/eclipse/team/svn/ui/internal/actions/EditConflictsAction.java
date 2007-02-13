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
package org.eclipse.team.svn.ui.internal.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.subversion.client.utils.Command;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.core.internal.util.File2Resource;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.conflicts.ConflictsCompareInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action to edit conflicts
 */
public class EditConflictsAction extends WorkspaceAction {
    private IFile selectedResource;
    
    public EditConflictsAction() {
        super();
    }
    
    public EditConflictsAction(IFile selectedResource) {
        this();
        this.selectedResource = selectedResource;
    }

    /**
     * edit the conflicts using built-in merger
     * 
     * @param resource
     * @param conflictOldFile
     * @param conflictWorkingFile
     * @param conflictNewFile
     * @throws InvocationTargetException
     */
    private void editConflictsInternal(IFile resource, IFile conflictOldFile,
            IFile conflictWorkingFile, IFile conflictNewFile)
            throws InvocationTargetException, InterruptedException {
        CompareConfiguration cc = new CompareConfiguration();
        ConflictsCompareInput fInput = new ConflictsCompareInput(cc);
        fInput.setResources(conflictOldFile, conflictWorkingFile,
                conflictNewFile, (IFile) resource);
        CompareUI.openCompareEditorOnPage(fInput, getTargetPage());
    }

    /**
     * edit the conflicts using an external merger
     * 
     * @param resource
     * @param conflictOldFile
     * @param conflictWorkingFile
     * @param conflictNewFile
     * @throws InvocationTargetException
     */
    private void editConflictsExternal(IFile resource, IFile conflictOldFile,
            IFile conflictWorkingFile, IFile conflictNewFile)
            throws CoreException, InvocationTargetException, InterruptedException {
        try {
            IPreferenceStore preferenceStore = SVNUIPlugin.getPlugin()
                    .getPreferenceStore();
            String mergeProgramLocation = preferenceStore
                    .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_LOCATION);
            String mergeProgramParameters = preferenceStore
                    .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_PARAMETERS);

            if (mergeProgramLocation.equals("")) { //$NON-NLS-1$
                throw new SVNException(Policy
                        .bind("EditConflictsAction.noMergeProgramConfigured")); //$NON-NLS-1$
            }
            File mergeProgramFile = new File(mergeProgramLocation);
            if (!mergeProgramFile.exists()) {
                throw new SVNException(Policy
                        .bind("EditConflictsAction.mergeProgramDoesNotExist")); //$NON-NLS-1$
            }

            Command command = new Command(mergeProgramLocation);
            String[] parameters = mergeProgramParameters.split(" ");
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = replaceParameter(parameters[i], "${theirs}", //$NON-NLS-1$
                        conflictNewFile.getLocation().toFile()
                                .getAbsolutePath());
                parameters[i] = replaceParameter(parameters[i], "${yours}", //$NON-NLS-1$
                        conflictWorkingFile.getLocation().toFile()
                                .getAbsolutePath());
                parameters[i] = replaceParameter(parameters[i], "${base}", //$NON-NLS-1$
                        conflictOldFile.getLocation().toFile()
                                .getAbsolutePath());
                parameters[i] = replaceParameter(parameters[i], "${merged}", //$NON-NLS-1$
                        resource.getLocation().toFile().getAbsolutePath());
            }
            command.setParameters(parameters);
            command.exec();

            command.waitFor();
            resource.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
     */
    protected void execute(final IAction action)
            throws InvocationTargetException, InterruptedException {

        run(new WorkspaceModifyOperation() {
            public void execute(IProgressMonitor monitor)
                    throws CoreException, InvocationTargetException, InterruptedException {
                IFile resource;
                if (selectedResource == null)
                    resource = (IFile) getSelectedResources()[0];
                else
                    resource = selectedResource;
                ISVNLocalResource svnResource = SVNWorkspaceRoot
                        .getSVNResourceFor(resource);
                try {
                    IFile conflictNewFile = (IFile) File2Resource
                            .getResource(svnResource.getStatus()
                                    .getFileConflictNew());
                    IFile conflictOldFile = (IFile) File2Resource
                            .getResource(svnResource.getStatus()
                                    .getFileConflictOld());
                    IFile conflictWorkingFile = (IFile) File2Resource
                            .getResource(svnResource.getStatus()
                                    .getFileConflictWorking());

                    IPreferenceStore preferenceStore = SVNUIPlugin.getPlugin()
                            .getPreferenceStore();
                    if (preferenceStore
                            .getBoolean(ISVNUIConstants.PREF_MERGE_USE_EXTERNAL)) {
                        editConflictsExternal(resource, conflictOldFile,
                                conflictWorkingFile, conflictNewFile);
                    } else {
                        editConflictsInternal(resource, conflictOldFile,
                                conflictWorkingFile, conflictNewFile);
                    }

                } catch (SVNException e) {
                    throw new InvocationTargetException(e);
                }
            }

        }, false /* cancelable */, PROGRESS_BUSYCURSOR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
     */
    protected String getErrorTitle() {
        return Policy.bind("EditConflictsAction.errorTitle"); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForSVNResource(org.eclipse.team.svn.core.internal.ISVNResource)
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
    
    private String replaceParameter(String input, String pattern, String value) {
         StringBuffer result = new StringBuffer();
         //startIdx and idxOld delimit various chunks of input; these
         //chunks always end where pattern begins
         int startIdx = 0;
         int idxOld = 0;
         while ((idxOld = input.indexOf(pattern, startIdx)) >= 0) {
           //grab a part of input which does not include pattern
           result.append( input.substring(startIdx, idxOld) );
           //add value to take place of pattern
           result.append( value );

           //reset the startIdx to just after the current match, to see
           //if there are any further matches
           startIdx = idxOld + pattern.length();
         }
         //the final chunk will go to the end of input
         result.append( input.substring(startIdx) );
         return result.toString();
      }
}