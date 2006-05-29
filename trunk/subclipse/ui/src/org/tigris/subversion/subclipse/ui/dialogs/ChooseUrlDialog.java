package org.tigris.subversion.subclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.history.Branches;
import org.tigris.subversion.subclipse.core.history.Alias;
import org.tigris.subversion.subclipse.core.history.AliasManager;
import org.tigris.subversion.subclipse.core.history.Tags;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.CreateRemoteFolderAction;
import org.tigris.subversion.subclipse.ui.actions.DeleteRemoteResourceAction;
import org.tigris.subversion.subclipse.ui.repository.RepositoryFilters;
import org.tigris.subversion.subclipse.ui.repository.model.AllRootsElement;
import org.tigris.subversion.subclipse.ui.repository.model.RemoteContentProvider;

public class ChooseUrlDialog extends TrayDialog {
    private static final int LIST_HEIGHT_HINT = 250;
    private static final int LIST_WIDTH_HINT = 450;

    private TreeViewer treeViewer;
    private Action refreshAction;
    private Action newFolderAction;
    private Action deleteFolderAction;

    private String url;
    private String name;
    private String[] urls;
    private String[] names;
    private IResource resource;
    private String message;
    private boolean multipleSelect = false;
    private ISVNRepositoryLocation repositoryLocation;
    private boolean foldersOnly = false;
    private boolean includeBranchesAndTags = true;

    public ChooseUrlDialog(Shell parentShell, IResource resource) {
        super(parentShell);
        this.resource = resource;
        refreshAction = new Action(Policy.bind("ChooseUrlDialog.refresh"), SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
            public void run() {
                refreshViewer(true);
            }
        };
        newFolderAction = new Action(Policy.bind("NewRemoteFolderWizard.title")) { //$NON-NLS-1$
            public void run() {
                CreateRemoteFolderAction createAction = new CreateRemoteFolderAction();
                createAction.selectionChanged(null, treeViewer.getSelection());
                createAction.run(null);
                refreshViewer(true);
            }
        };
        deleteFolderAction = new Action(Policy.bind("ChooseUrlDialog.delete")) { //$NON-NLS-1$
            public void run() {
                DeleteRemoteResourceAction deleteAction = new DeleteRemoteResourceAction();
                deleteAction.selectionChanged(null, treeViewer.getSelection());
                deleteAction.run(null);
                refreshViewer(true);
            }
        };
    }

	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("ChooseUrlDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (message != null) {
			Label messageLabel = new Label(composite, SWT.NONE);
			messageLabel.setText(message);
		}

		if (multipleSelect) treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		else treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        RemoteContentProvider contentProvider = new RemoteContentProvider();
        contentProvider.setIncludeBranchesAndTags(includeBranchesAndTags);
        contentProvider.setResource(resource);
        treeViewer.setContentProvider(contentProvider);
        if( foldersOnly )
        	treeViewer.addFilter(RepositoryFilters.FOLDERS_ONLY);

        //        treeViewer.setLabelProvider(new WorkbenchLabelProvider());
        treeViewer.setLabelProvider(new RemoteLabelProvider());
        if (repositoryLocation == null) {
	        if (resource == null) treeViewer.setInput(new AllRootsElement());
	        else {
	            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
	            ISVNRepositoryLocation repository = svnResource.getRepository();
	            if (repository == null) treeViewer.setInput(new AllRootsElement());
	            else treeViewer.setInput(svnResource.getRepository());
	        }
        } else treeViewer.setInput(repositoryLocation);

		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = LIST_HEIGHT_HINT;
		data.widthHint = LIST_WIDTH_HINT;
		treeViewer.getControl().setLayoutData(data);

        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                okPressed();
            }
        });

        // Create the popup menu
        MenuManager menuMgr = new MenuManager();
        Tree tree = treeViewer.getTree();
        Menu menu = menuMgr.createContextMenu(tree);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(newFolderAction);
                if (!treeViewer.getSelection().isEmpty()) manager.add(deleteFolderAction);
                manager.add(refreshAction);
            }

        });
        menuMgr.setRemoveAllWhenShown(true);
        tree.setMenu(menu);

		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.CHOOSE_URL_DIALOG);	
        
		return composite;
	}

    protected void refreshViewer(boolean refreshRepositoriesFolders) {
        if (treeViewer == null) return;
        if (refreshRepositoriesFolders)
            SVNProviderPlugin.getPlugin().getRepositories().refreshRepositoriesFolders();
        treeViewer.refresh();
    }

    protected void okPressed() {
        ISelection selection = treeViewer.getSelection();
        if (!selection.isEmpty() && (selection instanceof IStructuredSelection)) {
            IStructuredSelection structured = (IStructuredSelection)selection;
            Object first = structured.getFirstElement();
            if (first instanceof ISVNRemoteResource) {
            	url = ((ISVNRemoteResource)first).getUrl().toString();
            	name = ((ISVNRemoteResource)first).getName();
            }
            if (first instanceof ISVNRepositoryLocation) url = ((ISVNRepositoryLocation)first).getUrl().toString();
            if (first instanceof Alias) url = AliasManager.transformUrl(resource, (Alias)first);
            ArrayList urlArray = new ArrayList();
            ArrayList nameArray = new ArrayList();
            Iterator iter = structured.iterator();
            while (iter.hasNext()) {
            	Object selectedItem = iter.next();
            	if (selectedItem instanceof ISVNRemoteResource) {
            		urlArray.add(((ISVNRemoteResource)selectedItem).getUrl().toString());
            		nameArray.add(((ISVNRemoteResource)selectedItem).getName());
            	}
            }
            urls = new String[urlArray.size()];
            urlArray.toArray(urls);
            names = new String[nameArray.size()];
            nameArray.toArray(names);
        }
        super.okPressed();
    }

    public String getUrl() {
        return url;
    }
    public void setRepositoryLocation(ISVNRepositoryLocation repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

	public void setFoldersOnly(boolean foldersOnly) {
		this.foldersOnly = foldersOnly;
	}

	class RemoteLabelProvider extends LabelProvider implements IColorProvider, IFontProvider{
		private WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

		public Color getForeground(Object element) {
			return workbenchLabelProvider.getForeground(element);
		}

		public Color getBackground(Object element) {
			return workbenchLabelProvider.getBackground(element);
		}

		public Font getFont(Object element) {
			return workbenchLabelProvider.getFont(element);
		}

		public Image getImage(Object element) {
			if (element instanceof Branches) return SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_BRANCHES_CATEGORY).createImage();
			if (element instanceof Tags) return SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_VERSIONS_CATEGORY).createImage();
			if (element instanceof Alias) {
				if (((Alias)element).isBranch())
					return SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_BRANCH).createImage();
				else
					return SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_PROJECT_VERSION).createImage();
			}
			return workbenchLabelProvider.getImage(element);
		}

		public String getText(Object element) {
			if (element instanceof Branches) return Policy.bind("ChooseUrlDialog.branches"); //$NON-NLS-1$
			if (element instanceof Tags) return Policy.bind("ChooseUrlDialog.tags"); //$NON-NLS-1$
			if (element instanceof Alias) return ((Alias)element).getName();
			return workbenchLabelProvider.getText(element);
		}

	}

	public void setIncludeBranchesAndTags(boolean includeBranchesAndTags) {
		this.includeBranchesAndTags = includeBranchesAndTags;
	}

	public String getName() {
		return name;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		this.multipleSelect = multipleSelect;
	}

	public String[] getNames() {
		return names;
	}

	public String[] getUrls() {
		return urls;
	}
}
