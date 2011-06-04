/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnant.commands;

import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.Project;

import java.io.File;

/**
 * Fetches a summary of working copy status, similar to svn's svnversion utility.
 * 
 * @author Matt Doran (matt.doran@papercut.biz)
 */
public class WcVersion extends SvnCommand {

    private static final String MSG_CANT_GET_SUMMARY    = "Can't get summary status for path %s";
    private static final String MSG_UNMANAGED_PATH      = "The path '%s' is not under version control !";
    
    private File    path;
    private String  prefix;
    private boolean processUnversioned = false;

    /**
     * {@inheritDoc}
     */
    public void execute() {

        WCVersionSummary wcVersionSummary;

        try {
            wcVersionSummary = getWorkingCopySumary( getPath() );
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_CANT_GET_SUMMARY, getPath() );
        }

        // Save the status to ant properties.
        Project p = getProject();

        if( prefix == null ) {
            prefix = "";
        } else if( !prefix.endsWith( "." ) ) {
            prefix = prefix + ".";
        }

        p.setNewProperty( prefix + "repository.url", wcVersionSummary.reposURL );
        p.setNewProperty( prefix + "repository.path", wcVersionSummary.reposPath );

        p.setNewProperty( prefix + "revision.max", wcVersionSummary.getMaxRevision() );
        p.setNewProperty( prefix + "revision.max-with-flags", wcVersionSummary.getMaxRevisionWithFlags() );
        p.setNewProperty( prefix + "revision.range", wcVersionSummary.getRevisionRange() );

        p.setNewProperty( prefix + "committed.max", wcVersionSummary.getMaxCommitted() );
        p.setNewProperty( prefix + "committed.max-with-flags", wcVersionSummary.getMaxCommittedWithFlags() );

        if( wcVersionSummary.hasModified ) {
            p.setNewProperty( prefix + "modified", "true" );
        }

        if( wcVersionSummary.hasMixed ) {
            p.setNewProperty( prefix + "mixed", "true" );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrIsDirectory( "path", path );
    }

    /**
     * Fetch the summary version information for the given working copy path.
     * @param svnClient The svn client used to fetch the status.
     * @param wcpathfile The working copy path.
     * @return The <code>WCVersionSummary</code> storing version information about the working copy.
     * @throws SVNClientException Raised if there is a problem fetching working copy status.
     */
    private WCVersionSummary getWorkingCopySumary( File wcpathfile ) throws SVNClientException {
        ISVNStatus rootstatus = getClient().getSingleStatus( wcpathfile );
        if( rootstatus.getUrl() == null ) {
            throw ex( MSG_UNMANAGED_PATH, wcpathfile.getAbsolutePath() );
        }
        ISVNStatus[] statuses = getClient().getStatus( wcpathfile, true, true );
        return new WCVersionSummary( rootstatus, statuses, wcpathfile, processUnversioned );
    }

    /**
     * @return Returns the path.
     */
    public File getPath() {
        return path;
    }

    /**
     * @param path The path to set.
     */
    public void setPath( File path ) {
        this.path = path;
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    /**
     * @return Returns whether unversioned resources should be processed.
     */
    public boolean getProcessUnversioned() {
        return processUnversioned;
    }

    /**
     * @param processUnversioned
     *            Whether unversioned resources should be processed.
     */
    public void setProcessUnversioned( boolean processUnversioned ) {
        this.processUnversioned = processUnversioned;
    }

    /**
    * Holds summary status information about a working copy path.
    * 
    * @author Matt Doran (matt.doran@papercut.biz)
    */
    private static class WCVersionSummary {

        protected long    maxRevision  = 0;

        protected long    maxCommitted = 0;

        protected long    minRevision  = 0;

        protected boolean hasModified  = false;

        protected boolean hasMixed     = false;

        protected String  reposURL;

        protected String  reposPath;

        /**
         * Fetch the summary status information for the given working copy path.
         * @param svnClient The svn client used to fetch the status.
         * @param wcPathFile The working copy path.
         * @throws SVNClientException Raised if there is a problem fetching working copy status.
         */
        protected WCVersionSummary( ISVNStatus rootStatus, ISVNStatus[] statuses, File wcPathFile,
                        boolean processUnversioned ) {
            this.reposURL = rootStatus.getUrl().toString();

            String[] pathSegs = rootStatus.getUrl().getPathSegments();
            StringBuffer pathBuffer = new StringBuffer();
            for( int i = 0; i < pathSegs.length; i++ ) {
                pathBuffer.append( '/' ).append( pathSegs[i] );
            }
            this.reposPath = pathBuffer.toString();

            for( int i = 0; i < statuses.length; i++ ) {
                ISVNStatus status = statuses[i];

                if( !SVNStatusUtils.isManaged( status ) && !processUnversioned ) {
                    // Don't care about unversioned files
                    continue;
                }

                if( !this.hasModified && ((status.getTextStatus() != SVNStatusKind.NORMAL && status.getTextStatus() != SVNStatusKind.IGNORED) || (status.getPropStatus() != SVNStatusKind.NORMAL && status.getPropStatus() != SVNStatusKind.NONE)) ) {
                    this.hasModified = true;
                }

                if( SVNStatusUtils.isManaged( status ) ) {
                    
                    if( isExternal( rootStatus, status ) ) {
                        // don't include externals for the calculation process
                        continue;
                    }
                    
                    SVNRevision.Number rev = status.getLastChangedRevision();
                    long revNum = (rev != null) ? rev.getNumber() : 0;
                    if( revNum > this.maxRevision ) {
                        this.maxRevision = revNum;
                    }

                    if( revNum < this.minRevision ) {
                        this.minRevision = revNum;
                    }

                    SVNRevision.Number comRev = status.getLastChangedRevision();
                    long committedRev = (comRev != null) ? comRev.getNumber() : 0;
                    if( committedRev > this.maxCommitted ) {
                        this.maxCommitted = committedRev;
                    }
                }
            }

            if( (this.minRevision > 0) && (this.minRevision != this.maxRevision) ) {
                this.hasMixed = true;
            }
        }
        
        /**
         * Returns <code>true</code> if a status is associated with an external resources.
         * 
         * @param base      The base for the version calculation. Not <code>null</code>.
         * @param current   The current status to be used for the version calculation. Not <code>null</code>.
         * 
         * @return   <code>true</code> <=> The current status is an external.
         */
        private boolean isExternal( ISVNStatus base, ISVNStatus current ) {
            return ! current.getUrlString().startsWith( base.getUrlString() );
        }

        /**
         * Get the max revision.
         * @return The max revision
         */
        protected String getMaxRevision() {
            return String.valueOf( maxRevision );
        }

        /**
         * Get the max revision with flags indicating other status information.
         * @return The max revision with flags.
         */
        protected String getMaxRevisionWithFlags() {
            return getMaxRevision() + getFlags();
        }

        protected String getMaxCommitted() {
            return String.valueOf( maxCommitted );
        }

        protected String getMaxCommittedWithFlags() {
            return getMaxCommitted() + getFlags();
        }

        /**
         * Gets the revision range including flags.  If there is a mixed working copy the format of the
         * range is <min>:<max><flags>.
         * 
         * If the working copy is not mixed, the same output as <code>getMaxRevisionWithFlags()</code> is returned.
         * 
         * @return The revision range string.
         */
        protected String getRevisionRange() {
            if( hasMixed ) {
                return String.valueOf( minRevision ) + ":" + getMaxRevisionWithFlags();
            } else {
                return getMaxRevisionWithFlags();
            }
        }

        /**
         * Returns status flags string. (M - modified, X - mixed).
         * @return The status flags.
         */
        private String getFlags() {
            return (hasModified ? "M" : "") + (hasMixed ? "X" : "");
        }

    }
}
