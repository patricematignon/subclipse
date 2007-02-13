/*
 *  Copyright(c) 2003-2004 by the authors indicated in the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tigris.subversion.svnclientadapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Notification handler :
 * It sends notifications to all listeners 
 */
public abstract class SVNNotificationHandler {
    protected List notifylisteners = new ArrayList();
    protected int command;
    protected boolean logEnabled = true;
    protected File baseDir = new File(".");
        
    /**
     * Add a notification listener
     */
    public void add(ISVNNotifyListener listener) {
        notifylisteners.add(listener);
    }

    /**
     * Remove a notification listener 
     */
    public void remove(ISVNNotifyListener listener) {
        notifylisteners.remove(listener);
    }
    
    public void enableLog() {
        logEnabled = true;
    }
    
    /**
     * disable logging. calls to logMessage, logCompleted, logCommandLine do nothing 
     * Note that errors and exceptions are not disabled
     */
    public void disableLog() {
        logEnabled = false;
    }
        
    public void logMessage(String message) {
        if (logEnabled) {
            for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
                ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
                listener.logMessage(message);
            }
        }                        
    }

    public void logError(String message) {
        for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
            ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
            listener.logError(message);
        }                        
    }

    public void logRevision(long revision, String path) {
        if (logEnabled) {
            for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
                ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
                listener.logRevision(revision, path);
            }
        }                        
    }    

    public void logCompleted(String message) {
        if (logEnabled) {
            for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
                ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
                listener.logCompleted(message);
            }
        }                        
    }    

	/**
	 * set the command
	 * @param command
	 * @param files
	 */
    public void setCommand(int command) {
		this.command = command;        
       	for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
           	ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
           	listener.setCommand(command);
       	}
    }
    
    /**
     * log the command line
     * @param commandLine
     */
    public void logCommandLine(String commandLine) {
        if (logEnabled && !skipCommand()) {
            for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
                ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
                listener.logCommandLine(commandLine);
            }
        }                        
    }

    /**
     * To call when a method of ClientAdapter throw an exception
     */        
    public void logException(Exception clientException) {
        Throwable e = clientException;
        while (e != null) {
            logError(e.getMessage());
            e = e.getCause();                
        }
    }
    
    /**
     * set the baseDir : directory to use as base directory when path is relative
     * @param baseDir
     */
    public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;    	
    }

	public void setBaseDir() {
		this.baseDir = new File(".");
	}
    
    private File getAbsoluteFile(String path) {
		File f = new File(path);
		if (!f.isAbsolute()) {
			f = new File(baseDir,path);
		}
		return f;
    }
    
    public void notifyListenersOfChange(String path) {
		File f = getAbsoluteFile(path);
		if (f == null) {
			// this should not happen
			logMessage("Warning : invalid path :"+path);
			return;
		}
		
		SVNNodeKind kind;
		if (f.isFile()) {
			kind = SVNNodeKind.FILE;
		} else
		if (f.isDirectory()) {
			kind = SVNNodeKind.DIR;
		} else {
			kind = SVNNodeKind.UNKNOWN;
		}

		for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
			ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
			listener.onNotify(f, kind);
		}  

    }
    
    public void notifyListenersOfChange(String path, SVNNodeKind kind) {
		File f = getAbsoluteFile(path);
		if (f == null) {
			// this should not happen
			logMessage("Warning : invalid path :"+path);
			return;
		}

        for(Iterator it=notifylisteners.iterator(); it.hasNext();) {
            ISVNNotifyListener listener = (ISVNNotifyListener)it.next();
            listener.onNotify(f, kind);
        }  
    }
    
    /**
     * For certain commands we just want to skip the logging of the
     * command line
     */
    protected boolean skipCommand() {
        if (command == ISVNNotifyListener.Command.CAT ||
                command == ISVNNotifyListener.Command.INFO ||
                command == ISVNNotifyListener.Command.LOG ||
                command == ISVNNotifyListener.Command.LS ||
                command == ISVNNotifyListener.Command.PROPGET ||
                command == ISVNNotifyListener.Command.PROPLIST ||
                command == ISVNNotifyListener.Command.STATUS )
            return true;
        else
            return false;
    }
    
}
