package org.tigris.subversion.subclipse.ui.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNSubscriber;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;

/**
 * @author mml
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVNOperations {

	

	
	private static SVNOperations instance;
	
	private SVNOperations(){
		
	}
	private void internalPut(SVNTeamProvider provider, IResource[] resources, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		// ensure the progress monitor is not null
		progress = Policy.monitorFor(progress);
		progress.beginTask(Policy.bind("PutAction.working"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			if (resources[i].getType() == IResource.FILE) {
				internalPut(provider, (IFile)resources[i], overrideIncoming, progress);
			} else if (depth > 0) { //Assume that resources are either files or containers.
				internalPut(provider, (IContainer)resources[i], depth, overrideIncoming, progress);
			}
			progress.worked(1);
		}
		progress.done();
	}
	
	/*
	 * Get the file if it is out-of-sync.
	 */
	private void internalPut(SVNTeamProvider provider, IFile localFile, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		ThreeWaySynchronizer synchronizer = SVNSubscriber.getInstance().getSynchronizer();
		IResourceVariantComparator comparator = SVNSubscriber.getInstance().getResourceComparator();
		ISVNRemoteResource remote = getResourceVariant(provider, localFile);
		byte[] baseBytes = synchronizer.getBaseBytes(localFile);
		IResourceVariant base = provider.getResourceVariant(localFile, baseBytes);
		
		// Check whether we are overriding a remote change
		if (base == null && remote != null && !overrideIncoming) {
			// The remote is an incoming (or conflicting) addition.
			// Do not replace unless we are overriding
			return;
		} else  if (base != null && remote == null) {
			// The remote is an incoming deletion
			if (!localFile.exists()) {
				// Conflicting deletion. Clear the synchronizer.
				synchronizer.flush(localFile, IResource.DEPTH_ZERO);
			} else if (!overrideIncoming) {
				// Do not override the incoming deletion
				return;
			}
		} else if (base != null && remote != null) {
			boolean same = comparator.compare(base, (IResourceVariant)remote);
			if (!synchronizer.isLocallyModified(localFile) && same) {
				// The base and remote are the same and there's no local changes
				// so nothing needs to be done
				return;
			}
			if (!same && !overrideIncoming) {
				// The remote has changed. Only override if specified
				return;
			}
		}
		
		// Handle an outgoing deletion
//		File diskFile = provider.getFile(localFile);
		if (!localFile.exists()) { 
//			diskFile.delete();
			synchronizer.flush(localFile, IResource.DEPTH_ZERO);
		} else {
			// Otherwise, upload the contents
			try {
				//Copy from the local file to the remote file:
				InputStream in = null;
				FileOutputStream out = null;
				try {
//					if(! diskFile.getParentFile().exists()) {
//						diskFile.getParentFile().mkdirs();
//					}
					in = localFile.getContents();
//					out = new FileOutputStream(diskFile);
					//Copy the contents of the local file to the remote file:
//					StreamUtil.pipe(in, out, diskFile.length(), progress, diskFile.getName());
					// Mark the file as read-only to require another checkout
					localFile.setReadOnly(true);
				} finally {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				}
				// Update the synchronizer base bytes
				remote = getResourceVariant(provider, localFile);
				synchronizer.setBaseBytes(localFile, ((RemoteFile)remote).asBytes());
			} catch (IOException e) {
				throw SVNException.wrapException(e);
			} catch (CoreException e) {
				throw SVNException.wrapException(e);
			}
		}
	}
	
	/*
	 * Get the folder and its children to the depth specified.
	 */
	private void internalPut(SVNTeamProvider provider, IContainer container, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		try {
			ThreeWaySynchronizer synchronizer = SVNSubscriber.getInstance().getSynchronizer();
			// Make the local folder state match the remote folder state
			List toDelete = new ArrayList();
			if (container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder)container;
				//File diskFile = provider.getResourceVariant(container);
				ISVNRemoteResource remote = getResourceVariant(provider, container);
				if (!folder.exists() && remote != null) {
					// Schedule the folder for removal but delay in
					// case the folder contains incoming changes
					//toDelete.add(diskFile);
				} else if (folder.exists() && remote == null) {
					// Create the remote directory and sync up the local
					//diskFile.mkdir();
					synchronizer.setBaseBytes(folder, provider.getResourceVariant(folder).asBytes());
				}
			}
			
			// Get the children
			IResource[] children = synchronizer.members(container);
			if (children.length > 0) {
				internalPut(provider, children, depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, overrideIncoming, progress);
			}
		
			// Remove any empty folders
			for (Iterator iter = toDelete.iterator(); iter.hasNext(); ) {
				File diskFile = (File) iter.next();
				if (diskFile.listFiles().length == 0) {
					diskFile.delete();
					synchronizer.flush(container, IResource.DEPTH_INFINITE);
				}
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}
	
	/**
	 * Checkin the given resources to the given depth by replacing the remote (i.e. file system)
	 * contents with the local workspace contents. 
	 * @param resources the resources
	 * @param depth the depth of the operation
	 * @param overrideIncoming indicate whether incoming remote changes should be replaced
	 * @param progress a progress monitor
	 * @throws TeamException
	 */
	public void checkin(IResource[] resources, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		try {
			// ensure the progress monitor is not null
			SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider.getProvider(resources[0].getProject());
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("PutAction.working"), 100); //$NON-NLS-1$
			// Refresh the subscriber so we have the latest remote state
			SVNSubscriber.getInstance().refresh(resources, depth, new SubProgressMonitor(progress, 30));
			internalPut(provider, resources, depth, overrideIncoming, new SubProgressMonitor(progress, 70));
		} finally {
			progress.done();
		}
	}
	

	private void internalGet(SVNTeamProvider provider, IResource[] resources, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
	
		progress.beginTask(Policy.bind("GetAction.working"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			if (resources[i].getType() == IResource.FILE) {
				internalGet(provider, (IFile) resources[i], overrideOutgoing, progress);
			} else if (depth != IResource.DEPTH_ZERO) {
				internalGet(provider, (IContainer)resources[i], depth, overrideOutgoing, progress);
			}
			progress.worked(1);
		}
	}
	
	/*
	 * Get the folder and its children to the depth specified.
	 */
	private void internalGet(SVNTeamProvider provider, IContainer container, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		try {
			ThreeWaySynchronizer synchronizer = SVNSubscriber.getInstance().getSynchronizer();
			// Make the local folder state match the remote folder state
			List toDelete = new ArrayList();
			if (container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder)container;
				ISVNRemoteResource remote = getResourceVariant(provider, container);
				if (!folder.exists() && remote != null) {
					// Create the local folder
					folder.create(false, true, progress);
					synchronizer.setBaseBytes(folder, ((RemoteFolder)remote).asBytes());
				} else if (folder.exists() && remote == null) {
					// Schedule the folder for removal but delay in
					// case the folder contains outgoing changes
					toDelete.add(folder);
				}
			}
			
			// Get the children
			IResource[] children = synchronizer.members(container);
			if (children.length > 0) {
				internalGet(provider, children, depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, overrideOutgoing, progress);
			}
		
			// Remove any empty folders
			for (Iterator iter = toDelete.iterator(); iter.hasNext(); ) {
				IFolder folder = (IFolder) iter.next();
				if (folder.members().length == 0) {
					folder.delete(false, true, progress);
					synchronizer.flush(folder, IResource.DEPTH_INFINITE);
				}
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	/*
	 * Get the file if it is out-of-sync.
	 */
	private void internalGet(SVNTeamProvider provider, IFile localFile, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		ThreeWaySynchronizer synchronizer = SVNSubscriber.getInstance().getSynchronizer();
		IResourceVariantComparator comparator = SVNSubscriber.getInstance().getResourceComparator();
		ISVNRemoteResource remote = getResourceVariant(provider, localFile);
		byte[] baseBytes = synchronizer.getBaseBytes(localFile);
		IResourceVariant base = provider.getResourceVariant(localFile, baseBytes);
		
		if (!synchronizer.hasSyncBytes(localFile) 
				|| (synchronizer.isLocallyModified(localFile) && !overrideOutgoing)) {
			// Do not overwrite the local modification
			return;
		}
		if (base != null && remote == null) {
			// The remote no longer exists so remove the local
			try {
				localFile.delete(false, true, progress);
				synchronizer.flush(localFile, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		}
		if (!synchronizer.isLocallyModified(localFile) && comparator.compare(base, (IResourceVariant)remote)) {
			// The base and remote are the same and there's no local changes
			// so nothing needs to be done
		}
		try {
			//Copy from the local file to the remote file:
			InputStream source = null;
			try {
				// Get the remote file content.
				source = ((RemoteFile)remote).getStorage(null).getContents();
				// Set the local file content to be the same as the remote file.
				if (localFile.exists())
					localFile.setContents(source, false, false, progress);
				else
					localFile.create(source, false, progress);
			} finally {
				if (source != null)
					source.close();
			}
			// Mark as read-only to force a checkout before editing
			localFile.setReadOnly(true);
			synchronizer.setBaseBytes(localFile, ((RemoteFile)remote).asBytes());
		} catch (IOException e) {
			throw SVNException.wrapException(e);
		} catch (CoreException e) {
			throw SVNException.wrapException(e);
		}
	}

	/*
	 * Get the resource variant for the given resource.
	 */
	private ISVNRemoteResource getResourceVariant(SVNTeamProvider provider, IResource resource) throws SVNException {
		return (ISVNRemoteResource)provider.getResourceVariant(resource);
	}
	
	/**
	 * Make the local state of the project match the remote state by getting any out-of-sync 
	 * resources. The overrideOutgoing flag is used to indicate whether locally modified
	 * files should also be replaced or left alone.
	 * @param resources the resources to get
	 * @param depth the depth of the operation
	 * @param overrideOutgoing whether locally modified resources should be replaced
	 * @param progress a progress monitor
	 * @throws TeamException
	 */
	public void get(IResource[] resources, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		try {
			SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider.getProvider(resources[0].getProject());
			// Traverse the resources and get any that are out-of-sync
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("GetAction.working"), 100); //$NON-NLS-1$
			// Refresh the subscriber so we have the latest remote state
			SVNSubscriber.getInstance().refresh(resources, depth, new SubProgressMonitor(progress, 30));
			internalGet(provider, resources, depth, overrideOutgoing, new SubProgressMonitor(progress, 70));
		} finally {
			progress.done();
		}
	}

	/**
	 * @return
	 */
	public static SVNOperations getInstance() {
		
		
		if(instance == null){
			instance = new SVNOperations();
		}
		return instance;
	}
	
}
