package org.eclipse.team.svn.ui.internal.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.ui.internal.IHelpContextIds;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.comments.CommitCommentArea;
import org.eclipse.team.svn.ui.internal.settings.CommentProperties;
import org.eclipse.team.svn.ui.internal.settings.ProjectProperties;
import org.eclipse.ui.PlatformUI;

public class SetCommitPropertiesDialog extends TrayDialog {
    
	private CommitCommentArea commitCommentArea;
    private ProjectProperties projectProperties;
    private Text issueText;
    private Text committerText;
    private String issue;
    private String author;
    private IDialogSettings settings;
    private SVNRevision revision;
    
    private Button okButton;
    private CommentProperties commentProperties;

    public SetCommitPropertiesDialog(Shell parentShell, SVNRevision revision, IResource theResource, ProjectProperties projectProperties) {
        super(parentShell);
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);

        try {
        	if (theResource != null)
        		commentProperties = CommentProperties.getCommentProperties(theResource);
		} catch (SVNException e1) {
			// So what!
		}

		commitCommentArea = new CommitCommentArea(this, null, commentProperties);
		if ((commentProperties != null) && (commentProperties.getMinimumLogMessageSize() != 0)) {
		    ModifyListener modifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    okButton.setEnabled(commitCommentArea.getText().getText().trim().length() >= commentProperties.getMinimumLogMessageSize());
                }		        
		    };
		    commitCommentArea.setModifyListener(modifyListener);
		}
		this.revision = revision;
		this.projectProperties = projectProperties;
		settings = SVNUIPlugin.getPlugin().getDialogSettings();
    }
    
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
	    
		getShell().setText(Policy.bind("SetCommitPropertiesDialog.revisionNumber", revision.toString()));  //$NON-NLS-1$//$NON-NLS-2$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (projectProperties != null) {
		    addBugtrackingArea(composite);
		}

		commitCommentArea.createArea(composite);
		commitCommentArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CommitCommentArea.OK_REQUESTED)
					okPressed();
			}
		});
	    addCommitterName(composite);
	    if (author != null) committerText.setText(author);

		// set F1 help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.CHANGE_REVPROPS);	
		
		return composite;
	}
	
    private void addBugtrackingArea(Composite composite) {
		Composite bugtrackingComposite = new Composite(composite, SWT.NULL);
		GridLayout bugtrackingLayout = new GridLayout();
		bugtrackingLayout.numColumns = 2;
		bugtrackingComposite.setLayout(bugtrackingLayout);
		
		Label label = new Label(bugtrackingComposite, SWT.NONE);
		label.setText(projectProperties.getLabel());
		issueText = new Text(bugtrackingComposite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 150;
		issueText.setLayoutData(data);
    }

    private void addCommitterName(Composite composite) {
		Composite committerComposite = new Composite(composite, SWT.NULL);
		GridLayout committerLayout = new GridLayout();
		committerLayout.numColumns = 2;
		committerComposite.setLayout(committerLayout);
		
		Label label = new Label(committerComposite, SWT.NONE);
		label.setText(Policy.bind("SetCommitPropertiesDialog.user"));
		committerText = new Text(committerComposite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 150;
		committerText.setLayoutData(data);
    }
	
    protected void okPressed() {
        saveLocation();
        if (projectProperties != null) {
            issue = issueText.getText().trim();
            if (projectProperties.isWarnIfNoIssue() && (issueText.getText().trim().length() == 0)) {
                if (!MessageDialog.openQuestion(getShell(), Policy.bind("SetCommitPropertiesDialog.title"), Policy.bind("SetCommitPropertiesDialog.0", projectProperties.getLabel()))) { //$NON-NLS-1$ //$NON-NLS-2$
                    issueText.setFocus();
                    return; //$NON-NLS-1$
                }
            }
            if (issueText.getText().trim().length() > 0) {
                String issueError = projectProperties.validateIssue(issueText.getText().trim());
                if (issueError != null) {
                    MessageDialog.openError(getShell(), Policy.bind("SetCommitPropertiesDialog.title"), issueError); //$NON-NLS-1$
                    issueText.selectAll();
                    issueText.setFocus();
                    return;
                }
            }
        }
        if (committerText.getText().trim().length() == 0) {
            MessageDialog.openError(getShell(), Policy.bind("SetCommitPropertiesDialog.title"), Policy.bind("SetCommitPropertiesDialog.noAuthor"));  //$NON-NLS-1$ //$NON-NLS-2$
        	committerText.selectAll();
        	committerText.setFocus();
            return; //$NON-NLS-1$
        }
        author = committerText.getText().trim();

        super.okPressed();
    }
    
    protected void cancelPressed() {
        saveLocation();
        super.cancelPressed();
    }

    private void saveLocation() {
        int x = getShell().getLocation().x;
        int y = getShell().getLocation().y;
        settings.put("CommitDialog.location.x", x); //$NON-NLS-1$
        settings.put("CommitDialog.location.y", y); //$NON-NLS-1$
        x = getShell().getSize().x;
        y = getShell().getSize().y;
        settings.put("CommitDialog.size.x", x); //$NON-NLS-1$
        settings.put("CommitDialog.size.y", y); //$NON-NLS-1$   
    }

    protected Point getInitialLocation(Point initialSize) {
	    try {
	        int x = settings.getInt("CommitDialog.location.x"); //$NON-NLS-1$
	        int y = settings.getInt("CommitDialog.location.y"); //$NON-NLS-1$
	        return new Point(x, y);
	    } catch (NumberFormatException e) {}
        return super.getInitialLocation(initialSize);
    }
    
    protected Point getInitialSize() {
	    try {
	        int x = settings.getInt("CommitDialog.size.x"); //$NON-NLS-1$
	        int y = settings.getInt("CommitDialog.size.y"); //$NON-NLS-1$
	        return new Point(x, y);
	    } catch (NumberFormatException e) {}
        return super.getInitialSize();
    }	

    /**
	 * Returns the comment.
	 * @return String
	 */
	public String getComment() {
	    if ((projectProperties != null) && (issue != null) && (issue.length() > 0)) {
	        if (projectProperties.isAppend()) 
	            return commitCommentArea.getComment() + "\n" + projectProperties.getResolvedMessage(issue) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	        else
	            return projectProperties.getResolvedMessage(issue) + "\n" + commitCommentArea.getComment(); //$NON-NLS-1$
	    }
		return commitCommentArea.getComment();
	}
	
	protected Button createButton(
		Composite parent,
		int id,
		String label,
		boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button;
			if ((commentProperties != null) && (commentProperties.getMinimumLogMessageSize() != 0)) {
				okButton.setEnabled(false);
			}
		}
		return button;
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

	public void setOldComment(String comment) {
		commitCommentArea.setOldComment(comment);
	}

	public void setOldAuthor(String oldAuthor) {
		this.author = oldAuthor;
	}

	public String getAuthor() {
		return author;
	}
}
