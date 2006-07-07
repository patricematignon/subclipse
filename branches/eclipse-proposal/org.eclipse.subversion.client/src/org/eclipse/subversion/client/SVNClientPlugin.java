package org.eclipse.subversion.client;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SVNClientPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.subversion.client";

	public static final String DEFAULT_ADAPTER = "default_adapter";

	// The shared instance
	private static SVNClientPlugin plugin;
	private static ISVNClientAdapter[] clients;
	private static ISVNClientAdapter defaultClient;
	
	/**
	 * The constructor
	 */
	public SVNClientPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static SVNClientPlugin getDefault() {
		return plugin;
	}
	
	public static ISVNClientAdapter[] getClientAdapters() throws Exception {
		if (clients == null) {
			ArrayList clientList = new ArrayList();
			IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
			IConfigurationElement[] configurationElements = pluginRegistry.getConfigurationElementsFor("org.eclipse.subversion.client.adapter");
			for (int i = 0; i < configurationElements.length; i++) {
				IConfigurationElement configurationElement = configurationElements[i];
				ISVNClientAdapter client = (ISVNClientAdapter)configurationElement.createExecutableExtension("class");
				if (client.isAvailable()) {
					client.setDisplayName(configurationElement.getAttribute("name") + " - " + client.getVersionString());
					clientList.add(client);
				}
			}	
			clients = new ISVNClientAdapter[clientList.size()];
			clientList.toArray(clients);
		}
		return clients;
		
	}
	
	public static void setDefaultAdapter(String client){
		ISVNClientAdapter[] clientArr = null;
		try {
			clientArr = getClientAdapters();
		} catch (Exception e) {
			return;
		}
		if ((clientArr == null) || (clientArr.length == 0)) return;
		if (client == null) {
			defaultClient = clientArr[0];
			return;
		}
		for (int i = 0; i < clientArr.length; i++) {
			if (clientArr[i].getAdapterName().equals(client)) {
				defaultClient = clientArr[i];
				break;
			}
		}
	}
	
	public static ISVNClientAdapter getClientAdapter() {
		if (defaultClient == null)
			setDefaultAdapter(null);
		return defaultClient;
	}

}
