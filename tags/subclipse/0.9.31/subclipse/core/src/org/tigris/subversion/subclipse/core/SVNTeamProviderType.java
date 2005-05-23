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
 *     Panagiotis Korros (panagiotis.korros@gmail.com) - ported autoshare code
 *     Magnus Naeslund	(mag@kite.se) - Added autoadd code
 *******************************************************************************/
package org.tigris.subversion.subclipse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;


/**
 * This class represents the SVN Provider's capabilities in the absence of a
 * particular project.
 */

public class SVNTeamProviderType extends RepositoryProviderType {

    private static AutoShareJob autoShareJob;
    
    public static class AutoShareJob extends Job {

        List projectsToShare = new ArrayList();
        
        AutoShareJob() {
            super("Auto-sharing imported subversion projects");
        }

        public boolean isQueueEmpty() {
            return projectsToShare.isEmpty();
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
         */
        public boolean shouldSchedule() {
            return !isQueueEmpty();
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
         */
        public boolean shouldRun() {
            synchronized (projectsToShare) {
                for (Iterator iter = projectsToShare.iterator(); iter.hasNext();) {
                    IProject project = (IProject) iter.next();
                    if (RepositoryProvider.isShared(project)) {
                        iter.remove();
                    }
                }
                return !projectsToShare.isEmpty();
            }
        }
        
        public void share(IProject project) {
            if (!RepositoryProvider.isShared(project)) {
                synchronized (projectsToShare) {
                    if (!projectsToShare.contains(project))
                        projectsToShare.add(project);
                }
                if(getState() == Job.NONE && !isQueueEmpty())
                    schedule();
            }
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            IProject next = null;
            next = getNextProject();
            monitor.beginTask(null, IProgressMonitor.UNKNOWN);
            while (next != null) {
                autoconnectSVNProject(next, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
                next = getNextProject();
            }
            monitor.done();
            return Status.OK_STATUS;
        }

        private IProject getNextProject() {
            IProject next = null;
            synchronized (projectsToShare) {
                if (!projectsToShare.isEmpty()) {
                    next = (IProject)projectsToShare.remove(0);
                }
            }
            return next;
        }
        
        /*
         * Auto-connect to the repository using .svn/ directories
         */
        private void autoconnectSVNProject(IProject project, IProgressMonitor monitor) {
            try {
                SVNWorkspaceRoot.setSharing(project, monitor);
            } catch (TeamException e) {
                SVNProviderPlugin.log(IStatus.ERROR, "Could not auto-share project " + project.getName(), e); //$NON-NLS-1$
            }
        }
    }
    
    private synchronized static AutoShareJob getAutoShareJob() {
        if (autoShareJob == null) {
            autoShareJob = new AutoShareJob();
            autoShareJob.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    // Reschedule the job if it has unprocessed projects
                    if (!autoShareJob.isQueueEmpty()) {
                        autoShareJob.schedule();
                    }
                }
            });
            autoShareJob.setSystem(true);
            autoShareJob.setPriority(Job.SHORT);
            // Must run with the workspace rule to ensure that projects added while we're running
            // can be shared
            autoShareJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
        }
        return autoShareJob;
    }
    
    
    /**
     * @see org.eclipse.team.core.RepositoryProviderType#supportsProjectSetImportRelocation()
     */
    public boolean supportsProjectSetImportRelocation() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.RepositoryProviderType#getProjectSetCapability()
     */
    public ProjectSetCapability getProjectSetCapability() {
        return new SVNProjectSetCapability();
    }

	public static class AutoAddJob extends Job {
		final static int MAX_RETRIES = 10;
		int reschedCount = 0;
		final IProject project;
		
		protected AutoAddJob(IProject project){
			super("Auto-adding newly created project to subversion: " + project.getName()); //$NON-NLS-1$
			this.project = project;
		}
		
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(null, IProgressMonitor.UNKNOWN);
			try{
				SVNProviderPlugin plugin = SVNProviderPlugin.getPlugin();
				
				if (plugin == null || plugin.getSimpleDialogsHelper() == null){
					if (++reschedCount > MAX_RETRIES){
						String errorString = "Subclipse core and/or ui didn't come up in " + MAX_RETRIES + " retries, failing.";  //$NON-NLS-1$
						System.err.println(errorString); // Let it be visible to the user
						throw new SVNException(errorString);
					}
					schedule(1000);
					return Status.OK_STATUS;
				}
				
				if (!plugin.getSimpleDialogsHelper().promptYesNo(
						"Auto-add "+project.getName()+" to source control", //$NON-NLS-1$
						  "The new project \""+ project.getName() +"\" was created in a subversion " + //$NON-NLS-1$
						  "controlled directory.\n\n" + //$NON-NLS-1$
						  "Would you like to automatically add it to source control?", true)) { //$NON-NLS-1$

					return Status.OK_STATUS;
				}
						
				SVNClientManager svnClientManager = plugin.getSVNClientManager();
				ISVNClientAdapter client = svnClientManager.createSVNClient();

				File file = project.getLocation().toFile();
				client.addDirectory(file, false);

				RepositoryProvider.map(project, SVNProviderPlugin.getTypeId());
				SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
						.getProvider(project, SVNProviderPlugin.getTypeId());

				plugin.getStatusCacheManager().refreshStatus(project,
						IResource.DEPTH_INFINITE);
				
			}catch(Exception e){
                SVNProviderPlugin.log(IStatus.ERROR, "Could not auto-add project " + project.getName(), e); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}finally{
				monitor.done();
			}
			return Status.OK_STATUS;
        }
		
    }	

	/**
     * Create and schedule an auto-add job
     */
	
	private static synchronized void createAutoAddJob(IProject project) {
		Job j = new AutoAddJob(project);
        j.setSystem(true);
        j.setPriority(Job.SHORT);
        j.setRule(ResourcesPlugin.getWorkspace().getRoot());
		j.schedule();
	}

	/* (non-Javadoc)
     * @see org.eclipse.team.core.RepositoryProviderType#metaFilesDetected(org.eclipse.core.resources.IProject, org.eclipse.core.resources.IContainer[])
     */
    public void metaFilesDetected(IProject project, IContainer[] containers) {
		boolean isProject = false;
		
        for (int i = 0; i < containers.length; i++) {
            IContainer container = containers[i];
            IContainer svnDir = null;
			
			if (!isProject && container.getType() == IContainer.PROJECT)
				isProject = true;
			
            if (container.getName().equals(".svn")) { //$NON-NLS-1$
                svnDir = container;
            } else {
                IResource resource = container.findMember(".svn"); //$NON-NLS-1$
                if (resource != null && resource.getType() != IResource.FILE) {
                    svnDir = (IContainer)resource;
                }
            }
            try {
                if (svnDir != null && !svnDir.isTeamPrivateMember())
                    svnDir.setTeamPrivateMember(true);
            } catch (CoreException e) {
                SVNProviderPlugin.log(IStatus.ERROR, "Could not flag meta-files as team-private for " + svnDir.getFullPath(), e); //$NON-NLS-1$
            }
        }
		
		if (!isProject)
			return; // Nothing more to do, all remaining operations are on projects

		// Somehow sometimes it doesn't work using project.findMember(".svn") here, this  
		// could be due to timing issue with workspace addition, so use trusty File instead.
		
		File svnDir = project.getLocation().append(".svn").toFile(); //$NON-NLS-1$
		
		if (svnDir != null && svnDir.exists() && svnDir.isDirectory()) {
			// It's a project and has toplevel .svn directory, lets share it!
			getAutoShareJob().share(project);
		} else {
			// It's a project and doesn't have .svn dir, let's see if we can add it!
			File parentSvnDir = project.getLocation().append("../.svn").toFile(); //$NON-NLS-1$
			
			if (parentSvnDir != null && parentSvnDir.exists()
					&& parentSvnDir.isDirectory()) {

				createAutoAddJob(project);
			}
		}
		
    }
	
	
}
