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
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.ISVNStatus.Kind;

/**
 * @author Panagiotis K
 */
public class SVNStatusSyncInfo extends SyncInfo {
    private final StatusInfo localStatusInfo;
    private final StatusInfo remoteStatusInfo;

    public SVNStatusSyncInfo(IResource local,
            				 StatusInfo localStatusInfo,
            				 StatusInfo remoteStatusInfo,
            				 IResourceVariantComparator comparator) {
        super(local,
              createBaseResourceVariant( local, localStatusInfo, remoteStatusInfo ),
              createLatestResourceVariant( local, localStatusInfo, remoteStatusInfo),
              comparator);
        this.localStatusInfo = localStatusInfo;
        this.remoteStatusInfo = remoteStatusInfo;
    }
    /* (non-Javadoc)
     * @see org.eclipse.team.core.synchronize.SyncInfo#calculateKind()
     */
    protected int calculateKind() throws TeamException {
        Kind localKind = localStatusInfo.getKind();
        Kind repositoryKind = remoteStatusInfo.getKind();

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
                return SyncInfo.INCOMING | SyncInfo.ADDITION;
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

    private static IResourceVariant createBaseResourceVariant(IResource local, StatusInfo localStatusInfo, StatusInfo remoteStatusInfo) {
        if( localStatusInfo == null
                || localStatusInfo.getRevision() == null )
          return null;
        return createResourceVariant(local, localStatusInfo.getRevision());
    }
    private static IResourceVariant createLatestResourceVariant(IResource local, StatusInfo localStatusInfo, StatusInfo remoteStatusInfo) {
        if( remoteStatusInfo == null
                || remoteStatusInfo.getKind() == Kind.DELETED )
            return null;
        if( remoteStatusInfo.getKind() == Kind.NONE && 
            localStatusInfo != null && isAddition(localStatusInfo.getKind()) ) {
            return null;
        }
        return createResourceVariant(local, remoteStatusInfo.getRevision());
    }

    private static IResourceVariant createResourceVariant(IResource local, SVNRevision.Number revision) {
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor( local );
        if( local.getType() == IResource.FILE ) {
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
