/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 * This class has an interface which is very similar to ISVNStatus but we make
 * sure to take as little memory as possible. This class also have a getBytes()
 * method and a constructor/factory method that takes bytes.
 * (However, the bytes are not complete representation of this status, just subset of interesting attributes)
 * 
 * This class and neither its LocalResourceStatus which is stored in the workspace metadata does NOT
 * stores resource revision intentionally.
 * The revision numbers changes too frequently and it does not provide too valuable imformation for synchronization
 * needs anyway. The lastChangedRevision() is more important here. 
 * 
 * @see org.tigris.subversion.svnclientadapter.ISVNStatus
 */
public abstract class ResourceStatus implements ISVNStatus, Serializable {

    static final long serialVersionUID = 1L;

    protected static final int FORMAT_VERSION_1 = 1;
    protected static final int FORMAT_VERSION_2 = 2;
    protected static final int FORMAT_VERSION_3 = 3;

    protected transient IResource resource;
    
    protected String url;
    protected File file; // file (absolute path) -- not stored in bytes in this class. Subclasses may store it ...
    protected long lastChangedRevision;
    protected long lastChangedDate;
    protected String lastCommitAuthor;
    protected int textStatus;
    protected int propStatus;
    protected int nodeKind;
	
    protected ResourceStatus() 
    {
    	super();
    }
    
	/**
	 * @param status
	 * @param url - Only needed when status.getUrl is Null, such as
	 *  for an svn:externals folder
	 */
	public ResourceStatus(ISVNStatus status, String url) {
		super();
    	/** a temporary variable serving as immediate cache for various status values */
    	Object aValue = null;

    	aValue = status.getUrlString();
        if (aValue == null) {
        	if (url == null)
        		this.url = null;
        	else
        		this.url = url;
        } else {
            this.url = (String) aValue;
        }

        aValue = status.getLastChangedRevision();
        if (aValue == null) {
            this.lastChangedRevision = SVNRevision.SVN_INVALID_REVNUM;
        } else {
            this.lastChangedRevision = ((SVNRevision.Number) aValue).getNumber();
        }

        aValue = status.getLastChangedDate();
        if (aValue == null) {
            this.lastChangedDate = -1;
        } else {
            this.lastChangedDate = ((Date) aValue).getTime();
        }

        this.lastCommitAuthor = status.getLastCommitAuthor();
        this.textStatus = status.getTextStatus().toInt();
        this.propStatus = status.getPropStatus().toInt();

        this.nodeKind = status.getNodeKind().toInt();
        
        this.file = status.getFile();
	}
	
    public String toString()
    {
    	return ((file != null) ? file.getAbsolutePath() : "") + " (" + lastChangedRevision + ") " + getTextStatus().toString();
    }
	
    /**
     * @return Returns the file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return Returns the absolute resource path.
     * (It is absolute since it was constructed as status.getFile().getAbsolutePath())
     */
    public IPath getIPath() {
        return new Path(getPath());
    }

    /**
     * @return Returns the absolute resource path (as String).
     * (It is absolute since it was constructed as status.getFile().getAbsolutePath())
     */
    public String getPath() {
        return file.getAbsolutePath();
    }

    public SVNStatusKind getTextStatus() {
        return SVNStatusKind.fromInt(textStatus);
    }

    public SVNStatusKind getPropStatus() {
        return SVNStatusKind.fromInt(propStatus);
    }

	/**
	 * Answer a 'join' of text and property statuses.
	 * The text has priority, i.e. the prop does not override the text status
	 * unless it is harmless - i.e. SVNStatusKind.NORMAL
	 * @param textStatus
	 * @param propStatus
	 * @return
	 */
	public SVNStatusKind getStatusKind()
	{
		if (!SVNStatusKind.NORMAL.equals(getTextStatus()) && !SVNStatusKind.NONE.equals(getTextStatus()))
		{
			return getTextStatus(); 
		}
		else
		{
			if (SVNStatusKind.MODIFIED.equals(getPropStatus()) || SVNStatusKind.CONFLICTED.equals(getPropStatus()))
			{
				return getPropStatus();
			}
			else
			{
				return getTextStatus();
			}
		}    		
	}

	public ISVNRepositoryLocation getRepository()
	{
		if (getUrlString() != null)
		{
			try {
				return SVNProviderPlugin.getPlugin().getRepository(getUrlString());
			} catch (SVNException e) {
				// an exception is thrown when resource	is not managed
				SVNProviderPlugin.log(e);
				return null;
			}
		}
		else
		{
			try {
				SVNTeamProvider teamProvider = (SVNTeamProvider)RepositoryProvider.getProvider(getResource().getProject(), SVNProviderPlugin.getTypeId());
				return teamProvider.getSVNWorkspaceRoot().getRepository();
			} catch (SVNException e) {
				// an exception is thrown when resource	is not managed
				SVNProviderPlugin.log(e);
				return null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getNodeKind()
	 */
    public SVNNodeKind getNodeKind() {
        return SVNNodeKind.fromInt(nodeKind);
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getUrl()
	 */
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

    public String getUrlString()
    {
    	return url;
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastChangedRevision()
	 */
    public Number getLastChangedRevision() {
        if (lastChangedRevision == SVNRevision.SVN_INVALID_REVNUM) {
            return null;
        } else {
            return new SVNRevision.Number(lastChangedRevision);
        }
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastChangedDate()
	 */
    public Date getLastChangedDate() {
        if (lastChangedDate == -1) {
            return null;
        } else {
            return new Date(lastChangedDate);
        }
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastCommitAuthor()
	 */
    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }
    
    /**
     * Initialize receivers internal state according the information encoded in the given DataInputStream.
     * (Which contents was created with {@link #getBytes()}
     * @param dis
     * @return encoding format version
     * @throws SVNException
     */
    protected int initFromBytes(StatusFromBytesStream dis) throws SVNException {
    	int version; 
        try {
            version = dis.readInt();
            if (version != FORMAT_VERSION_1 && version != FORMAT_VERSION_2 && version != FORMAT_VERSION_3) {
                throw new SVNException("Invalid format");
            }

            if (version == FORMAT_VERSION_2) {
                readFromVersion2Stream(dis);            	
            } else {
            	readFromVersion3Stream(dis);
            }            
        } catch (IOException e) {
            throw new SVNException(
                    "cannot create RemoteResourceStatus from bytes", e);
        }
        return version;
    }

	private void readFromVersion3Stream(StatusFromBytesStream dis) throws IOException {

        // url
		url = dis.readString();

        // lastChangedRevision
		lastChangedRevision = dis.readLong();

        // lastChangedDate
		lastChangedDate = dis.readLong();

        // lastCommitAuthor
		lastCommitAuthor = dis.readString();

        // textStatus
		textStatus = dis.readInt();

        // propStatus
		propStatus = dis.readInt();

        // nodeKind
		nodeKind=  dis.readInt();
	}

	/**
	 * Just for backwards compatibility with workspaces stored with previous version
	 * @param dis
	 * @throws IOException
	 */
	private void readFromVersion2Stream(StatusFromBytesStream dis) throws IOException {
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
		if ((url == null) || (lastCommitAuthorString.equals(""))) {
		    lastCommitAuthor = null;
		} else {
		    lastCommitAuthor = lastCommitAuthorString;
		}

		// textStatus
		textStatus = dis.readInt();

		// propStatus
		propStatus = dis.readInt();

		// revision
		// originally, the ResourceStatus also contained revision data.
		// we do not store them anymore, but for backwards compatibilty,
		// we maintain the byte[] array offsets so we store/read 0 here.
		//revision = dis.readLong();
		setRevisionNumber(dis.readLong());

		// nodeKind
		nodeKind = dis.readInt();
	}

    /**
     * Get the status encoded in bytes
     * @return byte[] with externalized status 
     */
    public byte[] getBytes() {    	
    	StatusToBytesStream out = new StatusToBytesStream();
        getBytesInto(out);
        return out.toByteArray();
    }

    /**
     * Encode the internal state into given DataOutputStream
     * Decoding is done by {@link #initFromBytes(DataInputStream)}
     * @param dos
     */
    protected void getBytesInto(StatusToBytesStream dos) {
        try {
            dos.writeInt(FORMAT_VERSION_3);

            // url
            dos.writeString(url);

            // lastChangedRevision
            dos.writeLong(lastChangedRevision);

            // lastChangedDate
            dos.writeLong(lastChangedDate);

            // lastCommitAuthor
            dos.writeString(lastCommitAuthor);

            // textStatus
            dos.writeInt(textStatus);

            // propStatus
            dos.writeInt(propStatus);

            // nodeKind
            dos.writeInt(nodeKind);

        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Set the revision number - for internal purposes only.
     * This class does not contain revision anymore.
     * However subclasses might add it.
     * This method is expected to be called from initFromBytes() method only!
     */
    protected void setRevisionNumber(long revision)
    {
    	//Do not set anything. There is no revision here.
    	//However subclass may added it
    }
    
    /**
     * Gets the resource this status is corresponding to.
     * Use either ResourceInfo.getType() or getNodeKind() to determine the proper kind of resource.
     * The resource does not need to exists (yet)
     * @return IResource
     * @throws SVNException
     */
    public synchronized IResource getResource() throws SVNException
    {
    	if (resource == null) {
    		return SVNWorkspaceRoot.getResourceFor(this);
    	}
    	return resource;
    }

    /**
     * Performance optimized ByteArrayOutputStream for storing status data.
   	 * This is one-purpose specialized stream without need for synchronization
   	 * or generic bounds checking
     */
    protected static final class StatusToBytesStream extends ByteArrayOutputStream
    {
    	protected StatusToBytesStream()
    	{
    		//Set the default size which should fit for most cases
    		super(256);
    	}
    	
    	/**
    	 * Overrides the standard {@link ByteArrayOutputStream#write(int)}.
    	 * This is one-purpose specialized stream without need for synchronization.
    	 * The method does not check for available capacity, the {@link #ensureCapacity(int)} has to be explicitely called prior
    	 */
        public final void write(int b) {
        	buf[count] = (byte)b;
        	count++;
        }

        /**
         * Ensure the stream is able to store next n bytes.
         * Grow the array if necessary.
         * @param length
         */
        private void ensureCapacity(int length)
        {
        	int newcount = count + length;
        	if (newcount > buf.length) {
        	    byte newbuf[] = new byte[Math.max(buf.length + 100, newcount)];
        	    System.arraycopy(buf, 0, newbuf, 0, count);
        	    buf = newbuf;
        	}        	
        }
        
    	/**
    	 * Overrides the standard {@link ByteArrayOutputStream#toByteArray()}.
    	 * This is one-purpose stream so we don't have to return the copy of the buffer,
    	 * so we return the ByteArrays' buffer itself directly.
    	 */
        public final byte[] toByteArray() {
        	return buf;
        }

        public final void writeLong(long v) throws IOException {
        	ensureCapacity(8);
            write((byte) (v >>> 56));
            write((byte) (v >>> 48));
            write((byte) (v >>> 40));
            write((byte) (v >>> 32));
            write((byte) (v >>> 24));
            write((byte) (v >>> 16));
            write((byte) (v >>>  8));
            write((byte) (v >>>  0));
        }

        public final void writeInt(int v) throws IOException {
        	ensureCapacity(4);
            write((v >>> 24) & 0xFF);
            write((v >>> 16) & 0xFF);
            write((v >>>  8) & 0xFF);
            write((v >>>  0) & 0xFF);
        }

        public final void writeBoolean(boolean v) throws IOException {
        	ensureCapacity(1);
        	write(v ? 1 : 0);
        }
        
        public final void writeString(String v) throws IOException {
        	int length = (v != null) ? v.length() : 0;
        	writeInt(length);
        	ensureCapacity(length * 2);
        	for (int i = 0; i < length; i++) {
        		char c = v.charAt(i);
                write((c >>> 8) & 0xFF);
                write((c >>> 0) & 0xFF);
			}
        }
    }
    
    /**
     * Performance optimized ByteArrayInputStream for storing status data.
   	 * This is one-purpose specialized stream without need for synchronization
   	 * or generic bounds checking
     */
    protected static final class StatusFromBytesStream extends ByteArrayInputStream
    {
    	private DataInputStream dis;
    	
    	protected StatusFromBytesStream(byte buf[])    	
    	{
    		super(buf);
    		this.dis = new DataInputStream(this);
    	}

    	/**
    	 * Overrides the standatd {@link ByteArrayInputStream#read()}
    	 * This is one-purpose specialized stream without need for synchronization.
    	 */
        public final int read() {
        	return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

    	/**
    	 * Overrides the standatd {@link ByteArrayInputStream#read(byte[], int, int)}
    	 * This is one-purpose specialized stream without need for synchronization.
    	 */
        public final int read(byte b[], int off, int len) {
        	if (pos >= count) {
        	    return -1;
        	}
        	if (pos + len > count) {
        	    len = count - pos;
        	}
        	if (len <= 0) {
        	    return 0;
        	}
        	System.arraycopy(buf, pos, b, off, len);
        	pos += len;
        	return len;
        }

        public final long readLong() throws IOException {
        	return this.dis.readLong();
        }

        public final int readInt() throws IOException {
        	return this.dis.readInt();
        }

        public final boolean readBoolean() throws IOException {
        	return this.dis.readBoolean();
        }

        public final String readString() throws IOException {
        	int length = this.dis.readInt();
        	if (length == 0) {
        		return null;
        	}
        	char[] chars = new char[length];
        	for (int i = 0; i < length; i++) {
				chars[i] = this.dis.readChar();
			}
        	return new String(chars);
        }

        public final String readUTF() throws IOException {
        	return this.dis.readUTF();
        }

    }
}
