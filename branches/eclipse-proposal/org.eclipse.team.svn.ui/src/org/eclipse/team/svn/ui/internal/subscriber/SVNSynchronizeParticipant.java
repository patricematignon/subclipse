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
package org.eclipse.team.svn.ui.internal.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.core.internal.sync.SVNStatusSyncInfo;
import org.eclipse.team.svn.core.internal.sync.SVNWorkspaceSubscriber;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.internal.ScopableSubscriberParticipant;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;


/**
 * This is an example synchronize participant for the file system provider. It will allow
 * showing synchronization state for local resources mapped to a remote file system
 * location.
 * 
 * @since 3.0
 */
public class SVNSynchronizeParticipant extends ScopableSubscriberParticipant implements IChangeSetProvider{
	
	/**
	 * The particpant ID as defined in the plugin manifest
	 */
	public static final String ID = "org.eclipse.team.svn.participant"; //$NON-NLS-1$
	
	/**
	 * Contxt menu action group for synchronize view actions
	 */
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_1 = "context_group_1";

	private ChangeSetCapability capability;

	/**
	 * A custom label decorator that will show the remote mapped path for each
	 * file.
	 */
	private class SVNParticipantLabelDecorator extends LabelProvider implements ILabelDecorator {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			try {
				if (element instanceof ISynchronizeModelElement) {
					IResource resource = ((ISynchronizeModelElement) element).getResource();
					if (resource != null) {
						SVNStatusSyncInfo info = (SVNStatusSyncInfo) SVNWorkspaceSubscriber.getInstance().getSyncInfo(resource);
						if (info != null)
						{
							return text + info.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			} catch (TeamException e) {
			}
			return null;
		}
	}
	
	/**
	 * Action group that contributes the get an put menus to the context menu 
	 * in the synchronize view
	 */
	private class SVNParticipantActionGroup extends SynchronizePageActionGroup {
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
		 */
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			UpdateSynchronizeAction updateAction = new UpdateSynchronizeAction(Policy.bind("SyncAction.update"), configuration); //$NON-NLS-1$
			updateAction.setId("org.eclipse.team.svn.ui.internal.syncViewUpdate");
			updateAction.setActionDefinitionId("org.eclipse.team.svn.ui.internal.update");
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					updateAction);
			CommitSynchronizeAction commitAction = new CommitSynchronizeAction(Policy.bind("SyncAction.commit"), configuration); //$NON-NLS-1$
			commitAction.setId("org.eclipse.team.svn.ui.internal.syncViewCommit");
			commitAction.setActionDefinitionId("org.eclipse.team.svn.ui.internal.commit");
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					commitAction);

			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					new Separator());
			
			OverrideAndUpdateSynchronizeAction overrideAction = new OverrideAndUpdateSynchronizeAction(Policy.bind("SyncAction.override"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					overrideAction);						
			MarkMergedSynchronizeAction markMergedAction = new MarkMergedSynchronizeAction(Policy.bind("SyncAction.markMerged"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					markMergedAction);	

			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					new Separator());
			
			AddSynchronizeAction addAction = new AddSynchronizeAction(Policy.bind("SyncAction.add"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					addAction);				
			IgnoreSynchronizeAction ignoreAction = new IgnoreSynchronizeAction(Policy.bind("SyncAction.ignore"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					ignoreAction);	
			RevertSynchronizeAction revertAction = new RevertSynchronizeAction(Policy.bind("SyncAction.revert"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					revertAction);
			ShowHistorySynchronizeAction historyAction = new ShowHistorySynchronizeAction(Policy.bind("SyncAction.history"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					historyAction);	
			ShowPropertiesSynchronizeAction propertiesAction = new ShowPropertiesSynchronizeAction(Policy.bind("SyncAction.properties"), configuration); //$NON-NLS-1$
			propertiesAction.setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_PROPERTIES));			
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					propertiesAction);				
			EditConflictsSynchronizeAction conflictsAction = new EditConflictsSynchronizeAction(Policy.bind("SyncAction.conflicts"), configuration); //$NON-NLS-1$				
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					conflictsAction);
			ResolveSynchronizeAction resolveAction = new ResolveSynchronizeAction(Policy.bind("SyncAction.resolve"), configuration); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					resolveAction);
		}

	}

	/**
	 * No arg contructor used for
	 * creation of persisted participant after startup
	 */
	public SVNSynchronizeParticipant() {
	}

	public SVNSynchronizeParticipant(ISynchronizeScope scope) {
		super(scope);
		setSubscriber(SVNWorkspaceSubscriber.getInstance());
	}

	/**
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(String secondaryId, IMemento memento) throws PartInitException {
		super.init(secondaryId, memento);
		setSubscriber(SVNWorkspaceSubscriber.getInstance());
	}

	 protected ISynchronizeParticipantDescriptor getDescriptor() {
        return TeamUI.getSynchronizeManager().getParticipantDescriptor(ID);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		
		ILabelDecorator labelDecorator = new SVNParticipantLabelDecorator();
		configuration.addLabelDecorator(labelDecorator);
		
		// Add support for showing mode buttons
		configuration.setSupportedModes(ISynchronizePageConfiguration.ALL_MODES);
		configuration.setMode(ISynchronizePageConfiguration.BOTH_MODE);
		
		// Create the action group that contributes the get and put actions
		configuration.addActionContribution(new SVNParticipantActionGroup());
		// Add the get and put group to the context menu
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP_1);
	}

	public ChangeSetCapability getChangeSetCapability() {
        if (capability == null) {
			capability = new SVNChangeSetCapability();
        }
        return capability;
	}
}
