package org.tigris.subversion.subclipse.ui.authentication;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.subclipse.ui.Policy;

public class PasswordPromptDialog extends Dialog {
    private String realm;
    private String username;
    private String password;
    private boolean save;
    private boolean maySave;
    private Text userText;
    private Text passwordText;
    private Button saveButton;
    private Button okButton;
    
    private static int WIDTH = 300;

    public PasswordPromptDialog(Shell parentShell, String realm, String username, boolean maySave) {
        super(parentShell);
        this.realm = realm;
        this.username = username;
        this.maySave = maySave;
    }
    
	protected Control createDialogArea(Composite parent) {
	    Composite rtnGroup = (Composite)super.createDialogArea(parent);
	    getShell().setText(Policy.bind("PasswordPromptDialog.title")); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		rtnGroup.setLayout(layout);
		rtnGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));	
		
		Label userLabel = new Label(rtnGroup, SWT.NONE);
		userLabel.setText(Policy.bind("PasswordPromptDialog.username")); //$NON-NLS-1$
		userText = new Text(rtnGroup, SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = WIDTH;
		userText.setLayoutData(gd);
		userText.setText(username == null? "": username);
		
		Label pwdLabel = new Label(rtnGroup, SWT.NONE);
		pwdLabel.setText(Policy.bind("PasswordPromptDialog.password")); //$NON-NLS-1$
		passwordText = new Text(rtnGroup, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = WIDTH;
		passwordText.setLayoutData(gd);
		passwordText.setEchoChar('*');
		
		if (maySave) {
		    saveButton = new Button(rtnGroup, SWT.CHECK);
		    saveButton.setText(Policy.bind("PasswordPromptDialog.save")); //$NON-NLS-1$
		    gd = new GridData();
		    gd.horizontalSpan = 2;
		    saveButton.setLayoutData(gd);
		}
		
		ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent me) {
                okButton.setEnabled((userText.getText().trim().length() > 0) ||
                 (passwordText.getText().trim().length() > 0));
            }		    
		};
		
		userText.addModifyListener(modifyListener);
		passwordText.addModifyListener(modifyListener);
		
		if (username != null) passwordText.setFocus();
		else userText.setFocus();
		
		return rtnGroup;
	}
	
	public Button createButton(Composite parent, int id, String label, boolean isDefault) {
		Button button = super.createButton(parent, id, label, isDefault);
		if (id == IDialogConstants.OK_ID) {
		    okButton = button;
		    okButton.setEnabled(false);
		}
		return button;
	}

    protected void okPressed() {
        username = userText.getText().trim();
        password = passwordText.getText().trim();
        if (maySave) save = saveButton.getSelection();
        super.okPressed();
    }
    public boolean isSave() {
        return save;
    }
    public String getPassword() {
        return password;
    }
    
    public String getUsername() {
        return username;
    }    
}
