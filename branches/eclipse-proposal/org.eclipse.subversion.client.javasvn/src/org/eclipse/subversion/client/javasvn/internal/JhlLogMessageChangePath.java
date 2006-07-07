package org.eclipse.subversion.client.javasvn.internal;

import org.eclipse.subversion.client.ISVNLogMessageChangePath;
import org.eclipse.subversion.client.SVNRevision;
import org.tigris.subversion.javahl.ChangePath;

/**
 * This class has been copied from javahl and modified a bit.
 * We cannot use original ChangePath because constructor visibility is package
 */
public class JhlLogMessageChangePath implements ISVNLogMessageChangePath
{
    public JhlLogMessageChangePath(ChangePath changePath)
    {
        this.path = changePath.getPath();
        this.copySrcPath = changePath.getCopySrcPath();
        this.action = changePath.getAction();
        this.copySrcRevision = null;
        if (changePath.getCopySrcRevision() != -1) {
            this.copySrcRevision = new SVNRevision.Number(changePath.getCopySrcRevision());	
        }
    }

	public JhlLogMessageChangePath(String path, SVNRevision.Number copySrcRevision, String copySrcPath, char action)
    {
        this.path = path;
        this.copySrcRevision = copySrcRevision;
        this.copySrcPath = copySrcPath;
        this.action = action;
    }

    /** Path of commited item */
    private String path;

    /** Source revision of copy (if any). */
    private SVNRevision.Number copySrcRevision;

    /** Source path of copy (if any). */
    private String copySrcPath;

    /** 'A'dd, 'D'elete, 'R'eplace, 'M'odify */
    private char action;

    /**
     * Retrieve the path to the commited item
     * @return  the path to the commited item
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Retrieve the copy source revision (if any)
     * @return  the copy source revision (if any)
     */
    public SVNRevision.Number getCopySrcRevision()
    {
    	return copySrcRevision;    
    }

    /**
     * Retrieve the copy source path (if any)
     * @return  the copy source path (if any)
     */
    public String getCopySrcPath()
    {
        return copySrcPath;
    }

    /**
     * Retrieve action performed
     * @return  action performed
     */
    public char getAction()
    {
        return action;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return getPath();
    }
}
