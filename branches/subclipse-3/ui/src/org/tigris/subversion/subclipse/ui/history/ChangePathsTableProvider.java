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
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
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
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * This class provides the table and it's required components for a change path
 * This is used from HistoryView
 */
public class ChangePathsTableProvider {

    private ILogEntry currentLogEntry;
    private TableViewer viewer;
    private Font currentPathFont;
        
    /**
     * Constructor for HistoryTableProvider.
     */
    public ChangePathsTableProvider() {
        super();
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
                    return ""+changePath.getAction();
                case COL_PATH:
                	return changePath.getPath();
                case COL_DESCRIPTION:
                    if (changePath.getCopySrcPath() != null) {
                    	return Policy.bind("ChangePathsTableProvider.copiedfrom", 
                                changePath.getCopySrcPath(),
                                changePath.getCopySrcRevision().toString());
                    } else {
                    	return "";
                    }
            }
            return ""; //$NON-NLS-1$
        }
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            LogEntryChangePath changePath = (LogEntryChangePath)element;
            if (changePath == null)
                return null;
            
            SVNUrl url = changePath.getUrl();
            if (url == null) {
                return null;
            }
            
            ISVNRemoteResource remoteResource = currentLogEntry.getRemoteResource();
            if (remoteResource == null) {
                return null;
            }
            
            SVNUrl currentUrl = remoteResource.getUrl();
            if (currentUrl == null) {
                return null;
            }
            
            if (currentUrl.equals(url)) {
                if (currentPathFont == null) {
                    Font defaultFont = JFaceResources.getDefaultFont();
                    FontData[] data = defaultFont.getFontData();
                    for (int i = 0; i < data.length; i++) {
                        data[i].setStyle(SWT.BOLD);
                    }               
                    currentPathFont = new Font(viewer.getTable().getDisplay(), data);
                }
                return currentPathFont;
            }
            return null;
        }
        
    }
    
    /**
     * Create a TableViewer that can be used to display a list of ILogEntry instances.
     * Ths method provides the labels and sorter but does not provide a content provider
     * 
     * @param parent
     * @return TableViewer
     */
    public TableViewer createTable(Composite parent) {
        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
    
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        
        TableViewer viewer = new TableViewer(table);
        
        createColumns(table, layout, viewer);

        viewer.setLabelProvider(new ChangePathLabelProvider());
        
        table.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if(currentPathFont != null) {
                    currentPathFont.dispose();
                }
            }
        });
        
        this.viewer = viewer;
        return viewer;
    }
    
    /**
     * Creates the columns for the history table.
     */
    private void createColumns(Table table, TableLayout layout, TableViewer viewer) {
        // action
        TableColumn col = new TableColumn(table, SWT.NONE);
        col.setResizable(false);
        col.setText(Policy.bind("ChangePathsTableProvider.action")); //$NON-NLS-1$
        layout.addColumnData(new ColumnWeightData(5, true));
    
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

    public void setLogEntry(ILogEntry logEntry) {
        this.currentLogEntry = logEntry;
        viewer.setInput(logEntry);
    }

}
