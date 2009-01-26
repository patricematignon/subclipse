package org.eclipse.team.svn.ui.internal.dialogs;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.history.ILogEntry;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.IHelpContextIds;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.util.UrlCombo;
import org.eclipse.ui.PlatformUI;

public class SwitchDialog extends TrayDialog {
    
    private static final int REVISION_WIDTH_HINT = 40;
 
    private IResource resource;
    
    private UrlCombo urlCombo;
    private Text revisionText;
    private Button logButton;
    private Button headButton;
    private Button revisionButton;
    
    private Button okButton;
    
    private SVNUrl url;
    private SVNRevision revision;

    public SwitchDialog(Shell parentShell, IResource resource) {
        super(parentShell);
        this.resource = resource;
    }
    
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("SwitchDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label urlLabel = new Label(composite, SWT.NONE);
		urlLabel.setText(Policy.bind("SwitchDialog.url")); //$NON-NLS-1$
		
		urlCombo = new UrlCombo(composite, resource.getProject().getName());
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
            String anUrl = svnResource.getStatus().getUrlString();
            if (anUrl != null) urlCombo.setText(anUrl);
        } catch (SVNException e1) {}
        urlCombo.getCombo().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOkButtonStatus();
            }         
        });
		
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Policy.bind("SwitchDialog.browse")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ChooseUrlDialog dialog = new ChooseUrlDialog(getShell(), resource);
                if ((dialog.open() == ChooseUrlDialog.OK) && (dialog.getUrl() != null)) {
                    urlCombo.setText(dialog.getUrl());
                    setOkButtonStatus();
                }
            }
		});

		Group revisionGroup = new Group(composite, SWT.NULL);
		revisionGroup.setText(Policy.bind("SwitchDialog.revision")); //$NON-NLS-1$
		GridLayout revisionLayout = new GridLayout();
		revisionLayout.numColumns = 3;
		revisionGroup.setLayout(revisionLayout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		revisionGroup.setLayoutData(data);
		
		headButton = new Button(revisionGroup, SWT.RADIO);
		headButton.setText(Policy.bind("SwitchDialog.head")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 3;
		headButton.setLayoutData(data);
		
		revisionButton = new Button(revisionGroup, SWT.RADIO);
		revisionButton.setText(Policy.bind("SwitchDialog.revision")); //$NON-NLS-1$
		
		headButton.setSelection(true);
		
		revisionText = new Text(revisionGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = REVISION_WIDTH_HINT;
		revisionText.setLayoutData(data);
		revisionText.setEnabled(false);
		
		revisionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOkButtonStatus();
            }		    
		});
		
		logButton = new Button(revisionGroup, SWT.PUSH);
		logButton.setText(Policy.bind("MergeDialog.showLog")); //$NON-NLS-1$
		logButton.setEnabled(false);
		logButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showLog();
            }
		});	
		
		SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                revisionText.setEnabled(revisionButton.getSelection());
                logButton.setEnabled(revisionButton.getSelection());
                setOkButtonStatus();
                if (revisionButton.getSelection()) {
                    revisionText.selectAll();
                    revisionText.setFocus();
                }
            }
		};
		
		headButton.addSelectionListener(listener);
		revisionButton.addSelectionListener(listener);

		// Add F1 help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.SWITCH_DIALOG);
		
		return composite;
	}

	protected void showLog() {
	    ISVNRemoteResource remoteResource = null;
        try {
            remoteResource = SVNWorkspaceRoot.getSVNResourceFor(resource).getRepository().getRemoteFile(new SVNUrl(urlCombo.getText()));
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Policy.bind("MergeDialog.showLog"), e.toString()); //$NON-NLS-1$
            return;
        }
        if (remoteResource == null) {
            MessageDialog.openError(getShell(), Policy.bind("MergeDialog.showLog"), Policy.bind("MergeDialog.urlError") + " " + urlCombo.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return;	            
        }	
        HistoryDialog dialog = new HistoryDialog(getShell(), remoteResource);
        if (dialog.open() == HistoryDialog.CANCEL) return;
        ILogEntry[] selectedEntries = dialog.getSelectedLogEntries();
        if (selectedEntries.length == 0) return;
        revisionText.setText(Long.toString(selectedEntries[selectedEntries.length - 1].getRevision().getNumber()));
        setOkButtonStatus();
    }
	
    protected void okPressed() {
        urlCombo.saveUrl();
        try {
            url = new SVNUrl(urlCombo.getText());
            if (headButton.getSelection()) revision = SVNRevision.HEAD;
            else {
                try {
                    revision = SVNRevision.getRevision(revisionText.getText().trim());
                } catch (ParseException e1) {
                  MessageDialog.openError(getShell(), Policy.bind("SwitchDialog.title"), Policy.bind("SwitchDialog.invalid")); //$NON-NLS-1$ //$NON-NLS-2$
                  return;   
                }
            }
        } catch (MalformedURLException e) {
            MessageDialog.openError(getShell(), Policy.bind("SwitchDialog.title"), e.getMessage()); //$NON-NLS-1$
            return;
        }
        super.okPressed();
    }

    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) okButton = button;    
        return button;
    }
    
    private void setOkButtonStatus() {
        okButton.setEnabled((urlCombo.getText().length() > 0) && (headButton.getSelection() || (revisionText.getText().trim().length() > 0)));
    }
    
    public SVNRevision getRevision() {
        return revision;
    }
    public SVNUrl getUrl() {
        return url;
    }

}