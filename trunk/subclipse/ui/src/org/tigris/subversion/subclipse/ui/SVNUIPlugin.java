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
package org.tigris.subversion.subclipse.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.ui.actions.ShowOutOfDateFoldersAction;
import org.tigris.subversion.subclipse.ui.authentication.SVNPromptUserPassword;
import org.tigris.subversion.subclipse.ui.console.SVNOutputConsole;
import org.tigris.subversion.subclipse.ui.repository.RepositoryManager;
import org.tigris.subversion.subclipse.ui.repository.model.SVNAdapterFactory;
import org.tigris.subversion.subclipse.ui.util.SimpleDialogsHelper;
/**
 * UI Plugin for Subversion provider-specific workbench functionality.
 */
public class SVNUIPlugin extends AbstractUIPlugin {
	/**
	 * The id of the SVN plug-in
	 */
	public static final String ID = "org.tigris.subversion.subclipse.ui"; //$NON-NLS-1$
	public static final String DECORATOR_ID = "org.tigris.subversion.subclipse.ui.decorator"; //$NON-NLS-1$
	public static final String PROVIDER_ID="org.tigris.subversion.subclipse.core.svnnature"; //$NON-NLS-1$
	/**
	 * Property constant indicating the decorator configuration has changed. 
	 */
	public static final String P_DECORATORS_CHANGED = SVNUIPlugin.ID  + ".P_DECORATORS_CHANGED";	 //$NON-NLS-1$
	/**
	 * The singleton plug-in instance
	 */
	private static SVNUIPlugin plugin;
	
	/**
	 * The repository manager
	 */
	private RepositoryManager repositoryManager;
    
    private ImageDescriptors imageDescriptors;
    
    private Preferences preferences;
	
//	// Property change listener
//	IPropertyChangeListener teamUIListener = new IPropertyChangeListener() {
//		public void propertyChange(PropertyChangeEvent event) {
//			if (event.getProperty().equals(TeamUI.GLOBAL_IGNORES_CHANGED)) {
//				SVNLightweightDecorator.refresh();
//			}
//		}
//	};
    
    private SVNMarkerListener markerListener;
    
    private ShowOutOfDateFoldersAction showOutOfDateFoldersAction;
	
	
    public static void log(CoreException e) {
		log(e.getStatus().getSeverity(), Policy.bind("simpleInternal"), e); //$NON-NLS-1$
	}
    public static void log(int severity, String message, Throwable e) {
		log(new Status(severity, ID, 0, message, e));
	}
    
	

	
	public SVNUIPlugin(){
		super();
		plugin = this;
	
	}
	

	/**
	 * Convenience method to get the currently active workbench page. Note that
	 * the active page may not be the one that the usr perceives as active in
	 * some situations so this method of obtaining the activae page should only
	 * be used if no other method is available.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) return null;
		return window.getActivePage();
	}
	
	/**
	 * Creates a busy cursor and runs the specified runnable.
	 * May be called from a non-UI thread.
	 * 
	 * @param parent the parent Shell for the dialog
	 * @param cancelable if true, the dialog will support cancelation
	 * @param runnable the runnable
	 * 
	 * @exception InvocationTargetException when an exception is thrown from the runnable
	 * @exception InterruptedException when the progress monitor is cancelled
	 */
	
	public static void runWithProgress(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {

		boolean createdShell = false;
		try {
			if (parent == null || parent.isDisposed()) {
				Display display = Display.getCurrent();
				if (display == null) {
					// cannot provide progress (not in UI thread)
					runnable.run(new NullProgressMonitor());
					return;
				}
				// get the active shell or a suitable top-level shell
				parent = display.getActiveShell();
				if (parent == null) {
					parent = new Shell(display);
					createdShell = true;
				}
			}
			// pop up progress dialog after a short delay
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
				public void run() {
					try {
						runnable.run(new NullProgressMonitor());
					} catch (InvocationTargetException e) {
						holder[0] = e;
					} catch (InterruptedException e) {
						holder[0] = e;
					}
				}
			});
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else {
					throw (InterruptedException) holder[0];
				}
			}
			//new TimeoutProgressMonitorDialog(parent, TIMEOUT).run(true /*fork*/, cancelable, runnable);
		} finally {
			if (createdShell) parent.dispose();
		}
	}	

	
	
	/**
	 * Returns the singleton plug-in instance.
	 * 
	 * @return the plugin instance
	 */
	public static SVNUIPlugin getPlugin() {
		// If the instance has not been initialized, we will wait.
		// This can occur if multiple threads try to load the plugin at the same
		// time (see bug 33825: http://bugs.eclipse.org/bugs/show_bug.cgi?id=33825)
		while (plugin == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// ignore and keep trying
			}
		}
		return plugin;
	}

	/**
	 * Returns the repository manager
	 * 
	 * @return the repository manager
	 */
	public synchronized RepositoryManager getRepositoryManager() {
		if (repositoryManager == null) {
			repositoryManager = new RepositoryManager();
			repositoryManager.startup();
		}
		return repositoryManager;
	}
	
	/**
	 * Convenience method for logging statuses to the plugin log
	 * 
	 * @param status  the status to log
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	public static void log(String msg) {
		getPlugin().getLog().log(new Status(IStatus.INFO, SVNUIPlugin.ID, 0, msg, null));
	}	

	public static void log(TeamException e) {
		getPlugin().getLog().log(new Status(e.getStatus().getSeverity(), SVNUIPlugin.ID, 0, Policy.bind("simpleInternal"), e)); //$NON-NLS-1$
	}

	// flags to tailor error reporting
	public static final int PERFORM_SYNC_EXEC = 1;
	public static final int LOG_TEAM_EXCEPTIONS = 2;
	public static final int LOG_CORE_EXCEPTIONS = 4;
	public static final int LOG_OTHER_EXCEPTIONS = 8;
	public static final int LOG_NONTEAM_EXCEPTIONS = LOG_CORE_EXCEPTIONS | LOG_OTHER_EXCEPTIONS;
	private SVNOutputConsole console;
    private URL baseURL;
	
	/**
	 * Convenience method for showing an error dialog 
	 * @param shell a valid shell or null
	 * @param exception the exception to be report
	 * @param title the title to be displayed
	 * @return IStatus the status that was displayed to the user
	 */
	public static IStatus openError(Shell shell, String title, String message, Throwable exception) {
		return openError(shell, title, message, exception, LOG_OTHER_EXCEPTIONS);
	}
	
	/**
	 * Convenience method for showing an error dialog 
	 * @param shell a valid shell or null
	 * @param exception the exception to be report
	 * @param title the title to be displayed
	 * @param flags customizing attributes for the error handling
	 * @return IStatus the status that was displayed to the user
	 */
	public static IStatus openError(Shell providedShell, String title, String message, Throwable exception, int flags) {
		// Unwrap InvocationTargetExceptions
		if (exception instanceof InvocationTargetException) {
			Throwable target = ((InvocationTargetException)exception).getTargetException();
			// re-throw any runtime exceptions or errors so they can be handled by the workbench
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			} 
			return openError(providedShell, title, message, target, flags);
		}
		
		// Determine the status to be displayed (and possibly logged)
		IStatus status = null;
		boolean log = false;
		if (exception instanceof CoreException) {
			status = ((CoreException)exception).getStatus();
			log = ((flags & LOG_CORE_EXCEPTIONS) > 0);
		} else if (exception instanceof TeamException) {
			status = ((TeamException)exception).getStatus();
			log = ((flags & LOG_TEAM_EXCEPTIONS) > 0);
		} else if (exception instanceof InterruptedException) {
			return new SVNStatus(IStatus.OK, Policy.bind("ok")); //$NON-NLS-1$
		} else if (exception != null) {
			status = new SVNStatus(IStatus.ERROR, Policy.bind("internal"), exception); //$NON-NLS-1$
			log = ((flags & LOG_OTHER_EXCEPTIONS) > 0);
			if (title == null) title = Policy.bind("SimpleInternal"); //$NON-NLS-1$
		}
		
		// Check for a build error and report it differently
		if (status.getCode() == IResourceStatus.BUILD_FAILED) {
			message = Policy.bind("buildError"); //$NON-NLS-1$
			log = true;
		}
		
		// Check for multi-status with only one child
		if (status.isMultiStatus() && status.getChildren().length == 1) {
			status = status.getChildren()[0];
		}
		if (status.isOK()) return status;
		
		// Log if the user requested it
		if (log) SVNUIPlugin.log(status);
		
		// Create a runnable that will display the error status
		final String displayTitle = title;
		final String displayMessage = message;
		final IStatus displayStatus = status;
		final IOpenableInShell openable = new IOpenableInShell() {
			public void open(Shell shell) {
				if (displayStatus.getSeverity() == IStatus.INFO && !displayStatus.isMultiStatus()) {
					MessageDialog.openInformation(shell, Policy.bind("information"), displayStatus.getMessage()); //$NON-NLS-1$
				} else {
					ErrorDialog.openError(shell, displayTitle, displayMessage, displayStatus);
				}
			}
		};
		openDialog(providedShell, openable, flags);
		
		// return the status we display
		return status;
	}
	
	/**
	 * Interface that allows a shell to be passed to an open method. The
	 * provided shell can be used without sync-execing, etc.
	 */
	public interface IOpenableInShell {
		public void open(Shell shell);
	}
	
	/**
	 * Open the dialog code provided in the IOpenableInShell, ensuring that 
	 * the provided whll is valid. This method will provide a shell to the
	 * IOpenableInShell if one is not provided to the method.
	 * 
	 * @param providedShell
	 * @param openable
	 * @param flags
	 */
	public static void openDialog(Shell providedShell, final IOpenableInShell openable, int flags) {
		// If no shell was provided, try to get one from the active window
		if (providedShell == null) {
			IWorkbenchWindow window = SVNUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				providedShell = window.getShell();
				// sync-exec when we do this just in case
				flags = flags | PERFORM_SYNC_EXEC;
			}
		}

		// Create a runnable that will display the error status
		final Shell shell = providedShell;
		Runnable outerRunnable = new Runnable() {
			public void run() {
				Shell displayShell;
				if (shell == null) {
					Display display = Display.getCurrent();
					displayShell = new Shell(display);
				} else {
					displayShell = shell;
				}
				openable.open(displayShell);
				if (shell == null) {
					displayShell.dispose();
				}
			}
		};

		// Execute the above runnable as determined by the parameters
		if (shell == null || (flags & PERFORM_SYNC_EXEC) > 0) {
			Display display;
			if (shell == null) {
				display = Display.getCurrent();
				if (display == null) {
					display = Display.getDefault();
				}
			} else {
				display = shell.getDisplay();
			}
			display.syncExec(outerRunnable);
		} else {
			outerRunnable.run();
		}
	}
	
	public void start(BundleContext ctxt)throws Exception{
		super.start(ctxt);
		SVNAdapterFactory factory = new SVNAdapterFactory();
		
		Platform.getAdapterManager().registerAdapters(factory, ISVNRemoteFile.class); 
		Platform.getAdapterManager().registerAdapters(factory, ISVNRemoteFolder.class); 
		Platform.getAdapterManager().registerAdapters(factory, ISVNRepositoryLocation.class);
//		Platform.getAdapterManager().registerAdapters(factory, RepositoryRoot.class);
		
		baseURL = ctxt.getBundle().getEntry("/"); //$NON-NLS-1$
		
        preferences = new Preferences(getPreferenceStore());
		preferences.initializePreferences();
		
		markerListener = new SVNMarkerListener();
        SVNProviderPlugin.addResourceStateChangeListener(markerListener);
		
//		// if the global ignores list is changed then update decorators.
		//TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[]{new SVNWorkspaceSynchronizeParticipant()});
		try {
		    console = new SVNOutputConsole();
	    } catch (RuntimeException e) {
	        // Don't let the console bring down the SVN UI
	        log(IStatus.ERROR, "Errors occurred starting the SVN console", e); //$NON-NLS-1$
	    }
		SVNProviderPlugin.getPlugin().setSvnPromptUserPassword(new SVNPromptUserPassword());
		SVNProviderPlugin.getPlugin().setSimpleDialogsHelper(new SimpleDialogsHelper());
		SVNProviderPlugin.getPlugin().setSvnFileModificationValidatorPrompt(new SVNFileModificationValidatorPrompt());
		
		showOutOfDateFoldersAction = new ShowOutOfDateFoldersAction();
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void stop(BundleContext ctxt) throws Exception {
		super.stop(ctxt);
//		TeamUI.removePropertyChangeListener(listener);
		try {
			if (repositoryManager != null)
				repositoryManager.shutdown();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}

        console.shutdown();
	}
	
    /**
     * Returns the standard display to be used. The method first checks, if
     * the thread calling this method has an associated display. If so, this
     * display is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay() {
        Display display= Display.getCurrent();
        if (display == null) {
            display= Display.getDefault();
        }
        return display;     
    }

	
    /**
     * Returns the image descriptor for the given image ID.
     * Returns null if there is no such image.
     */
    public ImageDescriptor getImageDescriptor(String id) {
        if (imageDescriptors == null) {
            imageDescriptors = new ImageDescriptors();
            imageDescriptors.initializeImages(baseURL,
            		getPreferenceStore().getInt(ISVNUIConstants.PREF_MENU_ICON_SET));
        }
        return imageDescriptors.getImageDescriptor(id);
    }

	/**
	 * Temporarily detach the console from the message source
	 */
	public void disableConsoleListener() {
		SVNProviderPlugin.getPlugin().setConsoleListener(null);
	}

	/**
	 * Reconnect the console view to the message source
	 */
	public void enableConsoleListener() {
        SVNProviderPlugin.getPlugin().setConsoleListener(console);
	}

	
	public static String getCharset(String name, InputStream stream) throws IOException {
		IContentDescription description = getContentDescription(name, stream);
		return description == null ? null : description.getCharset();

	}
	public static IContentDescription getContentDescription(String name, InputStream stream) throws IOException  {
		// tries to obtain a description for this file contents
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		try {
			return contentTypeManager.getDescriptionFor(stream, name, IContentDescription.ALL);
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					// Ignore exceptions on close
				}
		}
	}
	
	/**
	 * Gets the one and only SVN console managed by this plugin
	 * @return the SVN console
	 */
	public SVNOutputConsole getConsole() {
		return console;
	}
	
	public ShowOutOfDateFoldersAction getShowOutOfDateFoldersAction() {
		return showOutOfDateFoldersAction;
	}
}
