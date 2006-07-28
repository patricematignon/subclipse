/*******************************************************************************
 * Copyright (c) 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client.plugin;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SVNClientPlugin extends Plugin {


// The plug-in ID
public static final String PLUGIN_ID = "org.eclipse.subversion.client";

// The shared instance
private static SVNClientPlugin plugin;

// All available client adapters
private static ISVNClientAdapter[] clients;

// The default adapter
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
			try {
				ISVNClientAdapter client = (ISVNClientAdapter)configurationElement.createExecutableExtension("class");
				if (client.isAvailable()) {
					client.setDisplayName(configurationElement.getAttribute("name") + " - " + client.getVersionString());
					clientList.add(client);
				}
			} catch(Exception e) {
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
