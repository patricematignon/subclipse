/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.client;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNNotifyListener;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.util.ReentrantLock;

/**
 * This class manages svn operations. beginOperation must be called before a
 * batch of svn operations and endOperation after
 * 
 * All changed .svn directories are refreshed using resource.refreshLocal
 * SyncFileChangeListener will then find that some meta files have changed and
 * will refresh the corresponding resources.
 */
public class OperationManager implements ISVNNotifyListener {
	// track resources that have changed in a given operation
	private ReentrantLock lock = new ReentrantLock();

	private Set changedResources = new LinkedHashSet();

	private ISVNClientAdapter svnClient = null;

	private OperationProgressNotifyListener operationNotifyListener = null;
	
	private static OperationManager instance;

	/*
	 * private contructor
	 */
	private OperationManager() {
	}

	/**
	 * Returns the singleton instance of the synchronizer.
	 */
	public static OperationManager getInstance() {
		if (instance == null) {
			instance = new OperationManager();
		}
		return instance;
	}

	/**
	 * Begins a batch of operations.
	 */
	public void beginOperation(ISVNClientAdapter aSvnClient) {
		lock.acquire();
		this.svnClient = aSvnClient;
		aSvnClient.addNotifyListener(this);
		if (lock.getNestingCount() == 1) {
			changedResources.clear();
		}
	}

	/**
	 * Begins a batch of operations.
	 * Forward notifications to messageNotifyListener
	 */
	public void beginOperation(ISVNClientAdapter aSvnClient, OperationProgressNotifyListener anOperationNotifyListener) {
		this.operationNotifyListener = anOperationNotifyListener;
		beginOperation(aSvnClient);
	}	
	
	/**
	 * Ends a batch of operations. Pending changes are committed only when the
	 * number of calls to endOperation() balances those to beginOperation().
	 */
	public void endOperation() throws SVNException {
		try {
			if (lock.getNestingCount() == 1) {
				svnClient.removeNotifyListener(this);
				for (Iterator it = changedResources.iterator(); it.hasNext();) {
					IResource resource = (IResource) it.next();
                    try {
                        // .svn directory will be refreshed so all files in the
                        // directory including resource will
                        // be refreshed later (@see SyncFileChangeListener)
                        resource.refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
                        if(Policy.DEBUG_METAFILE_CHANGES) {
                            System.out.println("[svn]" + SVNProviderPlugin.getPlugin().getAdminDirectoryName() + " dir refreshed : " + resource.getFullPath()); //$NON-NLS-1$
                        }
                    } catch (CoreException e) {
                        throw SVNException.wrapException(e);
                    }                    
                    
				}
			}
		} finally {
			lock.release();
			operationNotifyListener = null;
		}
	}

	public void onNotify(File path, SVNNodeKind kind) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();

		IPath pathEclipse = new Path(path.getAbsolutePath());

        if (kind == SVNNodeKind.UNKNOWN)  { // delete, revert 
            IPath pathEntries = pathEclipse.removeLastSegments(1).append(
            		SVNProviderPlugin.getPlugin().getAdminDirectoryName());
            IResource entries = workspaceRoot.getContainerForLocation(pathEntries);
            if (entries == null) //probably the pathEclipse was project itself
            {
            	entries = workspaceRoot.getProject(pathEclipse.lastSegment()).getFolder(new Path(SVNProviderPlugin.getPlugin().getAdminDirectoryName()));
            }
            changedResources.add(entries);
        }
        else
        {
            IResource resource = null;
			IResource svnDir = null;
			if (kind == SVNNodeKind.DIR) {
				// when the resource is a directory, two .svn directories can
				// potentially
				// be modified
				resource = workspaceRoot.getContainerForLocation(pathEclipse);
				if (resource != null && resource.getType() != IResource.ROOT) {
					if (resource.getProject() != resource) {
						// if resource is a project. We can't refresh ../.svn
						svnDir = resource.getParent().getFolder(
								new Path(SVNProviderPlugin.getPlugin().getAdminDirectoryName()));
						changedResources.add(svnDir);
					}
                    svnDir = ((IContainer) resource).getFolder(new Path(
                    		SVNProviderPlugin.getPlugin().getAdminDirectoryName()));
                    changedResources.add(svnDir);
				}
			} else if (kind == SVNNodeKind.FILE) {
				resource = workspaceRoot.getFileForLocation(pathEclipse);

				if (resource != null) {
					svnDir = resource.getParent().getFolder(
							new Path(SVNProviderPlugin.getPlugin().getAdminDirectoryName()));
					changedResources.add(svnDir);
				}
			}
		}
        
		if (operationNotifyListener != null)
		{
			operationNotifyListener.onNotify(path, kind);
			if ((operationNotifyListener.getMonitor() != null) && (operationNotifyListener.getMonitor().isCanceled()))
			{
				try {
					svnClient.cancelOperation();
				} catch (SVNClientException e) {
					SVNProviderPlugin.log(SVNException.wrapException(e));
				}
			}
		}
	}

	public void logCommandLine(String commandLine) {
	}

	public void logRevision(long revision, String path) {
	}

	public void logCompleted(String message) {
		if (operationNotifyListener != null)
		{
			operationNotifyListener.logMessage(message);
		}
	}

	public void logError(String message) {
	}

	public void logMessage(String message) {
		if (operationNotifyListener != null)
		{
			operationNotifyListener.logMessage(message);
		}
	}

	public void setCommand(int command) {
	}
}