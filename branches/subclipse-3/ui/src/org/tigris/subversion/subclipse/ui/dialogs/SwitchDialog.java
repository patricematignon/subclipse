package org.tigris.subversion.subclipse.ui.dialogs;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class SwitchDialog extends Dialog {
    
    private static final int URL_WIDTH_HINT = 450;
    
    private static final String HEAD = "HEAD";
    
    private IResource resource;
    
    private Combo urlCombo;
    private Combo revisionCombo;
    private Button headButton;
    private Button revisionButton;
    
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
		urlLabel.setText(Policy.bind("SwitchDialog.url"));
		
		urlCombo = new Combo(composite, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		urlCombo.setLayoutData(data);
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
            SVNUrl svnUrl = svnResource.getStatus().getUrl();
            if (svnUrl != null) urlCombo.setText(svnResource.getStatus().getUrl().toString());
        } catch (SVNException e1) {}
		
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Policy.bind("SwitchDialog.browse"));
		browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ChooseUrlDialog dialog = new ChooseUrlDialog(getShell(), resource);
                if ((dialog.open() == ChooseUrlDialog.OK) && (dialog.getUrl() != null)) {
                    urlCombo.setText(dialog.getUrl());
                }
            }
		});

		Group revisionGroup = new Group(composite, SWT.NULL);
		revisionGroup.setText(Policy.bind("SwitchDialog.revision"));
		GridLayout revisionLayout = new GridLayout();
		revisionLayout.numColumns = 3;
		revisionGroup.setLayout(revisionLayout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		revisionGroup.setLayoutData(data);
		
		headButton = new Button(revisionGroup, SWT.RADIO);
		Label headLabel = new Label(revisionGroup, SWT.NONE);
		headLabel.setText(Policy.bind("SwitchDialog.head"));
		data = new GridData();
		data.horizontalSpan = 2;
		headLabel.setLayoutData(data);
		
		revisionButton = new Button(revisionGroup, SWT.RADIO);
		Label revisionLabel = new Label(revisionGroup, SWT.NONE);
		revisionLabel.setText(Policy.bind("SwitchDialog.revision"));
		
		headButton.setSelection(true);
		
		revisionCombo = new Combo(revisionGroup, SWT.BORDER);
		revisionCombo.add(HEAD);
		revisionCombo.setText(HEAD);
		revisionCombo.setEnabled(false);
		
		SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (headButton.getSelection()) revisionCombo.setText(HEAD);
                revisionCombo.setEnabled(revisionButton.getSelection());
            }
		};
		
		headButton.addSelectionListener(listener);
		revisionButton.addSelectionListener(listener);
		
		return composite;
	}
	
    protected void okPressed() {
        try {
            url = new SVNUrl(urlCombo.getText().trim());
            if (headButton.getSelection() || revisionCombo.getText().trim().equalsIgnoreCase(HEAD)) revision = SVNRevision.HEAD;
            else {
                try {
                    revision = SVNRevision.getRevision(revisionCombo.getText().trim());
                } catch (ParseException e1) {
                  MessageDialog.openError(getShell(), Policy.bind("SwitchDialog.title"), Policy.bind("SwitchDialog.invalid"));
                  return;   
                }
            }
        } catch (MalformedURLException e) {
            MessageDialog.openError(getShell(), Policy.bind("SwitchDialog.title"), e.getMessage());
            return;
        }
        super.okPressed();
    }

    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        return super.createButton(parent, id, label, defaultButton);
    }
    
    public SVNRevision getRevision() {
        return revision;
    }
    public SVNUrl getUrl() {
        return url;
    }

}
