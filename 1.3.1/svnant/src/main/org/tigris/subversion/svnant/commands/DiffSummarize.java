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
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.BuildException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Diff summary (as in "svn diff --summarize"). The first version only displays
 * the diff summary as the CLI client does. The objective is to make the
 * resources available for manipulation by other tasks.
 * 
 * @author Michael Ludwig
 */
public class DiffSummarize extends Diff {

    private int     depth          = 1000;   // default depth of directory tree descent
    private boolean ignoreAncestry = true;
    private boolean logToFile      = false;
    private String  encoding       = "UTF-8";

    public void setEncoding( String e ) {
        encoding = e;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutFile( File f ) {
        super.setOutFile( f );
        this.logToFile = true;
    }

    /*
     * ISVNClientAdapter#diffSummarize : parameter ignoreAncestry = false
     * svn CLI : --notice-ancestry
     * svnant : ancestry="true"
     */
    public void setAncestry( boolean b ) {
        this.ignoreAncestry = !b;
    }

    public void setDepth( int d ) {
        this.depth = d;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() {
        BufferedWriter out = null;
        if( logToFile ) {
            File f = getOutFile();
            try {
                log( "output to file: " + f );
                out = new BufferedWriter( new OutputStreamWriter( (new FileOutputStream( f )), encoding ) );
            } catch( IOException ex ) {
                throw ex( ex, ex.getMessage() );
            }
        }
        try {

            // summarize only supported on repo, so on URLs
            logAction( true );

            SVNDiffSummary[] summary = getClient().diffSummarize( getOldUrl(), getOldTargetRevision(), getNewUrl(),
                            getNewTargetRevision(), depth, ignoreAncestry );

            StringBuilder sb = new StringBuilder();
            for( SVNDiffSummary s : summary ) {
                sb.setLength( 0 );
                char first = Character.toUpperCase( s.getDiffKind().toString().charAt( 0 ) );
                if( (first != 'A') && (first != 'M') && (first != 'D') ) {
                    warning( "the diff kind '%s' is currently not known", Character.valueOf( first ) );
                }
                sb.append( s.propsChanged() ? "M" : " " );
                // log(String.format("%s %s %s\n", status, propSt, s.getPath()));
                sb.append( " " );
                sb.append( s.getPath() );
                if( logToFile ) {
                    sb.append( "\n" );
                    out.write( sb.toString() );
                } else {
                    log( sb.toString() );
                }
            }
        } catch( SVNClientException ex ) {
            throw new BuildException( ex );
        } catch( IOException ex ) {
            throw new BuildException( ex );
        } finally {
            SvnAntUtilities.close( out );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        super.validateAttributes();
        SvnAntUtilities.attrNotEmpty( "encoding", encoding );
    }

}
