/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion  
 *******************************************************************************/

package org.tigris.subversion.subclipse.ui.preferences;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.tigris.subversion.subclipse.ui.*;
import org.tigris.subversion.svnclientadapter.*;

/**
 * SVN Preference Page
 * 
 * Allows the configuration of SVN specific options.
 * 
 */
public class SVNPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Combo svnInterfaceCombo;
    private Button deepCalcCheckbox;
	
	public SVNPreferencesPage() {
		// sort the options by display text
		setDescription(Policy.bind("SVNPreferencePage.description")); //$NON-NLS-1$
	}

	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent, int widthChars) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		GC gc = new GC(combo);
	 	gc.setFont(combo.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();		
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, widthChars);
		gc.dispose();
		combo.setLayoutData(data);
		return combo;
	}

	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
	}

	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		
		
		return composite;
	}

	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		createLabel(composite, Policy.bind("SVNPreferencePage.svnClientInterface")); //$NON-NLS-1$
        svnInterfaceCombo = createCombo(composite);

		createLabel(composite, ""); createLabel(composite, ""); //$NON-NLS-1$ //$NON-NLS-2$
		deepCalcCheckbox = createCheckBox(composite, Policy.bind("SVNPreferencePage.calculateDeepDecoration"));		
		initializeValues();
		
        svnInterfaceCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

        
        
//		WorkbenchHelp.setHelp(svnInterfaceCombo, IHelpContextIds.PREF_QUIET);
		Dialog.applyDialogFont(parent);
		return composite;
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();

        svnInterfaceCombo.add(Policy.bind("SVNPreferencePage.svnjavahl")); //$NON-NLS-1$
        svnInterfaceCombo.add(Policy.bind("SVNPreferencePage.svncommandline")); //$NON-NLS-1$

        deepCalcCheckbox.setSelection(store.getBoolean(ISVNUIConstants.PREF_CALCULATE_DIRTY));
        
        if (store.getInt(ISVNUIConstants.PREF_SVNINTERFACE) == SVNClientAdapterFactory.JAVAHL_CLIENT)
            svnInterfaceCombo.select(0);
        else
            svnInterfaceCombo.select(1);
	}

   /**
	* @see IWorkbenchPreferencePage#init(IWorkbench)
	*/
	public void init(IWorkbench workbench) {
	}

	/**
	 * OK was clicked. Store the SVN preferences.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		
        if (svnInterfaceCombo.getSelectionIndex() == 0)
            store.setValue(ISVNUIConstants.PREF_SVNINTERFACE, SVNClientAdapterFactory.JAVAHL_CLIENT);
        else
            store.setValue(ISVNUIConstants.PREF_SVNINTERFACE, SVNClientAdapterFactory.COMMANDLINE_CLIENT);
		
        store.setValue(ISVNUIConstants.PREF_CALCULATE_DIRTY, deepCalcCheckbox.getSelection());
        
//		CVSProviderPlugin.getPlugin().setQuietness(
//			getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
		
		SVNUIPlugin.getPlugin().savePluginPreferences();
		return true;
	}

	/**
	 * Defaults was clicked. Restore the SVN preferences to
	 * their default values
	 */
	protected void performDefaults() {
		super.performDefaults();
		IPreferenceStore store = getPreferenceStore();
        
        if (store.getDefaultInt(ISVNUIConstants.PREF_SVNINTERFACE) == SVNClientAdapterFactory.JAVAHL_CLIENT) 
            svnInterfaceCombo.select(0);
        else
            svnInterfaceCombo.select(1);
        
        deepCalcCheckbox.setSelection(false);
	}

 
   /**
	* Returns preference store that belongs to the our plugin.
	* This is important because we want to store
	* our preferences separately from the desktop.
	*
	* @return the preference store for this plugin
	*/
	protected IPreferenceStore doGetPreferenceStore() {
		return SVNUIPlugin.getPlugin().getPreferenceStore();
	}

}
