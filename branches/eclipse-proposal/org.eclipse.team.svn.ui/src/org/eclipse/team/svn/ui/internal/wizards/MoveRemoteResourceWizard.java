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
package org.eclipse.team.svn.ui.internal.wizards;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;

/**
 * Wizard to move a remote resource
 */
public class MoveRemoteResourceWizard extends Wizard {
	private MoveRemoteResourceWizardMainPage mainPage;
    private CommentCommitWizardPage commitCommentPage; 
    private ISVNRemoteResource selection;
    private Dialog parentDialog;
	
   
	public MoveRemoteResourceWizard(ISVNRemoteResource selection) {
		setWindowTitle(Policy.bind("moveRemoteFolderWizard.title")); //$NON-NLS-1$
        this.selection = selection;
	}

	/**
	 * Creates the wizard pages
	 */
	public void addPages() {
		// add the main page
        mainPage = new MoveRemoteResourceWizardMainPage(
            "newRemoteFolderPage1",  //$NON-NLS-1$ 
            Policy.bind("MoveRemoteFolderWizard.heading"), //$NON-NLS-1$
            SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_NEW_FOLDER));
        mainPage.setRemoteResource(selection);
		addPage(mainPage);
        
        // add commit comment page
        String pageTitle = Policy.bind("CommentCommitWizardPage.pageTitle"); //$NON-NLS-1$
        String pageDescription = Policy.bind("CommentCommitWizardPage.pageDescription"); //$NON-NLS-1$
        ImageDescriptor image = SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_NEW_FOLDER);
        commitCommentPage = new CommentCommitWizardPage(parentDialog, pageTitle, pageTitle, image, pageDescription);
        addPage(commitCommentPage); 
                
	}
    
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
        try {
            SVNUIPlugin.runWithProgress(getContainer().getShell(), false /*cancelable*/, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        SVNProviderPlugin.getPlugin().getRepositoryResourcesManager().
                            moveRemoteResource(
                                selection,mainPage.getParentFolder(),
                                mainPage.getResourceName(),
                                commitCommentPage.getComment(),monitor);
                        
                    } catch (SVNException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            // operation canceled
        } catch (InvocationTargetException e) {
            SVNUIPlugin.openError(getContainer().getShell(), Policy.bind("exception"), null, e.getCause(), SVNUIPlugin.PERFORM_SYNC_EXEC); //$NON-NLS-1$
            return false;
        }
	   return true;
	}
    
    /**
     * Method setParentDialog.
     * @param dialog
     */
    public void setParentDialog(Dialog dialog) {
        this.parentDialog = dialog;
    }    
    
}
