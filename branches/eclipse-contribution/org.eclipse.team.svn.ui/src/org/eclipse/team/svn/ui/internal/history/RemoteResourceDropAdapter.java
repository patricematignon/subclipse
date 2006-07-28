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
package org.eclipse.team.svn.ui.internal.history;

import org.eclipse.team.svn.ui.internal.actions.RemoteResourceTransfer;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.part.IDropActionDelegate;


public class RemoteResourceDropAdapter implements IDropActionDelegate {

	public boolean run(Object source, Object target) {
		if (source != null && target instanceof IHistoryView) {
			RemoteResourceTransfer transfer = RemoteResourceTransfer.getInstance();
			Object file = transfer.fromByteArray((byte[]) source);
			((IHistoryView) target).showHistoryFor(file);

		}
		return false;
	}

}
