/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Daniel Bradby 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.properties;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.*;
import org.eclipse.ui.dialogs.*;
import org.tigris.subversion.subclipse.core.*;
import org.tigris.subversion.subclipse.core.resources.*;
import org.tigris.subversion.subclipse.ui.*;
import org.tigris.subversion.svnclientadapter.*;

public class SVNPropertyPage extends PropertyPage {

    private static final int TEXT_FIELD_WIDTH = 50;

    private Text ignoredValue;
    private Text managedValue;
    private Text hasRemoteValue;
    private Text urlValue;
    private Text lastChangedRevisionValue;
    private Text lastChangedDateValue;
    private Text lastCommitAuthorValue;
    private Text textStatusValue;
    private Text mergedValue;
    private Text deletedValue;
    private Text modifiedValue;
    private Text addedValue;
    private Text revisionValue;
    private Text copiedValue;
    private Text urlCopiedFromValue;
    private Text pathValue;

    public SVNPropertyPage() {
        super();
    }

    private void addFirstSection(Composite parent) {
        Composite composite = createDefaultComposite(parent);

        //Label for path field
        Label pathLabel = new Label(composite, SWT.NONE);
        pathLabel.setText("Path");

        // Path text field
        Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        pathValueText.setLayoutData(gd);
        pathValueText.setText(((IResource) getElement()).getFullPath().toString());
    }

    private void addSeparator(Composite parent) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        separator.setLayoutData(gridData);
    }

    private void addSecondSection(Composite parent) {
        Composite composite = createDefaultComposite(parent);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Ignored");
        ignoredValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Managed");
        managedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Has Remote");
        hasRemoteValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("URL");
        urlValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Last Changed Revision");
        lastChangedRevisionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Last Changed Date");
        lastChangedDateValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Last Commit Author");
        lastCommitAuthorValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Status");
        textStatusValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Merged");
        mergedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Deleted");
        deletedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Modified");
        modifiedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Added");
        addedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Revision");
        revisionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Copied");
        copiedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText("Path");

        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        pathValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
        pathValue.setLayoutData(gd);

        // Populate owner text field
        try {
            IResource resource = (IResource) getElement();
            SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(resource
                    .getProject(), SVNProviderPlugin.getTypeId());
            if (svnProvider == null) return;

            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
            if (svnResource == null) return;

            ISVNStatus status = svnResource.getStatus();

            if (status.getUrlCopiedFrom() != null) {

                label = new Label(composite, SWT.NONE);
                label.setText("URL Copied From");
                urlCopiedFromValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
                urlCopiedFromValue.setText(status.getUrlCopiedFrom() != null ? status
                        .getUrlCopiedFrom() : "");
            }

            ignoredValue.setText(new Boolean(status.isIgnored()).toString());
            managedValue.setText(new Boolean(status.isManaged()).toString());
            hasRemoteValue.setText(new Boolean(status.isIgnored()).toString());
            urlValue.setText(status.getUrl() != null ? status.getUrl().toString() : "");
            lastChangedRevisionValue.setText(status.getLastChangedRevision() != null ? status
                    .getLastChangedRevision().toString() : "");
            lastChangedDateValue.setText(status.getLastChangedDate() != null ? status
                    .getLastChangedDate().toString() : "");
            lastCommitAuthorValue.setText(status.getLastCommitAuthor() != null ? status
                    .getLastCommitAuthor() : "");
            textStatusValue.setText(status.getTextStatus() != null ? status.getTextStatus()
                    .toString() : "");
            mergedValue.setText(new Boolean(status.isMerged()).toString());
            deletedValue.setText(new Boolean(status.isDeleted()).toString());
            modifiedValue.setText(new Boolean(status.isModified()).toString());
            addedValue.setText(new Boolean(status.isAdded()).toString());
            revisionValue.setText(status.getRevision() != null ? status.getRevision().toString()
                    : "");
            copiedValue.setText(new Boolean(status.isCopied()).toString());
            pathValue.setText(status.getPath() != null ? status.getPath() : "");

        } catch (Exception e) {
            SVNUIPlugin.log(new Status(Status.ERROR, SVNUIPlugin.ID, TeamException.UNABLE,
                    "Property Exception", e));
        }
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);

        composite.setLayoutData(data);

        addFirstSection(composite);
        addSeparator(composite);
        addSecondSection(composite);
        
        Dialog.applyDialogFont(parent);
        return composite;
    }

    private Composite createDefaultComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        return composite;
    }

    protected void performDefaults() {
    }

    public boolean performOk() {
        return true;
    }

}
