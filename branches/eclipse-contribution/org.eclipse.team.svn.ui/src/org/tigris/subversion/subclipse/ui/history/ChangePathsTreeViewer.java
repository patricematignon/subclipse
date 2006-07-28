/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.ui.history;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

/**
 * Compressed folder representation of the affected paths panel.
 * 
 * @author Eugene Kuleshov
 */
class ChangePathsTreeViewer extends TreeViewer {
    ILogEntry currentLogEntry;
    Font currentPathFont;
        
    public ChangePathsTreeViewer(Composite parent, SVNHistoryPage page) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI /*| SWT.FULL_SELECTION*/);
        // tree.setHeaderVisible(true);
        // tree.setLinesVisible(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        getControl().setLayoutData(data);
        getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if(currentPathFont != null) {
                    currentPathFont.dispose();
                }
            }
        });
    
        setLabelProvider(new ChangePathLabelProvider());
        setContentProvider(new ChangePathsTreeContentProvider(page));
    }
    
    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        this.currentLogEntry = (ILogEntry) input;

        // expand all, select and show first element. is there a better way? 
        expandAll();
        final TreeItem[] items = ((Tree) getControl()).getItems();
        if(items!=null && items.length>0) {
          setSelection(Collections.singletonList(items[0]));
          ((Tree) getControl()).showSelection();
        }
    }
    
    
    /**
     * The label provider.
     */
    class ChangePathLabelProvider extends LabelProvider implements IFontProvider {
        
        public String getText(Object element) {
            if(element instanceof LogEntryChangePath) {
                LogEntryChangePath changePath = (LogEntryChangePath) element;
                String path = changePath.getPath();
                int n = path.lastIndexOf('/');
                if(n>-1) path = path.substring(n+1);
                if (changePath.getCopySrcPath() == null) {
                    return path;
                }
                return path + " [" +  //$NON-NLS-1$
                  Policy.bind("ChangePathsTableProvider.copiedfrom",  //$NON-NLS-1$
                      changePath.getCopySrcPath(),
                      changePath.getCopySrcRevision().toString())+"]";
            } else if(element instanceof HistoryFolder) {
                HistoryFolder f = (HistoryFolder) element;
                if(f.getCopySrcPath()==null) {
                    return f.getPath() + (f.getChildCount()==0 ? "" : " ["+f.getChildCount()+"]");
                }
                return f.getPath() + (f.getChildCount()==0 ? " [" : " ["+f.getChildCount()+"] [") +
                  Policy.bind("ChangePathsTableProvider.copiedfrom",  //$NON-NLS-1$
                      f.getCopySrcPath(), 
                      f.getCopySrcRevision().toString())+"]";
            }
            return element.toString();
        }
        
        public Image getImage(Object element) {
            String id = null;
            if(element instanceof LogEntryChangePath) {
              LogEntryChangePath changePath = (LogEntryChangePath)element;
              switch(changePath.getAction()) {
                case 'A':  id = ISVNUIConstants.IMG_FILEADD_PENDING;  break;
                case 'D':  id = ISVNUIConstants.IMG_FILEDELETE_PENDING;  break;
                // case 'M':  
                default:   id = ISVNUIConstants.IMG_FILEMODIFIED_PENDING;  break;
              }

            } else if(element instanceof HistoryFolder) {
              HistoryFolder folder = (HistoryFolder) element;
              if(folder.getChildren().length==0) {
                switch(folder.getAction()) {
                  case 'A':  id = ISVNUIConstants.IMG_FILEADD_PENDING;  break;
                  case 'D':  id = ISVNUIConstants.IMG_FILEDELETE_PENDING;  break;
                  // case 'M':
                  default:   id = ISVNUIConstants.IMG_FILEMODIFIED_PENDING;  break;
                }
              } else {
                  switch(folder.getAction()) {
                    case 'A':  id = ISVNUIConstants.IMG_FOLDERADD_PENDING;  break;
                    case 'D':  id = ISVNUIConstants.IMG_FOLDERDELETE_PENDING;  break;
                    case 'M':  id = ISVNUIConstants.IMG_FOLDERMODIFIED_PENDING;  break;
                    default:   id = ISVNUIConstants.IMG_FOLDER;  break;
                  }
              }
            }
            if(id==null) return null;
            ImageDescriptor descriptor = SVNUIPlugin.getPlugin().getImageDescriptor(id);
            return descriptor==null ? null : descriptor.createImage();
        }
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            if(element==null || currentLogEntry==null ||
                !(element instanceof LogEntryChangePath)) {
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
            
            SVNUrl url = ((LogEntryChangePath)element).getUrl();
            if (currentUrl.equals(url)) {
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
            return null;
        }
        
    }
    
    
    static final LogEntryChangePath[] EMPTY_CHANGE_PATHS = new LogEntryChangePath[0];
    
    static class ChangePathsTreeContentProvider implements ITreeContentProvider {

      private final SVNHistoryPage page;

      ChangePathsTreeContentProvider(SVNHistoryPage page) {
        this.page = page;
      }

      public Object[] getChildren(Object parentElement) {
        if(parentElement instanceof HistoryFolder) {
          return ((HistoryFolder) parentElement).getChildren();
        }
        return null;
      }

      public Object getParent(Object element) {
        return null;
      }

      public boolean hasChildren(Object element) {
        if(element instanceof HistoryFolder) {
          HistoryFolder folder = (HistoryFolder) element;
          return folder.getChildren().length > 0;
        }
        return false;
      }

      public Object[] getElements(Object inputElement) {
        if( !this.page.isShowChangePaths() || !(inputElement instanceof ILogEntry)) {
          return EMPTY_CHANGE_PATHS;
        }

        if(this.page.currentLogEntryChangePath != null) {

        }

        ILogEntry logEntry = (ILogEntry) inputElement;
        if(SVNProviderPlugin.getPlugin().getSVNClientManager().isFetchChangePathOnDemand()) {
          if(this.page.currentLogEntryChangePath != null) {
            return getGroups(this.page.currentLogEntryChangePath);
          }
          this.page.scheduleFetchChangePathJob(logEntry);
          return EMPTY_CHANGE_PATHS;
        }

        return getGroups(logEntry.getLogEntryChangePaths());
      }

      private Object[] getGroups(LogEntryChangePath[] changePaths) {
        // 1st pass. Collect folder names
        Set folderNames = new HashSet();
        for(int i = 0; i < changePaths.length; i++) {
          folderNames.add(getFolderName(changePaths[ i]));
        }

        // 2nd pass. Sorting out explicitly changed folders
        TreeMap folders = new TreeMap();
        for(int i = 0; i < changePaths.length; i++) {
          LogEntryChangePath changePath = changePaths[ i];
          String path = changePath.getPath();
          if(folderNames.contains(path)) {
            // changed folder
            HistoryFolder folder = (HistoryFolder) folders.get(path);
            if(folder == null) {
              folder = new HistoryFolder(changePath);
              folders.put(path, folder);
            }
          } else {
            // changed resource
            path = getFolderName(changePath);
            HistoryFolder folder = (HistoryFolder) folders.get(path);
            if(folder == null) {
              folder = new HistoryFolder(path);
              folders.put(path, folder);
            }
            folder.add(changePath);
          }
        }

        return folders.values().toArray(new Object[folders.size()]);
      }

      private String getFolderName(LogEntryChangePath changePath) {
        String path = changePath.getPath();
        int n = path.lastIndexOf('/');
        return n > -1 ? path.substring(0, n) : path;
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.page.currentLogEntryChangePath = null;
      }

    }
    
}
