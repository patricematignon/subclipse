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
package org.tigris.subversion.subclipse.ui.history;

 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntry;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.OpenRemoteFileAction;
import org.tigris.subversion.subclipse.ui.console.TextViewerAction;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;


/**
 * The history view allows browsing of an array of resource revisions
 */
public class HistoryView extends ViewPart {
	private IFile file;

	// cached for efficiency
	private LogEntry[] entries;

	private HistoryTableProvider historyTableProvider;
	
	private TableViewer tableViewer;
	private TextViewer textViewer;
	
    private OpenRemoteFileAction openAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action getRevisionAction;
	private Action refreshAction;
	private Action linkWithEditorAction;

	private SashForm sashForm;
	private SashForm innerSashForm;

	private LogEntry currentSelection; 
	private boolean linkingEnabled;

	private IPreferenceStore settings;
	
	private FetchLogEntriesJob fetchLogEntriesJob = null;
	private boolean shutdown = false;

	public static final String VIEW_ID = "org.tigris.subversion.subclipse.ui.history.HistoryView"; //$NON-NLS-1$

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
			if(part == HistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partOpened(IWorkbenchPart part) {
			if(part == HistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partClosed(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
	};
	
	private IPartListener2 partListener2 = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		public void partHidden(IWorkbenchPartReference ref) {
		}
		public void partVisible(IWorkbenchPartReference ref) {
			if(ref.getPart(true) == HistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
		}
	};

    /**
     * All Actions use this class 
     * This action :
     * - updates currentSelection
     * - action.run 
     */
    private Action getContextMenuAction(String title, final IWorkspaceRunnable action) {
            return new Action(title) {
            public void run() {
                try {
                    if (file == null) return;
                    ISelection selection = tableViewer.getSelection();
                    if (!(selection instanceof IStructuredSelection)) return;
                    IStructuredSelection ss = (IStructuredSelection)selection;
                    currentSelection = (LogEntry)ss.getFirstElement();
                    new ProgressMonitorDialog(getViewSite().getShell()).run(false, true, new WorkspaceModifyOperation() {
                        protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
                            try {               
                                action.run(monitor);
                            } catch (CoreException e) {
                                throw new InvocationTargetException(e);
                            }
                        }
                    });
                } catch (InvocationTargetException e) {
                    SVNUIPlugin.openError(getViewSite().getShell(), null, null, e, SVNUIPlugin.LOG_NONTEAM_EXCEPTIONS);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
            
            // we don't allow multiple selection
            public boolean isEnabled() {
                ISelection selection = tableViewer.getSelection();
                if (!(selection instanceof IStructuredSelection)) return false;
                IStructuredSelection ss = (IStructuredSelection)selection;
                if(ss.size() != 1) return false;
                return true;
            }
        };
    }
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Refresh (toolbar)
		SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
		refreshAction = new Action(Policy.bind("HistoryView.refreshLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
			public void run() {
				refresh();
			}
		};
		refreshAction.setToolTipText(Policy.bind("HistoryView.refresh")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH));
		
		//  Link with Editor (toolbar)
		linkWithEditorAction = new Action(Policy.bind("HistoryView.linkWithLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_LINK_WITH_EDITOR_ENABLED)) { //$NON-NLS-1$
			public void run() {
				setLinkingEnabled(isChecked());
			}
		};
		linkWithEditorAction.setToolTipText(Policy.bind("HistoryView.linkWithLabel")); //$NON-NLS-1$
		linkWithEditorAction.setHoverImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_LINK_WITH_EDITOR));
		linkWithEditorAction.setChecked(isLinkingEnabled());

		// Double click open action
        openAction = new OpenRemoteFileAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

        // get contents        
		getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				ISVNRemoteFile remoteFile = (ISVNRemoteFile)currentSelection.getRemoteResource();
				monitor.beginTask(null, 100);
				try {
					if(confirmOverwrite()) {
						InputStream in = ((IResourceVariant)remoteFile).getStorage(new SubProgressMonitor(monitor,50)).getContents();
						file.setContents(in, false, true, new SubProgressMonitor(monitor, 50));				
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				} finally {
					monitor.done();
				}
			}
		});
		WorkbenchHelp.setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);	

        // update to the selected revision
		getRevisionAction = getContextMenuAction(Policy.bind("HistoryView.getRevisionAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				ISVNRemoteFile remoteFile = (ISVNRemoteFile)currentSelection.getRemoteResource();
				try {
					if(confirmOverwrite()) {
						// Update does not support overwriting the WC, so it must be reverted first
						ISVNLocalFile svnFile = SVNWorkspaceRoot.getSVNFileFor(file);
						if (svnFile.isModified()) {
							svnFile.revert();
						}

						SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(file.getProject());
                        provider.update(new IResource[] {file}, remoteFile.getLastChangedRevision(), monitor);					 
						historyTableProvider.setFile(remoteFile);
						tableViewer.refresh();
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				}
			}
		});
		WorkbenchHelp.setHelp(getRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);	

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
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(refreshAction);
		tbm.add(linkWithEditorAction);
		tbm.update(false);
        
        IActionBars actionBars = getViewSite().getActionBars();
        
		// Create actions for the text editor (copy and select all)
		copyAction = new TextViewerAction(textViewer, ITextOperationTarget.COPY);
		copyAction.setText(Policy.bind("HistoryView.copy")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
		
		selectAllAction = new TextViewerAction(textViewer, ITextOperationTarget.SELECT_ALL);
		selectAllAction.setText(Policy.bind("HistoryView.selectAll")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);

		actionBars.updateActionBars();

        // Contribute actions to popup menu for the comments area
		menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTextMenu(menuMgr);
			}
		});
		StyledText text = textViewer.getTextWidget();
		menu = menuMgr.createContextMenu(text);
		text.setMenu(menu);
	}
    
	/*
	 * Method declared on IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		settings = SVNUIPlugin.getPlugin().getPreferenceStore();
		this.linkingEnabled = settings.getBoolean(ISVNUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING);

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = createTable(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		textViewer = createText(innerSashForm);
		sashForm.setWeights(new int[] { 70, 30 });

		contributeActions();
		// set F1 help
		WorkbenchHelp.setHelp(sashForm, IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();

		// add listener for editor page activation - this is to support editor linking
		getSite().getPage().addPartListener(partListener);  
		getSite().getPage().addPartListener(partListener2); 
	}

	public void dispose() {
	    shutdown = true;
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
	}   

	/**
	 * Creates the group that displays lists of the available repositories
	 * and team streams.
	 *
	 * @param the parent composite to contain the group
	 * @return the group control
	 */
	protected TableViewer createTable(Composite parent) {
		
		historyTableProvider = new HistoryTableProvider();
		TableViewer viewer = historyTableProvider.createTable(parent);
		
		// set the content provider for the table
		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				// Short-circuit to optimize
				if (entries != null) return entries;
				
				if (!(inputElement instanceof ISVNRemoteFile)) return null;
				final ISVNRemoteFile remoteFile = (ISVNRemoteFile)inputElement;

				if(fetchLogEntriesJob == null) {
					fetchLogEntriesJob = new FetchLogEntriesJob();
				}
				if(fetchLogEntriesJob.getState() != Job.NONE) {
					fetchLogEntriesJob.cancel();
					try {
						fetchLogEntriesJob.join();
					} catch (InterruptedException e) {
						SVNUIPlugin.log(new SVNException(Policy.bind("HistoryView.errorFetchingEntries", remoteFile.getName()), e)); //$NON-NLS-1$
					}
				}
				fetchLogEntriesJob.setRemoteFile(remoteFile);
				Utils.schedule(fetchLogEntriesJob, getViewSite());

				return new Object[0];
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				entries = null;
			}
		});
		
        // set the selectionchanged listener for the table
        // updates the comments when selection changes
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
				LogEntry entry = (LogEntry)ss.getFirstElement();
				textViewer.setDocument(new Document(entry.getComment()));
			}
		});
		
		return viewer;
	}

    /**
     * Create the TextViewer for the logEntry comments 
     */
	protected TextViewer createText(Composite parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				copyAction.update();
			}
		});
		return result;
	}

	/**
	 * Returns the table viewer contained in this view.
	 */
	protected TableViewer getViewer() {
		return tableViewer;
	}

	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance()};
		tableViewer.addDropSupport(ops, transfers, new HistoryDropAdapter(tableViewer, this));
	}

    /**
     * fill the popup menu for the table
     */
	private void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		if (file != null) {
			// Add the "Add to Workspace" action if 1 revision is selected.
			ISelection sel = tableViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					if (((IStructuredSelection)sel).size() == 1) {
						manager.add(getContentsAction);
						manager.add(getRevisionAction);
						manager.add(new Separator());
					}
				}
			}
		}
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(refreshAction);
		manager.add(new Separator("additions-end")); //$NON-NLS-1$
	}
    
    /**
     * fill the popup menu for the comments area 
     */
	private void fillTextMenu(IMenuManager manager) {
		manager.add(copyAction);
		manager.add(selectAllAction);
	}
    
//	/**
//	 * Makes the history view visible in the active perspective. If there
//	 * isn't a history view registered <code>null</code> is returned.
//	 * Otherwise the opened view part is returned.
//	 */
//	public static HistoryView openInActivePerspective() {
//		try {
//			return (HistoryView)SVNUIPlugin.getActivePage().showView(VIEW_ID);
//		} catch (PartInitException pe) {
//			return null;
//		}
//	}

	/** (Non-javadoc)
	 * Method declared on IWorkbenchPart
	 */
	public void setFocus() {
		if (tableViewer != null) {
			Table control = tableViewer.getTable();
			if (control != null && !control.isDisposed()) {
				control.setFocus();
			}
		}
	}

	/**
	 * An editor has been activated.  Fetch the history if it is shared with SVN and the history view
	 * is visible in the current page.
	 * 
	 * @param editor the active editor
	 * @since 3.0
	 */
	protected void editorActivated(IEditorPart editor) {
		// Only fetch contents if the view is shown in the current page.
		if (editor == null || !isLinkingEnabled() || !checkIfPageIsVisible()) {
			return;
		}       
		IEditorInput input = editor.getEditorInput();
		// Handle compare editors opened from the Synchronize View
		// TODO uncommnet when there is sync support        
		//        if (input instanceof SyncInfoCompareInput) {
		//            SyncInfoCompareInput syncInput = (SyncInfoCompareInput) input;
		//            SyncInfo info = syncInput.getSyncInfo();
		//            if(info instanceof SVNSyncInfo && info.getLocal().getType() == IResource.FILE) {
		//                ISVNRemoteFile remote = (ISVNRemoteFile)info.getRemote();
		//                ISVNRemoteFile base = (ISVNRemoteFile)info.getBase();
		//                if(remote != null) {
		//                    showHistory(remote, false);
		//                } else if(base != null) {
		//                    showHistory(base, false);
		//                }
		//            }
		//        // Handle editors opened on remote files
		//        } else
		if(input instanceof RemoteFileEditorInput) {
			ISVNRemoteFile remote = ((RemoteFileEditorInput)input).getSVNRemoteFile();
			if(remote != null) {
				showHistory(remote, false);
			}
			// Handle regular file editors
		} else if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			showHistory(file, false);           
		}
	}
	
	private boolean checkIfPageIsVisible() {
		return getViewSite().getPage().isPartVisible(this);
	}
	
	/**
	 * Shows the history for the given ISVNRemoteFile in the view.
	 */
	public void showHistory(ISVNRemoteFile remoteFile, boolean refetch) {
		if (remoteFile == null) {
			tableViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
			return;
		}
		ISVNRemoteFile existingFile = historyTableProvider.getISVNFile(); 
		if(!refetch && existingFile != null && existingFile.equals(remoteFile)) return;
		this.file = null;
		historyTableProvider.setFile(remoteFile);
		tableViewer.setInput(remoteFile);
		setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName())); //$NON-NLS-1$
		setTitleToolTip(remoteFile.getRepositoryRelativePath());
	}

	/**
	 * Shows the history for the given IResource in the view.
	 * 
	 * Only files are supported for now.
	 */
	public void showHistory(IResource resource, boolean refetch) {
		if (resource instanceof IFile) {
			IFile newfile = (IFile)resource;
			if(!refetch && this.file != null && newfile.equals(this.file)) {
				return;
			} 
			this.file = newfile;
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(file.getProject(), SVNProviderPlugin.getTypeId());
			if (teamProvider != null) {
				try {
					ISVNRemoteFile remoteFile = (ISVNRemoteFile)SVNWorkspaceRoot.getBaseResourceFor(file);
					if (remoteFile != null) {
						historyTableProvider.setFile(remoteFile);
						tableViewer.setInput(remoteFile);
						setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName())); //$NON-NLS-1$
						setTitleToolTip(remoteFile.getRepositoryRelativePath());
					}
				} catch (TeamException e) {
					SVNUIPlugin.openError(getViewSite().getShell(), null, null, e);
				}				
			}
		} else {
			this.file = null;
			tableViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
		}
		
	}
	
	/**
	 * Shows the history for the given ISVNRemoteFile in the view.
	 */
	public void showHistory(ISVNRemoteFile remoteFile, String currentRevision) {
		if (remoteFile == null) {
			tableViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
			return;
		}
		this.file = null;
		historyTableProvider.setFile(remoteFile);
		tableViewer.setInput(remoteFile);
		setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName()));
		setTitleToolTip(""); //$NON-NLS-1$
	}
	
    
	/**
     * Ask the user to confirm the overwrite of the file if the file has been modified
     * since last commit
	 */
	private boolean confirmOverwrite() {
		if (file!=null && file.exists()) {
			ISVNLocalFile svnFile = SVNWorkspaceRoot.getSVNFileFor(file);
			try {
				if(svnFile.isModified()) {
					String title = Policy.bind("HistoryView.overwriteTitle"); //$NON-NLS-1$
					String msg = Policy.bind("HistoryView.overwriteMsg"); //$NON-NLS-1$
					final MessageDialog dialog = new MessageDialog(getViewSite().getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
					final int[] result = new int[1];
					getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						result[0] = dialog.open();
					}});
					if (result[0] != 0) {
						// cancel
						return false;
					}
				}
			} catch(SVNException e) {
				SVNUIPlugin.log(e.getStatus());
			}
		}
		return true;
	}
	
	/*
	 * Refresh the view by refetching the log entries for the remote file
	 */
	private void refresh() {
		entries = null;
        // show a Busy Cursor during refresh
		BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Enabled linking to the active editor
	 * @since 3.0
	 */
	public void setLinkingEnabled(boolean enabled) {
		this.linkingEnabled = enabled;
		
		// remember the last setting in the dialog settings     
		settings.setValue(ISVNUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING, enabled);
		
		// if turning linking on, update the selection to correspond to the active editor
		if (enabled) {
			editorActivated(getSite().getPage().getActiveEditor());
		}
	}
	
	/**
	 * Returns if linking to the ative editor is enabled or disabled.
	 * @return boolean indicating state of editor linking.
	 */
	private boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	/**
	 * Select the revision in the receiver.
	 */
	public void selectRevision(SVNRevision revision) {
		if (entries == null) {
			return;
		}
	
		ILogEntry entry = null;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getRevision().equals(revision)) {
				entry = entries[i];
				break;
			}
		}
	
		if (entry != null) {
			IStructuredSelection selection = new StructuredSelection(entry);
			tableViewer.setSelection(selection, true);
		}
	}

	private class FetchLogEntriesJob extends Job {
		public ISVNRemoteFile remoteFile;
		public FetchLogEntriesJob() {
			super(Policy.bind("HistoryView.fetchHistoryJob"));  //$NON-NLS-1$;
		}
		public void setRemoteFile(ISVNRemoteFile file) {
			this.remoteFile = file;
		}
		public IStatus run(IProgressMonitor monitor) {
			try {
				if(remoteFile != null && !shutdown) {
					entries = remoteFile.getLogEntries(monitor);
					final SVNRevision revisionId = remoteFile.getRevision();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableViewer != null && ! tableViewer.getTable().isDisposed()) {
								tableViewer.add(entries);
								selectRevision(revisionId);
							}
						}
					});
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return e.getStatus();
			}
		}
	};
	
}
