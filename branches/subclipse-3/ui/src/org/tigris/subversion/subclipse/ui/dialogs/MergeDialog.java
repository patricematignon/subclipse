package org.tigris.subversion.subclipse.ui.dialogs;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class MergeDialog extends Dialog {
    
    private static final int URL_WIDTH_HINT = 450;
    
    private static final String HEAD = "HEAD";
    
    private Combo fromUrlCombo;
    private Button fromBrowseButton;
    private Combo fromRevisionCombo;
    private Button fromHeadButton;
    private Button fromRevisionButton;

    private Button useFromUrlButton;
    private Combo toUrlCombo;
    private Button toBrowseButton;
    private Combo toRevisionCombo;
    private Button toHeadButton;
    private Button toRevisionButton;
    
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
		
		fromUrlCombo = new Combo(fromGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		fromUrlCombo.setLayoutData(data);
		fromUrlCombo.add(urlText.getText());
		fromUrlCombo.setText(urlText.getText());
		
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
		
		fromRevisionCombo = new Combo(fromRevisionComposite, SWT.BORDER);
		fromRevisionCombo.add(HEAD);
		fromRevisionCombo.setText(HEAD);
		
		SelectionListener fromListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (fromHeadButton.getSelection()) fromRevisionCombo.setText(HEAD);
                fromRevisionCombo.setEnabled(fromRevisionButton.getSelection());
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
		
		toUrlCombo = new Combo(toGroup, SWT.BORDER);
		data = new GridData();
		data.widthHint = URL_WIDTH_HINT;
		toUrlCombo.setLayoutData(data);
		toUrlCombo.add(urlText.getText());
		toUrlCombo.setText(urlText.getText());
		toUrlCombo.setEnabled(false);
		
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
		
		toRevisionCombo = new Combo(toRevisionComposite, SWT.BORDER);
		toRevisionCombo.add(HEAD);
		toRevisionCombo.setText(HEAD);
		
		SelectionListener toListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (toHeadButton.getSelection()) toRevisionCombo.setText(HEAD);
                toRevisionCombo.setEnabled(toRevisionButton.getSelection());
            }
		};
		
		toHeadButton.addSelectionListener(toListener);
		toRevisionButton.addSelectionListener(toListener);
		
		SelectionListener browseListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ChooseUrlDialog dialog = new ChooseUrlDialog(getShell(), resource);
                if ((dialog.open() == ChooseUrlDialog.OK) && (dialog.getUrl() != null)) {
                    if (e.getSource() == fromBrowseButton) {
                        fromUrlCombo.setText(dialog.getUrl());
                        if (useFromUrlButton.getSelection()) toUrlCombo.setText(dialog.getUrl());
                    } else toUrlCombo.setText(dialog.getUrl());
                }               
            }
		};
		
		fromBrowseButton.addSelectionListener(browseListener);
		toBrowseButton.addSelectionListener(browseListener);
		
		useFromUrlButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (useFromUrlButton.getSelection()) toUrlCombo.setText(fromUrlCombo.getText());
                toBrowseButton.setEnabled(!useFromUrlButton.getSelection());
                toUrlCombo.setEnabled(!useFromUrlButton.getSelection());
            }		    
		});
		
		fromUrlCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (useFromUrlButton.getSelection()) toUrlCombo.setText(fromUrlCombo.getText());
            }		    
		});
		
		fromUrlCombo.setFocus();
		
		return composite;
	}
	
    protected void okPressed() {
        try {
            fromUrl = new SVNUrl(fromUrlCombo.getText().trim());
            if (fromHeadButton.getSelection() || fromRevisionCombo.getText().trim().equalsIgnoreCase(HEAD)) fromRevision = SVNRevision.HEAD;
            else {
                try {
                    fromRevision = SVNRevision.getRevision(fromRevisionCombo.getText().trim());
                } catch (ParseException e1) {
                  MessageDialog.openError(getShell(), Policy.bind("MergeDialog.title"), Policy.bind("MergeDialog.invalidFrom"));
                  return;   
                }
            }
            toUrl = new SVNUrl(toUrlCombo.getText().trim());
            if (toHeadButton.getSelection() || toRevisionCombo.getText().trim().equalsIgnoreCase(HEAD)) toRevision = SVNRevision.HEAD;
            else {
                try {
                    toRevision = SVNRevision.getRevision(toRevisionCombo.getText().trim());
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
