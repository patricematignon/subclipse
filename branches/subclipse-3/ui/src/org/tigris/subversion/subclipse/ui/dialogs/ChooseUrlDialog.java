package org.tigris.subversion.subclipse.ui.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.repository.model.AllRootsElement;
import org.tigris.subversion.subclipse.ui.repository.model.RemoteContentProvider;

public class ChooseUrlDialog extends Dialog {
    private static final int LIST_HEIGHT_HINT = 250;
    private static final int LIST_WIDTH_HINT = 450;
    
    private TreeViewer treeViewer;
    
    private String url;
    private IResource resource;

    public ChooseUrlDialog(Shell parentShell, IResource resource) {
        super(parentShell);
        this.resource = resource;
    }
    
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("ChooseUrlDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		treeViewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
        RemoteContentProvider contentProvider = new RemoteContentProvider();
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(new WorkbenchLabelProvider());
        if (resource == null) treeViewer.setInput(new AllRootsElement());     
        else {
            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
            ISVNRepositoryLocation repository = svnResource.getRepository();
            if (repository == null) treeViewer.setInput(new AllRootsElement());
            else treeViewer.setInput(svnResource.getRepository());
        }
        
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = LIST_HEIGHT_HINT;
		data.widthHint = LIST_WIDTH_HINT;
		treeViewer.getControl().setLayoutData(data);

        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                okPressed();
            }
        }); 
        
		return composite;
	}

    protected void okPressed() {
        ISelection selection = treeViewer.getSelection();
        if (!selection.isEmpty() && (selection instanceof IStructuredSelection)) {
            IStructuredSelection structured = (IStructuredSelection)selection;
            Object first = structured.getFirstElement();
            if (first instanceof ISVNRemoteResource) url = ((ISVNRemoteResource)first).getUrl().toString();
            if (first instanceof ISVNRepositoryLocation) url = ((ISVNRepositoryLocation)first).getUrl().toString();
        }
        super.okPressed();
    }
    
    public String getUrl() {
        return url;
    }
}
