/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 * This class has an interface which is very similar to ISVNStatus but we make sure
 * to take as little memory as possible. This class also have a getBytes() method
 * and a constructor that takes bytes.
 * @see org.tigris.subversion.svnclientadapter.ISVNStatus
 */
public class LocalResourceStatus {
	private static int FORMAT_VERSION_1 = 1;
    protected String url;
    protected long lastChangedRevision;
    protected long lastChangedDate;
    protected String lastCommitAuthor;
    protected int textStatus;
    protected int propStatus;
    protected long revision;
    protected int nodeKind;
    protected String urlCopiedFrom;
    protected String path; // absolute path

    public LocalResourceStatus() {
        
    }
    
    public LocalResourceStatus(ISVNStatus status) {
    	if (status.getUrl() == null) {
    		this.url = null;
        } else {
        	this.url = status.getUrl().toString();
        }
        
        if (status.getLastChangedRevision() == null) {
        	this.lastChangedRevision = SVNRevision.SVN_INVALID_REVNUM;
        } else {
        	this.lastChangedRevision = status.getLastChangedRevision().getNumber();
        }
        
        if (status.getLastChangedDate() == null) {
        	this.lastChangedDate = -1;
        } else {
            this.lastChangedDate = status.getLastChangedDate().getTime();   
        }
            
        this.lastCommitAuthor = status.getLastCommitAuthor();
        this.textStatus = status.getTextStatus().toInt();
        this.propStatus = status.getPropStatus().toInt();
        
        if (status.getRevision() == null) {
            this.revision = SVNRevision.SVN_INVALID_REVNUM;
        } else {
        	this.revision = status.getRevision().getNumber();
        }
        
        this.nodeKind = status.getNodeKind().toInt();
        
        if (status.getUrlCopiedFrom() == null) {
        	this.urlCopiedFrom = null;
        } else {
        	this.urlCopiedFrom = status.getUrlCopiedFrom().toString();
        }
        this.path = status.getFile().getAbsolutePath();
    }
    
	public SVNUrl getUrl() {
		if (url == null) {
			return null;
        } else {
        	try {
				return new SVNUrl(url);
			} catch (MalformedURLException e) {
                return null;
			}
        }
	}

	public Number getLastChangedRevision() {
        if (lastChangedRevision == SVNRevision.SVN_INVALID_REVNUM) {
        	return null;
        } else {
        	return new SVNRevision.Number(lastChangedRevision);
        }
	}

	public Date getLastChangedDate() {
		if (lastChangedDate == -1) {
			return null;
        } else {
        	return new Date(lastChangedDate);
        }
	}

	public String getLastCommitAuthor() {
		return lastCommitAuthor;
	}

	public SVNStatusKind getTextStatus() {
		return SVNStatusKind.fromInt(textStatus);
	}

	public SVNStatusKind getPropStatus() {
		return SVNStatusKind.fromInt(propStatus);
	}

    public Number getRevision() {
        if (revision == SVNRevision.SVN_INVALID_REVNUM) {
            return null;
        } else {
            return new SVNRevision.Number(revision);
        }
	}

	public SVNNodeKind getNodeKind() {
		return SVNNodeKind.fromInt(nodeKind);
	}

	public SVNUrl getUrlCopiedFrom() {
        if (urlCopiedFrom == null) {
            return null;
        } else {
            try {
                return new SVNUrl(urlCopiedFrom);
            } catch (MalformedURLException e) {
                return null;
            }
        }
	}
    
    /**
     * @return Returns the file.
     */
    public File getFile() {
        return new File(path);
    }
    
    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
        try {
            dos.writeInt(FORMAT_VERSION_1);
            
            // url
            if (url == null) {
            	dos.writeUTF("");
    		} else {
    			dos.writeUTF(url);
    		}
            
            // lastChangedRevision
  			dos.writeLong(lastChangedRevision);
            
            // lastChangedDate
   			dos.writeLong(lastChangedDate);
            
            // lastCommitAuthor
    		if (lastCommitAuthor == null) {
    			dos.writeUTF("");
    		} else {
    			dos.writeUTF(lastCommitAuthor);
    		}
            
            // textStatus
    		dos.writeInt(textStatus);
            
            // propStatus
    		dos.writeInt(propStatus);
            
            // revision
   			dos.writeLong(revision);

            // nodeKind
    		dos.writeInt(nodeKind);
    		
            // urlCopiedFrom
            if (urlCopiedFrom == null) {
    			dos.writeUTF("");
    		} else {
    			dos.writeUTF(urlCopiedFrom);
    		}
            
            // file
            dos.writeUTF(path);
        } catch (IOException e) {
            return null;
        }        
		return out.toByteArray();
    }
    
    public LocalResourceStatus(byte[] bytes) throws SVNException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(in);
        try {
            if (dis.readInt() != FORMAT_VERSION_1) {
            	throw new SVNException("Invalid format");
            }
            
            // url
        	String urlString = dis.readUTF();
            if (urlString.equals("")) {
            	url = null;
            } else {
            	url = urlString;
            }

            // lastChangedRevision
            lastChangedRevision = dis.readLong();
            
            // lastChangedDate
            lastChangedDate = dis.readLong();

            // lastCommitAuthor
            String lastCommitAuthorString = dis.readUTF(); 
            if ((url == null) && (lastCommitAuthorString.equals(""))) {
                lastCommitAuthor = null;
            } else {
                lastCommitAuthor = lastCommitAuthorString;
            }

            // textStatus
            textStatus = dis.readInt();
            
            // propStatus
            propStatus = dis.readInt();

            // revision
            revision = dis.readLong();
            
            // nodeKind
            nodeKind = dis.readInt();
            
            // urlCopiedFrom
            String urlCopiedFromString = dis.readUTF();
            if (urlCopiedFromString.equals("")) {
                urlCopiedFrom = null;
            } else {
                urlCopiedFrom = urlString;
            }
            
            // path
            path = dis.readUTF();
        } catch (IOException e){
        	throw new SVNException("cannot create LocalResourceStatus from bytes",e);
        }   
    }

    
    /**
     * Returns if is managed by svn (added, normal, modified ...)
     * @return if managed by svn
     */
    public boolean isManaged()
    {
        SVNStatusKind textStatus = getTextStatus();
        return ((!textStatus.equals(SVNStatusKind.UNVERSIONED)) &&
                (!textStatus.equals(SVNStatusKind.NONE)) &&
                (!textStatus.equals(SVNStatusKind.IGNORED)));
    }

    /**
     * Returns if the resource has a remote counter-part
     * @return has version in repository
     */
    public boolean hasRemote()
    {
        SVNStatusKind textStatus = getTextStatus();
        return ((isManaged()) && (!textStatus.equals(SVNStatusKind.ADDED)));
    }    
    
    public boolean isAdded() {
    	return getTextStatus().equals(SVNStatusKind.ADDED);
    }
 
    public boolean isDeleted() {
        return getTextStatus().equals(SVNStatusKind.DELETED);
    }
    
    public boolean isIgnored() {
        return getTextStatus().equals(SVNStatusKind.IGNORED);
    }

    public boolean isTextMerged() {
        return getTextStatus().equals(SVNStatusKind.MERGED);
    }
    
    
    public boolean isTextModified() {
        return getTextStatus().equals(SVNStatusKind.MODIFIED);
    }

    public boolean isTextConflicted() {
        return getTextStatus().equals(SVNStatusKind.CONFLICTED);
    }    
    
    public boolean isCopied() {
        return urlCopiedFrom != null; 
    }
    
}
