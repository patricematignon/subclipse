/*
 * Created on 13 août 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.subversion.subclipse.ui.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

/**
 * @author cedric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVNRepositoryPropertiesPage extends PropertyPage {
    private ISVNRepositoryLocation location;
    private static final String FAKE_PASSWORD = "*********"; //$NON-NLS-1$
    private Text loginText;
    private Text passwordText;
    private Text customLabelText;
    private Button useUrlLabelButton;
    private Button useCustomLabelButton;
    private boolean passwordChanged;
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		initialize();
        
        GridLayout layout;
        Label label;
        Text text;
        GridData data;
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
        
        Listener labelListener = new Listener() {
            public void handleEvent(Event event) {
                updateWidgetEnablements();
            }
        };
        
        // group for label
		Composite labelGroup = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		labelGroup.setLayout(layout);		

        // use url as label
		useUrlLabelButton = new Button(labelGroup, SWT.RADIO);
		useUrlLabelButton.setText("Use the repository url as the label");
        useUrlLabelButton.addListener(SWT.Selection,labelListener);
		data = new GridData();
		data.horizontalSpan = 2;
		useUrlLabelButton.setLayoutData(data);		

        // use custom label
		useCustomLabelButton = new Button(labelGroup, SWT.RADIO);
		useCustomLabelButton.setText("Use a custom label : ");
        useCustomLabelButton.addListener(SWT.Selection,labelListener);
		data = new GridData();
		useCustomLabelButton.setLayoutData(data);
		customLabelText = new Text(labelGroup, SWT.SINGLE | SWT.BORDER);
        customLabelText.addListener(SWT.Modify, labelListener);
		data = new GridData();
        data.widthHint = 200;
        customLabelText.setLayoutData(data);
        
        // empty label to separate
        label = new Label(composite, SWT.NONE);
        
        // group for login and password
        Composite userPasswordGroup = new Composite(composite, SWT.NONE);
        userPasswordGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout();
        layout.numColumns = 2;
        userPasswordGroup.setLayout(layout);
        
        // login
        label = new Label(userPasswordGroup, SWT.NONE);
        label.setText("Login :");
        loginText = new Text(userPasswordGroup, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        loginText.setLayoutData(data);

        // password
        label = new Label(userPasswordGroup, SWT.NONE);
        label.setText("Password :");
        passwordText = new Text(userPasswordGroup, SWT.SINGLE | SWT.BORDER| SWT.PASSWORD);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        passwordText.setLayoutData(data);        
        passwordText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                passwordChanged = !passwordText.getText().equals(FAKE_PASSWORD);
            }
        });
        initializeValues();
		return composite;
	}

    
    /**
     * Updates widget enablements and sets error message if appropriate.
     */
    protected void updateWidgetEnablements() {
        if (useUrlLabelButton.getSelection()) {
            customLabelText.setEnabled(false);
        } else {
            customLabelText.setEnabled(true);
        }
        validateFields();
    }    
    
    private void validateFields() {
        if (customLabelText.isEnabled()) {
            if (customLabelText.getText().length() == 0) {
                setValid(false);
                return;
            }
        }
 
        setErrorMessage(null);
        setValid(true);
    }    

    /**
     * Set the initial values of the widgets
     */
    private void initializeValues() {
        passwordChanged = false;
        
        loginText.setText(location.getUsername());
        passwordText.setText(FAKE_PASSWORD);
        
        // get the repository label
        String label = location.getLabel();
        useUrlLabelButton.setSelection(label == null);
        useCustomLabelButton.setSelection(!useUrlLabelButton.getSelection());
        if (label == null) {
            label = location.getLocation();
        }
        customLabelText.setText(label);
    }    

    /**
     * Initializes the page
     */
    private void initialize() {
        location = null;
        IAdaptable element = getElement();
        if (element instanceof ISVNRepositoryLocation) {
            location = (ISVNRepositoryLocation)element;
        } else {
            Object adapter = element.getAdapter(ISVNRepositoryLocation.class);
            if (adapter instanceof ISVNRepositoryLocation) {
                location = (ISVNRepositoryLocation)adapter;
            }
        }
    }    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        super.performDefaults();
        initializeValues();
    }    

    /*
     * @see PreferencesPage#performOk
     */
    public boolean performOk() {
        if (passwordChanged) {
            location.setPassword(passwordText.getText());
        	passwordChanged = false;
        }
        location.setUsername(loginText.getText());
        
        if (useCustomLabelButton.getSelection()) {
        	location.setLabel(customLabelText.getText());
        } else {
        	location.setLabel(null);
        }
        
        try {
            SVNRepositories repositories = SVNProviderPlugin.getPlugin().getRepositories();
            repositories.addOrUpdateRepository(location);
		} catch (SVNException e) {
			handle(e);
            return false;
		}
        
        return true;
    }    

    /**
     * Shows the given errors to the user.
     */
    protected void handle(Throwable e) {
        SVNUIPlugin.openError(getShell(), null, null, e);
    }    
    
    
}
