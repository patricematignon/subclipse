package org.tigris.subversion.subclipse.ui.dialogs;

import java.net.MalformedURLException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.comments.CommitCommentArea;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class BranchTagDialog extends Dialog {
    
    private static final int URL_WIDTH_HINT = 450;
    
    private IResource resource;
    
    private Text toUrlText;
    private Button serverButton;
    private CommitCommentArea commitCommentArea;
    
    private SVNUrl url;
    private SVNUrl toUrl;
    private boolean createOnServer;
    private String comment;

    public BranchTagDialog(Shell parentShell, IResource resource) {
        super(parentShell);
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);
		commitCommentArea = new CommitCommentArea(this, null, Policy.bind("BranchTagDialog.enterComment"));       
        this.resource = resource;
    }
    
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("BranchTagDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group repositoryGroup = new Group(composite, SWT.NULL);
		repositoryGroup.setText(Policy.bind("BranchTagDialog.repository"));
		repositoryGroup.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		repositoryGroup.setLayoutData(data);
		
		Label fromUrlLabel = new Label(repositoryGroup, SWT.NONE);
		fromUrlLabel.setText(Policy.bind("BranchTagDialog.url")); //$NON-NLS-1$
		
		Text urlText = new Text(repositoryGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		urlText.setLayoutData(data);
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
            url = svnResource.getStatus().getUrl();
            if (url != null) urlText.setText(svnResource.getStatus().getUrl().toString());
        } catch (SVNException e1) {}
        urlText.setEditable(false);
        
		Label toUrlLabel = new Label(repositoryGroup, SWT.NONE);
		toUrlLabel.setText(Policy.bind("BranchTagDialog.toUrl")); //$NON-NLS-1$   
		
		Composite urlComposite = new Composite(repositoryGroup, SWT.NULL);
		GridLayout urlLayout = new GridLayout();
		urlLayout.numColumns = 2;
		urlLayout.marginWidth = 0;
		urlLayout.marginHeight = 0;
		urlComposite.setLayout(urlLayout);
		data = new GridData(GridData.FILL_BOTH);
		urlComposite.setLayoutData(data);
		
		toUrlText = new Text(urlComposite, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		toUrlText.setLayoutData(data);
		toUrlText.setText(urlText.getText());
		
		Button browseButton = new Button(urlComposite, SWT.PUSH);
		browseButton.setText(Policy.bind("SwitchDialog.browse"));
		browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ChooseUrlDialog dialog = new ChooseUrlDialog(getShell(), resource);
                if ((dialog.open() == ChooseUrlDialog.OK) && (dialog.getUrl() != null)) {
                    toUrlText.setText(dialog.getUrl());
                }
            }
		});	
		
		Composite serverComposite = new Composite(repositoryGroup, SWT.NULL);
		GridLayout serverLayout = new GridLayout();
		serverLayout.numColumns = 2;
		serverLayout.marginWidth = 0;
		serverLayout.marginHeight = 0;
		serverComposite.setLayout(serverLayout);
		data = new GridData(GridData.FILL_BOTH);
		serverComposite.setLayoutData(data);	
		
		serverButton = new Button(serverComposite, SWT.CHECK);
		serverButton.setSelection(true);
		Label serverLabel = new Label(serverComposite, SWT.NONE);
		serverLabel.setText(Policy.bind("BranchTagDialog.server")); //$NON-NLS-1$  
		
		Label label = createWrappingLabel(composite);
		label.setText(Policy.bind("BranchTagDialog.note")); //$NON-NLS-1$  
		
		commitCommentArea.createArea(composite);
		commitCommentArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CommitCommentArea.OK_REQUESTED)
					okPressed();
			}
		});
		
		toUrlText.setFocus();

		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.BRANCH_TAG_DIALOG);
		
		return composite;
	}
	
    protected void okPressed() {
        createOnServer = serverButton.getSelection();
        comment = commitCommentArea.getComment();
        try {
            toUrl = new SVNUrl(toUrlText.getText().trim());
        } catch (MalformedURLException e) {
            MessageDialog.openError(getShell(), Policy.bind("BranchTagDialog.title"), e.getMessage());
            return;
        }
        super.okPressed();
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

    public String getComment() {
        return comment;
    }
    public boolean isCreateOnServer() {
        return createOnServer;
    }
    public SVNUrl getToUrl() {
        return toUrl;
    }
    public SVNUrl getUrl() {
        return url;
    }
}
