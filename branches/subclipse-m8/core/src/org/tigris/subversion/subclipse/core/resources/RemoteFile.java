/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * This class provides the implementation of ISVNRemoteFile and IManagedFile for
 * use by the repository and sync view.
 */
public class RemoteFile extends SVNRemoteResource implements ISVNRemoteFile  {

    // buffer for file contents received from the server
    private byte[] contents;
	private static final int BUFSIZE = 0;


    public RemoteFile(RemoteFolder parent, 
                      ISVNRepositoryLocation repository,
                      SVNUrl url,
                      SVNRevision revision,
                      boolean hasProps,
                      SVNRevision.Number lastChangedRevision,
                      Date date,
                      String author) throws SVNException
	{
		super(parent,repository,url,revision,hasProps,lastChangedRevision,date,author);
	}

    public RemoteFile(ISVNRepositoryLocation repository, SVNUrl url, SVNRevision revision) {
        super(repository, url, revision);
    }

    
    
	/**
	 * @see ISVNRemoteFile#getContents()
	 */
	public InputStream getContents() throws IOException{
        // we cache the contents as getContents can be called several times
        // on the same RemoteFile
		
            if (contents == null)
            {
                ISVNClientAdapter svnClient = repository.getSVNClient();
                InputStream inputStream;
                try {
                    inputStream = svnClient.getContent(url, getLastChangedRevision());
                    contents = new byte[inputStream.available()];
                    inputStream.read(contents);
		        } catch (SVNClientException e) {
                    e.printStackTrace();
                    //TODO: don't leave this like this.
		        }
            }
            return new ByteArrayInputStream(contents);
	}

	/*
	 * @see IRemoteResource#members(IProgressMonitor)
	 */
	public ISVNRemoteResource[] members(IProgressMonitor progress) throws TeamException {
		return new ISVNRemoteResource[0];
	}

	/*
	 * @see IRemoteResource#isContainer()
	 */
	public boolean isContainer() {
		return false;
	}

	public boolean equals(Object target) {
		if (this == target)
			return true;
		if (!(target instanceof RemoteFile))
			return false;
		RemoteFile remote = (RemoteFile) target;
		return super.equals(target) && remote.getLastChangedRevision() == getLastChangedRevision();
	}

    public String getCreatorDisplayName(){
        return this.getAuthor();
    }
    public String getContentIdentifier(){
		return this.getRevision().toString();
    }
    public String getComment() throws SVNException{
		ILogEntry[] entries = getLogEntries(new NullProgressMonitor());
        if(entries == null || entries.length ==0) {
            return "None";
        }else {
            return entries[0].getComment();
        }
    }

   

	

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#getStorage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
		try{
			this.getContents();
		}catch(IOException e){
			throw new TeamException(e.getMessage());
			//TODO:this probably shouldn't do this. 
			//also put in the monitor manipulation.
		}
		return (IStorage)this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
	 */
	public byte[] asBytes() {
		ByteArrayOutputStream bos = null;
		byte[] contents = null;
		try{
			InputStream in = getContents();
			bos = new ByteArrayOutputStream();
			byte[] buf = new byte[BUFSIZE];
			while(true){
				int read = in.read(buf,0,buf.length);
				bos.write(buf, 0, read);
				if(read == -1)break;
			}
			bos.flush();
			
			contents = bos.toByteArray();
		}catch(IOException e){
			
		}finally{
			
			try{if(bos!=null)bos.close();}catch(Exception e){}
		}
		return contents;
	}

}
