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
package org.tigris.subversion.svnant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author cedric
 *
 * Creates a directory directly in a repository or creates a
 * directory on disk and schedules it for addition 
 */
public class Mkdir extends SvnCommand {
    /** the url of dir to create */
    private SVNUrl url = null;

    /** the path to create */
    private File path = null;

    /** message (when url is used) */
    private String message = null;

    public void execute(SVNClientAdapter svnClient) throws BuildException {
        validateAttributes();

		log("Svn : Creating a new directory under revision control");

        if (url != null) {
            try {
                svnClient.mkdir(url, message);
            } catch (ClientException e) {
                throw new BuildException("Can't make dir "+url, e);
            }
        } else {
            try {
                svnClient.mkdir(path);
            } catch (ClientException e) {
                throw new BuildException("Can't make dir "+path, e);
            }
        }

    }

    /**
     * Ensure we have a consistent and legal set of attributes
     */
    protected void validateAttributes() throws BuildException {
        if ((url == null) && (path == null))
            throw new BuildException("url or path attributes must be set");
        if ((url != null) && (path != null))
            throw new BuildException("Either url or path attributes must be set");
        if ((url != null) && (message == null))
            throw new BuildException("Message attribute must be set when url is used");

    }

	/**
	 * set the url of the new directory
	 * @param url
	 */
    public void setUrl(SVNUrl url) {
        this.url = url;
    }

	/**
	 * set the path of the new directory
	 * @param path
	 */
    public void setPath(File path) {
        this.path = path;
    }

	/**
	 * set the message for commit (only when using url)
	 * @param message
	 */
    public void setMessage(String message) {
        this.message = message;
    }

}