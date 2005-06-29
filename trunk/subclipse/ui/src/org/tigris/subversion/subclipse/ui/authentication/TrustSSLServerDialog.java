package org.tigris.subversion.subclipse.ui.authentication;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.subclipse.ui.Policy;

public class TrustSSLServerDialog extends Dialog {
    private String info;
    private boolean allowPermanently;
    
    public static final int REJECT = 1;
    public static final int TEMPORARY = 0;
    public static final int PERMANENT = 2;

    public TrustSSLServerDialog(Shell parentShell, String info, boolean allowPermanently) {
        super(parentShell);
        this.info = info;
        this.allowPermanently = allowPermanently;
    }
    
	protected Control createDialogArea(Composite parent) {
	    Composite rtnGroup = (Composite)super.createDialogArea(parent);
	    getShell().setText(Policy.bind("TrustSSLServerDialog.title")); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		rtnGroup.setLayout(layout);
		rtnGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));	
		
		Label infoLabel = new Label(rtnGroup, SWT.NONE);
		infoLabel.setText(info);
	    
	    return rtnGroup;
	}

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, REJECT, Policy.bind("TrustSSLServerDialog.reject"), false); //$NON-NLS-1$
        createButton(parent, TEMPORARY, Policy.bind("TrustSSLServerDialog.temporary"), true); //$NON-NLS-1$
        if (allowPermanently) createButton(parent, PERMANENT, Policy.bind("TrustSSLServerDialog.permanent"), false); //$NON-NLS-1$        
    }
    
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}
}
