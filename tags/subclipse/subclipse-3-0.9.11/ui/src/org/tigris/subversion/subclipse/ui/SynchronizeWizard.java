/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.synchronize.GlobalRefreshResourceSelectionPage;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.tigris.subversion.subclipse.core.SVNSubscriber;
import org.tigris.subversion.subclipse.ui.subscriber.SVNSynchronizeParticipant;

public class SynchronizeWizard extends Wizard {
	private GlobalRefreshResourceSelectionPage selectionPage;
	private IWizard importWizard;
	
	public SynchronizeWizard() {
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_WIZBAN_SHARE));
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
		selectionPage = new GlobalRefreshResourceSelectionPage(SVNSubscriber.getInstance().roots());
		selectionPage.setTitle("Synchronize File System Example");
		selectionPage.setMessage("Synchronize File System Example");
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
