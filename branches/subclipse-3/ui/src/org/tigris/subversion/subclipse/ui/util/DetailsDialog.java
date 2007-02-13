/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple superclass for detail button dialogs.
 */
abstract public class DetailsDialog extends Dialog {
	/**
	 * The Details button.
	 */
	private Button detailsButton;

	/**
	 * The Ok button.
	 */
	private Button okButton;

	/**
	 * The title of the dialog.
	 */
	private String title;
	
	/**
	 * The error message
	 */
	private Label errorMessageLabel;

	/**
	 * The SWT list control that displays the error details.
	 */
	private Composite detailsComposite;

	/**
	 * Indicates whether the error details viewer is currently created.
	 */
	private boolean detailsCreated = false;
	
	/**
	 * The key for the image to be displayed (one of the image constants on Dialog)
	 */
	private String imageKey = null;
	
	/**
	 * Creates a details pane dialog.
	 * Note that the dialog will have no visual representation (no widgets)
	 * until it is told to open.
	 *
	 * @param parentShell the shell under which to create this dialog
	 * @param dialogTitle the title to use for this dialog
	 * @param message the message to show in this dialog
	 * @param status the error to show to the user
	 * @param displayMask the mask to use to filter the displaying of child items,
	 *   as per <code>IStatus.matches</code>
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	public DetailsDialog(Shell parentShell, String dialogTitle) {
		super(parentShell);
		this.title = dialogTitle;
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 * Handles the pressing of the Ok or Details button in this dialog.
	 * If the Ok button was pressed then close this dialog.  If the Details
	 * button was pressed then toggle the displaying of the error details area.
	 * Note that the Details button will only be visible if the error being
	 * displayed specifies child details.
	 */
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.DETAILS_ID) {  // was the details button pressed?
			toggleDetailsArea();
		} else {
			super.buttonPressed(id);
		} 
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Details buttons
		if(includeOkButton()) {
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}
		if (includeCancelButton()) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
		detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
		updateEnablements();
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 */
	final protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite)super.createDialogArea(parent);
		
		// create image
		Image image = JFaceResources.getImageRegistry().get(getImageKey());
		if (image != null) {
			// create a composite to split the dialog area in two
			Composite top = new Composite(composite, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			layout.numColumns = 2;
			top.setLayout(layout);
			top.setLayoutData(new GridData(GridData.FILL_BOTH));
			top.setFont(parent.getFont());
		
			// add the image to the left of the composite
			Label label = new Label(top, 0);
			image.setBackground(label.getBackground());
			label.setImage(image);
			label.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER |
				GridData.VERTICAL_ALIGN_CENTER));
				
			// add a composite to the right to contain the custom components
			Composite right = new Composite(top, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			right.setLayout(layout);
			right.setLayoutData(new GridData(GridData.FILL_BOTH));
			right.setFont(parent.getFont());
			createMainDialogArea(right);
		} else {
			createMainDialogArea(composite);
		}
		
		errorMessageLabel = new Label(composite, SWT.NONE);
		errorMessageLabel.setLayoutData(new GridData(
			GridData.GRAB_HORIZONTAL |
			GridData.HORIZONTAL_ALIGN_FILL));
		errorMessageLabel.setFont(parent.getFont());
		errorMessageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		
		return composite;
	}
	
	/**
	 * Creates the dialog's top composite
	 * 
	 * @param parent the parent composite
	 */
	abstract protected void createMainDialogArea(Composite parent);

	/**
	 * Create this dialog's drop-down list component.
	 *
	 * @param parent the parent composite
	 * @return the drop-down list component
	 */
	abstract protected Composite createDropDownDialogArea(Composite parent);
	
	/**
	 * Toggles the unfolding of the details area.  This is triggered by
	 * the user pressing the details button.
	 */
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		Point oldSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		if (detailsCreated) {
			detailsComposite.dispose();
			detailsCreated = false;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		} else {
			detailsComposite = createDropDownDialogArea((Composite)getContents());
			detailsCreated = true;
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		}
	
		Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	
		getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
	}
	
	final protected void setErrorMessage(String error) {
		if(errorMessageLabel != null) {
			if(error == null || error.length() == 0) {
				errorMessageLabel.setText(""); //$NON-NLS-1$
			} else {
				errorMessageLabel.setText(error);
			}
			errorMessageLabel.update();
		}
	}
	
	final protected void setPageComplete(boolean complete) {
		if(okButton != null ) {
			okButton.setEnabled(complete);
		}
	}
	
	abstract protected void updateEnablements();
	
	protected boolean includeCancelButton() {
		return true;
	}
	
	protected boolean includeOkButton() {
		return true;
	}
	
	/**
	 * Returns the imageKey.
	 * @return String
	 */
	protected String getImageKey() {
		return imageKey;
	}


	/**
	 * Sets the imageKey.
	 * @param imageKey The imageKey to set
	 */
	protected void setImageKey(String imageKey) {
		this.imageKey = imageKey;
	}


}
