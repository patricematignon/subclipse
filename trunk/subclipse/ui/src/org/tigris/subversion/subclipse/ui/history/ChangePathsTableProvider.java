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
package org.tigris.subversion.subclipse.ui.history;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * This class provides the table and it's required components for a change path
 * This is used from HistoryView
 */
public class ChangePathsTableProvider extends TableViewer {
    ILogEntry currentLogEntry;
    Font currentPathFont;
        
    public ChangePathsTableProvider(Composite parent, IContentProvider contentProvider) {
      super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
      
      TableLayout layout = new TableLayout();
      GridData data = new GridData(GridData.FILL_BOTH);
      
      Table table = (Table) getControl();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      table.setLayoutData(data);    
      table.setLayout(layout);
      table.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          if(currentPathFont != null) {
            currentPathFont.dispose();
          }
        }
      });
      
      createColumns(table, layout);
      
      setLabelProvider(new ChangePathLabelProvider());
      setContentProvider(contentProvider);
    }
    /**
     * Constructor for HistoryTableProvider.
     */
    public ChangePathsTableProvider(Composite parent, SVNHistoryPage page) {
      this(parent, new ChangePathsTableContentProvider(page));
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        this.currentLogEntry = (ILogEntry) input;
    }
    
    /**
     * Creates the columns for the history table.
     */
    private void createColumns(Table table, TableLayout layout) {
        // action
        TableColumn col = new TableColumn(table, SWT.NONE);
        col.setResizable(true);
        col.setText(Policy.bind("ChangePathsTableProvider.action")); //$NON-NLS-1$
        layout.addColumnData(new ColumnWeightData(10, true));
    
        // path
        col = new TableColumn(table, SWT.NONE);
        col.setResizable(true);
        col.setText(Policy.bind("ChangePathsTableProvider.path")); //$NON-NLS-1$
        layout.addColumnData(new ColumnWeightData(45, true));
    
        // description
        col = new TableColumn(table, SWT.NONE);
        col.setResizable(true);
        col.setText(Policy.bind("ChangePathsTableProvider.description")); //$NON-NLS-1$
        layout.addColumnData(new ColumnWeightData(50, true));
    }
    
    
    //column constants
    private static final int COL_ACTION = 0;
    private static final int COL_PATH = 1;
    private static final int COL_DESCRIPTION = 2;

    /**
     * The label provider.
     */
    class ChangePathLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            LogEntryChangePath changePath = (LogEntryChangePath)element;
            if (changePath == null) return ""; //$NON-NLS-1$
            switch (columnIndex) {
                case COL_ACTION:
                    return ""+changePath.getAction(); //$NON-NLS-1$
                case COL_PATH:
                	return changePath.getPath();
                case COL_DESCRIPTION:
                    if (changePath.getCopySrcPath() != null) {
                    	return Policy.bind("ChangePathsTableProvider.copiedfrom",  //$NON-NLS-1$
                                changePath.getCopySrcPath(),
                                changePath.getCopySrcRevision().toString());
                    }
                    return ""; //$NON-NLS-1$
            }
            return ""; //$NON-NLS-1$
        }
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            if(currentLogEntry==null || element==null) {
                return null;
            }
            
            SVNUrl url = ((LogEntryChangePath)element).getUrl();
            
            ISVNRemoteResource remoteResource = currentLogEntry.getRemoteResource();
            if (remoteResource == null) {
                return null;
            }
            
            SVNUrl currentUrl = remoteResource.getUrl();
            if (currentUrl == null || !currentUrl.equals(url)) {
                return null;
            }

            if (currentPathFont == null) {
                Font defaultFont = JFaceResources.getDefaultFont();
                FontData[] data = defaultFont.getFontData();
                for (int i = 0; i < data.length; i++) {
                    data[i].setStyle(SWT.BOLD);
                }               
                currentPathFont = new Font(getControl().getDisplay(), data);
            }
            return currentPathFont;
        }
        
    }

    
    static final LogEntryChangePath[] EMPTY_CHANGE_PATHS = new LogEntryChangePath[ 0];

    static class ChangePathsTableContentProvider implements IStructuredContentProvider {

      private final SVNHistoryPage page;

      ChangePathsTableContentProvider(SVNHistoryPage page) {
        this.page = page;
      }

      public Object[] getElements(Object inputElement) {
        if( !this.page.isShowChangePaths() || !(inputElement instanceof ILogEntry)) {
          return EMPTY_CHANGE_PATHS;
        }

        ILogEntry logEntry = (ILogEntry) inputElement;
        if(SVNProviderPlugin.getPlugin().getSVNClientManager().isFetchChangePathOnDemand()) {
          if(this.page.currentLogEntryChangePath != null) {
            return this.page.currentLogEntryChangePath;
          }
          this.page.scheduleFetchChangePathJob(logEntry);
          return EMPTY_CHANGE_PATHS;
        }

        return logEntry.getLogEntryChangePaths();
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.page.currentLogEntryChangePath = null;
      }

    }
    
}
