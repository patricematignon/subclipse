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

import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * svn Cat. 
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public class Cat extends SvnCommand {

    /** url */
    private SVNUrl      url      = null;

    /** destination file. */
    private File        destFile = null;

    /** revision */
    private SVNRevision revision = SVNRevision.HEAD;

    /**
     * {@inheritDoc}
     */
    public void execute() {

        if( destFile == null ) {
            destFile = new File( getProject().getBaseDir(), url.getLastPathSegment() );
        }
        InputStream is = null;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream( destFile );
            is = getClient().getContent( url, revision );
            byte[] buffer = new byte[5000];
            int read = is.read( buffer );
            while( read != -1 ) {
                os.write( buffer, 0, read );
                read = is.read( buffer );
            }
        } catch( IOException e ) {
            throw new BuildException( "Can't get the content of the specified file", e );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't get the content of the specified file", e );
        } finally {
            SvnAntUtilities.close( os );
            SvnAntUtilities.close( is );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrNotNull( "url", url );
        if( destFile != null ) {
            SvnAntUtilities.attrCanWrite( "destFile", destFile );
        }
        SvnAntUtilities.attrNotNull( "revision", revision );
    }

    /**
     * Sets the URL; required.
     * @param url The url to set
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * @param destFile the destFile to set
     */
    public void setDestFile( File destFile ) {
        this.destFile = destFile;
    }

    /**
     * Sets the revision
     * 
     * @param revision
     */
    public void setRevision( String revision ) {
        this.revision = getRevisionFrom( revision );
    }

}
