/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.svnproperties;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.ISVNProperty;

/**
 * Dialog to set a svn property 
 */
public class SetSvnPropertyDialog extends Dialog {
	private ISVNProperty property;   // null when we set a new property
	private ISVNLocalResource svnResource;	
	private Text propertyNameText;
	private Text propertyValueText;
	private Text fileText;
	private Button textRadio;
	private Button fileRadio;
	private Button browseButton;
	private Label statusMessageLabel;
	private Button recurseCheckbox;	
	
	private String propertyName;
	private String propertyValue;
	private File propertyFile;
	private boolean recurse;

	/**
	 * create a new SetSvnPropertyDialog to set a new property on the given resource
	 * @param parentShell
	 * @param svnResource
	 */
	public SetSvnPropertyDialog(Shell parentShell, ISVNLocalResource svnResource) {
		this(parentShell,svnResource,null);
	}

	/**
	 * create a new SetSvnPropertyDialog to modify an existing property on the given resource
	 * @param parentShell
	 * @param svnResource
	 * @param property
	 */
	public SetSvnPropertyDialog(Shell parentShell, ISVNLocalResource svnResource, ISVNProperty property) {
		super(parentShell);
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);
		this.property = property;
		this.svnResource = svnResource;
	}

	private boolean isNewProperty() {
		return property == null;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		updateEnablements();
		return control;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Label label;
		GridData gridData;

		Listener updateEnablementsListener = new Listener() {
			public void handleEvent(Event event) {
				updateEnablements();
			}
		};
		
		Listener validateListener = new Listener() {
			public void handleEvent(Event event) {
				validate();
			}
		};

		Listener updatePropertiesListener = new Listener() {
			public void handleEvent(Event event) {
				updateProperties();
			}
		};
		
		
		Composite area = (Composite)super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		area.setLayout(layout);

		// create the property name label and the corresponding Text		
		Composite composite = new Composite(area, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		label = new Label(composite,SWT.LEFT);
		label.setText(Policy.bind("SetSvnPropertyDialog.propertyName")); //$NON-NLS-1$

		propertyNameText = new Text(composite,SWT.SINGLE|SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;
		gridData.grabExcessHorizontalSpace = true;
		propertyNameText.setLayoutData(gridData);
		if (property != null) {
			propertyNameText.setText(property.getName());
			propertyNameText.setEditable(false);
		}
		propertyNameText.addListener(SWT.Modify,validateListener);

		// create the group
		Group group = new Group(area,SWT.NULL);
		group.setText(Policy.bind("SetSvnPropertyDialog.propertyContent")); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);
		layout = new GridLayout();
		group.setLayout(layout); 		

		// create "Enter a text property" radio button
		textRadio = new Button(group, SWT.RADIO);
		textRadio.setText(Policy.bind("SetSvnPropertyDialog.enterTextProperty")); //$NON-NLS-1$
		textRadio.addListener(SWT.Selection,updateEnablementsListener);
		textRadio.setSelection(true);	
		
		// create the Text for the content
		propertyValueText = new Text(group,SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 100;
		gridData.widthHint = 300;
		gridData.horizontalIndent = 30;
		gridData.grabExcessHorizontalSpace = true;
		propertyValueText.setLayoutData(gridData);
		if (property != null) {
			propertyValueText.setText(property.getValue());
		}
		propertyValueText.addListener(SWT.Modify,updatePropertiesListener);

		// create "Use a file" radio button
		fileRadio = new Button(group, SWT.RADIO);
		fileRadio.setText(Policy.bind("SetSvnPropertyDialog.useFile")); //$NON-NLS-1$
		fileRadio.addListener(SWT.Selection,updateEnablementsListener);
		
		// file input and button
		composite = new Composite(group, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		fileText = new Text(composite, SWT.SINGLE|SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 30;
		fileText.setLayoutData(gridData);
		fileText.setEditable(false);
		browseButton = new Button(composite,SWT.PUSH);
		browseButton.setText(Policy.bind("SetSvnPropertyDialog.browse")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(getShell(),SWT.OPEN);
				String res = fileDialog.open();
				if (res != null) {
					fileText.setText(res);
					validate();
				}
			}
		});

		// checkbox 
		recurseCheckbox = new Button(area,SWT.CHECK);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		recurseCheckbox.setLayoutData(gridData);
		recurseCheckbox.setText(Policy.bind("SetSvnPropertyDialog.setPropertyRecursively")); //$NON-NLS-1$
		recurseCheckbox.setSelection(false);
		if (!svnResource.isFolder()) {
			recurseCheckbox.setEnabled(false);
		}
		recurseCheckbox.addListener(SWT.Modify,updatePropertiesListener);
	
		// status message
		statusMessageLabel = new Label(area, SWT.LEFT);
		statusMessageLabel.setText(""); //$NON-NLS-1$
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		statusMessageLabel.setLayoutData(data);
		statusMessageLabel.setFont(parent.getFont());	
	
		return area;
	}

	private void updateEnablements() {
		if (textRadio.getSelection()) {
			browseButton.setEnabled(false);
			fileText.setEnabled(false);
			propertyValueText.setEnabled(true);
		} else {
			browseButton.setEnabled(true);
			fileText.setEnabled(true);			
			propertyValueText.setEnabled(false);
		}
		validate();
	}


	/**
	 * get the name of the property to set 
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * get the value of the property or null if a file is used to set the property
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * get the file to use as a property value or null if plain text is used instead 
	 */
	public File getPropertyFile() {
		return propertyFile;
	}

	public boolean getRecurse() {
		return recurse;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Policy.bind("SetSvnPropertyDialog.shellText")); //$NON-NLS-1$
	}

	private void updateProperties() {
		propertyName = propertyNameText.getText();
		if (textRadio.getSelection()) {
			propertyFile = null;
			propertyValue = propertyValueText.getText();
		} else {
			propertyFile = new File(fileText.getText());
			propertyValue = null;
		}
		recurse = recurseCheckbox.getSelection();
	}

	private void validate() {
		// update the properties
		updateProperties();
		
		// verify property name
		if (propertyName.equals("")) { //$NON-NLS-1$
			setError(""); //$NON-NLS-1$
			return;
		} else {
			if (isNewProperty()) {
				try {
					// if we are setting a new property, make sure the property does not exist
					if (svnResource.getSvnProperty(getPropertyName()) != null) {
						setError(Policy.bind("SetSvnPropertyDialog.anotherPropertyHasSameName")); //$NON-NLS-1$
						return;				
					}
				} catch (SVNException e) {
					// we ignore the exception, we can't do much more ...
				}
			}
		}
		
		// verify file
		if (propertyFile != null) {
			if (fileText.getText().equals("")) { //$NON-NLS-1$
				setError(""); //$NON-NLS-1$
			} else {
				if (!getPropertyFile().exists()) {
					setError(Policy.bind("SetSvnPropertyDialog.fileDoesNotExist")); //$NON-NLS-1$
					return;
				}
			}
		}
		setError(null);
	}

	private void setError(String text) {
		if (text == null) {
			statusMessageLabel.setText(""); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			statusMessageLabel.setText(text);
			statusMessageLabel.setForeground(JFaceColors.getErrorText(getShell().getDisplay()));
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}


}
