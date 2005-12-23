package org.tigris.subversion.subclipse.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.subclipse.ui.Policy;

public class RelocateWizardUrlPage extends WizardPage {
	private String url;
	private Text newUrlText;

	public RelocateWizardUrlPage(String pageName, String title, ImageDescriptor titleImage, String url) {
		super(pageName, title, titleImage);
		this.url = url;
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Label urlLabel = new Label(outerContainer, SWT.NONE);
		urlLabel.setText(Policy.bind("RelocateWizard.urlLabel")); //$NON-NLS-1$
		Text urlText = new Text(outerContainer, SWT.BORDER);
		urlText.setEditable(false);
		GridData data = new GridData();
		data.widthHint = 300;
		urlText.setLayoutData(data);
		urlText.setText(url);
		
		Label newUrlLabel = new Label(outerContainer, SWT.NONE);
		newUrlLabel.setText(Policy.bind("RelocateWizard.newUrlLabel")); //$NON-NLS-1$
		newUrlText = new Text(outerContainer, SWT.BORDER);
		data = new GridData();
		data.widthHint = 300;
		newUrlText.setLayoutData(data);
		newUrlText.setText(url);
		
		newUrlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(newUrlText.getText().trim().length() > 0 && !newUrlText.getText().trim().equals(url));
			}			
		});

		setMessage(Policy.bind("RelocateWizard.newUrl")); //$NON-NLS-1$
		
		setControl(outerContainer);			
	}
	
	public String getNewUrl() {
		return newUrlText.getText().trim();
	}

}
