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
package org.tigris.subversion.subclipse.ui.wizards;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;

/**
 * Wizard page for entering information about a SVN repository location.
 * This wizard can be initialized using setProperties or using setDialogSettings
 */
public class ConfigurationWizardMainPage extends SVNWizardPage {
	private boolean showValidate;
	private boolean validate;
	
	// Widgets
	
	// User
	private Combo userCombo;
	// Password
	private Text passwordText;
	
	// Host
	private Combo urlCombo;

	// Validation
	private Button validateButton;
	
	private static final int COMBO_HISTORY_LENGTH = 5;
	
	private Properties properties = null;
	
	// Dialog store id constants
	private static final String STORE_USERNAME_ID =
		"ConfigurationWizardMainPage.STORE_USERNAME_ID";//$NON-NLS-1$
	private static final String STORE_URL_ID =
		"ConfigurationWizardMainPage.STORE_URL_ID";//$NON-NLS-1$
	private static final String STORE_DONT_VALIDATE_ID =
		"ConfigurationWizardMainPage.STORE_DONT_VALIDATE_ID";//$NON-NLS-1$
	
	// In case the page was launched from a different wizard	
	private IDialogSettings settings;
	
	/**
	 * ConfigurationWizardMainPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public ConfigurationWizardMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories.  The assumption is made that all histories
	 * should be of length <code>ConfigurationWizardMainPage.COMBO_HISTORY_LENGTH</code>.
	 *
	 * @param history the current history
	 * @param newEntry the entry to add to the history
	 * @return the history with the new entry appended
	 */
	private String[] addToHistory(String[] history, String newEntry) {
		ArrayList l = new ArrayList(Arrays.asList(history));

        l.remove(newEntry);
        l.add(0,newEntry);
    
        // since only one new item was added, we can be over the limit
        // by at most one item
        if (l.size() > COMBO_HISTORY_LENGTH)
            l.remove(COMBO_HISTORY_LENGTH);

		String[] r = new String[l.size()];
		l.toArray(r);
		return r;
	}

	public IDialogSettings getDialogSettings() {
		return settings;
	}

	public void setDialogSettings(IDialogSettings settings) {
		this.settings = settings;
	}
    
	/**
	 * Creates the UI part of the page.
	 * 
	 * @param parent  the parent of the created widgets
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_NEW_REPOSITORY_PAGE);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				validateFields();
			}
		};
		
		Group g = createGroup(composite, Policy.bind("ConfigurationWizardMainPage.Location_1")); //$NON-NLS-1$
		
		// repository Url
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.url")); //$NON-NLS-1$
		urlCombo = createEditableCombo(g);
		urlCombo.addListener(SWT.Selection, listener);
		urlCombo.addListener(SWT.Modify, listener);
		
		g = createGroup(composite, Policy.bind("ConfigurationWizardMainPage.Authentication_2")); //$NON-NLS-1$
		
		// User name
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.userName")); //$NON-NLS-1$
		userCombo = createEditableCombo(g);
		userCombo.addListener(SWT.Selection, listener);
		userCombo.addListener(SWT.Modify, listener);
		
		// Password
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.password")); //$NON-NLS-1$
		passwordText = createTextField(g);
		passwordText.setEchoChar('*');

		// create a composite to ensure the validate button is in its own tab group
		if (showValidate) {
			Composite validateButtonTabGroup = new Composite(composite, SWT.NONE);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			validateButtonTabGroup.setLayoutData(data);
			validateButtonTabGroup.setLayout(new FillLayout());

			validateButton = new Button(validateButtonTabGroup, SWT.CHECK);
			validateButton.setText(Policy.bind("ConfigurationWizardAutoconnectPage.validate")); //$NON-NLS-1$
			validateButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					validate = validateButton.getSelection();
				}
			});
		}
		
		initializeValues();
		validateFields();
		urlCombo.setFocus();
		
		setControl(composite);
	}
	/**
	 * Utility method to create an editable combo box
	 * 
	 * @param parent  the parent of the combo box
	 * @return the created combo
	 */
	protected Combo createEditableCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.NULL);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
	}
	
	protected Group createGroup(Composite parent, String text) {
		Group group = new Group(parent, SWT.NULL);
		group.setText(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		//data.widthHint = GROUP_WIDTH;
		
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		return group;
	}
	
	/**
	 * @see SVNWizardPage#finish
	 */
	public boolean finish(IProgressMonitor monitor) {
		// Set the result to be the current values
		Properties result = new Properties();
		result.setProperty("user", userCombo.getText()); //$NON-NLS-1$
		result.setProperty("password", passwordText.getText()); //$NON-NLS-1$
		result.setProperty("url", urlCombo.getText()); //$NON-NLS-1$
		this.properties = result;
		
		saveWidgetValues();
		
		return true;
	}
	/**
	 * Returns the properties for the repository connection
	 * 
	 * @return the properties or null
	 */
	public Properties getProperties() {
		return properties;
	}
	/**
	 * Initializes states of the controls.
	 */
	private void initializeValues() {
		// Set remembered values
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] hostNames = settings.getArray(STORE_URL_ID);
			if (hostNames != null) {
				for (int i = 0; i < hostNames.length; i++) {
					urlCombo.add(hostNames[i]);
				}
			}
			String[] userNames = settings.getArray(STORE_USERNAME_ID);
			if (userNames != null) {
				for (int i = 0; i < userNames.length; i++) {
					userCombo.add(userNames[i]);
				}
			}
			if (showValidate) {
				validate = !settings.getBoolean(STORE_DONT_VALIDATE_ID);
				validateButton.setSelection(validate);
			}
		}
		
		if(properties != null) {
			String method = (String)properties.getProperty("connection"); //$NON-NLS-1$
	
			String user = (String)properties.getProperty("user"); //$NON-NLS-1$
			if (user != null) {
				userCombo.setText(user);
			}
	
			String password = (String)properties.getProperty("password"); //$NON-NLS-1$
			if (password != null) {
				passwordText.setText(password);
			}
	
			String host = (String)properties.getProperty("url"); //$NON-NLS-1$
			if (host != null) {
				urlCombo.setText(host);
			}
		}
	}

	/**
	 * Saves the widget values for the next time
	 */
	private void saveWidgetValues() {
		// Update history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] userNames = settings.getArray(STORE_USERNAME_ID);
			if (userNames == null) userNames = new String[0];
			userNames = addToHistory(userNames, userCombo.getText());
			settings.put(STORE_USERNAME_ID, userNames);

			String[] hostNames = settings.getArray(STORE_URL_ID);
			if (hostNames == null) hostNames = new String[0];
			hostNames = addToHistory(hostNames, urlCombo.getText());
			settings.put(STORE_URL_ID, hostNames);

			if (showValidate) {
				settings.put(STORE_DONT_VALIDATE_ID, !validate);
			}
		}
	}

    /**
     * if set, we will propose the user to validate 
     */
	public void setShowValidate(boolean showValidate) {
		this.showValidate = showValidate;
	}
	
	/**
	 * Sets the properties for the repository connection
	 * 
	 * @param properties  the properties or null
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;		
	}
	
	/**
	 * Validates the contents of the editable fields and set page completion 
	 * and error messages appropriately. Call each time url or username is modified 
	 */
	private void validateFields() {
		String url = urlCombo.getText();
		if (url.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidUrl", e.getMessage())); //$NON-NLS-1$);
			setPageComplete(false);			
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
	
    /**
     * true if validate has been checked
     * @return
     */
	public boolean getValidate() {
		return validate;
	}
    
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			urlCombo.setFocus();
		}
	}
}