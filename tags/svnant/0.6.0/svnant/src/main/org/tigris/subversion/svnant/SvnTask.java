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
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;

/**
 * Svn Task
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTask extends Task {
    private String username = null;
    private String password = null;
    private Vector commands = new Vector();
    private int logLevel = 0;
    private File logFile = new File("svn.log");

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public void setLogLevel(int logLevel) {
		if (logLevel > 3)
			logLevel = 3;
		else
		if (logLevel < 0)
			logLevel = 0;
		this.logLevel = logLevel;
	}

	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

    public void addCheckout(Checkout a) {
        commands.addElement(a);
    }

    public void addAdd(Add a) {
        commands.addElement(a);
    }

    public void addCommit(Commit a) {
        commands.addElement(a);
    }

    public void addCopy(Copy a) {
        commands.addElement(a);
    }

    public void addDelete(Delete a) {
        commands.addElement(a);
    }

    public void addExport(Export a) {
        commands.addElement(a);
    }

    public void addImport(Import a) {
        commands.addElement(a);
    }

    public void addMkdir(Mkdir a) {
        commands.addElement(a);
    }

    public void addMove(Move a) {
        commands.addElement(a);
    }

    public void addUpdate(Update a) {
        commands.addElement(a);
    }
    
    public void addPropset(Propset a) {
        commands.addElement(a);
    }

    public void execute() throws BuildException {
    	
    	// this must be done before creating client !

        SVNClientAdapter svnClient = new SVNClientAdapter();

        if (username != null)
            svnClient.setUsername(username);

        if (password != null)
            svnClient.setPassword(password);
            
        if (logLevel != 0)
			SVNClientAdapter.enableLogging(logLevel,logFile);

        for (int i = 0; i < commands.size(); i++) {
            SvnCommand command = (SvnCommand) commands.elementAt(i);
            Feedback feedback = new Feedback(command);
			svnClient.addNotifyListener(feedback);
            command.execute(svnClient);
            svnClient.removeNotifyListener(feedback);
        }
        
        // disable logging
		if (logLevel != 0)
			SVNClientAdapter.enableLogging(0,logFile);        
    }

}