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
package org.tigris.subversion.subclipse.ui.preferences;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.decorator.SVNDecoratorConfiguration;
import org.tigris.subversion.subclipse.ui.decorator.SVNLightweightDecorator;

/**
 * The preference page for decoration
 * 
 */
public class SVNDecoratorPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text fileTextFormat;
	private Label fileTextFormatExample;
	
	private Text folderTextFormat;
	private Label folderTextFormatExample;
	
	private Text projectTextFormat;
	private Label projectTextFormatExample;
	
	private Text dirtyFlag;
	private Text addedFlag;
	
	class StringPair {
		String s1;
		String s2;
	}
	
	class TextPair {
		TextPair(Text t1, Label t2) {
			this.t1 = t1;
			this.t2 = t2;
		}
		Text t1;
		Label t2;
	}
	
	/**
	 * Constructor for SVNDecoratorPreferencesPage.
	 */
	public SVNDecoratorPreferencesPage() {
		setDescription(Policy.bind("SVNDecoratorPreferencesPage.description")); //$NON-NLS-1$;
	}

    /**
     * creates the following controls (sample)
     * File Format : [{added_flag}{dirty_flag}{name} {revision}  {date}  {author}]  [Add Variables]
     * Example : [                           ]
     * supportedBindings is a map of {key : description} (ex : {"name","name of the resource being decorated"})
     * @returns the text control for the format and the text control for the example         
     */
	protected TextPair createFormatEditorControl(
        Composite composite, 
        String title, 
        String buttonText, 
        final Map supportedBindings) {
        
        createLabel(composite, title, 1);
		
        Text format = new Text(composite, SWT.BORDER);
		format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		format.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {				
				updateExamples();
			}
		});
		Button b = new Button(composite, SWT.NONE);
		b.setText(buttonText);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, b.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		b.setLayoutData(data);
		final Text formatToInsert = format;
		b.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				addVariables(formatToInsert, supportedBindings);
			}			
		});
		
		createLabel(composite, "", 1); //$NON-NLS-1$
		//blank labels to balance the grid.  there is probably a better way to do this... -mml 11/11/03
		Label example = new Label(composite,SWT.SHADOW_IN);
		

		example.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createLabel(composite, "", 1); // spacer //$NON-NLS-1$
		return new TextPair(format, example);
	}
	
    /**
     * updates the examples
     */
	protected void updateExamples() {
		Map bindings = new HashMap();
  
		bindings.put(SVNDecoratorConfiguration.RESOURCE_REVISION, "74"); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_AUTHOR, "cchab"); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_DATE, DateFormat.getInstance().format(Calendar.getInstance().getTime())); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_URL, "http://localhost:8080/svn/repos/"); //$NON-NLS-1$
		bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, dirtyFlag.getText());
		bindings.put(SVNDecoratorConfiguration.ADDED_FLAG, addedFlag.getText());
            
        String example;
        example = SVNDecoratorConfiguration.decorate("file.txt",fileTextFormat.getText(), bindings);
        fileTextFormatExample.setText(example);
        example = SVNDecoratorConfiguration.decorate("folder",folderTextFormat.getText(), bindings);
        folderTextFormatExample.setText(example);
        example = SVNDecoratorConfiguration.decorate("Project",projectTextFormat.getText(), bindings);                   
        projectTextFormatExample.setText(example);
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
				
		// create a tab folder for the page
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		// text decoration options
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Policy.bind("SVNDecoratorPreferencesPage.textLabel"));//$NON-NLS-1$		
		tabItem.setControl(createTextDecoratorPage(tabFolder));
		
		initializeValues();
//		WorkbenchHelp.setHelp(tabFolder, IHelpContextIds.DECORATORS_PREFERENCE_PAGE);
		Dialog.applyDialogFont(parent);
		return tabFolder;
	}
	
    /**
     * creates the controls for the first tab folder (Decorator page)
     */
	private Control createTextDecoratorPage(Composite parent) {
		Composite fileTextGroup = new Composite(parent, SWT.NULL);
		GridLayout	layout = new GridLayout();
		layout.numColumns = 3;
		fileTextGroup.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		fileTextGroup.setLayoutData(data);

		createLabel(fileTextGroup, Policy.bind("SVNDecoratorPreferencesPage.selectFormats"), 3); //$NON-NLS-1$

		TextPair format = createFormatEditorControl(fileTextGroup, 
            Policy.bind("SVNDecoratorPreferencesPage.fileFormat"), 
            Policy.bind("SVNDecoratorPreferencesPage.addVariables"), getFileBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		fileTextFormat = format.t1;
		fileTextFormatExample = format.t2;
        
		format = createFormatEditorControl(fileTextGroup, 
            Policy.bind("SVNDecoratorPreferencesPage.folderFormat"), 
            Policy.bind("SVNDecoratorPreferencesPage.addVariables"), getFolderBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		folderTextFormat = format.t1;
		folderTextFormatExample = format.t2;
        
		format = createFormatEditorControl(fileTextGroup, 
            Policy.bind("SVNDecoratorPreferencesPage.projectFormat"), 
            Policy.bind("SVNDecoratorPreferencesPage.addVariables"), getProjectBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		projectTextFormat = format.t1;
		projectTextFormatExample = format.t2;

		createLabel(fileTextGroup, Policy.bind("SVNDecoratorPreferencesPage.labelDecorationOutgoing"), 1); //$NON-NLS-1$
		dirtyFlag = new Text(fileTextGroup, SWT.BORDER);
		dirtyFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirtyFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateExamples();
			}
		});
		createLabel(fileTextGroup, "", 1); // spacer //$NON-NLS-1$

		createLabel(fileTextGroup, Policy.bind("SVNDecoratorPreferencesPage.labelDecorat&ionAdded"), 1); //$NON-NLS-1$
		addedFlag = new Text(fileTextGroup, SWT.BORDER);
		addedFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addedFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateExamples();
			}
		});

		return fileTextGroup;	
	}
    
    /**
     * creates a label
     */
	private Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		
		fileTextFormat.setText(store.getString(ISVNUIConstants.PREF_FILETEXT_DECORATION));
		folderTextFormat.setText(store.getString(ISVNUIConstants.PREF_FOLDERTEXT_DECORATION));
		projectTextFormat.setText(store.getString(ISVNUIConstants.PREF_PROJECTTEXT_DECORATION));
		
		addedFlag.setText(store.getString(ISVNUIConstants.PREF_ADDED_FLAG));
		dirtyFlag.setText(store.getString(ISVNUIConstants.PREF_DIRTY_FLAG));
		
		setValid(true);
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
		store.setValue(ISVNUIConstants.PREF_FILETEXT_DECORATION, fileTextFormat.getText());
		store.setValue(ISVNUIConstants.PREF_FOLDERTEXT_DECORATION, folderTextFormat.getText());
		store.setValue(ISVNUIConstants.PREF_PROJECTTEXT_DECORATION, projectTextFormat.getText());
		
		store.setValue(ISVNUIConstants.PREF_ADDED_FLAG, addedFlag.getText());
		store.setValue(ISVNUIConstants.PREF_DIRTY_FLAG, dirtyFlag.getText());
		
		SVNLightweightDecorator.refresh();
		
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
		
		fileTextFormat.setText(store.getDefaultString(ISVNUIConstants.PREF_FILETEXT_DECORATION));
		folderTextFormat.setText(store.getDefaultString(ISVNUIConstants.PREF_FOLDERTEXT_DECORATION));
		projectTextFormat.setText(store.getDefaultString(ISVNUIConstants.PREF_PROJECTTEXT_DECORATION));
		
		addedFlag.setText(store.getDefaultString(ISVNUIConstants.PREF_ADDED_FLAG));
		dirtyFlag.setText(store.getDefaultString(ISVNUIConstants.PREF_DIRTY_FLAG));
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
	
	/**
	 * Add another variable to the given target. The variable is inserted at current position
     * A ListSelectionDialog is shown and the choose the variables to add 
	 */
	private void addVariables(Text target, Map bindings) {
	
		final List variables = new ArrayList(bindings.size());
		
		ILabelProvider labelProvider = new LabelProvider() {
			public String getText(Object element) {
				return ((StringPair)element).s1 + " - " + ((StringPair)element).s2; //$NON-NLS-1$
			}
		};
		
		IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[]) variables.toArray(new StringPair[variables.size()]);
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
		
		for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
			StringPair variable = new StringPair();
			variable.s1 = (String) it.next(); // variable
			variable.s2 = (String) bindings.get(variable.s1); // description
			variables.add(variable);				
		}
	
		ListSelectionDialog dialog =
			new ListSelectionDialog(
				this.getShell(),
				this,
				contentsProvider,
				labelProvider,
				Policy.bind("SVNDecoratorPreferencesPage.selectVariablesToAdd")); //$NON-NLS-1$
		dialog.setTitle(Policy.bind("SVNDecoratorPreferencesPage.AddVariables")); //$NON-NLS-1$
		if (dialog.open() != ListSelectionDialog.OK)
			return;
	
		Object[] result = dialog.getResult();
		
		for (int i = 0; i < result.length; i++) {
			target.insert("{"+((StringPair)result[i]).s1 +"}"); //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}

    /**
     * get the map of {variable,description} to use for folders with createFormatEditorControl
     */    	
	private Map getFolderBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(SVNDecoratorConfiguration.RESOURCE_NAME, Policy.bind("SVNDecoratorPreferencesPage.nameResourceVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_REVISION, Policy.bind("SVNDecoratorPreferencesPage.revisionResourceVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagDirtyVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.ADDED_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagAddedVariable")); //$NON-NLS-1$
		bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagDirtyVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_AUTHOR, Policy.bind("SVNDecoratorPreferencesPage.authorVariable")); //$NON-NLS-1$        
        bindings.put(SVNDecoratorConfiguration.RESOURCE_DATE, Policy.bind("SVNDecoratorPreferencesPage.dateVariable")); //$NON-NLS-1$        
		return bindings;
	}

    /**
     * get the map of {variable,description} to use for files with createFormatEditorControl
     */     
    private Map getFileBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(SVNDecoratorConfiguration.RESOURCE_NAME, Policy.bind("SVNDecoratorPreferencesPage.nameResourceVariable")); //$NON-NLS-1$
		bindings.put(SVNDecoratorConfiguration.RESOURCE_REVISION, Policy.bind("SVNDecoratorPreferencesPage.revisionResourceVariable")); //$NON-NLS-1$
		bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagDirtyVariable")); //$NON-NLS-1$
		bindings.put(SVNDecoratorConfiguration.ADDED_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagAddedVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_AUTHOR, Policy.bind("SVNDecoratorPreferencesPage.authorVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_DATE, Policy.bind("SVNDecoratorPreferencesPage.dateVariable")); //$NON-NLS-1$                
		return bindings;
	}

    /**
     * get the map of {variable,description} to use for projects with createFormatEditorControl
     */     
    private Map getProjectBindingDescriptions() {
        Map bindings = new HashMap();
        bindings.put(SVNDecoratorConfiguration.RESOURCE_NAME, Policy.bind("SVNDecoratorPreferencesPage.nameResourceVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.DIRTY_FLAG, Policy.bind("SVNDecoratorPreferencesPage.flagDirtyVariable")); //$NON-NLS-1$
        bindings.put(SVNDecoratorConfiguration.RESOURCE_URL, Policy.bind("SVNDecoratorPreferencesPage.remoteLocationVariable")); //$NON-NLS-1$                    
        return bindings;
    }    
    
}
