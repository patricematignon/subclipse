/*******************************************************************************
 * Copyright (c) 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.history;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.svn.core.internal.IResourceStateChangeListener;
import org.eclipse.team.svn.core.internal.ISVNLocalFile;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNStatus;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.ChangeCommitPropertiesCommand;
import org.eclipse.team.svn.core.internal.history.AliasManager;
import org.eclipse.team.svn.core.internal.history.ILogEntry;
import org.eclipse.team.svn.core.internal.history.LogEntry;
import org.eclipse.team.svn.core.internal.history.LogEntryChangePath;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.IHelpContextIds;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.actions.OpenRemoteFileAction;
import org.eclipse.team.svn.ui.internal.actions.WorkspaceAction;
import org.eclipse.team.svn.ui.internal.console.TextViewerAction;
import org.eclipse.team.svn.ui.internal.dialogs.BranchTagDialog;
import org.eclipse.team.svn.ui.internal.dialogs.SetCommitPropertiesDialog;
import org.eclipse.team.svn.ui.internal.internal.Utils;
import org.eclipse.team.svn.ui.internal.operations.BranchTagOperation;
import org.eclipse.team.svn.ui.internal.operations.MergeOperation;
import org.eclipse.team.svn.ui.internal.operations.ReplaceOperation;
import org.eclipse.team.svn.ui.internal.settings.ProjectProperties;
import org.eclipse.team.svn.ui.internal.util.LinkList;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * <code>IHistoryPage</code> for generic history view 
 * 
 * @author Eugene Kuleshov (migration from legacy history view)
 */
public class SVNHistoryPage extends HistoryPage implements IResourceStateChangeListener {

  private SashForm svnHistoryPageControl;
  private SashForm innerSashForm;

  HistoryTableProvider historyTableProvider;
  TableViewer tableHistoryViewer;
  StructuredViewer changePathsViewer;
  TextViewer textViewer;

  private boolean showComments;
  private boolean showAffectedPaths;
  private boolean wrapCommentsText;
  boolean shutdown = false;

  private ProjectProperties projectProperties;

  // cached for efficiency
  ILogEntry[] entries;
  LogEntryChangePath[] currentLogEntryChangePath;
  ILogEntry lastEntry;
  SVNRevision revisionStart = SVNRevision.HEAD;

  AbstractFetchJob fetchLogEntriesJob = null;
  AbstractFetchJob fetchAllLogEntriesJob = null;
  AbstractFetchJob fetchNextLogEntriesJob = null;
  FetchChangePathJob fetchChangePathJob = null;
  AliasManager tagManager;

  IResource resource;
  ISVNRemoteResource remoteResource;
  ISelection selection;

  private IAction getNextAction;
  private IAction getAllAction;
  private IAction toggleStopOnCopyAction;
  private IAction toggleShowComments;
  private IAction toggleWrapCommentsAction;
  private IAction toggleShowAffectedPathsAction;

  private IAction openAction;
  private IAction getContentsAction;
  private IAction updateToRevisionAction;
  private IAction openChangedPathAction;
  private IAction showDifferencesAsUnifiedDiffAction;
  private IAction createTagFromRevisionAction;
  private IAction setCommitPropertiesAction;
  private IAction revertChangesAction;
  private IAction refreshAction;

  private ToggleAffectedPathsLayoutAction[] toggleAffectedPathsLayoutActions;

  private TextViewerAction copyAction;
  private TextViewerAction selectAllAction;

  private LinkList linkList;
  private boolean mouseDown = false; 
  private boolean dragEvent = false;
  private Cursor handCursor;
  private Cursor busyCursor;

  
  public SVNHistoryPage(Object object) {
	  SVNProviderPlugin.addResourceStateChangeListener(this);
  }

  public void dispose() {
    super.dispose();

    SVNProviderPlugin.removeResourceStateChangeListener(this);

    if(busyCursor!=null) {
      busyCursor.dispose();
    }
    if(handCursor!=null) {
      handCursor.dispose();
    }
  }
  
  public Control getControl() {
    return svnHistoryPageControl;
  }

  public void setFocus() {
    // TODO Auto-generated method stub

  }

  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    return remoteResource == null ? null : remoteResource.getRepositoryRelativePath() + " in "
        + remoteResource.getRepository();
  }

  public boolean isValidInput(Object object) {
    if(object instanceof IResource) {
      RepositoryProvider provider = RepositoryProvider.getProvider(((IResource) object).getProject());
      return provider instanceof SVNTeamProvider;
    } else if(object instanceof ISVNRemoteResource) {
      return true;
    }

    // TODO
    // } else if(object instanceof CVSFileRevision) {
    // return true;
    // } else if(object instanceof CVSLocalFileRevision) {
    // return true;

    return false;
  }

  public void refresh() {
    entries = null;
    lastEntry = null;
    revisionStart = SVNRevision.HEAD;
    // show a Busy Cursor during refresh
    BusyIndicator.showWhile(tableHistoryViewer.getTable().getDisplay(), new Runnable() {
      public void run() {
        if(resource != null) {
          try {
        	  remoteResource = SVNWorkspaceRoot.getBaseResourceFor(resource);
        	  historyTableProvider.setRemoteResource(remoteResource);
              projectProperties = ProjectProperties.getProjectProperties(resource);
          } catch(SVNException e) {
          }
        }
        tableHistoryViewer.refresh();
      }
    });
  }

  public Object getAdapter(Class adapter) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean inputSet() {
    Object input = getInput();

    if(input instanceof IResource) {
      IResource res = (IResource) input;
      RepositoryProvider teamProvider = RepositoryProvider.getProvider(res.getProject(), SVNProviderPlugin.getTypeId());
      if(teamProvider != null) {
        try {
          ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(res);
          if(localResource != null && !localResource.getStatus().isAdded() && localResource.getStatus().isManaged()) {
            this.resource = res;
            this.remoteResource = localResource.getBaseResource();

            this.projectProperties = ProjectProperties.getProjectProperties(res);
            this.historyTableProvider.setRemoteResource(this.remoteResource);
            this.tableHistoryViewer.setInput(this.remoteResource);
            // setContentDescription(Policy.bind("HistoryView.titleWithArgument",
            // baseResource.getName())); //$NON-NLS-1$
            // setTitleToolTip(baseResource.getRepositoryRelativePath());
            return true;
          }
        } catch(TeamException e) {
          SVNUIPlugin.openError(getSite().getShell(), null, null, e);
        }
      }

    } else if(input instanceof ISVNRemoteResource) {
      this.resource = null;
      this.remoteResource = (ISVNRemoteResource) input;

      this.projectProperties = ProjectProperties.getProjectProperties(this.remoteResource);
      this.historyTableProvider.setRemoteResource(this.remoteResource);
      this.tableHistoryViewer.setInput(this.remoteResource);
      // setContentDescription(Policy.bind("HistoryView.titleWithArgument",
      // remoteResource.getName())); //$NON-NLS-1$
      // setTitleToolTip(remoteResource.getRepositoryRelativePath());
      return true;

    }

    return false;
  }

  public void createControl(Composite parent) {
    this.busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
    this.handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);

    IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
    this.showComments = store.getBoolean(ISVNUIConstants.PREF_SHOW_COMMENTS);
    this.wrapCommentsText = store.getBoolean(ISVNUIConstants.PREF_WRAP_COMMENTS);
    this.showAffectedPaths = store.getBoolean(ISVNUIConstants.PREF_SHOW_PATHS);

    this.svnHistoryPageControl = new SashForm(parent, SWT.VERTICAL);
    this.svnHistoryPageControl.setLayoutData(new GridData(GridData.FILL_BOTH));

    this.toggleAffectedPathsLayoutActions = new ToggleAffectedPathsLayoutAction[] {
        new ToggleAffectedPathsLayoutAction(this, ISVNUIConstants.LAYOUT_FLAT),
        new ToggleAffectedPathsLayoutAction(this, ISVNUIConstants.LAYOUT_COMPRESSED),
      };
    
    createTableHistory(svnHistoryPageControl);
    createAffectedPathsViewer(store.getInt(ISVNUIConstants.PREF_AFFECTED_PATHS_LAYOUT));
    contributeActions();

    svnHistoryPageControl.setWeights(new int[] { 70, 30});

    // set F1 help
    // PlatformUI.getWorkbench().getHelpSystem().setHelp(svnHistoryPageControl,
    // IHelpContextIds.RESOURCE_HISTORY_VIEW);
    // initDragAndDrop();

    // add listener for editor page activation - this is to support editor
    // linking
    // getSite().getPage().addPartListener(partListener);
    // getSite().getPage().addPartListener(partListener2);
  }

  protected void createTableHistory(Composite parent) {
    this.historyTableProvider = new HistoryTableProvider();
    this.tableHistoryViewer = historyTableProvider.createTable(parent);

    // set the content provider for the table
    this.tableHistoryViewer.setContentProvider(new IStructuredContentProvider() {

      public Object[] getElements(Object inputElement) {
        // Short-circuit to optimize
        if(entries != null)
          return entries;

        if( !(inputElement instanceof ISVNRemoteResource))
          return null;
        final ISVNRemoteResource remoteResource = (ISVNRemoteResource) inputElement;

        IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
        int entriesToFetch = store.getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
        if (entriesToFetch > 0)
        	fetchLogEntriesJob = new FetchLogEntriesJob();
        else
        	fetchLogEntriesJob = new FetchAllLogEntriesJob();
        if(fetchLogEntriesJob.getState() != Job.NONE) {
          fetchLogEntriesJob.cancel();
          try {
            fetchLogEntriesJob.join();
          } catch(InterruptedException e) {
            SVNUIPlugin.log(new SVNException(
                Policy.bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
          }
        }
        fetchLogEntriesJob.setRemoteFile(remoteResource);
        Utils.schedule(fetchLogEntriesJob, SVNUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow()
            .getActivePage().getActivePart().getSite());

        return new Object[ 0];
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
    this.tableHistoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      private ILogEntry currentLogEntry;

      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        ILogEntry logEntry = getLogEntry((IStructuredSelection) selection);
        if(logEntry != currentLogEntry) {
          this.currentLogEntry = logEntry;
          updatePanels(selection);
        }

        SVNHistoryPage.this.selection = selection;
      }
    });

    // Double click open action
    this.tableHistoryViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
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
      getHistoryPageSite().getPart().getSite().registerContextMenu(menuMgr, tableHistoryViewer);
    }
  }

  private void fillTableMenu(IMenuManager manager) {
    // file actions go first (view file)
    manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
    // Add the "Add to Workspace" action if 1 revision is selected.
    ISelection sel = tableHistoryViewer.getSelection();
    if( !sel.isEmpty()) {
      if(sel instanceof IStructuredSelection) {
        if(((IStructuredSelection) sel).size() == 1) {
          if(resource != null && resource instanceof IFile) {
            manager.add(getGetContentsAction());
            manager.add(getUpdateToRevisionAction());
          }
          manager.add(getShowDifferencesAsUnifiedDiffAction());
          // if (resource != null) {
          manager.add(getCreateTagFromRevisionAction());
          // }
          manager.add(getSetCommitPropertiesAction());
        }
        if(resource != null)
          manager.add(getRevertChangesAction());
      }
    }
    manager.add(new Separator("additions")); //$NON-NLS-1$
    manager.add(getRefreshAction());
    manager.add(new Separator("additions-end")); //$NON-NLS-1$
  }

  public void createAffectedPathsViewer(int layout) {
    for(int i = 0; i < toggleAffectedPathsLayoutActions.length; i++) {
      ToggleAffectedPathsLayoutAction action = toggleAffectedPathsLayoutActions[ i];
      action.setChecked(layout == action.getLayout());
    }

    if(innerSashForm != null) {
      innerSashForm.dispose();
    }
    if(changePathsViewer != null) {
      changePathsViewer.getControl().dispose();
    }
    
    innerSashForm = new SashForm(svnHistoryPageControl, SWT.HORIZONTAL);

    switch(layout) {
      case ISVNUIConstants.LAYOUT_COMPRESSED:
        changePathsViewer = new ChangePathsTreeViewer(innerSashForm, this);
        break;
      default:
        changePathsViewer = new ChangePathsTableProvider(innerSashForm, this);
        break;
    }
    
    changePathsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        SVNHistoryPage.this.selection = changePathsViewer.getSelection();
      }
    });

    changePathsViewer.getControl().addListener(SWT.DefaultSelection, new Listener() {
      public void handleEvent(Event e) {
        getOpenChangedPathAction().run();
      }
    });

    createText(innerSashForm);
    setViewerVisibility();
    innerSashForm.layout();
    svnHistoryPageControl.layout();

    updatePanels(tableHistoryViewer.getSelection());
  }

  /**
   * Create the TextViewer for the logEntry comments
   */
  protected void createText(Composite parent) {
    this.textViewer = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
    this.textViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        copyAction.update();
      }
    });

    Font font = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(
        ISVNUIConstants.SVN_COMMENT_FONT);
    if(font != null) {
      this.textViewer.getTextWidget().setFont(font);
    }
    this.textViewer.getTextWidget().addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        if(e.button != 1) {
          return;
        }
        mouseDown = true;
      }

      public void mouseUp(MouseEvent e) {
        mouseDown = false;
        StyledText text = (StyledText) e.widget;
        int offset = text.getCaretOffset();
        if(dragEvent) {
          // don't activate a link during a drag/mouse up operation
          dragEvent = false;
          if(linkList != null && linkList.isLinkAt(offset)) {
            text.setCursor(handCursor);
          }
        } else {
          if(linkList != null && linkList.isLinkAt(offset)) {
            text.setCursor(busyCursor);
			try {
				URL url = new URL(linkList.getLinkAt(offset));
				PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Subclipse").openURL(url);
			} catch (Exception e1) {
				Program.launch(linkList.getLinkAt(offset));
			}
//            Program.launch(linkList.getLinkAt(offset));
            text.setCursor(null);
          }
        }
      }
    });

    this.textViewer.getTextWidget().addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        // Do not change cursor on drag events
        if(mouseDown) {
          if( !dragEvent) {
            StyledText text = (StyledText) e.widget;
            text.setCursor(null);
          }
          dragEvent = true;
          return;
        }
        StyledText text = (StyledText) e.widget;
        int offset = -1;
        try {
          offset = text.getOffsetAtLocation(new Point(e.x, e.y));
        } catch(IllegalArgumentException ex) {
        }
        if(offset == -1) {
          text.setCursor(null);
        } else if(linkList != null && linkList.isLinkAt(offset)) {
          text.setCursor(handCursor);
        } else {
          text.setCursor(null);
        }
      }
    });

    // Create actions for the text editor (copy and select all)
    copyAction = new TextViewerAction(this.textViewer, ITextOperationTarget.COPY);
    copyAction.setText(Policy.bind("HistoryView.copy")); //$NON-NLS-1$

    selectAllAction = new TextViewerAction(this.textViewer, ITextOperationTarget.SELECT_ALL);
    selectAllAction.setText(Policy.bind("HistoryView.selectAll")); //$NON-NLS-1$

    IHistoryPageSite parentSite = getHistoryPageSite();
    IPageSite pageSite = parentSite.getWorkbenchPageSite();
    IActionBars actionBars = pageSite.getActionBars();

    actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
    actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);
    actionBars.updateActionBars();

    // Contribute actions to popup menu for the comments area
    {
      MenuManager menuMgr = new MenuManager();
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener() {
        public void menuAboutToShow(IMenuManager menuMgr) {
          menuMgr.add(copyAction);
          menuMgr.add(selectAllAction);
        }
      });

      StyledText text = this.textViewer.getTextWidget();
      Menu menu = menuMgr.createContextMenu(text);
      text.setMenu(menu);
    }
  }

  private void contributeActions() {
    SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
    final IPreferenceStore store = plugin.getPreferenceStore();

    toggleShowComments = new Action(Policy.bind("HistoryView.showComments")) { //$NON-NLS-1$
      public void run() {
        showComments = isChecked();
        setViewerVisibility();
        store.setValue(ISVNUIConstants.PREF_SHOW_COMMENTS, showComments);
      }
    };
    toggleShowComments.setChecked(showComments);
    // PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextAction,
    // IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);

    // Toggle wrap comments action
    toggleWrapCommentsAction = new Action(Policy.bind("HistoryView.wrapComments")) { //$NON-NLS-1$
      public void run() {
        wrapCommentsText = isChecked();
        setViewerVisibility();
        store.setValue(ISVNUIConstants.PREF_WRAP_COMMENTS, wrapCommentsText);
      }
    };
    toggleWrapCommentsAction.setChecked(wrapCommentsText);
    // PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextWrapAction,
    // IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);

    // Toggle path visible action
    toggleShowAffectedPathsAction = new Action(Policy.bind("HistoryView.showAffectedPaths")) { //$NON-NLS-1$
      public void run() {
        showAffectedPaths = isChecked();
        setViewerVisibility();
        store.setValue(ISVNUIConstants.PREF_SHOW_PATHS, showAffectedPaths);
      }
    };
    toggleShowAffectedPathsAction.setChecked(showAffectedPaths);
    // PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleListAction,
    // IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);

    // Toggle stop on copy action
    toggleStopOnCopyAction = new Action(Policy.bind("HistoryView.stopOnCopy")) { //$NON-NLS-1$
      public void run() {
        refresh();
        SVNUIPlugin.getPlugin().getPreferenceStore().setValue(ISVNUIConstants.PREF_STOP_ON_COPY,
            toggleStopOnCopyAction.isChecked());
      }
    };
    toggleStopOnCopyAction.setChecked(store.getBoolean(ISVNUIConstants.PREF_STOP_ON_COPY));
    
    IHistoryPageSite parentSite = getHistoryPageSite();
    IPageSite pageSite = parentSite.getWorkbenchPageSite();
    IActionBars actionBars = pageSite.getActionBars();

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
    // tbm.add(getRefreshAction());
    tbm.add(new Separator());
    tbm.add(getGetNextAction());
    tbm.add(getGetAllAction());
    // tbm.add(getLinkWithEditorAction());
    tbm.update(false);
    
    actionBars.updateActionBars();  
  }

  ILogEntry getLogEntry(IStructuredSelection ss) {
    if(ss.getFirstElement() instanceof LogEntryChangePath) {
      return ((LogEntryChangePath) ss.getFirstElement()).getLogEntry();
    }
    return (ILogEntry) ss.getFirstElement();
  }

  void updatePanels(ISelection selection) {
    if(selection == null || !(selection instanceof IStructuredSelection)) {
      textViewer.setDocument(new Document("")); //$NON-NLS-1$
      changePathsViewer.setInput(null);
      return;
    }
    IStructuredSelection ss = (IStructuredSelection) selection;
    if(ss.size() != 1) {
      textViewer.setDocument(new Document("")); //$NON-NLS-1$
      changePathsViewer.setInput(null);
      return;
    }
    LogEntry entry = (LogEntry) ss.getFirstElement();
    textViewer.setDocument(new Document(entry.getComment()));
    StyledText text = textViewer.getTextWidget();
    if(projectProperties == null) {
      linkList = ProjectProperties.getUrls(entry.getComment());
    } else {
      linkList = projectProperties.getLinkList(entry.getComment());
    }
    if(linkList != null) {
      int[][] linkRanges = linkList.getLinkRanges();
      // String[] urls = linkList.getUrls();
      for(int i = 0; i < linkRanges.length; i++) {
        text.setStyleRange(new StyleRange(linkRanges[ i][ 0], linkRanges[ i][ 1], 
            JFaceColors.getHyperlinkText(Display.getCurrent()), null));
      }
    }
    changePathsViewer.setInput(entry);
  }

  void setViewerVisibility() {
    if(showComments && showAffectedPaths) {
      svnHistoryPageControl.setMaximizedControl(null);
      innerSashForm.setMaximizedControl(null);
    } else if(showComments) {
      svnHistoryPageControl.setMaximizedControl(null);
      innerSashForm.setMaximizedControl(textViewer.getTextWidget());
    } else if(showAffectedPaths) {
      svnHistoryPageControl.setMaximizedControl(null);
      innerSashForm.setMaximizedControl(changePathsViewer.getControl());
    } else {
      svnHistoryPageControl.setMaximizedControl(tableHistoryViewer.getControl());
    }

    changePathsViewer.refresh();
    textViewer.getTextWidget().setWordWrap(wrapCommentsText);
  }

  void setCurrentLogEntryChangePath(final LogEntryChangePath[] currentLogEntryChangePath) {
    this.currentLogEntryChangePath = currentLogEntryChangePath;
    if( !shutdown) {
      // Getting the changePaths
      /*
       * final SVNRevision.Number revisionId =
       * remoteResource.getLastChangedRevision();
       */
      getSite().getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          if(currentLogEntryChangePath != null && changePathsViewer != null
              && !changePathsViewer.getControl().isDisposed()) {
            // once we got the changePath, we refresh the table
            changePathsViewer.refresh();
            // selectRevision(revisionId);
          }
        }
      });
    }
  }

  /**
   * Select the revision in the receiver.
   */
  public void selectRevision(SVNRevision.Number revision) {
    if(entries == null) {
      return;
    }

    ILogEntry entry = null;
    for(int i = 0; i < entries.length; i++) {
      if(entries[ i].getRevision().equals(revision)) {
        entry = entries[ i];
        break;
      }
    }

    if(entry != null) {
      IStructuredSelection selection = new StructuredSelection(entry);
      tableHistoryViewer.setSelection(selection, true);
    }
  }

  public void scheduleFetchChangePathJob(ILogEntry logEntry) {
    if(fetchChangePathJob == null) {
      fetchChangePathJob = new FetchChangePathJob();
    }
    if(fetchChangePathJob.getState() != Job.NONE) {
      fetchChangePathJob.cancel();
      try {
        fetchChangePathJob.join();
      } catch(InterruptedException e) {
        e.printStackTrace();
        // SVNUIPlugin.log(new
        // SVNException(Policy.bind("HistoryView.errorFetchingEntries",
        // remoteResource.getName()), e)); //$NON-NLS-1$
      }
    }
    fetchChangePathJob.setLogEntry(logEntry);
    Utils.schedule(fetchChangePathJob, getSite());
  }

  public boolean isShowChangePaths() {
    // return toggleShowAffectedPathsAction.isChecked();
    return true;
  }

  private IAction getOpenRemoteFileAction() {
    if(openAction == null) {
      openAction = new Action() {
        public void run() {
          OpenRemoteFileAction delegate = new OpenRemoteFileAction();
          delegate.init(this);
          delegate.selectionChanged(this, tableHistoryViewer.getSelection());
          if(isEnabled()) {
            try {
              // disableEditorActivation = true;
              delegate.run(this);
            } finally {
              // disableEditorActivation = false;
            }
          }
        }
      };
    }
    return openAction;
  }

  // open changed Path (double-click)
  private IAction getOpenChangedPathAction() {
    if(openChangedPathAction == null) {
      openChangedPathAction = new Action() {
        public void run() {
          OpenRemoteFileAction delegate = new OpenRemoteFileAction();
          delegate.init(this);
          delegate.selectionChanged(this, changePathsViewer.getSelection());
          if(isEnabled()) {
            try {
              // disableEditorActivation = true;
              delegate.run(this);
            } finally {
              // disableEditorActivation = false;
            }
          }
        }
      };
    }
    return openChangedPathAction;

  }

  // get contents Action (context menu)
  private IAction getGetContentsAction() {
    if(getContentsAction == null) {
      getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
            public void run(IProgressMonitor monitor) throws CoreException {
              ISelection selection = getSelection();
              if( !(selection instanceof IStructuredSelection))
                return;
              IStructuredSelection ss = (IStructuredSelection) selection;
              ISVNRemoteFile remoteFile = (ISVNRemoteFile) getLogEntry(ss).getRemoteResource();
              monitor.beginTask(null, 100);
              try {
                if(remoteFile != null) {
                  if(confirmOverwrite()) {
                    InputStream in = ((IResourceVariant) remoteFile).getStorage(new SubProgressMonitor(monitor, 50))
                        .getContents();
                    IFile file = (IFile) resource;
                    file.setContents(in, false, true, new SubProgressMonitor(monitor, 50));
                  }
                }
              } catch(TeamException e) {
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

  // get differences as unified diff action (context menu)
  private IAction getShowDifferencesAsUnifiedDiffAction() {
    if(showDifferencesAsUnifiedDiffAction == null) {
      showDifferencesAsUnifiedDiffAction = new Action(
          Policy.bind("HistoryView.showDifferences")) { //$NON-NLS-1$
        public void run() {
          ISelection selection = getSelection();
          if( !(selection instanceof IStructuredSelection))
            return;
          ILogEntry currentSelection = getLogEntry((IStructuredSelection) selection);
          FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
          dialog.setText("Select Unified Diff Output File");
          dialog.setFileName("revision" + currentSelection.getRevision().getNumber() + ".diff"); //$NON-NLS-1$
          String outFile = dialog.open();
          if(outFile != null) {
            final SVNUrl url = currentSelection.getResource().getUrl();
            final SVNRevision oldUrlRevision = new SVNRevision.Number(currentSelection.getRevision().getNumber() - 1);
            final SVNRevision newUrlRevision = currentSelection.getRevision();
            final File file = new File(outFile);
            if(file.exists()) {
              if( !MessageDialog.openQuestion(getSite().getShell(), Policy.bind("HistoryView.showDifferences"), Policy
                  .bind("HistoryView.overwriteOutfile", file.getName())))
                return;
            }
            BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
              public void run() {
                try {
                  ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
                  client.diff(url, oldUrlRevision, newUrlRevision, file, true);
                } catch(Exception e) {
                  MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.showDifferences"), e
                      .getMessage());
                }
              }
            });
          }
        }
      };
    }
    return showDifferencesAsUnifiedDiffAction;
  }

  // update to the selected revision (context menu)
  private IAction getUpdateToRevisionAction() {
    if(updateToRevisionAction == null) {
      updateToRevisionAction = getContextMenuAction(
          Policy.bind("HistoryView.getRevisionAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
            public void run(IProgressMonitor monitor) throws CoreException {
              ISelection selection = getSelection();
              if( !(selection instanceof IStructuredSelection))
                return;
              IStructuredSelection ss = (IStructuredSelection) selection;
              ISVNRemoteFile remoteFile = (ISVNRemoteFile) getLogEntry(ss).getRemoteResource();
              try {
                if(remoteFile != null) {
                  if(confirmOverwrite()) {
                    IFile file = (IFile) resource;
                    new ReplaceOperation(getSite().getPage().getActivePart(), file, remoteFile.getLastChangedRevision())
                        .run(monitor);
                    historyTableProvider.setRemoteResource(remoteFile);
                    Display.getDefault().asyncExec(new Runnable() {
                      public void run() {
                        tableHistoryViewer.refresh();
                      }
                    });
                  }
                }
              } catch(InvocationTargetException e) {
                throw new CoreException(new SVNStatus(IStatus.ERROR, 0, e.getMessage()));
              } catch(InterruptedException e) {
                // Cancelled by user
              }
            }
          });
      PlatformUI.getWorkbench().getHelpSystem().setHelp(updateToRevisionAction,
          IHelpContextIds.GET_FILE_REVISION_ACTION);
    }
    return updateToRevisionAction;
  }

  // get create tag from revision action (context menu)
  private IAction getCreateTagFromRevisionAction() {
    if(createTagFromRevisionAction == null) {
      createTagFromRevisionAction = new Action(
          Policy.bind("HistoryView.createTagFromRevision")) { //$NON-NLS-1$
        public void run() {
          ISelection selection = getSelection();
          if( !(selection instanceof IStructuredSelection))
            return;
          ILogEntry currentSelection = getLogEntry((IStructuredSelection) selection);
          BranchTagDialog dialog;
          if(resource == null)
            dialog = new BranchTagDialog(getSite().getShell(), historyTableProvider.getRemoteResource());
          else
            dialog = new BranchTagDialog(getSite().getShell(), resource);
          dialog.setRevisionNumber(currentSelection.getRevision().getNumber());
          if(dialog.open() == BranchTagDialog.CANCEL)
            return;
          final SVNUrl sourceUrl = dialog.getUrl();
          final SVNUrl destinationUrl = dialog.getToUrl();
          final String message = dialog.getComment();
          final SVNRevision revision = dialog.getRevision();
          boolean createOnServer = dialog.isCreateOnServer();
          IResource[] resources = { resource};
          try {
            if(resource == null) {
              BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                public void run() {
                  try {
                    ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
                    client.copy(sourceUrl, destinationUrl, message, revision);
                  } catch(Exception e) {
                    MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.createTagFromRevision"), e
                        .getMessage());
                  }
                }
              });
            } else {
              new BranchTagOperation(getSite().getPage().getActivePart(), resources, sourceUrl, destinationUrl,
                  createOnServer, dialog.getRevision(), message).run();
            }
          } catch(Exception e) {
            MessageDialog.openError(getSite().getShell(), Policy.bind("HistoryView.createTagFromRevision"), e
                .getMessage());
          }
        }
      };
    }
    return createTagFromRevisionAction;
  }

  private IAction getSetCommitPropertiesAction() {
    // set Action (context menu)
    if(setCommitPropertiesAction == null) {
      setCommitPropertiesAction = new Action(Policy.bind("HistoryView.setCommitProperties")) {
        public void run() {
          try {
            final ISelection selection = getSelection();
            if( !(selection instanceof IStructuredSelection))
              return;
            final ILogEntry ourSelection = getLogEntry((IStructuredSelection) selection);

            // Failing that, try the resource originally selected by the user if
            // from the Team menu

            // TODO: Search all paths from currentSelection and find the
            // shortest path and
            // get the resources for that instance (in order to get the 'best'
            // "bugtraq" properties)
            final ProjectProperties projectProperties = (resource != null) ? ProjectProperties
                .getProjectProperties(resource) : ProjectProperties.getProjectProperties(ourSelection
                .getRemoteResource()); // will return null!

            final ISVNResource svnResource = ourSelection.getRemoteResource() != null ? ourSelection
                .getRemoteResource() : ourSelection.getResource();

            SetCommitPropertiesDialog dialog = new SetCommitPropertiesDialog(getSite().getShell(), ourSelection
                .getRevision(), resource, projectProperties);
            // Set previous text - the text to edit
            dialog.setOldAuthor(ourSelection.getAuthor());
            dialog.setOldComment(ourSelection.getComment());

            boolean doCommit = (dialog.open() == Window.OK);
            if(doCommit) {
              final String author;
              final String commitComment;
              if(ourSelection.getAuthor().equals(dialog.getAuthor()))
                author = null;
              else
                author = dialog.getAuthor();
              if(ourSelection.getComment().equals(dialog.getComment()))
                commitComment = null;
              else
                commitComment = dialog.getComment();

              final ChangeCommitPropertiesCommand command = new ChangeCommitPropertiesCommand(svnResource
                  .getRepository(), ourSelection.getRevision(), commitComment, author);

              PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                  try {
                    command.run(monitor);
                    if(ourSelection instanceof LogEntry) {
                      LogEntry logEntry = (LogEntry) ourSelection;
                      logEntry.setComment(commitComment);
                      logEntry.setAuthor(author);
                    }
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {
                      public void run() {
                        tableHistoryViewer.refresh();
                        tableHistoryViewer.setSelection(selection, true);
                      }
                    });
                  } catch(SVNException e) {
                    throw new InvocationTargetException(e);
                  }
                }
              });
            }
          } catch(InvocationTargetException e) {
            SVNUIPlugin.openError(getSite().getShell(), null, null, e, SVNUIPlugin.LOG_NONTEAM_EXCEPTIONS);
          } catch(InterruptedException e) {
            // Do nothing
          } catch(SVNException e) {
            // TODO Auto-generated catch block
            SVNUIPlugin.openError(getSite().getShell(), null, null, e, SVNUIPlugin.LOG_TEAM_EXCEPTIONS);
          }
        }

        // we don't allow multiple selection
        public boolean isEnabled() {
          ISelection selection = getSelection();
          return selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1;
        }
      };
    }
    return setCommitPropertiesAction;
  }

  // get revert changes action (context menu)
  private IAction getRevertChangesAction() {
    if(revertChangesAction == null) {
      revertChangesAction = new Action() {
        public void run() {
          ISelection selection = getSelection();
          if( !(selection instanceof IStructuredSelection))
            return;
          final IStructuredSelection ss = (IStructuredSelection) selection;
          if(ss.size() == 1) {
            if( !MessageDialog.openConfirm(getSite().getShell(), getText(), Policy.bind(
                "HistoryView.confirmRevertRevision", resource.getFullPath().toString())))
              return;
          } else {
            if( !MessageDialog.openConfirm(getSite().getShell(), getText(), Policy.bind(
                "HistoryView.confirmRevertRevisions", resource.getFullPath().toString())))
              return;
          }
          BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
              ILogEntry firstElement = getFirstElement();
              ILogEntry lastElement = getLastElement();
              final SVNUrl path1 = firstElement.getResource().getUrl();
              final SVNRevision revision1 = firstElement.getRevision();
              final SVNUrl path2 = lastElement.getResource().getUrl();
              final SVNRevision revision2 = new SVNRevision.Number(lastElement.getRevision().getNumber() - 1);
              final IResource[] resources = { resource};
              try {
                WorkspaceAction mergeAction = new WorkspaceAction() {
                  protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
                    new MergeOperation(getSite().getPage().getActivePart(), resources, path1, revision1, path2,
                        revision2).run();
                  }
                };
                mergeAction.run(null);
              } catch(Exception e) {
                MessageDialog.openError(getSite().getShell(), revertChangesAction.getText(), e.getMessage());
              }
            }
          });
        }
      };
    }
    ISelection selection = getSelection();
    if(selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;
      if(ss.size() == 1) {
        ILogEntry currentSelection = getLogEntry(ss);
        revertChangesAction.setText(Policy.bind("HistoryView.revertChangesFromRevision", ""
            + currentSelection.getRevision().getNumber()));
      }
      if(ss.size() > 1) {
        ILogEntry firstElement = getFirstElement();
        ILogEntry lastElement = getLastElement();
        revertChangesAction.setText(Policy.bind("HistoryView.revertChangesFromRevisions", ""
            + lastElement.getRevision().getNumber(), "" + firstElement.getRevision().getNumber()));
      }
    }
    return revertChangesAction;
  }

  // Refresh action (toolbar)
  private IAction getRefreshAction() {
    if(refreshAction == null) {
      SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
      refreshAction = new Action(
          Policy.bind("HistoryView.refreshLabel"), plugin.getImageDescriptor(ISVNUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
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

    // Get Get All action (toolbar)
  private IAction getGetAllAction() {
    if(getAllAction == null) {
      SVNUIPlugin plugin = SVNUIPlugin.getPlugin();
      getAllAction = new Action(
          Policy.bind("HistoryView.getAll"), plugin.getImageDescriptor(ISVNUIConstants.IMG_GET_ALL)) { //$NON-NLS-1$
        public void run() {
          final ISVNRemoteResource remoteResource = historyTableProvider.getRemoteResource();
          if(fetchAllLogEntriesJob == null) {
            fetchAllLogEntriesJob = new FetchAllLogEntriesJob();
          }
          if(fetchAllLogEntriesJob.getState() != Job.NONE) {
            fetchAllLogEntriesJob.cancel();
            try {
              fetchAllLogEntriesJob.join();
            } catch(InterruptedException e) {
              SVNUIPlugin.log(new SVNException(Policy
                  .bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
            }
          }
          fetchAllLogEntriesJob.setRemoteFile(remoteResource);
          Utils.schedule(fetchAllLogEntriesJob, getSite());
        }
      };
      getAllAction.setToolTipText(Policy.bind("HistoryView.getAll")); //$NON-NLS-1$
    }
    return getAllAction;
  }
  
  // Get Get Next action (toolbar)
  public IAction getGetNextAction() {
    if(getNextAction == null) {
      getNextAction = new GetNextAction();
    }
    return getNextAction;
  }

  /**
   * All context menu actions use this class This action : - updates
   * currentSelection - action.run
   */
  private Action getContextMenuAction(String title, final IWorkspaceRunnable action) {
    return new Action(title) {
      public void run() {
        try {
          if(resource == null)
            return;
          PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
              try {
                action.run(monitor);
              } catch(CoreException e) {
                throw new InvocationTargetException(e);
              }
            }
          });
        } catch(InvocationTargetException e) {
          SVNUIPlugin.openError(getSite().getShell(), null, null, e, SVNUIPlugin.LOG_NONTEAM_EXCEPTIONS);
        } catch(InterruptedException e) {
          // Do nothing
        }
      }

      // we don't allow multiple selection
      public boolean isEnabled() {
        ISelection selection = getSelection();
        return selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1;
      }
    };
  }

  /**
   * Ask the user to confirm the overwrite of the file if the file has been
   * modified since last commit
   */
  private boolean confirmOverwrite() {
    IFile file = (IFile) resource;
    if(file != null && file.exists()) {
      ISVNLocalFile svnFile = SVNWorkspaceRoot.getSVNFileFor(file);
      try {
        if(svnFile.isDirty()) {
          String title = Policy.bind("HistoryView.overwriteTitle"); //$NON-NLS-1$
          String msg = Policy.bind("HistoryView.overwriteMsg"); //$NON-NLS-1$
          final MessageDialog dialog = new MessageDialog(getSite().getShell(), title, null, msg,
              MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
          final int[] result = new int[ 1];
          getSite().getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
              result[ 0] = dialog.open();
            }
          });
          if(result[ 0] != 0) {
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

  private ISelection getSelection() {
    return selection;
  }

  private ILogEntry getFirstElement() {
    ILogEntry firstElement = null;
    ISelection selection = getSelection();
    if(selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;
      Iterator iter = ss.iterator();
      while(iter.hasNext()) {
        ILogEntry element = (ILogEntry) iter.next();
        if(firstElement == null || element.getRevision().getNumber() > firstElement.getRevision().getNumber())
          firstElement = element;
      }
    }
    return firstElement;
  }

  private ILogEntry getLastElement() {
    ILogEntry lastElement = null;
    ISelection selection = getSelection();
    if(selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;
      Iterator iter = ss.iterator();
      while(iter.hasNext()) {
        ILogEntry element = (ILogEntry) iter.next();
        if(lastElement == null || element.getRevision().getNumber() < lastElement.getRevision().getNumber())
          lastElement = element;
      }
    }
    return lastElement;
  }

  
  private final class GetNextAction extends Action implements IPropertyChangeListener {
    GetNextAction() {
      super(Policy.bind("HistoryView.getNext"), SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_GET_NEXT));
      updateFromProperties();
      SVNUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(this);
    }

    public void run() {
      final ISVNRemoteResource remoteResource = historyTableProvider.getRemoteResource();
      if(fetchNextLogEntriesJob == null) {
        fetchNextLogEntriesJob = new FetchNextLogEntriesJob();
      }
      if(fetchNextLogEntriesJob.getState() != Job.NONE) {
        fetchNextLogEntriesJob.cancel();
        try {
          fetchNextLogEntriesJob.join();
        } catch(InterruptedException e) {
          SVNUIPlugin.log(new SVNException(Policy
              .bind("HistoryView.errorFetchingEntries", remoteResource.getName()), e)); //$NON-NLS-1$
        }
      }
      fetchNextLogEntriesJob.setRemoteFile(remoteResource);
      Utils.schedule(fetchNextLogEntriesJob, getSite());
    }

    public void propertyChange(PropertyChangeEvent event) {
      if(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH.equals(event.getProperty())) {
        updateFromProperties();
      }
    }

    private void updateFromProperties() {
      int entriesToFetch = SVNUIPlugin.getPlugin().getPreferenceStore().getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
      setToolTipText(Policy.bind("HistoryView.getNext") + " " + entriesToFetch); //$NON-NLS-1$
      if(entriesToFetch <= 0) {
        setEnabled(false);
      }
    }
  }

  
  private class FetchLogEntriesJob extends AbstractFetchJob {
    public ISVNRemoteResource remoteResource;

    public FetchLogEntriesJob() {
      super(Policy.bind("HistoryView.fetchHistoryJob")); //$NON-NLS-1$;
    }

    public void setRemoteFile(ISVNRemoteResource resource) {
      this.remoteResource = resource;
    }

    public IStatus run(IProgressMonitor monitor) {
      try {
        if(remoteResource != null && !shutdown) {
          if(resource == null) {
            if(remoteResource == null
                || !SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_SHOW_TAGS_IN_REMOTE)) {
              tagManager = null;
            } else {
              tagManager = new AliasManager(remoteResource.getUrl());
            }
          } else {
            tagManager = new AliasManager(resource);
          }
          SVNRevision pegRevision = remoteResource.getRevision();
          SVNRevision revisionEnd = new SVNRevision.Number(0);
          boolean stopOnCopy = toggleStopOnCopyAction.isChecked();
          IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
          int entriesToFetch = store.getInt(ISVNUIConstants.PREF_LOG_ENTRIES_TO_FETCH);
          long limit = entriesToFetch;
          entries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd, stopOnCopy,
              limit + 1, tagManager);
          long entriesLength = entries.length;
          if(entriesLength > limit) {
            ILogEntry[] fetchedEntries = new ILogEntry[ entries.length - 1];
            for(int i = 0; i < entries.length - 1; i++) {
              fetchedEntries[ i] = entries[ i];
            }
            entries = fetchedEntries;
            getNextAction.setEnabled(true);
          } else {
            getNextAction.setEnabled(false);
          }
          final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
          getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
              if(entries != null && tableHistoryViewer != null && !tableHistoryViewer.getTable().isDisposed()) {
                // once we got the entries, we refresh the table
                if(entries.length > 0) {
                  lastEntry = entries[ entries.length - 1];
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
      } catch(TeamException e) {
        return e.getStatus();
      }
    }
  }

  private class FetchNextLogEntriesJob extends AbstractFetchJob {
    public ISVNRemoteResource remoteResource;

    public FetchNextLogEntriesJob() {
      super(Policy.bind("HistoryView.fetchHistoryJob")); //$NON-NLS-1$;
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
          ILogEntry[] nextEntries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd,
              stopOnCopy, limit + 1, tagManager);
          long entriesLength = nextEntries.length;
          ILogEntry[] fetchedEntries = null;
          if(entriesLength > limit) {
            fetchedEntries = new ILogEntry[ nextEntries.length - 1];
            for(int i = 0; i < nextEntries.length - 1; i++)
              fetchedEntries[ i] = nextEntries[ i];
            getNextAction.setEnabled(true);
          } else {
              fetchedEntries = new ILogEntry[ nextEntries.length];
              for(int i = 0; i < nextEntries.length; i++)
                fetchedEntries[ i] = nextEntries[ i];
            getNextAction.setEnabled(false);
          }
          ArrayList entryArray = new ArrayList();
          if(entries == null)
            entries = new ILogEntry[ 0];
          for(int i = 0; i < entries.length; i++)
            entryArray.add(entries[ i]);
          for(int i = 0; i < fetchedEntries.length; i++)
            entryArray.add(fetchedEntries[ i]);
          entries = new ILogEntry[ entryArray.size()];
          entryArray.toArray(entries);
          getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
              if(entries != null && tableHistoryViewer != null && !tableHistoryViewer.getTable().isDisposed()) {
                // once we got the entries, we refresh the table
                ISelection selection = tableHistoryViewer.getSelection();
                tableHistoryViewer.refresh();
                tableHistoryViewer.setSelection(selection);
                if(entries.length > 0) {
                  lastEntry = entries[ entries.length - 1];
                  long lastEntryNumber = lastEntry.getRevision().getNumber();
                  revisionStart = new SVNRevision.Number(lastEntryNumber - 1);
                }
              }
            }
          });
        }
        return Status.OK_STATUS;
      } catch(TeamException e) {
        return e.getStatus();
      }
    }
  }
  
  private abstract class AbstractFetchJob extends Job {
	    public AbstractFetchJob(String name) {
		super(name);
	}

		public abstract void setRemoteFile(ISVNRemoteResource resource);

  }

  private class FetchAllLogEntriesJob extends AbstractFetchJob {
    public ISVNRemoteResource remoteResource;

    public FetchAllLogEntriesJob() {
      super(Policy.bind("HistoryView.fetchHistoryJob")); //$NON-NLS-1$;
    }

    public void setRemoteFile(ISVNRemoteResource resource) {
      this.remoteResource = resource;
    }

    public IStatus run(IProgressMonitor monitor) {
      try {
        if(remoteResource != null && !shutdown) {
          if(resource == null) {
            if(remoteResource == null
                || !SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_SHOW_TAGS_IN_REMOTE))
              tagManager = null;
            else
              tagManager = new AliasManager(remoteResource.getUrl());
          } else
            tagManager = new AliasManager(resource);
          SVNRevision pegRevision = remoteResource.getRevision();
          revisionStart = SVNRevision.HEAD;
          SVNRevision revisionEnd = new SVNRevision.Number(0);
          boolean stopOnCopy = toggleStopOnCopyAction.isChecked();
          long limit = 0;
          entries = remoteResource.getLogEntries(monitor, pegRevision, revisionStart, revisionEnd, stopOnCopy, limit,
              tagManager);
          final SVNRevision.Number revisionId = remoteResource.getLastChangedRevision();
          getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
              if(entries != null && tableHistoryViewer != null && !tableHistoryViewer.getTable().isDisposed()) {
                // once we got the entries, we refresh the table
                if(entries.length > 0) {
                  lastEntry = entries[ entries.length - 1];
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
      } catch(TeamException e) {
        return e.getStatus();
      }
    }
  }

  class FetchChangePathJob extends Job {
    public ILogEntry logEntry;

    public FetchChangePathJob() {
      super(Policy.bind("HistoryView.fetchChangePathJob")); //$NON-NLS-1$;
    }

    public void setLogEntry(ILogEntry logEntry) {
      this.logEntry = logEntry;
    }

    public IStatus run(IProgressMonitor monitor) {
      if(logEntry.getResource() != null) {
        setCurrentLogEntryChangePath(logEntry.getLogEntryChangePaths());
      }
      return Status.OK_STATUS;
    }
  }


  public static class ToggleAffectedPathsLayoutAction extends Action {
    private final SVNHistoryPage page;
    private final int layout;

    public ToggleAffectedPathsLayoutAction( SVNHistoryPage page, int layout) {
      super("", AS_RADIO_BUTTON);
      this.page = page;
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
        page.createAffectedPathsViewer(layout);
      }
    }
    
  }

  /* (non-Javadoc)
   * @see org.eclipse.team.svn.core.internal.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
   */
  public void resourceSyncInfoChanged(IResource[] changedResources) {
      for (int i = 0; i < changedResources.length; i++) {
          IResource changedResource = changedResources[i];
          if( changedResource.equals( resource ) ) {
				resourceChanged();
          }
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
                  SVNUIPlugin.openError(getHistoryPageSite().getShell(), null, null, e);
              }
      	}
      });
  }

  /* (non-Javadoc)
   * @see org.eclipse.team.svn.core.internal.IResourceStateChangeListener#resourceModified(org.eclipse.core.resources.IResource[])
   */
  public void resourceModified(IResource[] changedResources) {
  }

  /* (non-Javadoc)
   * @see org.eclipse.team.svn.core.internal.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
   */
  public void projectConfigured(IProject project) {
  }

  /* (non-Javadoc)
   * @see org.eclipse.team.svn.core.internal.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
   */
  public void projectDeconfigured(IProject project) {
  }

}
