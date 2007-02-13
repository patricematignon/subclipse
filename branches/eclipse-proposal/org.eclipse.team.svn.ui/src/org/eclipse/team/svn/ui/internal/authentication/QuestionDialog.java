package org.eclipse.team.svn.ui.internal.authentication;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.ui.internal.IHelpContextIds;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.PlatformUI;

public class QuestionDialog extends TrayDialog {
    private String realm;
    private String question;
    private boolean showAnswer;
    private boolean maySave;
    private Text answerText;
    private String answer;
    private boolean save;
    private Button saveButton;
    
    private static final int WIDTH = 300;

    public QuestionDialog(Shell parentShell, String realm, String question, 
            boolean showAnswer, boolean maySave) {
        super(parentShell);
        this.realm = realm;
        this.question = question;
        this.showAnswer = showAnswer;
        this.maySave = maySave;
    }
    
	protected Control createDialogArea(Composite parent) {
	    Composite rtnGroup = (Composite)super.createDialogArea(parent);
	    getShell().setText(Policy.bind("SVNPromptUserPassword.authentication")); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		rtnGroup.setLayout(layout);
		rtnGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		Label realmLabel = new Label(rtnGroup, SWT.NONE);
		realmLabel.setText(Policy.bind("PasswordPromptDialog.repository")); //$NON-NLS-1$
		Text realmText = new Text(rtnGroup, SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = WIDTH;
		realmText.setLayoutData(gd);
		realmText.setEditable(false);
		realmText.setText(realm);
		
		Label questionLabel = new Label(rtnGroup, SWT.NONE);
		questionLabel.setText(question);
		
		if (showAnswer) {
			answerText = new Text(rtnGroup, SWT.NONE);
			gd = new GridData();
			gd.widthHint = WIDTH;
			answerText.setLayoutData(gd);
		}
		
		if (maySave) {
		    saveButton = new Button(rtnGroup, SWT.CHECK);
		    saveButton.setText(Policy.bind("QuestionDialog.save")); //$NON-NLS-1$
		    gd = new GridData();
		    gd.horizontalSpan = 2;
		    saveButton.setLayoutData(gd);
		}
		
		// set F1 help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(rtnGroup, IHelpContextIds.QUESTION_DIALOG);	

		if (showAnswer)
			answerText.setFocus();
		
		return rtnGroup;
	}
	
    protected void okPressed() {
    	if (showAnswer)	answer = answerText.getText().trim();
        if (maySave) save = saveButton.getSelection();
        super.okPressed();
    }	

    public String getAnswer() {
        return answer;
    }
    public boolean isSave() {
        return save;
    }
}
