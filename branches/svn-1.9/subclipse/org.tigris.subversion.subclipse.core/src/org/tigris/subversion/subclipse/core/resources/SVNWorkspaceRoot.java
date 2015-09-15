/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.client.PeekStatusCommand;
import org.tigris.subversion.subclipse.core.commands.CheckoutCommand;
import org.tigris.subversion.subclipse.core.commands.ShareProjectCommand;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * This class provides static methods for checking out projects from a repository
 * into the local workspace and for converting IResources into SVNResources
 * <p>
 * Instances of this class represent a local workspace root (i.e. a project).
 * <br>
 * Here is how you can get a SVNWorkspaceRoot from an IProject : <br>
 * <code>
 * SVNTeamProvider teamProvider = (SVNTeamProvider)RepositoryProvider.getProvider(myIProject, SVNProviderPlugin.getTypeId()); <br>
 * SVNWorkspaceRoot svnProject = teamProvider.getSVNWorkspaceRoot();
 * </code>
 * </p>
 */
public class SVNWorkspaceRoot {

	private ISVNLocalFolder localRoot;
    private String url;
    private static Set<IProject> sharedProjects = new HashSet<IProject>();

	public SVNWorkspaceRoot(IContainer resource){
		this.localRoot = getSVNFolderFor(resource);
	}

	public static boolean isManagedBySubclipse(IProject project) {
		if (project == null)
			return false;

		synchronized (sharedProjects) {
			if (sharedProjects.contains(project))
				return true;
		}

		return null != RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId());
	}

	public static void setManagedBySubclipse(IProject project) {
		synchronized (sharedProjects) {
			sharedProjects.add(project);
		}
	}

	public static void unsetManagedBySubclipse(IProject project) {
		synchronized (sharedProjects) {
			sharedProjects.remove(project);
		}
	}

    /**
     * get the project name for the remote folder. The name is either the name of the
     * remote folder or the name in .project if this file exists.
     * @param folder
     * @param monitor
     * @return
     */
    public static String getProjectName(ISVNRemoteFolder folder,IProgressMonitor monitor) throws Exception {
        ISVNClientAdapter client = folder.getRepository().getSVNClient();
        try {
            client.getNotificationHandler().disableLog();
            String result = folder.getName();

            InputStream is = client.getContent(folder.getUrl().appendPath(".project"), SVNRevision.HEAD);
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(is);
            is.close();
            NodeList nl = doc.getDocumentElement().getChildNodes();
            for (int j = 0; j < nl.getLength(); ++j) {
                Node child = nl.item(j);
                if (child instanceof Element && "name".equals(child.getNodeName())) {
                    Node grandChild = child.getFirstChild();
                    if (grandChild instanceof Text) result = ((Text)grandChild).getData();
                }
            }

            return result;
        } catch (Exception e) {
            throw e;
        } finally {
        	if (client != null) {
	            client.getNotificationHandler().enableLog();
	            folder.getRepository().returnSVNClient(client);
        	}
        }
    }

	/**
	 * get a project for the remote folder. The name is either the name of the
	 * remote folder or the name in .project if this file exists.
	 * Project is not created. There is no check to see if the project already exists
	 * @param folder
	 * @param monitor
	 * @return
	 */
	public static IProject getProject(ISVNRemoteFolder folder,IProgressMonitor monitor) throws Exception {
	    final String name = getProjectName(folder, monitor);
        return getProject(name);
	}

	/**
	 * @param name
	 * @return
	 */
	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

    /**
	 * Checkout the remote resources into the local workspace as projects.
	 * Each resource will be checked out into the corresponding project.
	 * You can use getProject to get a project for a given remote Folder
	 *
	 * Resources existing in the local file system at the target project location but now
	 * known to the workbench will be overwritten.
	 */
	public static void checkout(
		final ISVNRemoteFolder[] resources,
		final IProject[] projects,
		final IProgressMonitor monitor)
		throws SVNException {
        CheckoutCommand command = new CheckoutCommand(resources, projects);
        command.run(monitor);
	}

	/**
	 * Create a remote directory in the SVN repository and link the project directory to this remote directory.
	 * The contents of the project are not imported.
     * if remoteDirName is null, the name of the project is used
     * if location is not in repositories, it is added
	 */
	public static void shareProject(ISVNRepositoryLocation location, IProject project, String remoteDirName, String comment, boolean createDirectory, IProgressMonitor monitor) throws TeamException {
		ShareProjectCommand command = new ShareProjectCommand(location, project, remoteDirName, createDirectory);
		command.setComment(comment);
        command.run(monitor);
    }

	/**
	 * Set the sharing for a project to enable it to be used with the SVNTeamProvider.
     * This is used when a project has .svn directory but is not shared in Eclipse.
	 * An exception is thrown if project does not have a remote directory counterpart
	 */
	public static void setSharing(IProject project, IProgressMonitor monitor) throws TeamException {

		// Ensure provided info matches that of the project
		LocalResourceStatus status = peekResourceStatusFor(project);

        // this folder needs to be managed but also to have a remote counter-part
        // because we need to know its url
        // we will change this exception !
        if (!status.hasRemote())
            throw new SVNException(new SVNStatus(IStatus.ERROR, Policy.bind("SVNProvider.infoMismatch", project.getName())));//$NON-NLS-1$

        String repositoryURL = null;
        ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClient();
        try {
            SVNProviderPlugin.disableConsoleLogging();
			ISVNInfo info = client.getInfoFromWorkingCopy(project.getLocation().toFile());
			if (info.getRepository() != null)
				repositoryURL = info.getRepository().toString();
		} catch (SVNClientException e) {
		} finally {
	        SVNProviderPlugin.enableConsoleLogging();
            SVNProviderPlugin.getPlugin().getSVNClientManager().returnSVNClient(client);
		}
		if (repositoryURL == null)
			repositoryURL = status.getUrlString();

		// Ensure that the provided location is managed
		SVNProviderPlugin.getPlugin().getRepositories().getRepository(repositoryURL, false);

		// Register the project with Team
		RepositoryProvider.map(project, SVNProviderPlugin.getTypeId());
	}
	
	public static void upgradeWorkingCopy(IProject project, IProgressMonitor monitor) throws TeamException {
		ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClient();
		try {
			client.upgrade(project.getLocation().toFile());
		} catch (SVNClientException e) {
			throw new TeamException(e.getMessage(), e);
		}
		finally {
			 SVNProviderPlugin.getPlugin().getSVNClientManager().returnSVNClient(client);
		}
	}

    /**
     * get the SVNLocalFolder for the given resource
     */
	public static ISVNLocalFolder getSVNFolderFor(IContainer resource) {
		return new LocalFolder(resource);
	}

    /**
     * get the SVNLocalFile for the given resource
     */
	public static ISVNLocalFile getSVNFileFor(IFile resource) {
		return new LocalFile(resource);
	}

    /**
     * get the SVNLocalResource for the given resource
     */
	public static ISVNLocalResource getSVNResourceFor(IResource resource) {
		if (resource.getType() == IResource.FILE)
			return getSVNFileFor((IFile) resource);
		else // container
			return getSVNFolderFor((IContainer) resource);
	}

    /**
     * get the SVNLocalResources for the given resources
     * @param resources
     * @return
     */
    public static ISVNLocalResource[] getSVNResourcesFor(IResource resources[]) {
        ISVNLocalResource[] svnResources = new ISVNLocalResource[resources.length];
        for (int i = 0; i < resources.length;i++) {
            svnResources[i] = getSVNResourceFor(resources[i]);
        }
        return svnResources;
    }

    /**
     * get the base resource corresponding to the local one
     * @param resource
     * @return
     * @throws SVNException
     */
	public static ISVNRemoteResource getBaseResourceFor(IResource resource) throws SVNException {
		ISVNLocalResource managed = getSVNResourceFor(resource);
		return managed.getBaseResource();
	}

    /**
     * get the latest remote resource corresponding to the local one
     * @param resource
     * @return
     * @throws SVNException
     */
    public static ISVNRemoteResource getLatestResourceFor(IResource resource) throws SVNException {
        ISVNLocalResource managed = getSVNResourceFor(resource);
        return managed.getLatestRemoteResource();
    }

    /**
     * Peek for (get) the resource status.
     * Do not descend to children and DO NOT affect sync cache in any way !
     * @param resource the IResource of which svn status were are looking for
     * @return LocalResourceStatus of the queried resource or null when no status was found (resource is not svn managed)
     */
    public static LocalResourceStatus peekResourceStatusFor(IResource resource) throws SVNException
    {
		PeekStatusCommand command = new PeekStatusCommand(resource);
		command.execute();
		return command.getLocalResourceStatus();
    }

    public static LocalResourceStatus peekResourceStatusFor(IPath path) throws SVNException
    {
		PeekStatusCommand command = new PeekStatusCommand(path);
		command.execute();
		return command.getLocalResourceStatus();
    }

	/**
     * get the repository for this project
	 */
	public ISVNRepositoryLocation getRepository() throws SVNException {
		if (url == null)
        {
            LocalResourceStatus status = localRoot.getStatus();
            if (!status.isManaged()) {
                throw new SVNException(Policy.bind("SVNWorkspaceRoot.notSVNFolder", localRoot.getName()));  //$NON-NLS-1$
            }
            url = status.getUrlString();
        }
		return SVNProviderPlugin.getPlugin().getRepository(url);
	}

    /**
     * get the svn folder corresponding to the project
     */
	public ISVNLocalFolder getLocalRoot() {
		return localRoot;
	}

	/**
	 * Return true if the resource is part of a link (i.e. a linked resource or
	 * one of it's children.
	 *
	 * @param container
	 * @return boolean
	 */
	public static boolean isLinkedResource(IResource resource) {
		return resource.isLinked(IResource.CHECK_ANCESTORS);
	}

	/**
	 * Return true when a resource is a SVN "meta" resource.
	 * I.e. .svn dir or any file within it.
	 * @param resource
	 * @return
	 */
	public static boolean isSvnMetaResource(IResource resource)
	{
		if ((resource.getType() == IResource.FOLDER) && (SVNProviderPlugin.getPlugin().isAdminDirectory(resource.getName())))
			return true;

        IResource parent = resource.getParent();
        if (parent == null) {
            return false;
        }
        else
        {
        	return isSvnMetaResource(parent);
        }
	}

	/**
	 * Return the resource type (FILE, FOLDER, PROJECT) of the resource specified by an absolute filesystem path
	 * @param a resource path relative to workspace root as returned by pathForLocation()
	 * @return IResource.FILE, IResource.FOLDER, IResource.PROJECT or IResource.ROOT or 0 if it could not be determined
	 */
	public static int getResourceType(IPath aResourcePath)
	{
		if (aResourcePath == null) return 0;
		IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(aResourcePath);
		return r == null ? 0 : r.getType();
	}

	/**
	 * Returns workspace resource for the given local file system <code>location</code>
	 * and which is a child of the given <code>parent</code> resource. Returns
	 * <code>parent</code> if parent's file system location equals to the given
	 * <code>location</code>. Returns <code>null</code> if <code>parent</code> is the
	 * workspace root.
	 *
	 * Resource does not have to exist in the workspace in which case resource
	 * type will be determined by the type of the local filesystem object.
	 */
    public static IResource getResourceFor(IResource parent, IPath location) {
    	if (parent == null || location == null) {
    		return null;
    	}

    	if (parent instanceof IWorkspaceRoot) {
    		return null;
    	}

    	if (!isManagedBySubclipse(parent.getProject())) {
    		return null;
    	}

    	if (!parent.getLocation().isPrefixOf(location)) {
    		return null;
    	}

		int segmentsToRemove = parent.getLocation().segmentCount();
    	IPath fullPath = parent.getFullPath().append(location.removeFirstSegments(segmentsToRemove));

    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    	IResource resource = root.findMember(fullPath,true);

    	if (resource != null) {
    		return resource;
    	}

    	if (parent instanceof IFile) {
    		return null;
    	}

		if (fullPath.isRoot()) {
			return root;
		} else if (fullPath.segmentCount() == 1) {
			return root.getProject(fullPath.segment(0));
		}
		
		if (!location.toFile().exists()) {
			if (location.toFile().getName().indexOf(".") == -1) {
				return root.getFolder(fullPath);
			}
		}

		if (location.toFile().isDirectory()) {
			return root.getFolder(fullPath);
		}

		return root.getFile(fullPath);
    }

    public static IResource getResourceFor(IResource parent, ISVNStatus status) {
    	if (status == null || status.getFile() == null) {
    		return null;
    	}
    	return getResourceFor(parent, new Path(status.getFile().getAbsolutePath()));
    }

    public static IResource[] getResourcesFor(IPath location) {
    	return getResourcesFor(location, true);
    }

    /**
     * Gets the resources to which the local filesystem <code>location</code> is corresponding to.
     * The resources do not need to exists (yet)
     * @return IResource[]
     * @throws SVNException
     */
    public static IResource[] getResourcesFor(IPath location, boolean includeProjects) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			IResource resource = getResourceFor(project, location);
			if (resource != null) {
				resources.add(resource);
			}
			if (includeProjects && isManagedBySubclipse(project) && location.isPrefixOf(project.getLocation())) {
				resources.add(project);
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
    }
    
    public static IResource[] getResourcesFor(IResource resource) {
    	Set<IResource> resources = new LinkedHashSet<IResource>();
    	resources.add(resource);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (!project.getLocation().equals(resource.getLocation()) && resource.getLocation().isPrefixOf(project.getLocation())) {
				resources.add(project);
			}
		}
    	return (IResource[]) resources.toArray(new IResource[resources.size()]);
    }

    /**
     * Gets the repository which the local filesystem <code>location</code> belongs to.
     */
    public static ISVNRepositoryLocation getRepositoryFor(IPath location) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (project.getLocation().isPrefixOf(location) && SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				try {
					SVNTeamProvider teamProvider = (SVNTeamProvider)RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId());
					return teamProvider.getSVNWorkspaceRoot().getRepository();
				} catch (SVNException e) {
					// an exception is thrown when resource	is not managed
					SVNProviderPlugin.log(e);
					return null;
				}
			}
		}
		return null;
    }
}

