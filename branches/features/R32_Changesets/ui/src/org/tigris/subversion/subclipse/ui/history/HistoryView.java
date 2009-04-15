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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.jface.action.IAction;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
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
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.tigris.subversion.subclipse.core.IResourceStateChangeListener;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.core.commands.ChangeCommitPropertiesCommand;
import org.tigris.subversion.subclipse.core.history.AliasManager;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.core.sync.SVNStatusSyncInfo;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.OpenRemoteFileAction;
import org.tigris.subversion.subclipse.ui.actions.RemoteResourceTransfer;
import org.tigris.subversion.subclipse.ui.actions.WorkspaceAction;
import org.tigris.subversion.subclipse.ui.console.TextViewerAction;
import org.tigris.subversion.subclipse.ui.dialogs.BranchTagDialog;
import org.tigris.subversion.subclipse.ui.dialogs.SetCommitPropertiesDialog;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.subclipse.ui.internal.Utils;
import org.tigris.subversion.subclipse.ui.operations.BranchTagOperation;
import org.tigris.subversion.subclipse.ui.operations.MergeOperation;
import org.tigris.subversion.subclipse.ui.operations.ReplaceOperation;
import org.tigris.subversion.subclipse.ui.settings.ProjectProperties;
import org.tigris.subversion.subclipse.ui.util.LinkList;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;


/**
 * The history view allows browsing of an array of resource revisions
 * call either 
 * - showHistory(IResource resource, boolean refetch) or
 * - showHistory(ISVNRemoteFile remoteFile, boolean refetch)
 */
public class HistoryView extends ViewPart implements IResourceStateChangeListener {
    // the resource for which we want to see the history or null if we use showHistory(ISVNRemoteResource)
	private IResource resource;
	
	private ISVNRemoteResource remoteResource;
	
	private ProjectProperties projectProperties;
	private LinkList linkList;
	private boolean mouseDown = false; 
	private boolean dragEvent = false;
	private Cursor handCursor;
	private Cursor busyCursor;
	
	private AliasManager tagManager;

	// cached for efficiency
	private ILogEntry[] entries;
	
	LogEntryChangePath[] currentLogEntryChangePath;

	private HistoryTableProvider historyTableProvider;
	StructuredViewer changePathsViewer;
    
	private TableViewer tableHistoryViewer;
	private TextViewer textViewer;
	
	private ISelection selection;
	
    private Action openAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action updateToRevisionAction;
	private Action refreshAction;
	private Action linkWithEditorAction;
    private Action openChangedPathAction;
    private Action setCommitPropertiesAction;
    private Action getAllAction;
    private Action getNextAction;
    private Action showDifferencesAsUnifiedDiffAction;
    private Action createTagFromRevisionAction;
    private Action revertChangesAction;

    private IAction toggleWrapCommentsAction;
    private IAction toggleShowComments;
    private IAction toggleShowAffectedPathsAction;
    private IAction toggleStopOnCopyAction;
    private ToggleAffectedPathsLayoutAction[] toggleAffectedPathsLayoutActions;
    
	private SashForm sashForm;
	private SashForm innerSashForm;

	private boolean linkingEnabled;
	private ILogEntry lastEntry;

	private IPreferenceStore settings;

    private boolean showComments;
	private boolean showAffectedPaths;
	private boolean wrapCommentsText;
	
	private FetchLogEntriesJob fetchLogEntriesJob = null;
	private FetchAllLogEntriesJob fetchAllLogEntriesJob = null;
	private FetchNextLogEntriesJob fetchNextLogEntriesJob = null;
	private boolean shutdown = false;
	private SVNRevision revisionStart = SVNRevision.HEAD;
	
	FetchChangePathJob fetchChangePathJob = null;
	
	private static HistoryView view;
    
    // we disable editor activation when double clicking on a log entry
    private boolean disableEditorActivation = false;

	public static final String VIEW_ID = "org.tigris.subversion.subclipse.ui.history.HistoryView"; //$NON-NLS-1$

	public HistoryView() {
		SVNUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(ISVNUIConstants.PREF_FETCH_CHANGE_PATH_ON_DEMAND)) {
					entries = null;
					lastEntry = null;
					currentLogEntryChangePath = null;
					tableHistoryViewer.refresh();
                    changePathsViewer.refresh();
				}
			}
		});
		
	    SVNProviderPlugin.addResourceStateChangeListener(this);
	    this.projectProperties = new ProjectProperties();
	    view = this;
        
        toggleAffectedPathsLayoutActions = new ToggleAffectedPathsLayoutAction[] {
            new ToggleAffectedPathsLayoutAction(this, ISVNUIConstants.LAYOUT_FLAT),
            new ToggleAffectedPathsLayoutAction(this, ISVNUIConstants.LAYOUT_COMPRESSED),
          };
        
        IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
        showComments = store.getBoolean(ISVNUIConstants.PREF_SHOW_COMMENTS);
        wrapCommentsText = store.getBoolean(ISVNUIConstants.PREF_WRAP_COMMENTS);
        showAffectedPaths = store.getBoolean(ISVNUIConstants.PREF_SHOW_PATHS);
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
                ISelection selection = getSelection();
                return selection instanceof IStructuredSelection && 
                        ((IStructuredSelection)selection).size() == 1;
            }
        };
    }

    private Action getSetCommitPropertiesAction() {
    	// set Action (context menu)
		if (setCommitPropertiesAction == null) {
	       	setCommitPropertiesAction = new Action(Policy.bind("HistoryView.setCommitProperties")) {
		        public void run() {
		        	try {
		        		final ISelection selection = getSelection();
		        		if (!(selection instanceof IStructuredSelection)) return;
		        		final ILogEntry ourSelection = getLogEntry((IStructuredSelection)selection);

    					// Failing that, try the resource originally selected by the user if from the Team menu

    					// TODO: Search all paths from currentSelection and find the shortest path and
    					// get the resources for that instance (in order to get the 'best' "bugtraq" properties) 
    					final ProjectProperties projectProperties = (resource != null) ? ProjectProperties.getProjectProperties(resource) : ProjectProperties.getProjectProperties(ourSelection.getRemoteResource()); // will return null!
    				
    					final ISVNResource svnResource = ourSelection.getRemoteResource() != null ? ourSelection.getRemoteResource() : 
    						ourSelection.getResource();

    					SVNUrl repositoryUrl = null;
    					if (ourSelection.getResource() != null) {
    						repositoryUrl = ourSelection.getResource().getUrl();
    					} else {
    						repositoryUrl = ourSelection.getRemoteResource().getUrl();
    					}
    														
    					SetCommitPropertiesDialog dialog = new SetCommitPropertiesDialog(getViewSite().getShell(), ourSelection.getRevision(), resource, projectProperties);
    					// Set previous text - the text to edit
    					dialog.setOldAuthor(ourSelection.getAuthor());
    					dialog.setOldComment(ourSelection.getComment());

    					boolean doCommit = (dialog.open() == Window.OK);		
    					if (doCommit) {
    						final String author;
    						final String commitComment;
    						if (ourSelection.getAuthor().equals(dialog.getAuthor()))
    							author = null;
    						else
    							author = dialog.getAuthor();
    						if (ourSelection.getComment().equals(dialog.getComment()))
    							commitComment = null;
    						else
    							commitComment = dialog.getComment();
    						
    						final ChangeCommitPropertiesCommand command = new ChangeCommitPropertiesCommand(svnResource.getRepository(), 
                                    ourSelection.getRevision(), commitComment, author);

			        		PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
			        			public void run(IProgressMonitor monitor) throws InvocationTargetException {
		        					try {
										command.run(monitor);
			        					if (ourSelection instanceof LogEntry) {
			        						LogEntry logEntry = (LogEntry)ourSelection;
											logEntry.setComment(commitComment);
                                            logEntry.setAuthor(author);
			        					}
			        					getSite().getShell().getDisplay().asyncExec(new Runnable() {
			        						public void run() {
			        							tableHistoryViewer.refresh();
			        							tableHistoryViewer.setSelection(selection, true);						
			        						}
			        					});
									} catch (SVNException e) {
										throw new InvocationTargetException(e);
									}
			        			}
			        		});
	    				}
		            } catch (InvocationTargetException e) {
		                SVNUIPlugin.openError(getViewSite().getShell(), null, null, e, SVNUIPlugin.LOG_NONTEAM_EXCEPTIONS);
		            } catch (InterruptedException e) {
		                // Do nothing
		            } catch (SVNException e) {
						// TODO Auto-generated catch block
		                SVNUIPlugin.openError(getViewSite().getShell(), null, null, e, SVNUIPlugin.LOG_TEAM_EXCEPTIONS);
					}
		        }
		        
		        // we don't allow multiple selection
		        public boolean isEnabled() {
                    ISelection selection = getSelection();
                    return selection instanceof IStructuredSelection && 
                           ((IStructuredSelection)selection).size() == 1;
		        }
		    };
    	}
		return setCommitPropertiesAction;
	}

    private ILogEntry getLogEntry(IStructuredSelection ss) {
      if(ss.getFirstElement() instanceof LogEntryChangePath) {
        return ((LogEntryChangePath)ss.getFirstElement()).getLogEntry();
      } else {
        return (ILogEntry) ss.getFirstElement();
      }
    }

    private ISelection getSelection() {
    	return selection;
    }

    // open remote file action (double-click)
	private Action getOpenRemoteFileAction() {
        if (openAction == null) {
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
            openChangedPathAction = new Action() {
                public void run() {
                    OpenRemoteFileAction delegate = new OpenRemoteFileAction();
                    delegate.init(this);
                    delegate.selectionChanged(this,changePathsViewer.getSelection());
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
	
	// Get Get All action (toolbar)
	private Action getGetAllAction() {
		if (getAllAction == null) {
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			getAllAction = new Action(Policy.bind("HistoryView.getAll"), plugin.getImageDescriptor(ISVNUIConstants.IMG_GET_ALL)) { //$NON-NLS-1$
				public void run() {
					final ISVNRemoteResource remoteResource = historyTableProvider.getRemoteResource();
					if(fetchAllLogEntriesJob == null) {
						fetchAllLogEntriesJob = new FetchAllLogEntriesJob();
					}
					if(fetchAllLogEntriesJob.getState() != Job.NONE) {
						fetchAllLogEntriesJob.cancel();
						try {
							fetchAllLogEntriesJob.join();
						} catch (InterruptedException e) {
							SVNUIPlugin.log(new SVNException(Policy.bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
						}
					}
					fetchAllLogEntriesJob.setRemoteFile(remoteResource);
					Utils.schedule(fetchAllLogEntriesJob, getViewSite());
				}
			};
			getAllAction.setToolTipText(Policy.bind("HistoryView.getAll")); //$NON-NLS-1$
		}
		return getAllAction;
	}
	
	// Get Get Next action (toolbar)
	public Action getGetNextAction() {
		if (getNextAction == null) {
			IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
			int entriesToFetch = store.getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
			SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
			getNextAction = new Action(Policy.bind("HistoryView.getNext"), plugin.getImageDescriptor(ISVNUIConstants.IMG_GET_NEXT)) { //$NON-NLS-1$
				public void run() {
					final ISVNRemoteResource remoteResource = historyTableProvider.getRemoteResource();
					if(fetchNextLogEntriesJob == null) {
						fetchNextLogEntriesJob = new FetchNextLogEntriesJob();
					}
					if(fetchNextLogEntriesJob.getState() != Job.NONE) {
						fetchNextLogEntriesJob.cancel();
						try {
							fetchNextLogEntriesJob.join();
						} catch (InterruptedException e) {
							SVNUIPlugin.log(new SVNException(Policy.bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
						}
					}
					fetchNextLogEntriesJob.setRemoteFile(remoteResource);
					Utils.schedule(fetchNextLogEntriesJob, getViewSite());
				}
			};
			getNextAction.setToolTipText(Policy.bind("HistoryView.getNext") + " " + entriesToFetch); //$NON-NLS-1$
			if (entriesToFetch <= 0) getNextAction.setEnabled(false);
		}
		return getNextAction;
	}
	
	// get revert changes action (context menu)
	private Action getRevertChangesAction() {
		if (revertChangesAction == null) {
			revertChangesAction = new Action() {
				public void run() {
					ISelection selection = getSelection();
					if (!(selection instanceof IStructuredSelection)) return;
					final IStructuredSelection ss = (IStructuredSelection)selection;
					if (ss.size() == 1) {
						if (!MessageDialog.openConfirm(getSite().getShell(), getText(), Policy.bind("HistoryView.confirmRevertRevision", resource.getFullPath().toString()))) return;
					} else {
						if (!MessageDialog.openConfirm(getSite().getShell(), getText(), Policy.bind("HistoryView.confirmRevertRevisions", resource.getFullPath().toString()))) return;
					}
					BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
						public void run() {
							ILogEntry firstElement = getFirstElement();
							ILogEntry lastElement = getLastElement();
							final SVNUrl path1 = firstElement.getResource().getUrl();
							final SVNRevision revision1 = firstElement.getRevision();
							final SVNUrl path2 = lastElement.getResource().getUrl();
							final SVNRevision revision2 = new SVNRevision.Number(lastElement.getRevision().getNumber() - 1);
							final IResource[] resources = { resource };
							try {
								WorkspaceAction mergeAction = new WorkspaceAction() {
									protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
										new MergeOperation(HistoryView.this, resources, path1, revision1, path2, revision2).run();      
									}									
								};
								mergeAction.run(null);
							} catch (Exception e) {
								MessageDialog.openError(getSite().getShell(), revertChangesAction.getText(), e.getMessage());   								
							}
						}						
					});
				}
			};
		}
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				ILogEntry currentSelection = getLogEntry(ss);      
				revertChangesAction.setText(Policy.bind("HistoryView.revertChangesFromRevision", "" + currentSelection.getRevision().getNumber()));
			}
			if (ss.size() > 1) {
				ILogEntry firstElement = getFirstElement();
				ILogEntry lastElement = getLastElement();
				revertChangesAction.setText(Policy.bind("HistoryView.revertChangesFromRevisions", "" + lastElement.getRevision().getNumber(), "" + firstElement.getRevision().getNumber()));
			}
		}
		revertChangesAction.setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_MERGE));
		return revertChangesAction;
	}
	
	private ILogEntry getFirstElement() {
		ILogEntry firstElement = null;
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Iterator iter = ss.iterator();
			while (iter.hasNext()) {
				ILogEntry element = (ILogEntry)iter.next();
				if (firstElement == null || element.getRevision().getNumber() > firstElement.getRevision().getNumber()) firstElement = element;
			}
		}
		return firstElement;
	}
	
	private ILogEntry getLastElement() {
		ILogEntry lastElement = null;
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Iterator iter = ss.iterator();
			while (iter.hasNext()) {
				ILogEntry element = (ILogEntry)iter.next();
				if (lastElement == null || element.getRevision().getNumber() < lastElement.getRevision().getNumber()) lastElement = element;
			}
		}
		return lastElement;
	}
	
	// get create tag from revision action (context menu)
	private Action getCreateTagFromRevisionAction() {
		if (createTagFromRevisionAction == null) {
			createTagFromRevisionAction = new Action(Policy.bind("HistoryView.createTagFromRevision"), SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_TAG)) { //$NON-NLS-1$
				public void run() {
                    ISelection selection = getSelection();
                    if (!(selection instanceof IStructuredSelection)) return;
                    ILogEntry currentSelection = getLogEntry((IStructuredSelection)selection); 
                    BranchTagDialog dialog;
                    if (resource == null) dialog = new BranchTagDialog(getSite().getShell(), historyTableProvider.getRemoteResource());
                    else dialog = new BranchTagDialog(getSite().getShell(), resource);
                    dialog.setRevisionNumber(currentSelection.getRevision().getNumber());
                    if (dialog.open() == BranchTagDialog.CANCEL) return;
                    final SVNUrl sourceUrl = dialog.getUrl();
                    final SVNUrl destinationUrl = dialog.getToUrl();
                    final String message = dialog.getComment();
                    final SVNRevision revision = dialog.getRevision();
                    boolean createOnServer = dialog.isCreateOnServer();
                    IResource[] resources = { resource };
                    try {
                    	if (resource == null) {
                    		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
								public void run() {
									try {
			                    		ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
			                    		client.copy(sourceUrl, destinationUrl, message, revision);                    											
									} catch (Exception e) {
										MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.createTagFromRevision"), e.getMessage());            
									}
								}                    			
                    		});
                    	}
                    	else new BranchTagOperation(HistoryView.this, resources, sourceUrl, destinationUrl, createOnServer, dialog.getRevision(), message).run();
                    } catch (Exception e) {
                    	MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.createTagFromRevision"), e.getMessage());                   	
                    }
                 }
			};			
		}
		return createTagFromRevisionAction;
	}
	
	// get differences as unified diff action (context menu)
	private Action getShowDifferencesAsUnifiedDiffAction() {
		if (showDifferencesAsUnifiedDiffAction == null) {
			showDifferencesAsUnifiedDiffAction = new Action(Policy.bind("HistoryView.showDifferences"), SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_DIFF)) { //$NON-NLS-1$
				public void run() {
                    ISelection selection = getSelection();
                    if (!(selection instanceof IStructuredSelection)) return;
                    ILogEntry currentSelection = getLogEntry((IStructuredSelection)selection);
					FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
					dialog.setText("Select Unified Diff Output File");
					dialog.setFileName("revision" + currentSelection.getRevision().getNumber() + ".diff"); //$NON-NLS-1$
					String outFile = dialog.open();
					if (outFile != null) {
						final SVNUrl url = currentSelection.getResource().getUrl();
						final SVNRevision oldUrlRevision = new SVNRevision.Number(currentSelection.getRevision().getNumber() - 1);
						final SVNRevision newUrlRevision = currentSelection.getRevision();
						final File file = new File(outFile);
						if (file.exists()) {
							if (!MessageDialog.openQuestion(getSite().getShell(), Policy.bind("HistoryView.showDifferences"), Policy.bind("HistoryView.overwriteOutfile", file.getName()))) return;
						}
						BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
							public void run() {
								try {
									ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
									client.diff(url, oldUrlRevision, newUrlRevision, file, true);
								} catch (Exception e) {
									MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.showDifferences"), e.getMessage());
								}
							}							
						});
					}
				}
			};
		}
		return showDifferencesAsUnifiedDiffAction;
	}
	
	// get contents Action (context menu)
	private Action getGetContentsAction() {
		if (getContentsAction == null) {
			getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
				public void run(IProgressMonitor monitor) throws CoreException {
					ISelection selection = getSelection();
                    if(!(selection instanceof IStructuredSelection)) return;
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    ISVNRemoteFile remoteFile = (ISVNRemoteFile)getLogEntry(ss).getRemoteResource();
					monitor.beginTask(null, 100);
					try {
                        if (remoteFile != null) {
    						if(confirmOverwrite()) {
    							InputStream in = ((IResourceVariant)remoteFile).getStorage(new SubProgressMonitor(monitor,50)).getContents();
    							IFile file = (IFile)resource;
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
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);
		}
		return getContentsAction;
	}

    // update to the selected revision (context menu)	
	private Action getUpdateToRevisionAction() {
		if (updateToRevisionAction == null) {
			updateToRevisionAction = getContextMenuAction(Policy.bind("HistoryView.getRevisionAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
				public void run(IProgressMonitor monitor) throws CoreException {
					ISelection selection = getSelection();
                    if(!(selection instanceof IStructuredSelection)) return;
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    ISVNRemoteFile remoteFile = (ISVNRemoteFile)getLogEntry(ss).getRemoteResource();
					try {
                        if (remoteFile != null) {
    						if(confirmOverwrite()) {
    						    IFile file = (IFile)resource;
                                new ReplaceOperation(HistoryView.this, file, remoteFile.getLastChangedRevision()).run(monitor);
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
			PlatformUI.getWorkbench().getHelpSystem().setHelp(updateToRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);	
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

        // Contribute actions to popup menu for the table
        {
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
        }
        
        final IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
        toggleShowComments = new Action(Policy.bind("HistoryView.showComments")) { //$NON-NLS-1$
            public void run() {
                showComments = isChecked();
                setViewerVisibility();
                store.setValue(ISVNUIConstants.PREF_SHOW_COMMENTS, showComments);
            }
        };
        toggleShowComments.setChecked(showComments);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextAction, IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);    

        // Toggle wrap comments action
        toggleWrapCommentsAction = new Action(Policy.bind("HistoryView.wrapComments")) { //$NON-NLS-1$
          public void run() {
            wrapCommentsText = isChecked();
            setViewerVisibility();
            store.setValue(ISVNUIConstants.PREF_WRAP_COMMENTS, wrapCommentsText);
          }
        };
        toggleWrapCommentsAction.setChecked(wrapCommentsText);
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextWrapAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);   
        
        // Toggle path visible action
        toggleShowAffectedPathsAction = new Action(Policy.bind("HistoryView.showAffectedPaths")) { //$NON-NLS-1$
            public void run() {
                showAffectedPaths = isChecked();
                setViewerVisibility();
                store.setValue(ISVNUIConstants.PREF_SHOW_PATHS, showAffectedPaths);
            }
        };
        toggleShowAffectedPathsAction.setChecked(showAffectedPaths);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleListAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);   

        // Toggle stop on copy action
        toggleStopOnCopyAction = new Action(Policy.bind("HistoryView.stopOnCopy")) { //$NON-NLS-1$
            public void run() {
                setStopOnCopy();
                store.setValue(ISVNUIConstants.PREF_STOP_ON_COPY, toggleStopOnCopyAction.isChecked());
            }
        };
        toggleStopOnCopyAction.setChecked(store.getBoolean(ISVNUIConstants.PREF_STOP_ON_COPY));
        
        IActionBars actionBars = getViewSite().getActionBars();

        // Contribute toggle text visible to the toolbar drop-down
        IMenuManager actionBarsMenu = actionBars.getMenuManager();
        actionBarsMenu.add(toggleWrapCommentsAction);
        actionBarsMenu.add(new Separator());
        actionBarsMenu.add(toggleShowComments);
        actionBarsMenu.add(toggleShowAffectedPathsAction);
        actionBarsMenu.add(toggleStopOnCopyAction);
        actionBarsMenu.add(new Separator());
        
        actionBarsMenu.add(toggleAffectedPathsLayoutActions[0]);
        actionBarsMenu.add(toggleAffectedPathsLayoutActions[1]);
        
        // Create the local tool bar
        IToolBarManager tbm = actionBars.getToolBarManager();
		tbm.add(getRefreshAction());
		tbm.add(getGetNextAction());
		tbm.add(getGetAllAction());
		tbm.add(getLinkWithEditorAction());
		tbm.update(false);
	}
	
	void setStopOnCopy() {
		refresh();
	}
    
    void setViewerVisibility() {
        if (showComments && showAffectedPaths) {
            sashForm.setMaximizedControl(null);
            innerSashForm.setMaximizedControl(null);
        } else if (showComments) {
            sashForm.setMaximizedControl(null);
            innerSashForm.setMaximizedControl(textViewer.getTextWidget());
        } else if (showAffectedPaths) {
            sashForm.setMaximizedControl(null);
            innerSashForm.setMaximizedControl(changePathsViewer.getControl());
        } else {
            sashForm.setMaximizedControl(tableHistoryViewer.getControl());
        }
      
        changePathsViewer.refresh();
        textViewer.getTextWidget().setWordWrap(wrapCommentsText);
    }
    
    public void setCurrentLogEntryChangePath( final LogEntryChangePath[] currentLogEntryChangePath) {
        this.currentLogEntryChangePath = currentLogEntryChangePath;
        if(!shutdown) {
            //Getting the changePaths
            /*
            final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
            */
            getSite().getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if(currentLogEntryChangePath != null && changePathsViewer != null && !changePathsViewer.getControl().isDisposed()) {
                            // once we got the changePath, we refresh the table 
                            changePathsViewer.refresh();
                            //selectRevision(revisionId);
                        }
                    }
                });
        }
    }
    
    public void scheduleFetchChangePathJob(ILogEntry logEntry) {
        if(fetchChangePathJob == null) {
            fetchChangePathJob = new FetchChangePathJob(this);
        }
        if(fetchChangePathJob.getState() != Job.NONE) {
            fetchChangePathJob.cancel();
            try {
                fetchChangePathJob.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                //SVNUIPlugin.log(new SVNException(Policy.bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
            }
        }
        fetchChangePathJob.setLogEntry(logEntry);
        Utils.schedule(fetchChangePathJob, getViewSite());
    }
    
    public boolean isShowChangePaths() {
        return toggleShowAffectedPathsAction.isChecked();
    }
    
	/*
	 * Method declared on IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
	    busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
	    handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		settings = SVNUIPlugin.getPlugin().getPreferenceStore();
		linkingEnabled = settings.getBoolean(ISVNUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING);

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        tableHistoryViewer = createTableHistory(sashForm);
        
        tableHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection = tableHistoryViewer.getSelection();
			}       	
        });
		
        IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
        createAffectedPathsViewer(store.getInt(ISVNUIConstants.PREF_AFFECTED_PATHS_LAYOUT));
        contributeActions();

        sashForm.setWeights(new int[] { 70, 30 });
        
		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm, IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();

		// add listener for editor page activation - this is to support editor linking
		getSite().getPage().addPartListener(partListener);  
	    getSite().getPage().addPartListener(partListener2);
        
        // setViewerVisibility();
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
				lastEntry = null;
				revisionStart = SVNRevision.HEAD;
			}
		});
		
        // set the selectionchanged listener for the table
        // updates the comments and affected paths when selection changes
		tableHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            private ILogEntry currentLogEntry;
            public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
                ILogEntry logEntry = getLogEntry(( IStructuredSelection) selection);
                if(logEntry!=currentLogEntry) {
                  this.currentLogEntry = logEntry;
                  updatePanels(selection);
                }
			}
		});
		
		return tableHistoryViewer;
	}

    /**
     * Create the TextViewer for the logEntry comments 
     */
	protected TextViewer createText(Composite parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				copyAction.update();
			}
		});
        
        Font font = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(ISVNUIConstants.SVN_COMMENT_FONT);
        if (font != null) result.getTextWidget().setFont(font);
        result.getTextWidget().addMouseListener(new MouseAdapter() {
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
        
        result.getTextWidget().addMouseMoveListener(new MouseMoveListener() {
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
        
        // Create actions for the text editor (copy and select all)
        copyAction = new TextViewerAction(result, ITextOperationTarget.COPY);
        copyAction.setText(Policy.bind("HistoryView.copy")); //$NON-NLS-1$
        
        selectAllAction = new TextViewerAction(result, ITextOperationTarget.SELECT_ALL);
        selectAllAction.setText(Policy.bind("HistoryView.selectAll")); //$NON-NLS-1$

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
        actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);
        actionBars.updateActionBars();

        // Contribute actions to popup menu for the comments area
        {
             MenuManager menuMgr = new MenuManager();
             menuMgr.setRemoveAllWhenShown(true);
             menuMgr.addMenuListener(new IMenuListener() {
                     public void menuAboutToShow(IMenuManager menuMgr) {
                             fillTextMenu(menuMgr);
                     }
             });
    
             StyledText text = result.getTextWidget();
             Menu menu = menuMgr.createContextMenu(text);
             text.setMenu(menu);
        }
        
        
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
        Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance(), RemoteResourceTransfer.getInstance()};
		tableHistoryViewer.addDropSupport(ops, transfers, new HistoryDropAdapter(tableHistoryViewer, this));
	}

    /**
     * fill the popup menu for the table
     */
	private void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		// Add the "Add to Workspace" action if 1 revision is selected.
		ISelection sel = tableHistoryViewer.getSelection();
		if (!sel.isEmpty()) {
			if (sel instanceof IStructuredSelection) {
				if (((IStructuredSelection)sel).size() == 1) {
					if (resource != null && resource instanceof IFile) {
						manager.add(getGetContentsAction());
						manager.add(getUpdateToRevisionAction());
					}
					manager.add(getShowDifferencesAsUnifiedDiffAction());
//					if (resource != null) {
						manager.add(getCreateTagFromRevisionAction());
//					}
					manager.add(getSetCommitPropertiesAction());
				}
				if (resource != null) manager.add(getRevertChangesAction());
			}
		}
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(getRefreshAction());
		manager.add(new Separator("additions-end")); //$NON-NLS-1$
	}
    
    private void fillTableChangePathMenu(IMenuManager manager) {
        // file actions go first (view file)
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
		 		 if (input instanceof SyncInfoCompareInput) {
        		 SyncInfoCompareInput syncInput = (SyncInfoCompareInput)input;
            SyncInfo info = syncInput.getSyncInfo();
            if(info instanceof SVNStatusSyncInfo &&info.getLocal().getType() == IResource.FILE) {
                ISVNRemoteFile remote =(ISVNRemoteFile)info.getRemote();
                ISVNRemoteFile base = (ISVNRemoteFile)info.getBase();
                if(remote != null) {
                    showHistory(remote, false);
                } else if(base != null) {
                    showHistory(base, false);
                }
            }
        // Handle editors opened on remote files
        } else if(input instanceof RemoteFileEditorInput) {
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
	 * Shows the history for the given IResource in the view.
	 * 
	 */
	public void showHistory(IResource resource, boolean refetch) {
		remoteResource = null;
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
					historyTableProvider.setResource(resource);
					this.resource = resource;
					tableHistoryViewer.setInput(baseResource);
					setContentDescription(Policy.bind("HistoryView.titleWithArgument", baseResource.getName())); //$NON-NLS-1$
					setTitleToolTip(baseResource.getRepositoryRelativePath());
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
		this.remoteResource = remoteResource;
		this.resource = null;
		if (remoteResource == null) {
			tableHistoryViewer.setInput(null);
			setContentDescription(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
			return;
		}
        projectProperties = ProjectProperties.getProjectProperties(remoteResource);
		historyTableProvider.setRemoteResource(remoteResource);
		historyTableProvider.setResource(null);
		tableHistoryViewer.setInput(remoteResource);
		setContentDescription(Policy.bind("HistoryView.titleWithArgument", remoteResource.getName())); //$NON-NLS-1$
		setTitleToolTip(""); //$NON-NLS-1$
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
		lastEntry = null;
		revisionStart = SVNRevision.HEAD;
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
        		revisionStart = SVNRevision.HEAD;
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
					if (resource == null) {
						if (remoteResource == null || !SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_SHOW_TAGS_IN_REMOTE))
							tagManager = null;
						else
							tagManager = new AliasManager(remoteResource.getUrl());
					}
					else tagManager = new AliasManager(resource);
					SVNRevision pegRevision = remoteResource.getRevision();
					SVNRevision revisionEnd = new SVNRevision.Number(0);
					boolean stopOnCopy = toggleStopOnCopyAction.isChecked();
					IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
					int entriesToFetch = store.getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
					long limit = entriesToFetch;
					entries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd, stopOnCopy, limit + 1, tagManager);
					long entriesLength = entries.length;
					if (entriesLength > limit) {
						ILogEntry[] fetchedEntries = new ILogEntry[entries.length - 1];
						for (int i = 0; i < entries.length - 1; i++)
							fetchedEntries[i] = entries[i];
						entries = fetchedEntries;
						getNextAction.setEnabled(true);
					} else getNextAction.setEnabled(false);
					final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableHistoryViewer != null && ! tableHistoryViewer.getTable().isDisposed()) {
                                // once we got the entries, we refresh the table 
								if (entries.length > 0) {
									lastEntry = entries[entries.length - 1];
									long lastEntryNumber = lastEntry.getRevision().getNumber();
									revisionStart = new SVNRevision.Number(lastEntryNumber - 1);
								}								
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
	
	private class FetchNextLogEntriesJob extends Job {
		public ISVNRemoteResource remoteResource;
		public FetchNextLogEntriesJob() {
			super(Policy.bind("HistoryView.fetchHistoryJob"));  //$NON-NLS-1$;
		}
		public void setRemoteFile(ISVNRemoteResource resource) {
			this.remoteResource = resource;
		}
		public IStatus run(IProgressMonitor monitor) {
			try {
				if(remoteResource != null && !shutdown) {
					SVNRevision pegRevision = remoteResource.getRevision();
					SVNRevision revisionEnd = new SVNRevision.Number(0);
					boolean stopOnCopy = toggleStopOnCopyAction.isChecked();
					IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
					int entriesToFetch = store.getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
					long limit = entriesToFetch;
					ILogEntry[] nextEntries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd, stopOnCopy, limit + 1, tagManager);
					long entriesLength = nextEntries.length;
					if (entriesLength > limit) {
						ILogEntry[] fetchedEntries = new ILogEntry[nextEntries.length - 1];
						for (int i = 0; i < nextEntries.length - 1; i++)
							fetchedEntries[i] = nextEntries[i];
						getNextAction.setEnabled(true);
					} else getNextAction.setEnabled(false);
					ArrayList entryArray = new ArrayList();
					if (entries == null) entries = new ILogEntry[0];
					for (int i = 0; i < entries.length; i++) entryArray.add(entries[i]);
					for (int i = 0; i < nextEntries.length; i++) entryArray.add(nextEntries[i]);
					entries = new ILogEntry[entryArray.size()];
					entryArray.toArray(entries);
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableHistoryViewer != null && ! tableHistoryViewer.getTable().isDisposed()) {
                                // once we got the entries, we refresh the table 
								ISelection selection = tableHistoryViewer.getSelection();
                                tableHistoryViewer.refresh();
                                tableHistoryViewer.setSelection(selection);
								if (entries.length > 0) {
									lastEntry = entries[entries.length - 1];
									long lastEntryNumber = lastEntry.getRevision().getNumber();
									revisionStart = new SVNRevision.Number(lastEntryNumber - 1);
								}
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
	
	private class FetchAllLogEntriesJob extends Job {
		public ISVNRemoteResource remoteResource;
		public FetchAllLogEntriesJob() {
			super(Policy.bind("HistoryView.fetchHistoryJob"));  //$NON-NLS-1$;
		}
		public void setRemoteFile(ISVNRemoteResource resource) {
			this.remoteResource = resource;
		}
		public IStatus run(IProgressMonitor monitor) {
			try {
				if(remoteResource != null && !shutdown) {
					if (resource == null) {
						if (remoteResource == null || !SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_SHOW_TAGS_IN_REMOTE))
							tagManager = null;
						else
							tagManager = new AliasManager(remoteResource.getUrl());
					}
					else tagManager = new AliasManager(resource);
					SVNRevision pegRevision = remoteResource.getRevision();
					revisionStart = SVNRevision.HEAD;
					SVNRevision revisionEnd = new SVNRevision.Number(0);
					boolean stopOnCopy = toggleStopOnCopyAction.isChecked();
					long limit = 0;
					entries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd, stopOnCopy, limit, tagManager);
					final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableHistoryViewer != null && ! tableHistoryViewer.getTable().isDisposed()) {
                                // once we got the entries, we refresh the table 
								if (entries.length > 0) {
									lastEntry = entries[entries.length - 1];
									long lastEntryNumber = lastEntry.getRevision().getNumber();
									revisionStart = new SVNRevision.Number(lastEntryNumber - 1);
								}										
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
	
	static class FetchChangePathJob extends Job {
		public ILogEntry logEntry;
        private final HistoryView historyView;
		
        public FetchChangePathJob(HistoryView historyView) {
			super(Policy.bind("HistoryView.fetchChangePathJob"));  //$NON-NLS-1$;
            this.historyView = historyView;
		}
		
		public void setLogEntry(ILogEntry logEntry) {
			this.logEntry = logEntry;
		}
		
		
		public IStatus run(IProgressMonitor monitor) {
		    if(logEntry.getResource()!=null) {
			    historyView.setCurrentLogEntryChangePath(logEntry.getLogEntryChangePaths());
            }
			return Status.OK_STATUS;	
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

	public static HistoryView getView() {
		return view;
	}
	
	/*
	 * Flatten the text in the multiline comment
	 */
	public static String flattenText(String string) {
		StringBuffer buffer = new StringBuffer(string.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '\r' || c == '\n') {
				if (!skipAdjacentLineSeparator)
					buffer.append('/'); 
				skipAdjacentLineSeparator = true;
			} else {
				buffer.append(c);
				skipAdjacentLineSeparator = false;
			}
		}
		return buffer.toString();
	}

    public void createAffectedPathsViewer(int layout) {
      for( int i = 0; i < toggleAffectedPathsLayoutActions.length; i++) {
        ToggleAffectedPathsLayoutAction action = toggleAffectedPathsLayoutActions[i];
        action.setChecked(layout==action.getLayout());
      }
      
      if(innerSashForm!=null) {
        innerSashForm.dispose();
      }
      if(changePathsViewer!=null) {
        changePathsViewer.getControl().dispose();
      }
      
      innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);

      switch(layout) {
        case ISVNUIConstants.LAYOUT_COMPRESSED:
          changePathsViewer = new ChangePathsTreeViewer(innerSashForm);
          changePathsViewer.setContentProvider(new ChangePathsTreeContentProvider(this));
          break;
        default:
          changePathsViewer = new ChangePathsTableProvider(innerSashForm);
          changePathsViewer.setContentProvider(new ChangePathsTableContentProvider(this));
          break;
      }
      
      changePathsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			selection = changePathsViewer.getSelection();
		}    	  
      });
      
      changePathsViewer.getControl().addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                getOpenChangedPathAction().run();
            }
        });
      
      textViewer = createText(innerSashForm);
      setViewerVisibility();
      innerSashForm.layout();
      sashForm.layout();
      
      updatePanels(tableHistoryViewer.getSelection());
    }

    
    private void updatePanels(ISelection selection) {
      if (selection == null || !(selection instanceof IStructuredSelection)) {
      	textViewer.setDocument(new Document("")); //$NON-NLS-1$
        changePathsViewer.setInput(null);
      	return;
      }
      IStructuredSelection ss = (IStructuredSelection)selection;
      if (ss.size() != 1) {
      	textViewer.setDocument(new Document("")); //$NON-NLS-1$
        changePathsViewer.setInput(null);
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
      changePathsViewer.setInput(entry);
    }

    public static class ToggleAffectedPathsLayoutAction extends Action {
      private final HistoryView view;
      private final int layout;

      public ToggleAffectedPathsLayoutAction( HistoryView view, int layout) {
        super("", AS_RADIO_BUTTON);
        this.view = view;
        this.layout = layout;
        
        String id = null; 
        switch(layout) {
          case ISVNUIConstants.LAYOUT_FLAT:
            setText(Policy.bind("HistoryView.affectedPathsFlatLayout")); //$NON-NLS-1$
            id = ISVNUIConstants.IMG_AFFECTED_PATHS_FLAT_LAYOUT;
            break;
          case ISVNUIConstants.LAYOUT_COMPRESSED:
            setText(Policy.bind("HistoryView.affectedPathsCompressedLayout")); //$NON-NLS-1$
            id = ISVNUIConstants.IMG_AFFECTED_PATHS_COMPRESSED_LAYOUT;
            break;
        }
        setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(id));
      }
      
      public int getLayout() {
        return this.layout;
      }

      public void run() {
        if (isChecked()) {
          SVNUIPlugin.getPlugin().getPreferenceStore().setValue(ISVNUIConstants.PREF_AFFECTED_PATHS_LAYOUT, layout);
          view.createAffectedPathsViewer(layout);
        }
      }
      
    }
    
}