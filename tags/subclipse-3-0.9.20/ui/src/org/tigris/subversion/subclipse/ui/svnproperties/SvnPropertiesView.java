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
package org.tigris.subversion.subclipse.ui.svnproperties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.tigris.subversion.subclipse.core.IResourceStateChangeListener;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.SVNPropertyDeleteAction;
import org.tigris.subversion.subclipse.ui.actions.SVNPropertyModifyAction;
import org.tigris.subversion.subclipse.ui.dialogs.AddKeywordsDialog;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * 
 * The <code>SvnPropertiesView</code> shows the svn properties for a svn local resource 
 * 
 */
public class SvnPropertiesView extends ViewPart {

	public static final String VIEW_ID = "org.tigris.subversion.subclipse.ui.svnproperties.SvnPropertiesView"; //$NON-NLS-1$

	private TableViewer tableViewer;
	private TextViewer textViewer;
	private ISVNLocalResource resource;
	private Action refreshAction;
	private Action addPropertyAction;
	private Action modifyPropertyAction;
	private Action deletePropertyAction;
	private Action setKeywordsAction;
	private Label statusLabel;
	private ISelectionListener pageSelectionListener;
	private IResourceStateChangeListener resourceStateChangeListener;

	class ResourceStateChangeListener implements IResourceStateChangeListener {
		/**
		 * the svn status of some resources changed. Refresh if we are concerned
		 */
		public void resourceSyncInfoChanged(IResource[] changedResources) {
			for (int i = 0; i < changedResources.length;i++) {
				if (changedResources[i].equals(resource.getIResource())) {
					refresh();
				}
			}
		}

		public void resourceModified(IResource[] changedResources) {}
    
		public void projectConfigured(IProject project) {}
    
		public void projectDeconfigured(IProject project) {}
	}


	public SvnPropertiesView() {
	}
    
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		SVNProviderPlugin.removeResourceStateChangeListener(resourceStateChangeListener);
		resourceStateChangeListener = null;
		getSite().getPage().removePostSelectionListener(pageSelectionListener);
		super.dispose();
	}

	class PropertiesLabelProvider implements ITableLabelProvider {
        
		public PropertiesLabelProvider() {
		}
        
		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element == null)
				return ""; //$NON-NLS-1$
			ISVNProperty svnProperty = (ISVNProperty)element; 
			
			String result = null;
			switch (columnIndex) {
				case 0 :
					result = svnProperty.getName();
					break;
				case 1 : 
					result = svnProperty.getValue();
					break;
			}
			// This method must not return null
			if (result == null) result = ""; //$NON-NLS-1$
			return result;

		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	}


	private TableViewer createTable(Composite parent) {
		Table table =	new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
        
		tableViewer = new TableViewer(table);
		createColumns(table, layout);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new PropertiesLabelProvider());
		return tableViewer;
	}

	/**
	 * Create the TextViewer 
	 */
	protected TextViewer createText(Composite parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		return result;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);		
		statusLabel = new Label(parent,SWT.LEFT);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		statusLabel.setLayoutData(gridData);

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = createTable(sashForm);
		textViewer = createText(sashForm);
		sashForm.setWeights(new int[] { 70, 30 });

		contributeActions();        
        
        pageSelectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        		handlePartSelectionChanged(part,selection);
        	}
        };
        
        getSite().getPage().addPostSelectionListener(pageSelectionListener);
        
		resourceStateChangeListener = new ResourceStateChangeListener();
		SVNProviderPlugin.addResourceStateChangeListener(resourceStateChangeListener);
	}

	/**
	 * called when the selection changed on another part  
	 */
	private void handlePartSelectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		
		try {
			Object first = ((IStructuredSelection)selection).getFirstElement(); 

			if (first instanceof IAdaptable) {
				IAdaptable a = (IAdaptable) first;
				Object adapter = a.getAdapter(IResource.class);
				if (adapter instanceof IResource) {
					IResource resource = (IResource)adapter;
					
					// If the resource isn't open or doesn't exist it won't have properties
					if (!resource.isAccessible()) {
						showSvnProperties(null);
					} else {
						ISVNLocalResource svnResource = (ISVNLocalResource)resource.getAdapter(ISVNLocalResource.class);
						showSvnProperties(svnResource);
					}
				}
			}
		} catch (SVNException e) {
		}	
		
	}

	private Action getRefreshAction() {
		if (refreshAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			refreshAction = new Action(Policy.bind("SvnPropertiesView.refreshLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
				public void run() {
					refresh();
				}
			};
			refreshAction.setToolTipText(Policy.bind("SvnPropertiesView.refresh")); //$NON-NLS-1$
			refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_DISABLED));
			refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH));
		}
		return refreshAction;
	}

	private Action getAddPropertyAction() {
		if (addPropertyAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			addPropertyAction = new Action(Policy.bind("SvnPropertiesView.addPropertyLabel")) { //$NON-NLS-1$
				public void run() {
					SetSvnPropertyDialog dialog = new SetSvnPropertyDialog(getSite().getShell(),resource);
					if (dialog.open() != SetSvnPropertyDialog.OK) return;
			
					try {
						if (dialog.getPropertyValue() != null) {
							resource.setSvnProperty(dialog.getPropertyName(), dialog.getPropertyValue(),dialog.getRecurse());
						} else {
							resource.setSvnProperty(dialog.getPropertyName(), dialog.getPropertyFile(),dialog.getRecurse());
						}
					} catch (SVNException e) {
						SVNUIPlugin.openError(
							getSite().getShell(), 
							Policy.bind("SvnPropertiesView.errorAddTitle"), //$NON-NLS-1$
							Policy.bind("SvnPropertiesView.errorAddMessage"),//$NON-NLS-1$ 
							e);
					}
										
				}
			};
			addPropertyAction.setToolTipText(Policy.bind("SvnPropertiesView.addPropertyTooltip")); //$NON-NLS-1$
		}
		return addPropertyAction;
	}

	private Action getSetKeywordsAction() {
		if (setKeywordsAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			setKeywordsAction = new Action(Policy.bind("SvnPropertiesView.addKeywordsLabel")) { //$NON-NLS-1$
				public void run() {
					try {
						AddKeywordsDialog dialog = new AddKeywordsDialog(getSite().getShell(),new IResource[] { resource.getIResource() });
						if (dialog.open() != AddKeywordsDialog.OK) return;
						dialog.updateKeywords();
					} catch (SVNException e) {
						SVNUIPlugin.openError(
						getSite().getShell(), 
						Policy.bind("SvnPropertiesView.errorAddKeywordsTitle"), //$NON-NLS-1$
						Policy.bind("SvnPropertiesView.errorAddKeywordsMessage"),//$NON-NLS-1$ 
						e);
					}
				}
			};
			setKeywordsAction.setToolTipText(Policy.bind("SvnPropertiesView.addKeywordsTooltip")); //$NON-NLS-1$
		}
		return setKeywordsAction;		
	}

	private Action getModifyPropertyAction() {
		if (modifyPropertyAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			modifyPropertyAction = new Action(Policy.bind("SvnPropertiesView.modifyPropertyLabel")) { //$NON-NLS-1$
				public void run() {
					SVNPropertyModifyAction delegate = new SVNPropertyModifyAction();
					delegate.init(this);
					delegate.selectionChanged(this,tableViewer.getSelection());
					delegate.run(this); 
				}
			};			
		}
		return modifyPropertyAction;
	}

	private Action getDeletePropertyAction() {
		if (deletePropertyAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			deletePropertyAction = new Action(Policy.bind("SvnPropertiesView.deletePropertyLabel")) { //$NON-NLS-1$
				public void run() {
					SVNPropertyDeleteAction delegate = new SVNPropertyDeleteAction();
					delegate.init(this);
					delegate.selectionChanged(this,tableViewer.getSelection());
					delegate.run(this); 
				}
			};			
		}
		return deletePropertyAction;
	}

	
	/**
	 * Adds the action contributions for this view.
	 */    
	public void contributeActions() {
		// Contribute actions to popup menu for the table
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		tableViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer); 

		// Create the local tool bar
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),getDeletePropertyAction());
		IToolBarManager tbm = actionBars.getToolBarManager();
		tbm.add(getRefreshAction());
		tbm.update(false);
        
		// set F1 help
//		WorkbenchHelp.setHelp(tableViewer.getControl(), IHelpContextIds.CVS_EDITORS_VIEW);

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				getModifyPropertyAction().run();
			}
		}); 

		// set the selectionchanged listener for the table
		// updates property value when selection changes
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() != 1) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
					return;
				}
				ISVNProperty property = (ISVNProperty)ss.getFirstElement();
				textViewer.setDocument(new Document(property.getValue()));
			}
		});

		tableViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					getDeletePropertyAction().run();
				}
			}
		});

	}

	/**
	 * fill the popup menu for the table
	 */
	private void fillTableMenu(IMenuManager manager) {
		manager.add(getRefreshAction());

		Action action = getAddPropertyAction();
		try { 		
			action.setEnabled(resource.isManaged());
		} catch (SVNException e) {
			action.setEnabled(false);
		}
		manager.add(action);

		action = getSetKeywordsAction();
		try { 		
			action.setEnabled(resource.isManaged());
		} catch (SVNException e) {
			action.setEnabled(false);
		}
		manager.add(action);		
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
    
	/**
	 * Method createColumns.
	 * @param table
	 * @param layout
	 * @param viewer
	 */
	private void createColumns(Table table, TableLayout layout) {

		TableColumn col;

		// name
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("SvnPropertiesView.propertyName")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(60, true));

		// value
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("SvnPropertiesView.propertyValue")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(120, true));
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * refresh the view
	 */
	public void refresh()  {

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					updateStatus();
					tableViewer.setInput(getSvnProperties());
					tableViewer.refresh();
				} catch (SVNException e) {
					// silently ignore exception
				}
			}
		});
	}

	/**
	 * update the status text
	 *
	 */
	private void updateStatus() {
		if (resource == null) {
			statusLabel.setText("");
			return;
		}
		try {
			LocalResourceStatus status = resource.getStatus();
			if (!status.isManaged()) {
				statusLabel.setText(Policy.bind("SvnPropertiesView.resourceNotManaged")); //$NON-NLS-1$
			} else 
			if (status.getPropStatus().equals(SVNStatusKind.MODIFIED))
			{
				statusLabel.setText(Policy.bind("SvnPropertiesView.somePropertiesModified")); //$NON-NLS-1$
			} else
			if (status.getPropStatus().equals(SVNStatusKind.NORMAL)) {
				statusLabel.setText(Policy.bind("SvnPropertiesView.noPropertiesModified")); //$NON-NLS-1$
			} else
			if (status.getPropStatus().equals(SVNStatusKind.CONFLICTED))
			{
				statusLabel.setText(Policy.bind("SvnPropertiesView.conflictOnProperties")); //$NON-NLS-1$
			}
		} catch (SVNException e) {
			statusLabel.setText(Policy.bind("SvnPropertiesView.errorGettingStatus")); //$NON-NLS-1$
		}
	}

	/**
	 * Shows the properties for the given resource 
	 */
	public void showSvnProperties(ISVNLocalResource resource) throws SVNException {
		this.resource = resource;
		if (resource != null) {
			setContentDescription(Policy.bind("SvnPropertiesView.titleWithArgument", resource.getName()));
		} else {
			setContentDescription("");
		}
		refresh();
	}

	private ISVNProperty[] getSvnProperties() throws SVNException {
		
		if(resource == null) {
			// can be a null resource if we have the view open before we select anything
			return null;
		} else {
			if (resource.isManaged()) {
				return resource.getSvnProperties();
			} else {
				return null;
			}
		}
	}

}
