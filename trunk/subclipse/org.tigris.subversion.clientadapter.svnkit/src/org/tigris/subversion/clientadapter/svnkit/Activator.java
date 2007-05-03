package org.tigris.subversion.clientadapter.svnkit;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.tigris.subversion.clientadapter.ISVNClientWrapper;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapter;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin implements ISVNClientWrapper {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.tigris.subversion.clientadapter.svnkit";

	// The shared instance
	private static Activator plugin;

	private String displayName;
	private String version;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public ISVNClientAdapter getAdapter() {
		if (this.isAvailable())
			return new SvnKitClientAdapter();
		else
			return null;
	}

	public String getAdapterID() {
		return SvnKitClientAdapterFactory.SVNKIT_CLIENT;
	}

	public String getVersionString() {
		return getVersionSynchronized();
	}

	private synchronized String getVersionSynchronized() {
		if (version == null) {
			if (this.isAvailable()) {
				SVNClientImpl adapter = SVNClientImpl.newInstance();
				version = adapter.getVersion().toString();
			} else
				version = "Not Available";
			}
		return version;
	}

	public boolean isAvailable() {
		return SvnKitClientAdapterFactory.isAvailable();
	}

	public void setDisplayName(String string) {
		displayName = string;
	}

	public String getDisplayName() {
		return displayName + " " + this.getVersionString();
	}

	public String getLoadErrors() {
		if (this.isAvailable())
			return "";
		return "Class org.tmatesoft.svn.core.javahl.SVNClientImpl not found.\nInstall the SVNKit plug-in from http://www.svnkit.com/";
	}


}
