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


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.wizards.SVNWizardPage;

/**
 * First wizard page for importing a project into a SVN repository.
 * This page prompts the user to select an existing repo or create a new one.
 * If the user selected an existing repo, then getLocation() will return it.
 */
public class RepositorySelectionPage extends SVNWizardPage {
	private TableViewer table;
	private Button useExistingRepo;
	private Button useNewRepo;
	
	private ISVNRepositoryLocation result;
	
	/**
	 * RepositorySelectionPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public RepositorySelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
    
    /**
     * Creates the table for the repositories
     */
	protected TableViewer createTable(Composite parent, int span) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = span;
		table.setLayoutData(data);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
	
		return new TableViewer(table);
	}
	/**
	 * Creates the UI part of the page.
	 * 
	 * @param parent  the parent of the created widgets
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_SELECT_REPOSITORY_PAGE);
		
		createWrappingLabel(composite, Policy.bind("RepositorySelectionPage.description"), 0 /* indent */, 1 /* columns */); //$NON-NLS-1$
		
		useNewRepo = createRadioButton(composite, Policy.bind("RepositorySelectionPage.useNew"), 1); //$NON-NLS-1$
		
		useExistingRepo = createRadioButton(composite, Policy.bind("RepositorySelectionPage.useExisting"), 1); //$NON-NLS-1$
		table = createTable(composite, 1);
		table.setContentProvider(new WorkbenchContentProvider());
		table.setLabelProvider(new WorkbenchLabelProvider());
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				result = (ISVNRepositoryLocation)((IStructuredSelection)table.getSelection()).getFirstElement();
				setPageComplete(true);
			}
		});

		useExistingRepo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (useNewRepo.getSelection()) {
					table.getTable().setEnabled(false);
					result = null;
				} else {
					table.getTable().setEnabled(true);
					result = (ISVNRepositoryLocation)((IStructuredSelection)table.getSelection()).getFirstElement();
				}
				setPageComplete(true);
			}
		});

		setControl(composite);

        ISVNRepositoryLocation[] locations = SVNUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
        AdaptableList input = new AdaptableList(locations);
        table.setInput(input);
        if (locations.length == 0) {
            useNewRepo.setSelection(true);  
        } else {
            useExistingRepo.setSelection(true); 
            table.setSelection(new StructuredSelection(locations[0]));
        }

	}
	
	public ISVNRepositoryLocation getLocation() {
		return result;
	}
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			useExistingRepo.setFocus();
		}
	}
}
