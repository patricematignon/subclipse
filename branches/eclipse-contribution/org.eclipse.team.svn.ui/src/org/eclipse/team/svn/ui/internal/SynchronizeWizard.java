/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.svn.core.internal.sync.SVNWorkspaceSubscriber;
import org.eclipse.team.svn.ui.internal.internal.GlobalRefreshResourceSelectionPage;
import org.eclipse.team.svn.ui.internal.subscriber.SVNSynchronizeParticipant;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

public class SynchronizeWizard extends Wizard {
	private GlobalRefreshResourceSelectionPage selectionPage;
	private IWizard importWizard;
	
	public SynchronizeWizard() {
		setDefaultPageImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_SYNCH));
		setNeedsProgressMonitor(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
	 */
	public String getWindowTitle() {
		return Policy.bind("GlobalRefreshSubscriberPage.0"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		selectionPage = new GlobalRefreshResourceSelectionPage(SVNWorkspaceSubscriber.getInstance().roots());
		selectionPage.setTitle(org.eclipse.team.svn.ui.internal.Policy.bind("SynchronizeWizard.title")); //$NON-NLS-1$
		selectionPage.setMessage(org.eclipse.team.svn.ui.internal.Policy.bind("SynchronizeWizard.message")); //$NON-NLS-1$
		addPage(selectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (importWizard != null) {
			return importWizard.performFinish();
		} else {
			IResource[] resources = selectionPage.getRootResources();
			if (resources != null && resources.length > 0) {
				SubscriberParticipant participant = new SVNSynchronizeParticipant(selectionPage.getSynchronizeScope());
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
				// We don't know in which site to show progress because a participant could actually be shown in multiple sites.
				participant.run(null /* no site */);
			}
			return true;
		}
	}
}
