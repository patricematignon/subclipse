/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client;

import java.io.File;


/**
 * A callback interface used for receiving notifications of a progress of
 * a subversion command invocation.
 * 
 * @author C�dric Chabanois <a
 *         href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public interface ISVNNotifyListener {
    
	/**
	 * An enumeration class representing the supported subversion commands/actions.
	 */
    public static final class Command {
        public static final int UNDEFINED = 0;
        public static final int ADD = 1;
        public static final int CHECKOUT = 2;
        public static final int COMMIT = 3;
        public static final int UPDATE = 4;
        public static final int MOVE = 5;
        public static final int COPY = 6;
        public static final int REMOVE = 7;
        public static final int EXPORT = 8;
        public static final int IMPORT = 9;    
        public static final int MKDIR = 10;
        public static final int LS = 11;
        public static final int STATUS = 12;
        public static final int LOG = 13;
        public static final int PROPSET = 14;
        public static final int PROPDEL = 15;
        public static final int REVERT = 16;
        public static final int DIFF = 17;
        public static final int CAT = 18;
        public static final int INFO = 19;
        public static final int PROPGET = 20;
		public static final int PROPLIST = 21;
		public static final int RESOLVED = 22;
		public static final int CREATE_REPOSITORY = 23;
		public static final int CLEANUP = 24;
		public static final int ANNOTATE = 25;
        public static final int SWITCH = 26;
        public static final int MERGE = 27;
        public static final int LOCK = 28;
        public static final int UNLOCK = 29;
        public static final int RELOCATE = 30;
    }    

    /**
     * Tell the callback the command to be executed
     * @param command one of {@link Command}.* constants
     */
    public void setCommand(int command);

    /**
     * called at the beginning of the command
     * @param commandLine
     */
    public void logCommandLine(String commandLine);
    
    /**
     * called multiple times during the execution of a command
     * @param message
     */
    public void logMessage(String message);
    
    /**
     * called when an error happen during a command
     * @param message
     */
    public void logError(String message);

    /**
     * Called when a command has completed to report
     * that the command completed against the specified
     * revision.
     *  
     * @param revision 
     * @param path - path to folder which revision is reported (either root, or some of svn:externals)
     */
    public void logRevision(long revision, String path);

    /**
     * called when a command has completed
     * @param message
     */    
    public void logCompleted(String message);

    /**
     * called when a subversion action happen on a file (add, delete, update ...)
     * @param path the canonical path of the file or dir
     * @param kind file or dir or unknown
     */
    public void onNotify(File path, SVNNodeKind kind);
    
}
