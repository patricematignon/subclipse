/*
 * Created on Apr 7, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.subversion.subclipse.core.sync;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.RemoteResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNStatus;

/**
 * Describes the synchronization of a <b>local</b> resource 
 * relative to a <b>remote</b> resource variant. There are two
 * types of comparison: two-way and three-way. 
 * The {@link IResourceVariantComparator} is used to decide which 
 * comparison type to use. 
 * </p>
 * <p>
 * For two-way comparisons, a <code>SyncInfo</code> node has a change
 * type. This will be one of IN-SYNC, ADDITION, DELETION or CHANGE determined
 * in the following manner.
 * <ul>
 * <li>A resource is considered an ADDITION if it exists locally and there is no remote.
 * <li>A resource is considered an DELETION if it does not exists locally and there is remote.
 * <li>A resource is considered a CHANGE if both the local and remote exist but the 
 * comparator indicates that they differ. The comparator may be comparing contents or
 * timestamps or some other resource state.
 * <li>A resource is considered IN_SYNC in all other cases.
 * </ul>
 * </p><p>
 * For three-way comparisons, the sync info node has a direction as well as a change
 * type. The direction is one of INCOMING, OUTGOING or CONFLICTING. The comparison
 * of the local and remote resources with a <b>base</b> resource is used to determine
 * the direction of the change.
 * <ul>
 * <li>Differences between the base and local resources
 * are classified as <b>outgoing changes</b>; if there is
 * a difference, the local resource is considered the
 * <b>outgoing resource</b>.
 * <li>Differences between the base and remote resources
 * are classified as <b>incoming changes</b>; if there is
 * a difference, the remote resource is considered the
 * <b>incoming resource</b>.
 * <li>If there are both incoming and outgoing changes, the resource 
 * is considered a <b>conflicting change</b>.
 * Again, the comparison of resources is done using the variant comparator provided
 * when the sync info was created.
 * </p>
 * @since 3.0
 */
public class SVNSyncInfo extends SyncInfo {

	/**
	 * @param local
	 * @param base
	 * @param remote
	 * @param comparator
	 * @todo Generated comment
	 */
	public SVNSyncInfo(IResource local, IResourceVariant base,
			IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, base, remote, comparator);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo#calculateKind()
	 */
	protected int calculateKind() throws TeamException {

		RemoteResource remote = (RemoteResource) getRemote();
		RemoteResource base = (RemoteResource)getBase();
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(getLocal());
		boolean remoteExists = (remote != null);
		//TODO: make 3 way
		//diff between base & local
		//diff between base & remote
		//diff between base & both
		if (!remoteExists && local.exists()) {
			return ADDITION;
		} else if (local.getStatus().getTextStatus() == ISVNStatus.Kind.MODIFIED) {
			return CHANGE;
		} else if (!local.exists() && remoteExists) {
			return DELETION;
		} else if (!getComparator().compare(getLocal(), getRemote())) {
			return CHANGE;
		} else {
			return IN_SYNC;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo#getLocalContentIdentifier()
	 */
	public String getLocalContentIdentifier() {
		IResource local = getLocal();
		try {
			if (local != null || local.getType() == IResource.FILE) {
				// it's a file, return the revision number if we can find one
				ISVNLocalFile cvsFile = SVNWorkspaceRoot
						.getSVNFileFor((IFile) local);
				return cvsFile.getStatus().getLastChangedRevision().toString();
			} else if (local != null || local.getType() == IResource.FOLDER) {
				ISVNLocalFolder svnFolder = SVNWorkspaceRoot
						.getSVNFolderFor((IFolder) local);
				return svnFolder.getStatus().getLastChangedRevision()
						.toString();
			}
		} catch (SVNException e) {
			SVNProviderPlugin.log(e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return getLocal().getName() + " " + getLocalContentIdentifier();
	}

}