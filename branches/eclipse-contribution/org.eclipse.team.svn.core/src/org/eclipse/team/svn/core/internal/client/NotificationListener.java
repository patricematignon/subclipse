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
package org.eclipse.team.svn.core.internal.client;

import java.io.*;

import org.eclipse.subversion.client.ISVNNotifyListener;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.team.svn.core.internal.*;

/**
 * This class listen to notifications from svnClientAdapter and redirect them to the console listener
 */
public class NotificationListener implements ISVNNotifyListener {

    private static NotificationListener instance;

    /*
     * private contructor 
     */
    private NotificationListener() {
     
    }
    
    private IConsoleListener getConsoleListener() {
    	return SVNProviderPlugin.getPlugin().getConsoleListener();
    }
    
    /**
     * Returns the singleton instance
     */
    public static NotificationListener getInstance() {      
        if(instance==null) {
            instance = new NotificationListener();
        }
        return instance;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#logCommandLine(java.lang.String)
	 */
	public void logCommandLine(String commandLine) {
		IConsoleListener consoleListener = getConsoleListener();		
        if (consoleListener != null) {
			consoleListener.logCommandLine(commandLine);
        }
	}

    public void logRevision(long revision, String path) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#logCompleted(java.lang.String)
	 */
	public void logCompleted(String message) {
		IConsoleListener consoleListener = getConsoleListener();
		if (consoleListener != null) {
        	consoleListener.logCompleted(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#logError(java.lang.String)
	 */
	public void logError(String message) {
		IConsoleListener consoleListener = getConsoleListener();
		if (consoleListener != null) {
        	consoleListener.logError(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#logMessage(java.lang.String)
	 */
	public void logMessage(String message) {
		IConsoleListener consoleListener = getConsoleListener();		
		if (consoleListener != null) {
			consoleListener.logMessage(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#onNotify(java.lang.String, org.eclipse.subversion.client.SVNNodeKind)
	 */
	public void onNotify(File path, SVNNodeKind kind) {
		IConsoleListener consoleListener = getConsoleListener();		
		if (consoleListener != null) {
			consoleListener.onNotify(path,kind);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNNotifyListener#setCommand(int)
	 */
	public void setCommand(int command) {
		IConsoleListener consoleListener = getConsoleListener();
		if (consoleListener != null) {
        	consoleListener.setCommand(command);
		}
	}

}
