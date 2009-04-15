/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.wizards.sharing;


import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.core.util.Util;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.wizards.ConfigurationWizardMainPage;
import org.tigris.subversion.svnclientadapter.ISVNStatus;

/**
 * This wizard helps the user to import a new project in their workspace
 * into a SVN repository for the first time.
 */
public class SharingWizard extends Wizard implements IConfigurationWizard {
	// The project to configure
	private IProject project;

	// The autoconnect page is used if .svn/ directories already exist.
	private ConfigurationWizardAutoconnectPage autoconnectPage;
	
	// The import page is used if .svn/ directories do not exist.
	private RepositorySelectionPage locationPage;
	
	// The page that prompts the user for connection information.
	private ConfigurationWizardMainPage createLocationPage;
	
	// The page that prompts the user for module name.
	private DirectorySelectionPage directoryPage;
	
	// The page that tells the user what's going to happen.
	private SharingWizardFinishPage finishPage;
	
	public SharingWizard() {
		IDialogSettings workbenchSettings = SVNUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewLocationWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("NewLocationWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("SharingWizard.title")); //$NON-NLS-1$
	}	

    /**
     * add pages
     */		
	public void addPages() {
		ImageDescriptor sharingImage = SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_SHARE);
		if (doesSVNDirectoryExist()) {
            // if .svn directory exists, we add the autoconnect page
			autoconnectPage = new ConfigurationWizardAutoconnectPage("autoconnectPage", Policy.bind("SharingWizard.autoConnectTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			autoconnectPage.setProject(project);
			autoconnectPage.setDescription(Policy.bind("SharingWizard.autoConnectTitleDescription")); //$NON-NLS-1$
			addPage(autoconnectPage);
		}
        else {
            // otherwise we add : 
            // - the repository selection page
            // - the create location page
            // - the module selection page
            // - the finish page 
			ISVNRepositoryLocation[] locations = SVNUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
			if (locations.length > 0) {
				locationPage = new RepositorySelectionPage("importPage", Policy.bind("SharingWizard.importTitle"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
				locationPage.setDescription(Policy.bind("SharingWizard.importTitleDescription")); //$NON-NLS-1$
				addPage(locationPage);
			}
			createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			createLocationPage.setDescription(Policy.bind("SharingWizard.enterInformationDescription")); //$NON-NLS-1$
			addPage(createLocationPage);
			createLocationPage.setDialogSettings(getDialogSettings());
			directoryPage = new DirectorySelectionPage("modulePage", Policy.bind("SharingWizard.enterModuleName"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			directoryPage.setDescription(Policy.bind("SharingWizard.enterModuleNameDescription")); //$NON-NLS-1$
			addPage(directoryPage);
			finishPage = new SharingWizardFinishPage("finishPage", Policy.bind("SharingWizard.readyToFinish"), sharingImage); //$NON-NLS-1$ //$NON-NLS-2$
			finishPage.setDescription(Policy.bind("SharingWizard.readyToFinishDescription")); //$NON-NLS-1$
			addPage(finishPage);
		}
	}
    
    /**
     * check if wizard can finish 
     */
	public boolean canFinish() {
		IWizardPage page = getContainer().getCurrentPage();
		if (page == locationPage) {
			if (locationPage.getLocation() == null) {
				return createLocationPage.isPageComplete();
			} else {
				return directoryPage.useProjectName() || directoryPage.getDirectoryName() != null;
			}
		} else if (page == directoryPage) {
			return directoryPage.useProjectName() || directoryPage.getDirectoryName() != null;
		} else if (page == finishPage) {
			return true;
		}
		return super.canFinish();
	}
    
    /**
     * get the next page
     */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == autoconnectPage) return null;
		if (page == locationPage) {
			if (locationPage.getLocation() == null) {
				return createLocationPage;
			} else {
				return directoryPage;
			}
		}
		if (page == createLocationPage) {
			return directoryPage;
		}
		if (page == directoryPage) {
			return finishPage;
		}
		return null;
	}
    
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		final boolean[] result = new boolean[] { true };
		try {
			final boolean[] doSync = new boolean[] { false };
			final boolean[] projectExists = new boolean[] { false };
			getContainer().run(true /* fork */, true /* cancel */, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						monitor.beginTask("", 100); //$NON-NLS-1$
						if (autoconnectPage != null && doesSVNDirectoryExist()) {
							// Autoconnect to the repository using svn/ directories
							
							ISVNStatus info = autoconnectPage.getFolderStatus();
							if (info == null) {
								// Error!
								return;
							}
							
							// Get the repository location (the get will add the locatin to the provider)
							boolean isPreviouslyKnown = SVNProviderPlugin.getPlugin().getRepositories().isKnownRepository(info.getUrl().toString());
	
							// Validate the connection if the user wants to
							boolean validate = autoconnectPage.getValidate();					
							
                            if (validate && !isPreviouslyKnown) {
								ISVNRepositoryLocation location = SVNProviderPlugin.getPlugin().getRepository(info.getUrl().toString());                            	
								// Do the validation
								try {
									location.validateConnection(new SubProgressMonitor(monitor, 50));
								} catch (final TeamException e) {
									// Exception validating. We can continue if the user wishes.
									final boolean[] keep = new boolean[] { false };
									getShell().getDisplay().syncExec(new Runnable() {
										public void run() {
											keep[0] = MessageDialog.openQuestion(getContainer().getShell(),
												Policy.bind("SharingWizard.validationFailedTitle"), //$NON-NLS-1$
												Policy.bind("SharingWizard.validationFailedText", new Object[] {e.getStatus().getMessage()})); //$NON-NLS-1$
										}
									});
									if (!keep[0]) {
										// Remove the root
										try {
											if (!isPreviouslyKnown) {
												SVNProviderPlugin.getPlugin().getRepositories().disposeRepository(location);
											}
										} catch (TeamException e1) {
											SVNUIPlugin.openError(getContainer().getShell(), Policy.bind("exception"), null, e1, SVNUIPlugin.PERFORM_SYNC_EXEC); //$NON-NLS-1$
										}
										result[0] = false;
										return;
									}
									// They want to keep the connection anyway. Fall through.
								}
							}
							
							// Set the sharing
							SVNWorkspaceRoot.setSharing(project, new SubProgressMonitor(monitor, 50));
						} 
                        else {
							// No svn directory : Share the project
							doSync[0] = true;
							// Check if the directory exists on the server
							ISVNRepositoryLocation location = null;
							boolean isKnown = false;
							try {
								location = getLocation();
								isKnown = SVNProviderPlugin.getPlugin().getRepositories().isKnownRepository(location.getLocation());
								
                                // Purge any svn folders that may exists in subfolders
                                SVNWorkspaceRoot.getSVNFolderFor(project).unmanage(null);

                                // check if the remote directory already exist
								String remoteDirectoryName = getRemoteDirectoryName();
								ISVNRemoteFolder folder = location.getRemoteFolder(remoteDirectoryName);
								if (folder.exists(new SubProgressMonitor(monitor, 50))) {
									projectExists[0] = true;
									final boolean[] sync = new boolean[] {true};
									if (autoconnectPage == null) {
										getShell().getDisplay().syncExec(new Runnable() {
											public void run() {
												sync[0] = MessageDialog.openQuestion(getShell(), Policy.bind("SharingWizard.couldNotImport"), Policy.bind("SharingWizard.couldNotImportLong", getRemoteDirectoryName())); //$NON-NLS-1$ //$NON-NLS-2$
											}
										});
									}
									result[0] = sync[0];
									doSync[0] = sync[0];
									return;
								}
							} catch (TeamException e) {
								SVNUIPlugin.openError(getShell(), null, null, e, SVNUIPlugin.PERFORM_SYNC_EXEC);
//								if (!isKnown && location != null) location.flushUserInfo();
								result[0] = false;
								doSync[0] = false;
								return;
							}
							
                            // Add the location to the provider if it is new
							if (!isKnown) {
								SVNProviderPlugin.getPlugin().getRepositories().addOrUpdateRepository(location);
							}
							
							// Create the remote module for the project
							SVNWorkspaceRoot.shareProject(location, project, getRemoteDirectoryName(), new SubProgressMonitor(monitor, 50));
							
							try{
								project.refreshLocal(IProject.DEPTH_INFINITE, new SubProgressMonitor(monitor, 50));
							}
							catch(CoreException ce){
								throw new TeamException(ce.getStatus());
							}
							
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
//			if (doSync[0]) {
//				// Sync of the project
//				IWorkbenchPage activePage = null; /* not sure how to get the active page */
//				SyncView view = SyncView.findViewInActivePage(activePage);
//				if (view != null) {
//					SVNSyncCompareInput input;
//					if (projectExists[0]) {
//						try {
//							String moduleName = getModuleName();
//							SVNTag tag;
//							if (autoconnectPage == null) {
//								TagSelectionDialog dialog = new TagSelectionDialog(getShell(), 
//									new ISVNFolder[] {(ISVNFolder)getLocation().getRemoteFolder(moduleName, null)}, 
//									Policy.bind("SharingWizard.selectTagTitle"),  //$NON-NLS-1$
//									Policy.bind("SharingWizard.selectTag"), //$NON-NLS-1$
//									TagSelectionDialog.INCLUDE_HEAD_TAG | TagSelectionDialog.INCLUDE_BRANCHES, 
//									false, /*don't show recurse option*/
//									IHelpContextIds.SHARE_WITH_EXISTING_TAG_SELETION_DIALOG);
//								dialog.setBlockOnOpen(true);
//								if (dialog.open() == Dialog.CANCEL) {
//									return false;
//								}
//								tag = dialog.getResult();
//							} else {
//								tag = autoconnectPage.getSharing().getTag();
//							}
//							input = new SVNSyncCompareUnsharedInput(project, getLocation(), moduleName, tag);
//						} catch (TeamException e) {
//							throw new InvocationTargetException(e);
//						}
//					} else {
//						input = new SVNSyncCompareInput(new IResource[] {project});
//					}
//					view.showSync(input, activePage);
//				}
//			}
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			SVNUIPlugin.openError(getContainer().getShell(), null, null, e);
		}

		return result[0];
	}

	/**
	 * Return an ISVNRepositoryLocation
	 */
	private ISVNRepositoryLocation getLocation() throws TeamException {
		// If there is an autoconnect page then it has the location
		if (autoconnectPage != null) {
			return autoconnectPage.getLocation();
		}
		
		// If the import page has a location, use it.
		if (locationPage != null) {
			ISVNRepositoryLocation location = locationPage.getLocation();
			if (location != null) return location;
		}
		
		// Otherwise, get the location from the create location page
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				createLocationPage.finish(new NullProgressMonitor());
			}
		});
		Properties properties = createLocationPage.getProperties();
		ISVNRepositoryLocation location = SVNProviderPlugin.getPlugin().getRepositories().createRepository(properties);
		return location;
	}
	/**
	 * Return the directory name in the remote repository where to put the project
	 */
	private String getRemoteDirectoryName() {
		// If there is an autoconnect page then it has the module name
		if (autoconnectPage != null) {
//			return autoconnectPage.getSharing().getRepository();
            return Util.getLastSegment(autoconnectPage.getSharingStatus().getUrl().toString());
		}
		String moduleName = directoryPage.getDirectoryName();
		if (moduleName == null) moduleName = project.getName();
		return moduleName;
	}
	/*
	 * @see IConfigurationWizard#init(IWorkbench, IProject)
	 */
	public void init(IWorkbench workbench, IProject project) {
		this.project = project;
	}
    
    /**
     * check if there is a valid svn directory
     */
	private boolean doesSVNDirectoryExist() {
		// Determine if there is an existing .svn/ directory from which configuration
		// information can be retrieved.
        boolean isSVNFolder = false;
		try {
		  ISVNLocalFolder folder = (ISVNLocalFolder)SVNWorkspaceRoot.getSVNResourceFor(project);
		  ISVNStatus info = folder.getStatus();
		  isSVNFolder = info.hasRemote();
          
		} catch (final TeamException e) {
            SVNUIPlugin.openError(getContainer().getShell(), null, null, e);
		}
        return isSVNFolder; 
	}
}