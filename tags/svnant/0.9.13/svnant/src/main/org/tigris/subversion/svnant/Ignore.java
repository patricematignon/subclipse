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
import java.io.FileFilter;

import org.apache.tools.ant.BuildException;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Ignore. put patterns to svn:ignore property
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class Ignore extends SvnCommand {
    /** file to ignore */
    private File file = null;

    /** do not fail when file or directory to ignore is not found */
    private boolean failonerror = false;

    /** directory to which to add patterns */
    private File dir = null;

    /** ignore recursively ? (only for dir attribute) */
    private boolean recurse = false;

    /** pattern to add to .svnignore */
    private String pattern;

    private ISVNClientAdapter svnClient;

    public void execute(ISVNClientAdapter svnClient) throws BuildException {
        this.svnClient = svnClient;
        validateAttributes();

		log("Svn : Putting patterns to svn:ignore property :");

        // deal with the single file
        if (file != null) {
            svnIgnoreFile(file);
        }

        // deal with a directory
        if (dir != null) {
            svnIgnorePattern(dir, pattern, recurse);
        }
    }

    /**
     * Ensure we have a consistent and legal set of attributes
     */
    protected void validateAttributes() throws BuildException {

        if ((file == null) && (dir == null))
            throw new BuildException("file or dir must be set");
        
        if (dir != null) {
            if (file != null)
                throw new BuildException("file must not be set when dir attribute is present");
            if (pattern == null)
                throw new BuildException("pattern must be set when dir attribute is present");            
        }

        if (file != null) {
            if (pattern != null)
                throw new BuildException("pattern must not be set when file attribute is present");            
        }

    }

    /**
     * Add a file to svn:ignore
     * @param svnClient
     * @param file
     * @throws BuildException
     */
    private void svnIgnoreFile(File file) throws BuildException {
        if (file.exists()) {
            try {
                svnClient.addToIgnoredPatterns(file.getParentFile(),file.getName());
            } catch (SVNClientException e) {
                throw new BuildException(
                        "Can't add file "+file.getAbsolutePath()+"to svn:ignore",e);
            }
        } else {
            String message = "Warning: Could not find file "
                    + file.getAbsolutePath();
            if (!failonerror) {
                log(message);
            } else {
                throw new BuildException(message);
            }
        }
    }

    /**
     * add the pattern to svn:ignore property on the directory
     * @param svnClient
     * @param dir
     * @param recursive
     * @throws BuildException
     */
    private void svnIgnorePattern(File dir, String pattern, boolean recursive) throws BuildException {
        
        // first add the pattern to the directory
        svnIgnorePattern(dir, pattern);
        
        if (recursive) {
            File file[] = dir.listFiles( new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && !pathname.getName().equals(".svn"); 
                }
            });
            for (int i = 0; i < file.length;i++) {
                svnIgnorePattern(file[i],pattern,true);
            }
        }
    }

    private void svnIgnorePattern(File dir, String pattern) throws BuildException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                log(
                    "Can't add a pattern to svn:ignore for a file. It needs to be a directory");
            } else {

                try {
                    svnClient.addToIgnoredPatterns(dir, pattern);
                } catch (SVNClientException e) {
                    throw new BuildException(
                        "Can't add pattern "+pattern+" to svn:ignore for "+dir.getAbsolutePath(), e);
                }
            }
        } else {
            String message = "Warning: Could not find directory "+dir.getAbsolutePath();
            if (!failonerror) {
                log(message);
            } else {
                throw new BuildException(message);
            }
        }

        
        
    }

	/**
	 * set file to ignore
	 * @param file
	 */
    public void setFile(File file) {
        this.file = file;
    }

	/**
	 * set the directory on which we will update the svn:ignore property
	 * @param dir
	 */
    public void setDir(File dir) {
        this.dir = dir;
    }

	/**
	 * if set, pattern will be added recursively on svn:ignore
	 * @param recursive
	 */
    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    /**
     * set the pattern to add to svn:ignore on the directory
     * @param pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
