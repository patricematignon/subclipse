/*
 * Created on 20 Ιουλ 2004
 */
package org.tigris.subversion.subclipse.core.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.ISVNStatus.Kind;

/**
 * @author Panagiotis K
 */
public class SVNStatusSyncInfo extends SyncInfo {
    
    private final ISVNStatus status;

    public SVNStatusSyncInfo(IResource local,
            				 ISVNStatus status,
            				 SVNRevision.Number revision,
            				 IResourceVariantComparator comparator) {
        super(local,
              createBaseResourceVariant( local, status),
              createLatestResourceVariant( local, status, revision),
              comparator);
        this.status = status;
    }
    /* (non-Javadoc)
     * @see org.eclipse.team.core.synchronize.SyncInfo#calculateKind()
     */
    protected int calculateKind() throws TeamException {
        Kind localKind = status.getTextStatus();
        Kind repositoryKind = status.getRepositoryTextStatus();

        if( localKind == Kind.NONE 
         || localKind == Kind.MISSING
         || localKind == Kind.INCOMPLETE) {
            return SyncInfo.INCOMING | SyncInfo.ADDITION;
        }
        else if( isDeletion( localKind ) ) {
            if( isChange( repositoryKind ) )
                return SyncInfo.CONFLICTING | SyncInfo.DELETION;
            if( isDeletion( repositoryKind ) )
                return SyncInfo.IN_SYNC;
            return SyncInfo.OUTGOING | SyncInfo.DELETION;
        }
        else if( isChange(localKind) ) {
            if( isChange( repositoryKind )
             || isAddition( repositoryKind ) 
             || isDeletion( repositoryKind ))
                return SyncInfo.CONFLICTING | SyncInfo.CHANGE;
            else
                return SyncInfo.OUTGOING | SyncInfo.CHANGE;
        }
        else if( isAddition( localKind ) ) {
            if( isAddition( repositoryKind ) )
                return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
            return SyncInfo.OUTGOING | SyncInfo.ADDITION;
        }
        else if( isNotModified(localKind) ) {
            if( isNotModified( repositoryKind) )
                return SyncInfo.IN_SYNC;
            if( repositoryKind == Kind.DELETED )
                return SyncInfo.INCOMING | SyncInfo.DELETION;
            if( repositoryKind == Kind.ADDED )
                return SyncInfo.CONFLICTING | SyncInfo.ADDITION;
            return SyncInfo.INCOMING | SyncInfo.CHANGE;
        }

       return SyncInfo.IN_SYNC;
    }
    private boolean isDeletion(Kind kind) {
        return kind == Kind.DELETED;
    }

    private boolean isChange(Kind kind) {
        return kind == Kind.MODIFIED 
              || kind == Kind.REPLACED
              || kind == Kind.OBSTRUCTED
              || kind == Kind.CONFLICTED
              || kind == Kind.MERGED;
    }
    private boolean isNotModified(Kind kind) {
        return kind == Kind.NORMAL
              || kind == Kind.EXTERNAL
              || kind == Kind.IGNORED;
    }
    private static boolean isAddition(Kind kind) {
        return kind == Kind.ADDED || kind == Kind.UNVERSIONED;
    }
    private static IResourceVariant createBaseResourceVariant(IResource resource, ISVNStatus status) {
        if( status.getRepositoryTextStatus() == Kind.ADDED )
            return null;
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor( resource );
        if( resource.getType() == IResource.FILE ) {
            return new RemoteFile( null, 
                  svnResource.getRepository(),
      			  status.getUrl(),
      			  status.getLastChangedRevision(),
    			  status.getLastChangedRevision(),
    			  status.getLastChangedDate(),
    			  status.getLastCommitAuthor());
        }
        else {
            return new RemoteFile( null,
                  svnResource.getRepository(),
        		  status.getUrl(),
        		  status.getLastChangedRevision(),
      			  status.getLastChangedRevision(),
      			  status.getLastChangedDate(),
      			  status.getLastCommitAuthor());
        }
    }

    private static IResourceVariant createLatestResourceVariant(IResource resource, ISVNStatus status, SVNRevision.Number revision) {
        if( status.getRepositoryTextStatus() == Kind.DELETED )
            return null;
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor( resource );
        if( resource.getType() == IResource.FILE ) {
            return new RemoteFile( null, 
                  svnResource.getRepository(),
                  svnResource.getUrl(),
    			  revision,
    			  revision,
    			  null,
    			  null);
        }
        else {
            return new RemoteFile( null,
                  svnResource.getRepository(),
                  svnResource.getUrl(),
      			  revision,
      			  revision,
      			  null,
      			  null);
        }
    }
}
