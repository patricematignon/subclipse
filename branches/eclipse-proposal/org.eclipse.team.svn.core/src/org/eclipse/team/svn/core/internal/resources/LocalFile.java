/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNKeywords;
import org.eclipse.team.svn.core.internal.ISVNLocalFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNResourceVisitor;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.client.OperationManager;

/**
 * Represents handles to SVN file on the local file system.
 */
public class LocalFile extends LocalResource implements ISVNLocalFile {

	/**
	 * Create a handle based on the given local resource.
	 */
	public LocalFile(IFile file) {
		super(file);
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getBaseResource()
     */
    public ISVNRemoteResource getBaseResource() throws SVNException {   	
		if (!hasRemote()) {// no base if no remote
			return null;
		}
		return new BaseFile(getStatus());
    }	
	
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#refreshStatus()
     */
    public void refreshStatus() throws SVNException {
    	SVNProviderPlugin.getPlugin().getStatusCacheManager().refreshStatus(resource, IResource.DEPTH_ZERO);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#isDirty()
     */
    public boolean isDirty() throws SVNException {
        return getStatus().isDirty();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#accept(org.eclipse.team.svn.core.internal.ISVNResourceVisitor)
     */
    public void accept(ISVNResourceVisitor visitor) throws SVNException {
        visitor.visitFile(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalFile#setKeywords(org.tigris.subversion.svnclientadapter.SVNKeywords)
     */
    public void setKeywords(SVNKeywords svnKeywords) throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.setKeywords(getFile(), svnKeywords, false);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalFile#addKeywords(org.tigris.subversion.svnclientadapter.SVNKeywords)
     */
    public void addKeywords(SVNKeywords svnKeywords) throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.addKeywords(getFile(), svnKeywords);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalFile#removeKeywords(org.tigris.subversion.svnclientadapter.SVNKeywords)
     */
    public void removeKeywords(SVNKeywords svnKeywords) throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.removeKeywords(getFile(), svnKeywords);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalFile#getKeywords()
     */
    public SVNKeywords getKeywords() throws SVNException {
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			return svnClient.getKeywords(getFile());
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e);
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#revert()
     */
    public void revert() throws SVNException {
       super.revert(false);
    }    
}


