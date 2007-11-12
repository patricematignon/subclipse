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
package org.tigris.subversion.subclipse.core.mapping;

import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.SubscriberChangeSetManager;

/**
 *
 */
public class SVNActiveChangeSetCollector extends SubscriberChangeSetManager {

	public SVNActiveChangeSetCollector(Subscriber subscriber) {
		super(subscriber);
	}

	protected ActiveChangeSet doCreateSet(String name) {
		return new SVNActiveChangeSet(this, name);
	}
}
