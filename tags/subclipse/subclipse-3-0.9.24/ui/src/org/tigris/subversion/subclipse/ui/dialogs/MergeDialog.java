package org.tigris.subversion.subclipse.ui.dialogs;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.help.WorkbenchHelp;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class MergeDialog extends Dialog {
    
    private static final int URL_WIDTH_HINT = 450;
    private static final int REVISION_WIDTH_HINT = 40;
    
    private Text fromUrlText;
    private Button fromBrowseButton;
    private Text fromRevisionText;
    private Button fromHeadButton;
    private Button fromRevisionButton;

    private Button useFromUrlButton;
    private Text toUrlText;
    private Button toBrowseButton;
    private Text toRevisionText;
    private Button toHeadButton;
    private Button toRevisionButton;
    
    private Button okButton;
    
    private IResource resource;
    
    private SVNUrl fromUrl;
    private SVNRevision fromRevision;
    private SVNUrl toUrl;
    private SVNRevision toRevision;

    public MergeDialog(Shell parentShell, IResource resource) {
        super(parentShell);
        this.resource = resource;
    }
    
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("MergeDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Policy.bind("MergeDialog.url"));
		
		Text urlText = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		urlText.setLayoutData(data);
		urlText.setEditable(false);
		
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
            SVNUrl svnUrl = svnResource.getStatus().getUrl();
            if (svnUrl != null) urlText.setText(svnResource.getStatus().getUrl().toString());
        } catch (SVNException e1) {}
        
		Group fromGroup = new Group(composite, SWT.NULL);
		fromGroup.setText(Policy.bind("MergeDialog.from"));
		GridLayout fromLayout = new GridLayout();
		fromLayout.numColumns = 2;
		fromGroup.setLayout(fromLayout);
		
		fromUrlText = new Text(fromGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		fromUrlText.setLayoutData(data);
		fromUrlText.setText(urlText.getText());
		
		fromBrowseButton = new Button(fromGroup, SWT.PUSH);
		fromBrowseButton.setText(Policy.bind("SwitchDialog.browse"));
		
		Composite fromRevisionComposite = new Composite(fromGroup, SWT.NULL);
		GridLayout fromRevisionLayout = new GridLayout();
		fromRevisionLayout.numColumns = 3;
		fromRevisionComposite.setLayout(fromRevisionLayout);
		data = new GridData(GridData.FILL_BOTH);
		fromRevisionComposite.setLayoutData(data);
		
		fromHeadButton = new Button(fromRevisionComposite, SWT.RADIO);
		Label fromHeadLabel = new Label(fromRevisionComposite, SWT.NONE);
		fromHeadLabel.setText(Policy.bind("SwitchDialog.head"));
		data = new GridData();
		data.horizontalSpan = 2;
		fromHeadLabel.setLayoutData(data);
		
		fromRevisionButton = new Button(fromRevisionComposite, SWT.RADIO);
		Label fromRevisionLabel = new Label(fromRevisionComposite, SWT.NONE);
		fromRevisionLabel.setText(Policy.bind("SwitchDialog.revision"));
		
		fromRevisionButton.setSelection(true);
		
		fromRevisionText = new Text(fromRevisionComposite, SWT.BORDER);
		data = new GridData();
		data.widthHint = REVISION_WIDTH_HINT;
		fromRevisionText.setLayoutData(data);
		
		SelectionListener fromListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                fromRevisionText.setEnabled(fromRevisionButton.getSelection());
                setOkButtonStatus();
                if (fromRevisionButton.getSelection()) {
                    fromRevisionText.selectAll();
                    fromRevisionText.setFocus();
                }               
            }
		};
		
		fromHeadButton.addSelectionListener(fromListener);
		fromRevisionButton.addSelectionListener(fromListener);		
		
		Group toGroup = new Group(composite, SWT.NULL);
		toGroup.setText(Policy.bind("MergeDialog.to"));
		GridLayout toLayout = new GridLayout();
		toLayout.numColumns = 2;
		toGroup.setLayout(toLayout);
		
		Composite useFromComposite = new Composite(toGroup, SWT.NULL);
		GridLayout useFromLayout = new GridLayout();
		useFromLayout.numColumns = 2;
		useFromComposite.setLayout(useFromLayout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		useFromComposite.setLayoutData(data);
		
		useFromUrlButton = new Button(useFromComposite, SWT.CHECK);
		useFromUrlButton.setSelection(true);
		
		Label useFromLabel = new Label(useFromComposite, SWT.NONE);
		useFromLabel.setText(Policy.bind("MergeDialog.useFrom"));
		
		toUrlText = new Text(toGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		toUrlText.setLayoutData(data);
		toUrlText.setText(urlText.getText());
		toUrlText.setEnabled(false);
		
		toBrowseButton = new Button(toGroup, SWT.PUSH);
		toBrowseButton.setText(Policy.bind("SwitchDialog.browse"));
		toBrowseButton.setEnabled(false);
		
		Composite toRevisionComposite = new Composite(toGroup, SWT.NULL);
		GridLayout toRevisionLayout = new GridLayout();
		toRevisionLayout.numColumns = 3;
		toRevisionComposite.setLayout(toRevisionLayout);
		data = new GridData(GridData.FILL_BOTH);
		toRevisionComposite.setLayoutData(data);
		
		toHeadButton = new Button(toRevisionComposite, SWT.RADIO);
		Label toHeadLabel = new Label(toRevisionComposite, SWT.NONE);
		toHeadLabel.setText(Policy.bind("SwitchDialog.head"));
		data = new GridData();
		data.horizontalSpan = 2;
		toHeadLabel.setLayoutData(data);
		
		toRevisionButton = new Button(toRevisionComposite, SWT.RADIO);
		Label toRevisionLabel = new Label(toRevisionComposite, SWT.NONE);
		toRevisionLabel.setText(Policy.bind("SwitchDialog.revision"));
		
		toRevisionButton.setSelection(true);
		
		toRevisionText = new Text(toRevisionComposite, SWT.BORDER);
		data = new GridData();
		data.widthHint = REVISION_WIDTH_HINT;
		toRevisionText.setLayoutData(data);
		
		SelectionListener toListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                toRevisionText.setEnabled(toRevisionButton.getSelection());
                setOkButtonStatus();
                if (toRevisionButton.getSelection()) {
                    toRevisionText.selectAll();
                    toRevisionText.setFocus();
                }                             
            }
		};
		
		toHeadButton.addSelectionListener(toListener);
		toRevisionButton.addSelectionListener(toListener);
		
		SelectionListener browseListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ChooseUrlDialog dialog = new ChooseUrlDialog(getShell(), resource);
                if ((dialog.open() == ChooseUrlDialog.OK) && (dialog.getUrl() != null)) {
                    if (e.getSource() == fromBrowseButton) {
                        fromUrlText.setText(dialog.getUrl());
                        if (useFromUrlButton.getSelection()) toUrlText.setText(dialog.getUrl());
                    } else toUrlText.setText(dialog.getUrl());
                    setOkButtonStatus();
                }               
            }
		};
		
		fromBrowseButton.addSelectionListener(browseListener);
		toBrowseButton.addSelectionListener(browseListener);
		
		useFromUrlButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (useFromUrlButton.getSelection()) toUrlText.setText(fromUrlText.getText());
                toBrowseButton.setEnabled(!useFromUrlButton.getSelection());
                toUrlText.setEnabled(!useFromUrlButton.getSelection());
                setOkButtonStatus();
            }		    
		});
		
		fromUrlText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (useFromUrlButton.getSelection()) toUrlText.setText(fromUrlText.getText());
            }		    
		});
		
		ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOkButtonStatus();
            }		    
		};
		
		fromUrlText.addModifyListener(modifyListener);
		fromRevisionText.addModifyListener(modifyListener);
		toUrlText.addModifyListener(modifyListener);
		toRevisionText.addModifyListener(modifyListener);
		
		fromUrlText.setFocus();
		WorkbenchHelp.setHelp(composite, IHelpContextIds.MERGE_DIALOG);

		return composite;
	}
	
    protected void okPressed() {
        try {
            fromUrl = new SVNUrl(fromUrlText.getText().trim());
            if (fromHeadButton.getSelection()) fromRevision = SVNRevision.HEAD;
            else {
                try {
                    fromRevision = SVNRevision.getRevision(fromRevisionText.getText().trim());
                } catch (ParseException e1) {
                  MessageDialog.openError(getShell(), Policy.bind("MergeDialog.title"), Policy.bind("MergeDialog.invalidFrom"));
                  return;   
                }
            }
            toUrl = new SVNUrl(toUrlText.getText().trim());
            if (toHeadButton.getSelection()) toRevision = SVNRevision.HEAD;
            else {
                try {
                    toRevision = SVNRevision.getRevision(toRevisionText.getText().trim());
                } catch (ParseException e1) {
                  MessageDialog.openError(getShell(), Policy.bind("MergeDialog.title"), Policy.bind("MergeDialog.invalidTo"));
                  return;   
                }
            }            
        } catch (MalformedURLException e) {
            MessageDialog.openError(getShell(), Policy.bind("MergeDialog.title"), e.getMessage());
            return;
        }
        super.okPressed();
    }
    
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
		    okButton = button;
		    okButton.setEnabled(false);
		}
        return button;
    }
    
    private void setOkButtonStatus() {
        boolean canFinish = true;
        if (!fromHeadButton.getSelection() && (fromRevisionText.getText().trim().length() == 0)) canFinish = false;
        else if (!toHeadButton.getSelection() && (toRevisionText.getText().trim().length() == 0)) canFinish = false;
        okButton.setEnabled(canFinish);
    }

    public SVNRevision getFromRevision() {
        return fromRevision;
    }
    public SVNUrl getFromUrl() {
        return fromUrl;
    }
    public SVNRevision getToRevision() {
        return toRevision;
    }
    public SVNUrl getToUrl() {
        return toUrl;
    }
}
