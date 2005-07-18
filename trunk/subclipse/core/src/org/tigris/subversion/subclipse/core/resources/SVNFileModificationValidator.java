package org.tigris.subversion.subclipse.core.resources;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.commands.LockResourcesCommand;

public class SVNFileModificationValidator implements IFileModificationValidator {

    public IStatus validateEdit(IFile[] files, Object context) {
        String comment = "";
        boolean stealLock = false;
	    SVNTeamProvider svnTeamProvider = null;
	    RepositoryProvider provider = RepositoryProvider.getProvider(files[0].getProject());
	    if ((provider != null) && (provider instanceof SVNTeamProvider)) {
            IFile[] readOnly = checkReadOnly(files);
            if (readOnly.length > 0) {
                if (context != null) {
                    ISVNFileModificationValidatorPrompt svnFileModificationValidatorPrompt = 
                        SVNProviderPlugin.getPlugin().getSvnFileModificationValidatorPrompt();
                    if (svnFileModificationValidatorPrompt != null) {
                        if (!svnFileModificationValidatorPrompt.prompt(readOnly, context))
                            return SVNStatus.CANCEL_STATUS;
                        comment = svnFileModificationValidatorPrompt.getComment();
                        stealLock = svnFileModificationValidatorPrompt.isStealLock();
                    }
                }
                svnTeamProvider = (SVNTeamProvider) provider;
                LockResourcesCommand command = new LockResourcesCommand(svnTeamProvider.getSVNWorkspaceRoot(), readOnly, stealLock, comment);
                try {
                    command.run(new NullProgressMonitor());
                } catch (SVNException e) {
                    e.printStackTrace();
                    return SVNStatus.CANCEL_STATUS;
                }
            }
        }
	    return SVNStatus.OK_STATUS;
    }

    public IStatus validateSave(IFile file) {
        return SVNStatus.OK_STATUS;
    }
    
    
    /**
     * This method does a second check on the files in the array
     * to verify their readOnly status.  There seems to be an Eclipse
     * bug under certain situations where we are passed files that do not have
     * this flag set.  This method will remove them from the array.
     */
    private IFile[] checkReadOnly(IFile[] files) {
        List fileList = new ArrayList(files.length);
        for (int i = 0; i < files.length; i++) {
    	    try {
                if (SVNWorkspaceRoot.getSVNResourceFor(files[i]).getStatus().isReadOnly())
                    fileList.add(files[i]);
            } catch (SVNException e) {
            }
        }
        return (IFile[]) fileList.toArray(new IFile[fileList.size()]);
    }

}
