/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.conflicts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListDialog;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

/**
 * Preference page to set the program for merge
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class DiffMergePreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private Text mergeProgramLocationText;

    private Text mergeProgramParametersText;

    class StringPair {
        String s1;

        String s2;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(gridData);

        // Group "Merge program"
        Group group = new Group(composite, SWT.NULL);
        group.setText(Policy.bind("DiffMergePreferencePage.mergeProgramGroup")); //$NON-NLS-1$
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        group.setLayoutData(gridData);

        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);

        // program used to resolve conflicted files
        createLabel(
                group,
                Policy
                        .bind("DiffMergePreferencePage.programToResolveConflictedFiles"), 2); //$NON-NLS-1$
        mergeProgramLocationText = new Text(group, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.widthHint = 200;
        gridData.grabExcessHorizontalSpace = true;
        mergeProgramLocationText.setLayoutData(gridData);
        mergeProgramLocationText.setEditable(false);
        Button browseMergeProgramButton = new Button(group, SWT.NONE);
        browseMergeProgramButton.setText(Policy
                .bind("DiffMergePreferencePage.browse")); //$NON-NLS-1$
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        browseMergeProgramButton.setLayoutData(gridData);
        browseMergeProgramButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
                String res = fileDialog.open();
                if (res != null) {
                    mergeProgramLocationText.setText(res);
                }
            }
        });

        createLabel(group, "", 2); //$NON-NLS-1$

        // parameters
        createLabel(group, Policy
                .bind("DiffMergePreferencePage.mergeProgramParameters"), 2); //$NON-NLS-1$
        mergeProgramParametersText = createFormatEditorControl(
                group,
                Policy.bind("DiffMergePreferencePage.mergeProgramVariables"), getMergeBindingDescriptions()); //$NON-NLS-1$

        createLabel(group, Policy
                .bind("DiffMergePreferencePage.tortoiseMergeComment"), //$NON-NLS-1$
                2);

        initializeValues();

        return composite;
    }

    protected Text createFormatEditorControl(Composite composite,
            String buttonText, final Map supportedBindings) {

        Text format = new Text(composite, SWT.BORDER);
        format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button b = new Button(composite, SWT.NONE);
        b.setText(buttonText);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        b.setLayoutData(data);
        final Text formatToInsert = format;
        b.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                addVariables(formatToInsert, supportedBindings);
            }
        });

        return format;
    }

    /**
     * Add another variable to the given target. The variable is inserted at
     * current position A ListSelectionDialog is shown and the choose the
     * variables to add
     */
    private void addVariables(Text target, Map bindings) {

        final List variables = new ArrayList(bindings.size());

        ILabelProvider labelProvider = new LabelProvider() {
            public String getText(Object element) {
                return ((StringPair) element).s1
                        + " - " + ((StringPair) element).s2; //$NON-NLS-1$
            }
        };

        IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return variables.toArray(new StringPair[variables.size()]);
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        };

        for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
            StringPair variable = new StringPair();
            variable.s1 = (String) it.next(); // variable
            variable.s2 = (String) bindings.get(variable.s1); // description
            variables.add(variable);
        }

        ListDialog dialog = new ListDialog(this.getShell());
        dialog.setContentProvider(contentsProvider);
        dialog.setAddCancelButton(true);
        dialog.setLabelProvider(labelProvider);
        dialog.setInput(variables);
        dialog.setTitle(Policy
                .bind("DiffMergePreferencePage.addVariableDialogTitle")); //$NON-NLS-1$
        if (dialog.open() != ListDialog.OK)
            return;

        Object[] result = dialog.getResult();

        for (int i = 0; i < result.length; i++) {
            target.insert("${" + ((StringPair) result[i]).s1 + "}"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * get the map of {variable,description} to use for merge program
     */
    private Map getMergeBindingDescriptions() {
        Map bindings = new HashMap();
        bindings
                .put(
                        "merged", Policy.bind("DiffMergePreferencePage.mergedVariableComment")); //$NON-NLS-1$ //$NON-NLS-2$
        bindings
                .put(
                        "theirs", Policy.bind("DiffMergePreferencePage.theirsVariableComment")); //$NON-NLS-1$ //$NON-NLS-2$
        bindings
                .put(
                        "yours", Policy.bind("DiffMergePreferencePage.yoursVariableComment")); //$NON-NLS-1$ //$NON-NLS-2$
        bindings
                .put(
                        "base", Policy.bind("DiffMergePreferencePage.baseVariableComment"));//$NON-NLS-1$ //$NON-NLS-2$
        return bindings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    /**
     * Initializes states of the controls from the preference store.
     */
    private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        mergeProgramLocationText.setText(store
                .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_LOCATION));
        mergeProgramParametersText.setText(store
                .getString(ISVNUIConstants.PREF_MERGE_PROGRAM_PARAMETERS));
    }

    /**
     * Defaults was clicked. Restore the SVN preferences to their default values
     */
    protected void performDefaults() {
        super.performDefaults();
        initializeValues();
    }

    /**
     * OK was clicked. Store the SVN preferences.
     * 
     * @return whether it is okay to close the preference page
     */
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();

        store.setValue(ISVNUIConstants.PREF_MERGE_PROGRAM_LOCATION,
                mergeProgramLocationText.getText());

        store.setValue(ISVNUIConstants.PREF_MERGE_PROGRAM_PARAMETERS,
                mergeProgramParametersText.getText());
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
     */
    protected IPreferenceStore doGetPreferenceStore() {
        return SVNUIPlugin.getPlugin().getPreferenceStore();
    }

}