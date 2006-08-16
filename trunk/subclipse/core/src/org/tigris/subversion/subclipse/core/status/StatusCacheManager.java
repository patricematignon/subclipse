/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.tigris.subversion.subclipse.core.ISVNCoreConstants;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.commands.GetInfoCommand;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Provides a method to get the status of a resource. <br>   
 * It is much more efficient to get the status of a set a resources than only
 * one resource at a time. For that we use a @link org.tigris.subversion.subclipse.core.status.StatusUpdateStrategy<br>
 * 
 * We use a tree (@link org.tigris.subversion.subclipse.core.status.StatusCacheComposite) to keep the status of the resources  
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class StatusCacheManager implements IResourceChangeListener, Preferences.IPropertyChangeListener {

	/** Name used for identifying SVN synchronization data in Resource>ResourceInfo#syncInfo storage */
	public static final QualifiedName SVN_BC_SYNC_KEY = new QualifiedName(SVNProviderPlugin.ID, "svn-bc-sync-key");

    private IStatusCache statusCache;
    private StatusUpdateStrategy statusUpdateStrategy;
    
    public StatusCacheManager() {
    	chooseUpdateStrategy();
		ResourcesPlugin.getWorkspace().getSynchronizer().add(StatusCacheManager.SVN_BC_SYNC_KEY);
    	statusCache = new SynchronizerSyncInfoCache();
    }

    private void chooseUpdateStrategy() {
        boolean recursiveStatusUpdate = SVNProviderPlugin.getPlugin().getPluginPreferences().getBoolean(ISVNCoreConstants.PREF_RECURSIVE_STATUS_UPDATE);
        statusUpdateStrategy = recursiveStatusUpdate ? (StatusUpdateStrategy)new RecursiveStatusUpdateStrategy(statusCache) : (StatusUpdateStrategy)new NonRecursiveStatusUpdateStrategy(statusCache);
    }
    
	/**
	 * @param resource
	 * @return true when the resource's status is present in cache
	 */
	public boolean hasCachedStatus(IResource resource)
	{
		return statusCache.hasCachedStatus(resource);
	}

    /**
     * A resource which ancestor is not managed is not managed
     * @param resource
     * @return true if an ancestor of the resource is not managed and false 
     *         if we don't know 
     */
    private boolean isAncestorNotManaged(IResource resource) {
        IResource parent = resource.getParent();
        if (parent == null) {
            return false;
        }
        
        while (parent != null) {
            LocalResourceStatus statusParent = statusCache.getStatus(parent);
            
            if (statusParent != null) {
            	if (!statusParent.isManaged()) {
            		return true;
            	}
            }
            parent = parent.getParent();
        }
        return false;
    }
    
    /**
     * update the cache using the given statuses
     * @param statuses
 	 * @param rule the scheduling rule to use when running this operation
     */
    protected List updateCache(final ISVNStatus[] statuses, ISchedulingRule rule) throws CoreException {
    	final List result = new ArrayList(statuses.length);
//    	if (ResourcesPlugin.getWorkspace().isTreeLocked())
//    	{
            for (int i = 0; i < statuses.length;i++) {        	
            	result.add(updateCache(statuses[i]));
            }    		
//    	}
//    	else
//    	{
//    		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
//			public void run(IProgressMonitor monitor) throws CoreException {
//		        for (int i = 0; i < statuses.length;i++) {        	
//		        	result.add(updateCache(statuses[i]));
//		        }
//			}}, rule, IWorkspace.AVOID_UPDATE, null);    		
//    	}
        return result;
    }

    /**
     * update the cache using the given statuses
     * @param status
     * @param workspaceRoot
     */
    protected IResource updateCache(LocalResourceStatus status) {
   		return statusCache.addStatus(status);
    }

    /**
     * update the cache using the given statuses
     * @param status
     * @param workspaceRoot
     */
    protected IResource updateCache(ISVNStatus status) {
   		return statusCache.addStatus(new LocalResourceStatus(status, getURL(status)));
    }

    /**
     * Get the status of the given resource.
     * If the status is not present in cache, it will be retrieved using the actual updateStrategy.
     * If recursive startegy is being used also all child resources would be updated,
     * otherwise only direct childern
     * @param resource whose status is required.
     *   
     * @throws SVNException
     */
    public LocalResourceStatus getStatus(IResource resource) throws SVNException {
        return getStatus(resource, statusUpdateStrategy);
    }

    /**
     * Get the status of the given resource.
     * If the status is not present in cache, it will be retrieved using the specified updateStrategy.
     * If recursive startegy is being used also all child resources would be updated,
     * otherwise only direct childern
     *
     * @param resource whose status is required.
     * @param useRecursiveStartegy when true also children statuses should be recursively updated 
     * @throws SVNException
     */
    public LocalResourceStatus getStatus(IResource resource, boolean useRecursiveStartegy) throws SVNException {
        return getStatus(resource,
				useRecursiveStartegy ? 
						(StatusUpdateStrategy) new RecursiveStatusUpdateStrategy(statusCache)
						: (StatusUpdateStrategy) new NonRecursiveStatusUpdateStrategy(statusCache));
    }

    /**
     * The cached statuses do not provide revision numbers anymore.
     * This method is the only place how to query for the revision of the resource explicitely.
     * @param resource
     * @return
     * @throws SVNException
     */
    public SVNRevision getResourceRevision(ISVNLocalResource resource) throws SVNException
    {    
    	if (resource == null) return null;
        GetInfoCommand command = new GetInfoCommand(resource);
        command.run(null);
        final ISVNInfo info = command.getInfo();

    	return (info != null) ? info.getRevision() : null;
    }
    
    /**
     * get the status of the given resource
     * @throws SVNException
     */
    private LocalResourceStatus getStatus(IResource resource, StatusUpdateStrategy strategy) throws SVNException {
    	if (!resource.exists() && !resource.isPhantom())
    	{
    		return null;
    	}
        LocalResourceStatus status = null;        
        status = statusCache.getStatus(resource);
        
        // we get it using svn 
        if (status == null)
        {
        	status = basicGetStatus(resource, strategy);
        }
        return status;
    }
    
    /**
     * Get the statuse(s) from the svn meta files
     * 
     * @param resource
     * @param strategy
     * @return
     * @throws SVNException
     */
    private LocalResourceStatus basicGetStatus(IResource resource, StatusUpdateStrategy strategy) throws SVNException 
	{
        LocalResourceStatus status = null;

   /* Code commented so that svn:externals that are multi-level deep will be 
    * decorated.  In this scenario, there can be unversioned files in the middle
    * of the svn:externals files.                                               */     
//        if (isAncestorNotManaged(resource)) {
//            // we know the resource is not managed because one of its ancestor is not managed
//       		status = new LocalResourceStatus(new SVNStatusUnversioned(resource.getLocation().toFile(),false)); 
//        } else {
            // we don't know if resource is managed or not, we must update its status
        	strategy.setStatusCache(statusCache);
        	setStatuses(strategy.statusesToUpdate(resource), resource);
        	status = statusCache.getStatus(resource);
//        }
        
        if (status == null) {
            status = new LocalResourceStatus(new SVNStatusUnversioned(resource.getLocation().toFile(),false), null);
        }
        
        return status;
    }

    /**
     * The cache manager handles itself the status retrieving. However this method can
     * be used to update the statuses of some resources  
     * 
     * @param statuses
 	 * @param rule the scheduling rule to use when running this operation
     */
    public void setStatuses(ISVNStatus[] statuses, ISchedulingRule rule) throws SVNException {
    	try {
			updateCache(statuses, rule);
		} catch (CoreException e) {
			throw SVNException.wrapException(e);
		}   		
    }

    /**
	 * Refresh the status of the given resource to the give depth. The depth can
	 * be deeper in case of phantom resources. These have to be traversed to
	 * infinite always ...
	 * 
	 * @param resource
	 * @param depth
	 * @return array of resources which were refreshed (including all phantoms
	 *         and their children)
	 * @throws SVNException
	 */
    public IResource[] refreshStatus(final IResource resource, final int depth) throws SVNException {
    	if (SVNWorkspaceRoot.isLinkedResource(resource)) { return new IResource[0]; }
    	final StatusUpdateStrategy strategy = 
    		(depth == IResource.DEPTH_INFINITE) 
							? (StatusUpdateStrategy) new RecursiveStatusUpdateStrategy(statusCache)
							: (StatusUpdateStrategy) new NonRecursiveStatusUpdateStrategy(statusCache);
		try {
			List refreshedResources = updateCache(strategy.statusesToUpdate(resource), resource);
			Set resourcesToRefresh = resourcesToRefresh(resource, depth, IContainer.INCLUDE_PHANTOMS, refreshedResources.size());
			for (Iterator iter = refreshedResources.iterator(); iter.hasNext();) {
				resourcesToRefresh.remove(iter.next());
			}
			//Resources which were not refreshed above (e.g. deleted resources)
			//We do it with depth = infinite, so the whole deleted trees are refreshed.
			for (Iterator it = resourcesToRefresh.iterator(); it.hasNext();) {
				IResource res = (IResource) it.next();
				if ((res.getType() != IResource.FILE) && res.isPhantom())
				{
					Set children = resourcesToRefresh(res, IResource.DEPTH_INFINITE, IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS, 0);
					for (Iterator iter = children.iterator(); iter.hasNext();) {
						IResource child = (IResource) iter.next();
						statusCache.removeStatus(child);
						refreshedResources.add(child);
					}
				}
				statusCache.removeStatus(res);
				refreshedResources.add(res);
			}
			return (IResource[]) refreshedResources.toArray(new IResource[refreshedResources.size()]);
		}
		catch (CoreException e)
		{
			throw SVNException.wrapException(e);
		}
    }

    private Set resourcesToRefresh(IResource resource, int depth, int flags, int expectedSize) throws CoreException
    {
        if (!resource.exists() && !resource.isPhantom())
        {
            return new HashSet(0);
        }
    	final Set resultSet = (expectedSize != 0) ? new HashSet(expectedSize) : new HashSet();
		resource.accept(new IResourceVisitor() {
			public boolean visit(IResource aResource) throws CoreException {
				resultSet.add(aResource);
				return true;
			}
		}, depth, flags);
		return resultSet;
    }
    
	/**
	 * Purge the status information from the cache
	 * @param root
	 * @param deep
	 */
	public void purgeCache(IContainer root, boolean deep) throws SVNException {
		statusCache.purgeCache(root, deep);
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (ISVNCoreConstants.PREF_RECURSIVE_STATUS_UPDATE.equals(event.getProperty()))  {
            chooseUpdateStrategy();
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 * 
	 * When a resource changes this method will be called in a PRE_BUILD to allow to flush all changes which were not
	 * saved during previous operations when the workspace was locked.
     *
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		statusCache.flushPendingStatuses();
	}

    // getStatuses returns null URL for svn:externals folder.  This will
    // get the URL using svn info command on the local resource
	private String getURL(ISVNStatus status) {
		String url = status.getUrlString();
		if (url == null && !(status.getTextStatus() == SVNStatusKind.UNVERSIONED) 
				&& !(status.getTextStatus() == SVNStatusKind.IGNORED)) {
		    try { 
		    	ISVNClientAdapter svnClient = SVNProviderPlugin.getPlugin().createSVNClient();
		    	ISVNInfo info = svnClient.getInfoFromWorkingCopy(status.getFile());
		    	SVNUrl svnurl = info.getUrl();
		    	url = (svnurl != null) ? svnurl.toString() : null;
		    } catch (SVNException e) {
			} catch (SVNClientException e) {
			}
		}
		return url;
	}

}
