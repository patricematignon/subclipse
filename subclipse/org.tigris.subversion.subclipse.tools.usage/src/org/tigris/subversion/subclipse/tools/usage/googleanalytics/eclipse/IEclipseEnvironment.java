/*******************************************************************************
 * Copyright (c) 2010 Subclipse project and others.
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.tools.usage.googleanalytics.eclipse;

import org.tigris.subversion.subclipse.tools.usage.googleanalytics.IGoogleAnalyticsParameters;

public interface IEclipseEnvironment extends IGoogleAnalyticsParameters {
	public IEclipseUserAgent getEclipseUserAgent();
}
