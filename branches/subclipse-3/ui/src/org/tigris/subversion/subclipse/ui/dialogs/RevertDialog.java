package org.tigris.subversion.subclipse.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

public class RevertDialog extends Dialog {
    
	private static final int WIDTH_HINT = 500;
	private final static int SELECTION_HEIGHT_HINT = 250;
    
    private IResource[] resourcesToRevert;
    private String url;
    private Object[] selectedResources;
    private CheckboxTableViewer listViewer;

    public RevertDialog(Shell parentShell, IResource[] resourcesToRevert, String url) {
        super(parentShell);
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);
		this.resourcesToRevert = resourcesToRevert;
		this.url = url;
    }
    
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("RevertDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = createWrappingLabel(composite);
		if (url == null) label.setText(Policy.bind("RevertDialog.url") + " " + Policy.bind("RevertDialog.multiple")); //$NON-NLS-1$
		else label.setText(Policy.bind("RevertDialog.url") + " " + url); //$NON-NLS-1$

		addResourcesArea(composite);
		WorkbenchHelp.setHelp(composite, IHelpContextIds.REVERT_DIALOG);

		return composite;
	}
	
	private void addResourcesArea(Composite composite) {
	    
		// add a description label
		Label label = createWrappingLabel(composite);
		label.setText(Policy.bind("RevertDialog.resources")); //$NON-NLS-1$
		// add the selectable checkbox list
		Table table = new Table(composite, 
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | 
                SWT.MULTI | SWT.CHECK | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		listViewer = new CheckboxTableViewer(table);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SELECTION_HEIGHT_HINT;
		data.widthHint = WIDTH_HINT;
		listViewer.getTable().setLayoutData(data);
		createColumns(table, layout);
		// set the contents of the list
		listViewer.setLabelProvider(new ITableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
			   String result = null;
			   switch (columnIndex) {
				case 0 :
	    			result = ""; 
					break;			
	            case 1:
	                if (url == null) result = ((IResource)element).getFullPath().toString();
	                else result = getResource((IResource)element);
	                if (result.length() == 0) result = ((IResource)element).getFullPath().toString();
	                break;
	            case 2:
				    result = getStatus((IResource)element);
	                break;
	            case 3:
				    result = getPropertyStatus((IResource)element);
	                break;	                
	            default:
	                result = "";
	                break;
	            }

			   return result;
			}
			// Strip off segments of path that are included in URL.
			private String getResource(IResource resource) {
			    String[] segments = resource.getFullPath().segments();
			    StringBuffer path = new StringBuffer();
			    for (int i = 0; i < segments.length; i++) {
			        path.append("/" + segments[i]);
			        if (url.endsWith(path.toString())) {
			            if (i == (segments.length - 2)) 
			                return resource.getFullPath().toString().substring(path.length() + 1);
			            else 
			                return resource.getFullPath().toString().substring(path.length());
			        }
			    }
			    return resource.getFullPath().toString();
            }
            public Image getColumnImage(Object element, int columnIndex) {
			    if (columnIndex == 1) {
			        if (element instanceof IAdaptable) {
						IWorkbenchAdapter adapter = (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(
								IWorkbenchAdapter.class);
						if (adapter == null) {
							return null;
						}
						ImageDescriptor descriptor = adapter.getImageDescriptor(element);
						if (descriptor == null) return null;
						Image image = (Image) SWTResourceUtil.getImageTable().get(descriptor);
						if (image == null) {
							image = descriptor.createImage();
							SWTResourceUtil.getImageTable().put(descriptor, image);
						}
						return image;						
			        }
			    }
				return null;
			}
            public void addListener(ILabelProviderListener listener) {
            }
            public void dispose() {
            }
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
            public void removeListener(ILabelProviderListener listener) {
            }
		});
		
		listViewer.setSorter(new RevertSorter(1));
		
		listViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return resourcesToRevert;
            }
            public void dispose() {
            }
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }	    
		});
		listViewer.setInput(new AdaptableResourceList(resourcesToRevert));
		if (selectedResources == null) {
		    setChecks();
		} else {
			listViewer.setCheckedElements(selectedResources);
		}
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResources = listViewer.getCheckedElements();
			}
		});
		
		addSelectionButtons(composite);
		
    }	
	
	private static String getStatus(IResource resource) {
	    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        String result = null;
	       try {
	           LocalResourceStatus status = svnResource.getStatus();
		       if (status.isTextConflicted())
		           result = Policy.bind("CommitDialog.conflicted"); //$NON-NLS-1$
		       else	            
	            if (status.isAdded())
                   result = Policy.bind("CommitDialog.added"); //$NON-NLS-1$
               else
               if (status.isDeleted())
                   result = Policy.bind("CommitDialog.deleted"); //$NON-NLS-1$
               else
               if (status.isTextModified())
                   result = Policy.bind("CommitDialog.modified"); //$NON-NLS-1$				           
               else
               if (!status.isManaged())
                   result = Policy.bind("CommitDialog.unversioned"); //$NON-NLS-1$
               else
                   result = "";
			} catch (TeamException e) {
			    result = "";
			}                   
	    return result;
    }
	
	private static String getPropertyStatus(IResource resource) {
	    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        String result = null;
	       try {
	            LocalResourceStatus status = svnResource.getStatus();
	            if (status.isPropConflicted())
	                result = Policy.bind("CommitDialog.conflicted"); //$NON-NLS-1$		            
	            else if ((svnResource.getStatus() != null) &&
	                (svnResource.getStatus().getPropStatus() != null) &&
	                (svnResource.getStatus().getPropStatus().equals(SVNStatusKind.MODIFIED)))
	                result = Policy.bind("CommitDialog.modified"); //$NON-NLS-1$		
                else
                    result = "";
			} catch (TeamException e) {
			    result = "";
			}                   
	    return result;
    }	
	
    /**
	 * Method createColumns.
	 * @param table
	 * @param layout
	 * @param viewer
	 */
	private void createColumns(Table table, TableLayout layout) {
	    // sortable table
		SelectionListener headerListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// column selected - need to sort
				int column = listViewer.getTable().indexOf((TableColumn) e.widget);
				RevertSorter oldSorter = (RevertSorter) listViewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
				oldSorter.setReversed(!oldSorter.isReversed());
				listViewer.refresh();
				} else {
					listViewer.setSorter(new RevertSorter(column));
				}
			}
		};

		TableColumn col;
		// check
		col = new TableColumn(table, SWT.NONE);
    	col.setResizable(false);
		layout.addColumnData(new ColumnPixelData(20, false));
		col.addSelectionListener(headerListener);

		// resource
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("PendingOperationsView.resource")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(120, true));
		col.addSelectionListener(headerListener);

		// text status
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("CommitDialog.status")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(50, true));
		col.addSelectionListener(headerListener);
		
		// property status
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("CommitDialog.property")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(50, true));
		col.addSelectionListener(headerListener);		

	}	
	
	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
	
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);
	
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.selectAll"), false); //$NON-NLS-1$
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
				selectedResources = null;
			}
		};
		selectButton.addSelectionListener(listener);
	
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, Policy.bind("ReleaseCommentDialog.deselectAll"), false); //$NON-NLS-1$
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
				selectedResources = new Object[0];
			}
		};
		deselectButton.addSelectionListener(listener);
	}	
    
	/**
	 * Returns the selected resources.
	 * @return IResource[]
	 */
	public IResource[] getSelectedResources() {
		if (selectedResources == null) {
			return resourcesToRevert;
		} else {
			List result = Arrays.asList(selectedResources);
			return (IResource[]) result.toArray(new IResource[result.size()]);
		}
	}
	
	protected static final int LABEL_WIDTH_HINT = 400;
	protected Label createWrappingLabel(Composite parent) {
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalIndent = 0;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = LABEL_WIDTH_HINT;
		label.setLayoutData(data);
		return label;
	}
	
	private void setChecks() {
	    listViewer.setAllChecked(true);
		selectedResources = listViewer.getCheckedElements();
	}
	
	private static class RevertSorter extends ViewerSorter {
		private boolean reversed = false;
		private int columnNumber;
		private static final int NUM_COLUMNS = 4;
		private static final int[][] SORT_ORDERS_BY_COLUMN = {
		    {0, 1, 2, 3}, 	/* check */    
			{1, 0, 2, 3},	/* resource */ 
			{2, 0, 1, 3},	/* status */
			{3, 0, 1, 2},	/* prop status */
		};
		
		public RevertSorter(int columnNumber) {
			this.columnNumber = columnNumber;
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			IResource r1 = (IResource)e1;
			IResource r2 = (IResource)e2;
			int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
			int result = 0;
			for (int i = 0; i < NUM_COLUMNS; ++i) {
				result = compareColumnValue(columnSortOrder[i], r1, r2);
				if (result != 0)
					break;
			}
			if (reversed)
				result = -result;
			return result;
		}
		
		private int compareColumnValue(int columnNumber, IResource r1, IResource r2) {
			switch (columnNumber) {
				case 0: /* check */
					return 0;
				case 1: /* resource */
					return collator.compare(r1.getFullPath().toString(), r2.getFullPath().toString());					
				case 2: /* status */
					return collator.compare(getStatus(r1), getStatus(r2));
				case 3: /* prop status */
					return collator.compare(getPropertyStatus(r1), getPropertyStatus(r2));					
				default:
					return 0;
			}
		}
	
		public int getColumnNumber() {
			return columnNumber;
		}

		public boolean isReversed() {
			return reversed;
		}

		public void setReversed(boolean newReversed) {
			reversed = newReversed;
		}

	}	

}
