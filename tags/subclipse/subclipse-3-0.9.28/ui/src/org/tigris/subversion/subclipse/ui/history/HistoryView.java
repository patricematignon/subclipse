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
package org.tigris.subversion.subclipse.ui.history;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceColors;
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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.tigris.subversion.subclipse.core.IResourceStateChangeListener;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
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
import org.tigris.subversion.subclipse.ui.operations.ReplaceOperation;
import org.tigris.subversion.subclipse.ui.settings.ProjectProperties;
import org.tigris.subversion.subclipse.ui.util.LinkList;
import org.tigris.subversion.svnclientadapter.SVNRevision;


/**
 * The history view allows browsing of an array of resource revisions
 * call either 
 * - showHistory(IResource resource, boolean refetch) or
 * - showHistory(ISVNRemoteFile remoteFile, boolean refetch)
 */
public class HistoryView extends ViewPart implements IResourceStateChangeListener {
	// the resource for which we want to see the history or null if we use showHistory(ISVNRemoteResource)
	private IResource resource;
	
	private ProjectProperties projectProperties;
	private LinkList linkList;
	private boolean mouseDown = false; 
	private boolean dragEvent = false;
	private Cursor handCursor;
	private Cursor busyCursor;

	// cached for efficiency
	private ILogEntry[] entries;

	private HistoryTableProvider historyTableProvider;
	private ChangePathsTableProvider changePathsTableProvider;
    
	private TableViewer tableHistoryViewer;
    private TableViewer tableChangePathViewer;
	private TextViewer textViewer;
	
    private Action openAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action updateToRevisionAction;
	private Action refreshAction;
	private Action linkWithEditorAction;
    private Action openChangedPathAction;

	private SashForm sashForm;
	private SashForm innerSashForm;

	private ILogEntry currentSelection; 
	private boolean linkingEnabled;

	private IPreferenceStore settings;
	
	private FetchLogEntriesJob fetchLogEntriesJob = null;
	private boolean shutdown = false;
    
    // we disable editor activation when double clicking on a log entry
    private boolean disableEditorActivation = false;

	public static final String VIEW_ID = "org.tigris.subversion.subclipse.ui.history.HistoryView"; //$NON-NLS-1$

	public HistoryView() {
	    SVNProviderPlugin.addResourceStateChangeListener(this);
	    this.projectProperties = new ProjectProperties();
	}

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
     * All context menu actions use this class 
     * This action :
     * - updates currentSelection
     * - action.run 
     */
    private Action getContextMenuAction(String title, final IWorkspaceRunnable action) {
            return new Action(title) {
            public void run() {
                try {
                    if (resource == null) return;
                    ISelection selection = tableHistoryViewer.getSelection();
                    if (!(selection instanceof IStructuredSelection)) return;
                    IStructuredSelection ss = (IStructuredSelection)selection;
                    currentSelection = (ILogEntry)ss.getFirstElement();
                    PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
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
                ISelection selection = tableHistoryViewer.getSelection();
                if (!(selection instanceof IStructuredSelection)) return false;
                IStructuredSelection ss = (IStructuredSelection)selection;
                if(ss.size() != 1) return false;
                currentSelection = (ILogEntry)ss.getFirstElement();
                return true;
            }
        };
    }
	
    // open remote file action (double-click)
	private Action getOpenRemoteFileAction() {
        if (openAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			openAction = new Action() {
				public void run() {
					OpenRemoteFileAction delegate = new OpenRemoteFileAction();
					delegate.init(this);
					delegate.selectionChanged(this,tableHistoryViewer.getSelection());
					if (isEnabled()) {
                        try {
                            disableEditorActivation = true;
                        	delegate.run(this);
                        } finally {
                            disableEditorActivation = false;
                        }
                    }
				}
			};			
		}
		return openAction;
	}

    // open changed Path (double-click)
    private Action getOpenChangedPathAction() {
        if (openChangedPathAction == null) {
            SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
            openChangedPathAction = new Action() {
                public void run() {
                    OpenRemoteFileAction delegate = new OpenRemoteFileAction();
                    delegate.init(this);
                    delegate.selectionChanged(this,tableChangePathViewer.getSelection());
                    if (isEnabled()) {
                        try {
                            disableEditorActivation = true;
                            delegate.run(this);
                        } finally {
                            disableEditorActivation = false;
                        }
                    }
                }
            };          
        }
        return openChangedPathAction;
        
    }
    
	// Refresh action (toolbar)
	private Action getRefreshAction() {
		if (refreshAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			refreshAction = new Action(Policy.bind("HistoryView.refreshLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
				public void run() {
					refresh();
				}
			};
			refreshAction.setToolTipText(Policy.bind("HistoryView.refresh")); //$NON-NLS-1$
			refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_DISABLED));
			refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH));			
		}
		return refreshAction;
	}

	//  Link with Editor action (toolbar)	
	private Action getLinkWithEditorAction() {
		if (linkWithEditorAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			linkWithEditorAction = new Action(Policy.bind("HistoryView.linkWithLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_LINK_WITH_EDITOR_ENABLED)) { //$NON-NLS-1$
				public void run() {
					setLinkingEnabled(isChecked());
				}
			};
			linkWithEditorAction.setToolTipText(Policy.bind("HistoryView.linkWithLabel")); //$NON-NLS-1$
			linkWithEditorAction.setHoverImageDescriptor(plugin.getImageDescriptor(ISVNUIConstants.IMG_LINK_WITH_EDITOR));
			linkWithEditorAction.setChecked(isLinkingEnabled());
		}
		return linkWithEditorAction;
	}
	
	// get contents Action (context menu)
	private Action getGetContentsAction() {
		if (getContentsAction == null) {
			getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
				public void run(IProgressMonitor monitor) throws CoreException {
					IFile file = (IFile)resource;
					ISVNRemoteFile remoteFile = (ISVNRemoteFile)currentSelection.getRemoteResource();
					monitor.beginTask(null, 100);
					try {
                        if (remoteFile != null) {
    						if(confirmOverwrite()) {
    							InputStream in = ((IResourceVariant)remoteFile).getStorage(new SubProgressMonitor(monitor,50)).getContents();
    							file.setContents(in, false, true, new SubProgressMonitor(monitor, 50));				
    						}
                        }
					} catch (TeamException e) {
						throw new CoreException(e.getStatus());
					} finally {
						monitor.done();
					}
				}
			});
			WorkbenchHelp.setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);
		}
		return getContentsAction;
	}

    // update to the selected revision (context menu)	
	private Action getUpdateToRevisionAction() {
		if (updateToRevisionAction == null) {
			updateToRevisionAction = getContextMenuAction(Policy.bind("HistoryView.getRevisionAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
				public void run(IProgressMonitor monitor) throws CoreException {
					IFile file = (IFile)resource;
					ISVNRemoteFile remoteFile = (ISVNRemoteFile)currentSelection.getRemoteResource();
					try {
                        if (remoteFile != null) {
    						if(confirmOverwrite()) {
                                new ReplaceOperation(HistoryView.this, new IResource[] {file}, remoteFile.getLastChangedRevision()).run(monitor);
      							historyTableProvider.setRemoteResource(remoteFile);
    							Display.getDefault().asyncExec(new Runnable() {
    								public void run() {
    								    tableHistoryViewer.refresh();
    								}
    							});
    						}
                        }
					} catch (InvocationTargetException e) {
						throw new CoreException(new SVNStatus(IStatus.ERROR, 0, e.getMessage()));
					} catch (InterruptedException e) {
						// Cancelled by user
					}
				} 
			});
			WorkbenchHelp.setHelp(updateToRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);	
		}
		return updateToRevisionAction;
	}
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Double click open action
		tableHistoryViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				getOpenRemoteFileAction().run();
			}
		});

        tableChangePathViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                getOpenChangedPathAction().run();
            }
        });
        
		// Contribute actions to popup menu for the table
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableHistoryViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		tableHistoryViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableHistoryViewer);

		// Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(getRefreshAction());
		tbm.add(getLinkWithEditorAction());
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
	    busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
	    handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		settings = SVNUIPlugin.getPlugin().getPreferenceStore();
		this.linkingEnabled = settings.getBoolean(ISVNUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING);

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableHistoryViewer = createTableHistory(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		textViewer = createText(innerSashForm);
		Font font = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(ISVNUIConstants.SVN_COMMENT_FONT);
		if (font != null) textViewer.getTextWidget().setFont(font);
		textViewer.getTextWidget().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}
				mouseDown = true;
			}
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
				StyledText text = (StyledText)e.widget;
				int offset = text.getCaretOffset();
				if (dragEvent) {
					// don't activate a link during a drag/mouse up operation
					dragEvent = false;
					if (linkList != null && linkList.isLinkAt(offset)) {
						text.setCursor(handCursor);
					}
				} else {
					if (linkList != null && linkList.isLinkAt(offset)) {	
						text.setCursor(busyCursor);
						openLink(linkList.getLinkAt(offset));
						text.setCursor(null);
					}
				}
			}			
		});
		
		textViewer.getTextWidget().addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
    			// Do not change cursor on drag events
    			if (mouseDown) {
    				if (!dragEvent) {
    					StyledText text = (StyledText)e.widget;
    					text.setCursor(null);
    				}
    				dragEvent = true;
    				return;
    			}
    			StyledText text = (StyledText)e.widget;
    			int offset = -1;
    			try {
    				offset = text.getOffsetAtLocation(new Point(e.x, e.y));
    			} catch (IllegalArgumentException ex) {}
    			if (offset == -1)
    				text.setCursor(null);
    			else if (linkList != null && linkList.isLinkAt(offset)) 
    				text.setCursor(handCursor);
    			else 
    				text.setCursor(null);                
            }		    
		});
		
        tableChangePathViewer = createTableChangePath(innerSashForm);
		sashForm.setWeights(new int[] { 70, 30 });

		contributeActions();
		// set F1 help
		WorkbenchHelp.setHelp(sashForm, IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();

		// add listener for editor page activation - this is to support editor linking
		getSite().getPage().addPartListener(partListener);  
		getSite().getPage().addPartListener(partListener2); 
	}
	
	private void openLink(String href) {
	    Program.launch(href);
    }

    public void dispose() {
	    shutdown = true;
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
		SVNProviderPlugin.removeResourceStateChangeListener(this);
		if (busyCursor != null) busyCursor.dispose();
		if (handCursor != null) handCursor.dispose();
	}   

    
    protected TableViewer createTableChangePath(Composite parent) {
        changePathsTableProvider = new ChangePathsTableProvider();
        tableChangePathViewer = changePathsTableProvider.createTable(parent);
        
        tableChangePathViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
                if ((inputElement == null) || (!(inputElement instanceof ILogEntry))) {
                    return null;
                }
                ILogEntry logEntry = (ILogEntry)inputElement;
				return logEntry.getLogEntryChangePaths();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
            
        });
        return tableChangePathViewer;
    }
    
	/**
	 * Creates the table that displays the log history
	 *
	 * @param the parent composite to contain the group
	 * @return the group control
	 */
	protected TableViewer createTableHistory(Composite parent) {
		
		historyTableProvider = new HistoryTableProvider();
		tableHistoryViewer = historyTableProvider.createTable(parent);
		
		// set the content provider for the table
		tableHistoryViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				// Short-circuit to optimize
				if (entries != null) return entries;
				
				if (!(inputElement instanceof ISVNRemoteResource)) return null;
				final ISVNRemoteResource remoteResource = (ISVNRemoteResource)inputElement;

				if(fetchLogEntriesJob == null) {
					fetchLogEntriesJob = new FetchLogEntriesJob();
				}
				if(fetchLogEntriesJob.getState() != Job.NONE) {
					fetchLogEntriesJob.cancel();
					try {
						fetchLogEntriesJob.join();
					} catch (InterruptedException e) {
						SVNUIPlugin.log(new SVNException(Policy.bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
					}
				}
				fetchLogEntriesJob.setRemoteFile(remoteResource);
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
		tableHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
                    changePathsTableProvider.setLogEntry(null);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() != 1) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
                    changePathsTableProvider.setLogEntry(null);
					return;
				}
				LogEntry entry = (LogEntry)ss.getFirstElement();
				textViewer.setDocument(new Document(entry.getComment()));
				StyledText text = textViewer.getTextWidget();
				if (projectProperties == null) linkList = ProjectProperties.getUrls(entry.getComment());
				else linkList = projectProperties.getLinkList(entry.getComment());
				if (linkList != null) {
					int[][] linkRanges = linkList.getLinkRanges();
					String[] urls = linkList.getUrls();
					for (int i = 0; i < linkRanges.length; i++) {
						  text.setStyleRange(new StyleRange(linkRanges[i][0], linkRanges[i][1], JFaceColors.getHyperlinkText(Display.getCurrent()), null));				       
					}
				}
                changePathsTableProvider.setLogEntry(entry);               
			}
		});
		
		return tableHistoryViewer;
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
		return tableHistoryViewer;
	}

	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance()};
		tableHistoryViewer.addDropSupport(ops, transfers, new HistoryDropAdapter(tableHistoryViewer, this));
	}

    /**
     * fill the popup menu for the table
     */
	private void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		if (resource != null) {
			// Add the "Add to Workspace" action if 1 revision is selected.
			ISelection sel = tableHistoryViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					if (((IStructuredSelection)sel).size() == 1) {
						if (resource instanceof IFile) {
							manager.add(getGetContentsAction());
							manager.add(getUpdateToRevisionAction());
							manager.add(new Separator());
						}
					}
				}
			}
		}
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(getRefreshAction());
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
		if (tableHistoryViewer != null) {
			Table control = tableHistoryViewer.getTable();
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
		if (disableEditorActivation) {
			return;
        }
        
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
	public void showHistory(ISVNRemoteResource remoteResource, boolean refetch) {
		if (remoteResource == null) {
			tableHistoryViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
			return;
		}
		ISVNRemoteResource existingRemoteResource = historyTableProvider.getRemoteResource(); 
		if(!refetch && existingRemoteResource != null && existingRemoteResource.equals(remoteResource)) return;
		this.resource = null;
        projectProperties = ProjectProperties.getProjectProperties(remoteResource);
		historyTableProvider.setRemoteResource(remoteResource);
		tableHistoryViewer.setInput(remoteResource);
		setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteResource.getName())); //$NON-NLS-1$
		setTitleToolTip(remoteResource.getRepositoryRelativePath());
	}

	/**
	 * Shows the history for the given IResource in the view.
	 * 
	 */
	public void showHistory(IResource resource, boolean refetch) {
		if(!refetch && this.resource != null && resource.equals(this.resource)) {
			return;
		} 
		
		RepositoryProvider teamProvider = RepositoryProvider.getProvider(resource.getProject(), SVNProviderPlugin.getTypeId());
		if (teamProvider != null) {
			try {
				ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
				if ( localResource != null
				        && !localResource.getStatus().isAdded()
				        && localResource.getStatus().isManaged() ) {
                    projectProperties = ProjectProperties.getProjectProperties(resource);
					ISVNRemoteResource baseResource = localResource.getBaseResource();
					historyTableProvider.setRemoteResource(baseResource);
					tableHistoryViewer.setInput(baseResource);
					setContentDescription(Policy.bind("HistoryView.titleWithArgument", baseResource.getName())); //$NON-NLS-1$
					setTitleToolTip(baseResource.getRepositoryRelativePath());
					this.resource = resource;
				}
			} catch (TeamException e) {
				SVNUIPlugin.openError(getViewSite().getShell(), null, null, e);
			}				
		}
	}
	
	/**
	 * Shows the history for the given ISVNRemoteFile in the view.
	 */
	public void showHistory(ISVNRemoteResource remoteResource, String currentRevision) {
		if (remoteResource == null) {
			tableHistoryViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
			return;
		}
		this.resource = null;
        projectProperties = ProjectProperties.getProjectProperties(remoteResource);
		historyTableProvider.setRemoteResource(remoteResource);
		tableHistoryViewer.setInput(remoteResource);
		setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteResource.getName())); //$NON-NLS-1$
		setTitleToolTip(""); //$NON-NLS-1$
	}
	
    
	/**
     * Ask the user to confirm the overwrite of the file if the file has been modified
     * since last commit
	 */
	private boolean confirmOverwrite() {
		IFile file = (IFile)resource;
		if (file!=null && file.exists()) {
			ISVNLocalFile svnFile = SVNWorkspaceRoot.getSVNFileFor(file);
			try {
				if(svnFile.isDirty()) {
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
		BusyIndicator.showWhile(tableHistoryViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
			    if (resource != null)
                    try {
                        projectProperties = ProjectProperties.getProjectProperties(resource);
                    } catch (SVNException e) {}
				tableHistoryViewer.refresh();
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
	public void selectRevision(SVNRevision.Number revision) {
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
			tableHistoryViewer.setSelection(selection, true);
		}
	}

	/**
	 * This method updates the history table, highlighting the current revison
	 * without refetching the log entries to preserve bandwidth.
	 * The user has to a manual refresh to get the new log entries. 
	 */
    private void resourceChanged() {
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
        	public void run() {
        		ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        		try {
                    if (localResource != null && !localResource.getStatus().isAdded()) {
                    	ISVNRemoteResource baseResource = localResource.getBaseResource();
                    	historyTableProvider.setRemoteResource(baseResource);
                    	tableHistoryViewer.refresh();
                    }
                } catch (SVNException e) {
                    SVNUIPlugin.openError(getViewSite().getShell(), null, null, e);
                }
        	}
        });
    }

	private class FetchLogEntriesJob extends Job {
		public ISVNRemoteResource remoteResource;
		public FetchLogEntriesJob() {
			super(Policy.bind("HistoryView.fetchHistoryJob"));  //$NON-NLS-1$;
		}
		public void setRemoteFile(ISVNRemoteResource resource) {
			this.remoteResource = resource;
		}
		public IStatus run(IProgressMonitor monitor) {
			try {
				if(remoteResource != null && !shutdown) {
					entries = remoteResource.getLogEntries(monitor);
					final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableHistoryViewer != null && ! tableHistoryViewer.getTable().isDisposed()) {
                                // once we got the entries, we refresh the table 
                                tableHistoryViewer.refresh();
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
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
     */
    public void resourceSyncInfoChanged(IResource[] changedResources) {
        for (int i = 0; i < changedResources.length; i++) {
            IResource changedResource = changedResources[i];
            if( changedResource.equals( resource ) ) {
				resourceChanged();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#resourceModified(org.eclipse.core.resources.IResource[])
     */
    public void resourceModified(IResource[] changedResources) {
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
     */
    public void projectConfigured(IProject project) {
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
     */
    public void projectDeconfigured(IProject project) {
    }
	
}
