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
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;

/**
 * svn propset. Set a property
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class Propset extends SvnCommand {
    /** the path of the file or dir on which to set the property */
    private File path = null;

    private File file;    
    private String propName = null;
    private String propValue = null;
    private boolean recurse = false;
    private SVNClientAdapter svnClient;

    public void execute(SVNClientAdapter svnClient) throws BuildException {
        this.svnClient = svnClient;
        validateAttributes();

        log("Svn : Propset");

        try {
            if (propValue != null)
                svnClient.propertySet(path,propName,propValue,recurse);
            else
                svnClient.propertySet(path,propName,file,recurse);
        } catch (ClientException e) {
            throw new BuildException("Can't set property "+propName, e);
        } catch (IOException e) {
            throw new BuildException("Can't set property "+propName, e);
        }
    }

    /**
     * Ensure we have a consistent and legal set of attributes
     */
    protected void validateAttributes() throws BuildException {
        if (path == null)
            throw new BuildException("path attribute must be set");
        if (propName == null)
            throw new BuildException("name attribute must be set");
        if ((propValue == null) && (file == null))
            throw new BuildException("value or file attribute must be set");
            
        if ((propValue != null) && (file != null))
            throw new BuildException("file attribute must not be set when value attribute is set");
            

    }

    /**
     * set the path of the file or directory on which to set the property
     */
    public void setPath(File path) {
        this.path = path;
    }

    /**
     * set the name of the property 
     */
    public void setName(String propName) {
        this.propName = propName;
    }
    
    /**
     * set the value of the property 
     */
    public void setValue(String propValue) {
        this.propValue = propValue;
    }
  
    /**
     * set the file that will be used as a value 
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * if set, property will be set recursively
     * @param recursive
     */
    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

}
