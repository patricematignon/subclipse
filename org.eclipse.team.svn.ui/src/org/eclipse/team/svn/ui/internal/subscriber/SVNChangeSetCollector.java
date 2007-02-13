package org.eclipse.team.svn.ui.internal.subscriber;

import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class SVNChangeSetCollector extends SyncInfoSetChangeSetCollector {
    /*
     * Constant used to add the collector to the configuration of a page so
     * it can be accessed by the SVN custom actions
     */
    public static final String SVN_CHECKED_IN_COLLECTOR = SVNUIPlugin.ID + ".SVNCheckedInCollector"; //$NON-NLS-1$

	public SVNChangeSetCollector(ISynchronizePageConfiguration configuration) {
		super(configuration);
        configuration.setProperty(SVNChangeSetCollector.SVN_CHECKED_IN_COLLECTOR, this);
	}

	protected void add(SyncInfo[] infos) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.ChangeSetManager#initializeSets()
	 */
	protected void initializeSets() {
		// Do nothing
	}
}
