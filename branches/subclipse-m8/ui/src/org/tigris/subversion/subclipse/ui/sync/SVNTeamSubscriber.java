/*
 * Created on Mar 29, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.subversion.subclipse.ui.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayRemoteTree;
import org.eclipse.team.core.variants.ThreeWaySubscriber;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

/**
 * @author mml
 *
 * @todo To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SVNTeamSubscriber extends ThreeWaySubscriber {
	
	public static String SUBSCRIBER_ID="svnsubscriber";
	private static SVNTeamSubscriber instance;
	
	public static SVNTeamSubscriber getInstance(){
		if(instance == null){
			instance = new SVNTeamSubscriber();
		}
		return instance;
	}
	
	/**
	 * @param synchronizer
	 * @todo Generated comment
	 */
	protected SVNTeamSubscriber(ThreeWaySynchronizer synchronizer) {
		super(synchronizer);
		instance = this;
	
	}
	
	public SVNTeamSubscriber() {
		
		super(new ThreeWaySynchronizer(new QualifiedName(SVNUIPlugin.ID, "workpsace-sync"))); //$NON-NLS-1$
		instance = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.ThreeWaySubscriber#getResourceVariant(org.eclipse.core.resources.IResource, byte[])
	 */
	public IResourceVariant getResourceVariant(IResource resource, byte[] bytes)
			throws TeamException {
		SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(resource.getProject(), SVNTeamProvider.PROVIDER_ID);
		return provider.getSVNWorkspaceRoot().getRepository().getRemoteFile(resource.getFullPath().toString());
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.ThreeWaySubscriber#createRemoteTree()
	 */
	protected ThreeWayRemoteTree createRemoteTree() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#roots()
	 */
	public IResource[] roots() {
		// TODO Auto-generated method stub
		return null;
	}
}
