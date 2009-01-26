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
package org.eclipse.team.svn.ui.internal.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.ISVNProperty;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.resources.LocalResourceStatus;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.IHelpContextIds;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class SVNPropertyPage extends PropertyPage {

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
    private Text lockOwner;
    private Text lockCreationDate;
    private Text lockComment;
   

    public SVNPropertyPage() {
        super();
    }

    private void addFirstSection(Composite parent) {
        Composite composite = createDefaultComposite(parent);

        //Label for path field
        Label pathLabel = new Label(composite, SWT.NONE);
        pathLabel.setText(Policy.bind("SVNPropertyPage.path")); //$NON-NLS-1$

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
        label.setText(Policy.bind("SVNPropertyPage.ignored")); //$NON-NLS-1$
        ignoredValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.managed")); //$NON-NLS-1$
        managedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.hasRemote")); //$NON-NLS-1$
        hasRemoteValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.url")); //$NON-NLS-1$
        urlValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.changedRevision")); //$NON-NLS-1$
        lastChangedRevisionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.changedDate")); //$NON-NLS-1$
        lastChangedDateValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.changedAuthor")); //$NON-NLS-1$
        lastCommitAuthorValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.status")); //$NON-NLS-1$
        textStatusValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.merged")); //$NON-NLS-1$
        mergedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.deleted")); //$NON-NLS-1$
        deletedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.modified")); //$NON-NLS-1$
        modifiedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.added")); //$NON-NLS-1$
        addedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.revision")); //$NON-NLS-1$
        revisionValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.copied")); //$NON-NLS-1$
        copiedValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.lockOwner"));  //$NON-NLS-1$
        lockOwner = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.lockCreationDate"));  //$NON-NLS-1$
        lockCreationDate = new Text(composite, SWT.WRAP | SWT.READ_ONLY);

        label = new Label(composite, SWT.NONE);
        label.setText(Policy.bind("SVNPropertyPage.lockComment"));  //$NON-NLS-1$
        lockComment = new Text(composite, SWT.MULTI | SWT.READ_ONLY);

        // Populate owner text field
        try {
            IResource resource = (IResource) getElement();
            SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(resource
                    .getProject(), SVNProviderPlugin.getTypeId());
            if (svnProvider == null) return;

            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
            if (svnResource == null) return;

            LocalResourceStatus status = svnResource.getStatus();
            SVNRevision revision = svnResource.getRevision();

            if (status.getUrlCopiedFrom() != null) {

                label = new Label(composite, SWT.NONE);
                label.setText(Policy.bind("SVNPropertyPage.copiedFrom")); //$NON-NLS-1$
                urlCopiedFromValue = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
                urlCopiedFromValue.setText(status.getUrlCopiedFrom() != null ? status
                        .getUrlCopiedFrom().toString() : ""); //$NON-NLS-1$
            }

            ignoredValue.setText(new Boolean(status.isIgnored()).toString());
            managedValue.setText(new Boolean(status.isManaged()).toString());
            hasRemoteValue.setText(new Boolean(status.isIgnored()).toString());
            urlValue.setText(status.getUrlString() != null ? status.getUrlString() : ""); //$NON-NLS-1$
            lastChangedRevisionValue.setText(status.getLastChangedRevision() != null ? status
                    .getLastChangedRevision().toString() : ""); //$NON-NLS-1$
            lastChangedDateValue.setText(status.getLastChangedDate() != null ? status
                    .getLastChangedDate().toString() : ""); //$NON-NLS-1$
            lastCommitAuthorValue.setText(status.getLastCommitAuthor() != null ? status
                    .getLastCommitAuthor() : ""); //$NON-NLS-1$
            textStatusValue.setText(status.getTextStatus() != null ? status.getTextStatus()
                    .toString() : ""); //$NON-NLS-1$
            mergedValue.setText(new Boolean(status.isTextMerged()).toString());
            deletedValue.setText(new Boolean(status.isDeleted()).toString());
            modifiedValue.setText(new Boolean(status.isTextModified()).toString());
            addedValue.setText(new Boolean(status.isAdded()).toString());
            revisionValue.setText(revision != null ? revision.toString() : ""); //$NON-NLS-1$
            copiedValue.setText(new Boolean(status.isCopied()).toString());
            lockOwner.setText(status.getLockOwner() != null ? status.getLockOwner() : ""); //$NON-NLS-1$
            lockCreationDate.setText(status.getLockOwner() != null ? status
                    .getLockCreationDate().toString() : ""); //$NON-NLS-1$
            lockComment.setText(status.getLockOwner() != null ? status.getLockComment() : ""); //$NON-NLS-1$
            // Get lock information from server if svn:needs-lock property is set
            if (status.getLockOwner() == null && status.getUrlString() != null) {
           		ISVNProperty prop = svnResource.getSvnProperty("svn:needs-lock");
           		if (prop != null) {
	           	    ISVNClientAdapter client = svnResource.getRepository().getSVNClient();
	            	try {
	            		ISVNInfo info = client.getInfo(status.getUrl());
	                    lockOwner.setText(info.getLockOwner() != null ? info.getLockOwner() : ""); //$NON-NLS-1$
	                    lockCreationDate.setText(info.getLockOwner() != null ? info
	                            .getLockCreationDate().toString() : ""); //$NON-NLS-1$
	                    lockComment.setText(info.getLockOwner() != null ? info.getLockComment() : ""); //$NON-NLS-1$
	            	} catch (Exception e) {
	            	}
           		}
            }
        } catch (Exception e) {
            SVNUIPlugin.log(new Status(IStatus.ERROR, SVNUIPlugin.ID, TeamException.UNABLE,
                    "Property Exception", e)); //$NON-NLS-1$
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
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.SVN_RESOURCE_PROPERTIES_PAGE);

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