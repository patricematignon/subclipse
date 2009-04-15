/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.wizards.sharing;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.comments.CommitCommentArea;
import org.tigris.subversion.subclipse.ui.settings.CommentProperties;
import org.tigris.subversion.subclipse.ui.wizards.SVNWizardPage;

/**
 * The finish page of the sharing wizard
 */
public class SharingWizardFinishPage extends SVNWizardPage {

	private CommitCommentArea commitCommentArea;
	private CommentProperties commentProperties;
	
	public SharingWizardFinishPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_FINISH_PAGE);
		Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
		label.setText(Policy.bind("SharingWizardFinishPage.message")); //$NON-NLS-1$
		GridData data = new GridData();
		data.widthHint = 350;
		label.setLayoutData(data);
		SharingWizard wizard = (SharingWizard)getWizard();
		IProject project = wizard.getProject();
        try {
            commentProperties = CommentProperties.getCommentProperties(project);
        } catch (SVNException e) {}
		commitCommentArea = new CommitCommentArea(null, null, commentProperties); //$NON-NLS-1$
		commitCommentArea.setOldComment(Policy.bind("SharingWizard.initialImport")); //$NON-NLS-1$
		if ((commentProperties != null) && (commentProperties.getMinimumLogMessageSize() != 0)) {
		    ModifyListener modifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    setPageComplete(commitCommentArea.getText().getText().trim().length() >= commentProperties.getMinimumLogMessageSize());
                }		        
		    };
		    commitCommentArea.setModifyListener(modifyListener);
		}		
		commitCommentArea.createArea(composite);
		setControl(composite);
	}
	
	public String getComment() {
		return commitCommentArea.getComment();
	}
}