/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.console;
import java.io.File;

import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.subclipse.core.client.IConsoleListener;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
/**
 * The console listener
 */
class ConsoleListener implements IConsoleListener {

	public void logCommandLine(String commandLine) {
		ConsoleView.appendConsoleLines(ConsoleDocument.DELIMITER, Policy
				.bind("Console.preExecutionDelimiter")); //$NON-NLS-1$
		ConsoleView.appendConsoleLines(ConsoleDocument.COMMAND, commandLine);
	}
	public void logMessage(String message) {
		ConsoleView.appendConsoleLines(ConsoleDocument.MESSAGE, "  " + message); //$NON-NLS-1$
	}
	public void logCompleted(String message) {
		ConsoleView.appendConsoleLines(ConsoleDocument.MESSAGE, "  " + message); //$NON-NLS-1$
	}
	public void logError(String message) {
		ConsoleView.appendConsoleLines(ConsoleDocument.ERROR, "  " + message); //$NON-NLS-1$
		// we show the console view if something goes wrong
		// findInActivePerspective must be called from the UI thread
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		display.syncExec(new Runnable() {
			public void run() {
				ConsoleView.findInActivePerspective();
			}
		});
	}
	public void onNotify(File path, SVNNodeKind kind) {
	}
	public void setCommand(int command) {
	}
}