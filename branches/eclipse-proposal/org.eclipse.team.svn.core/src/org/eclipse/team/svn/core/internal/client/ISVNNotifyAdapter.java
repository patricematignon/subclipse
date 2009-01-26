package org.eclipse.team.svn.core.internal.client;

import java.io.File;

import org.eclipse.subversion.client.SVNNodeKind;

public class ISVNNotifyAdapter implements IConsoleListener {

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#setCommand(int)
	 */
	public void setCommand(int command) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logCommandLine(java.lang.String)
	 */
	public void logCommandLine(String commandLine) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logMessage(java.lang.String)
	 */
	public void logMessage(String message) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logError(java.lang.String)
	 */
	public void logError(String message) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logRevision(long, java.lang.String)
	 */
	public void logRevision(long revision, String path) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logCompleted(java.lang.String)
	 */
	public void logCompleted(String message) {
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#onNotify(java.io.File, org.tigris.subversion.svnclientadapter.SVNNodeKind)
	 */
	public void onNotify(File path, SVNNodeKind kind) {
	}

}