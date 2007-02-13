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
package org.eclipse.subversion.client.javahl;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SVNClientPlugin extends Plugin {


// The plug-in ID
public static final String PLUGIN_ID = "org.eclipse.subversion.client.javahl";

// The shared instance
private static SVNClientPlugin plugin;

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

}
