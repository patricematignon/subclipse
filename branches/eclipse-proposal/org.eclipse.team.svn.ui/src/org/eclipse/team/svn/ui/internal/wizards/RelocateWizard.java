package org.eclipse.team.svn.ui.internal.wizards;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.repo.SVNRepositories;
import org.eclipse.team.svn.core.internal.repo.SVNRepositoryLocation;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;

public class RelocateWizard extends Wizard {
	private SVNRepositoryLocation repository;
	private IProject[] sharedProjects;
	private RelocateWizardWarningPage warningPage;
	private RelocateWizardUrlPage urlPage;

	public RelocateWizard(SVNRepositoryLocation repository) {
		super();
		this.repository = repository;
		setWindowTitle(Policy.bind("RelocateWizard.title")); //$NON-NLS-1$
		setSharedProjects();
	}
	
	public void addPages() {
        warningPage = new RelocateWizardWarningPage(
            "warningPage",  //$NON-NLS-1$ 
            Policy.bind("RelocateWizard.heading"), //$NON-NLS-1$
            SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_NEW_LOCATION)); 
		addPage(warningPage);
        urlPage = new RelocateWizardUrlPage(
                "urlPage",  //$NON-NLS-1$ 
                Policy.bind("RelocateWizard.heading"), //$NON-NLS-1$
                SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_NEW_LOCATION),
                repository.getUrl().toString()); 
        addPage(urlPage);
	}

	public boolean performFinish() {
		try {
			SVNRepositoryLocation newRepository = SVNRepositoryLocation.fromString(urlPage.getNewUrl());
			newRepository.setUsername(repository.getUsername());
			newRepository.setLabel(repository.getLabel());
			newRepository.validateConnection(new NullProgressMonitor());
			ISVNClientAdapter client = repository.getSVNClient();
			for (int i = 0; i < sharedProjects.length; i++) {
				client.relocate(repository.getUrl().toString(), newRepository.getUrl().toString(), sharedProjects[i].getLocation().toString(), true);
			}
            SVNRepositories repositories = SVNProviderPlugin.getPlugin().getRepositories();
            repositories.addOrUpdateRepository(newRepository);
			for (int i = 0; i < sharedProjects.length; i++) {
				SVNProviderPlugin.getPlugin().getStatusCacheManager().refreshStatus(sharedProjects[i], IResource.DEPTH_INFINITE);
				sharedProjects[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				RepositoryProvider provider = RepositoryProvider.getProvider(sharedProjects[i], SVNProviderPlugin.getTypeId());
				provider.setProject(sharedProjects[i]);
			}
			repositories.disposeRepository(repository);
		} catch (Exception e) {
			MessageDialog.openError(getShell(), Policy.bind("RelocateWizard.heading"), e.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	private void setSharedProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList shared = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(projects[i], SVNProviderPlugin.getTypeId());
			if (teamProvider!=null) {
			    try {
					SVNTeamProvider svnProvider = (SVNTeamProvider)teamProvider;
					if (svnProvider.getSVNWorkspaceRoot().getRepository().equals(repository)) shared.add(projects[i]);
			    } catch(Exception e) {}				
			}
		}
		sharedProjects = new IProject[shared.size()];
		shared.toArray(sharedProjects);
	}

	public IProject[] getSharedProjects() {
		return sharedProjects;
	}

}