/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.ui.internal.repository.model;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;

/**
 * A simple job scheduling rule for serializing jobs for an ICVSRepositoryLocation
 */
public class RepositoryLocationSchedulingRule implements ISchedulingRule {
	ISVNRepositoryLocation location;
	public RepositoryLocationSchedulingRule(ISVNRepositoryLocation location) {
		this.location = location;
	}
	public boolean isConflicting(ISchedulingRule rule) {
		if(rule instanceof RepositoryLocationSchedulingRule) {
			return ((RepositoryLocationSchedulingRule)rule).location.equals(location);
		}
		return false;
	}
	public boolean contains(ISchedulingRule rule) {
		return isConflicting(rule);
	}
}