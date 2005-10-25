package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.WorkspacePathValidator;
import org.tigris.subversion.subclipse.ui.operations.CheckoutAsProjectOperation;
import org.tigris.subversion.subclipse.ui.util.PromptingDialog;



public class CheckoutIntoAction extends CheckoutAsProjectAction {
	
	protected IPath intoDir;
	
	/*
	 * @see SVNAction#execute()
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
	    if (!WorkspacePathValidator.validateWorkspacePath()) return;
	    DirectoryDialog intoDirDia = new DirectoryDialog(shell);
	    intoDirDia.setMessage(Policy.bind("CheckoutInto.message"));
    	String intoDirString = intoDirDia.open();
    	if (intoDirString==null) {
    		return;
    	}
    	intoDir = new Path(intoDirString);
	    checkoutSelectionIntoWorkspaceDirectory();
	}
	
	/**
     * checkout into a workspace directory, ie as a project
     * @throws InvocationTargetException
     * @throws InterruptedException
     */	
	protected void checkoutSelectionIntoWorkspaceDirectory() throws InvocationTargetException, InterruptedException { 
	    run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
			    try {
					final ISVNRemoteFolder[] folders = getSelectedRemoteFolders();
							
					List targetProjects = new ArrayList();
					Map targetFolders = new HashMap();

					monitor.beginTask(null, 100);
					for (int i = 0; i < folders.length; i++) {
					    proceed = true;
					    if (folders[i].getRepository().getRepositoryRoot().toString().equals(folders[i].getUrl().toString())) {
						    shell.getDisplay().syncExec(new Runnable() {
	                            public void run() {
	        					     proceed = MessageDialog.openQuestion(shell, Policy.bind("CheckoutAsProjectAction.title"), Policy.bind("AddToWorkspaceAction.checkingOutRoot")); //$NON-NLS-1$                               
	                            }					        
						    });					        
					    }
					    if (proceed) {
							IProject project = SVNWorkspaceRoot.getProject(folders[i],monitor);
							targetFolders.put(project.getName(), folders[i]);
							targetProjects.add(project);
					    } else return;
					}
					

					projects = (IResource[]) targetProjects.toArray(new IResource[targetProjects.size()]);
					
					// if a project with the same name already exist, we ask the user if he want to overwrite it
					PromptingDialog prompt = new PromptingDialog(getShell(), projects, 
																  getOverwriteLocalAndFileSystemPrompt(), 
																  Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
					projects = prompt.promptForMultiple();
															
					if (projects.length != 0) {
						localFolders = new IProject[projects.length];
						remoteFolders = new ISVNRemoteFolder[projects.length];
						for (int i = 0; i < projects.length; i++) {
							localFolders[i] = (IProject)projects[i];
							remoteFolders[i] = (ISVNRemoteFolder)targetFolders.get(projects[i].getName());
						}
					}
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	    if (proceed) new CheckoutAsProjectOperation(getTargetPart(), remoteFolders, localFolders, intoDir).run();
	}

}
