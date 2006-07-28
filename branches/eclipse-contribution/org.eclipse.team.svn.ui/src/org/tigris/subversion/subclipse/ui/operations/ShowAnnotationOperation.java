/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.ui.operations;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.subversion.client.ISVNAnnotations;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.GetAnnotationsCommand;
import org.eclipse.team.svn.core.internal.commands.GetLogsCommand;
import org.eclipse.team.svn.core.internal.history.ILogEntry;
import org.eclipse.team.svn.core.internal.history.LogEntry;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.annotations.AnnotateBlock;
import org.tigris.subversion.subclipse.ui.annotations.AnnotateBlocks;
import org.tigris.subversion.subclipse.ui.annotations.AnnotateView;

/**
 * @author Brock Janiczak
 */
public class ShowAnnotationOperation extends SVNOperation {

    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final ISVNRemoteFile remoteFile;

    public ShowAnnotationOperation(IWorkbenchPart part, ISVNRemoteFile remoteFile, SVNRevision fromRevision) {
        super(part);
        this.remoteFile = remoteFile;
        this.fromRevision = fromRevision;
        this.toRevision = remoteFile.getLastChangedRevision();
    }
    
    public ShowAnnotationOperation(IWorkbenchPart part, ISVNRemoteFile remoteFile, SVNRevision fromRevision, SVNRevision toRevision) {
        super(part);
        this.remoteFile = remoteFile;
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }
    
    public ShowAnnotationOperation(IWorkbenchPart part, ISVNRemoteFile remoteFile) {
        this(part, remoteFile, SVNRevision.START);
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.operations.RepositoryProviderOperation#getTaskName(org.tigris.subversion.subclipse.core.SVNTeamProvider)
     */
    protected String getTaskName(SVNTeamProvider provider) {
        return Policy.bind("AnnotateOperation.0", provider.getProject().getName()); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.operations.SVNOperation#getTaskName()
     */
    protected String getTaskName() {
        return Policy.bind("AnnotateOperation.taskName"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.operations.SVNOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(IProgressMonitor monitor) throws SVNException, InterruptedException {
        monitor.beginTask(null, 100);

        try {
            GetAnnotationsCommand command = new GetAnnotationsCommand(remoteFile, fromRevision, toRevision);
            command.run(new SubProgressMonitor(monitor, 100));
            final ISVNAnnotations annotations = command.getAnnotations();
            final AnnotateBlocks annotateBlocks = new AnnotateBlocks(annotations);
            
            
    		// this is not needed if there is no live annotate
    		final RevisionInformation information= createRevisionInformation(annotateBlocks, Policy.subMonitorFor(monitor, 20));
    		
            // We aren't running from a UI thread
    		getShell().getDisplay().asyncExec(new Runnable() {
    			public void run() {

//  				is there an open editor for the given input? If yes, use live annotate
    				final AbstractDecoratedTextEditor editor= getEditor();
    				if (editor != null && promptForQuickDiffAnnotate()){
    					editor.showRevisionInformation(information, "org.tigris.subversion.subclipse.quickdiff.providers.SVNReferenceProvider"); //$NON-NLS-1$

    				} else {
    					try {
    						// Open the view
    						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    						if (window != null) {
    							try {
    								PlatformUI.getWorkbench().showPerspective("org.tigris.subversion.subclipse.ui.svnPerspective", window); //$NON-NLS-1$
    							} catch (WorkbenchException e1) {              
    								// If this does not work we will just open the view in the
    								// current perspective.
    							}
    						}
    						AnnotateView view = AnnotateView.openInActivePerspective();
    						view.showAnnotations(remoteFile, annotateBlocks.getAnnotateBlocks(), annotations.getInputStream());
    					} catch (PartInitException e1) {
    						collectStatus(e1.getStatus());
    					}
    				}
    			}
    		});
        } catch (SVNException e) {
            collectStatus(e.getStatus());
        } finally {
            monitor.done();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.TeamOperation#getGotoAction()
     */
    protected IAction getGotoAction() {
        return super.getGotoAction();
    }
    
	private AbstractDecoratedTextEditor getEditor() {
        final IWorkbench workbench= PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IEditorReference[] references= window.getActivePage().getEditorReferences();
        IResource resource= remoteFile.getResource();
		if (resource == null)
			return null;

		for (int i= 0; i < references.length; i++) {
			IEditorReference reference= references[i];
			try {
				if (resource != null && resource.equals(reference.getEditorInput().getAdapter(IFile.class))) {
					IEditorPart editor= reference.getEditor(false);
					if (editor instanceof AbstractDecoratedTextEditor)
						return (AbstractDecoratedTextEditor) editor;
					else {
						//editor opened is not a text editor - reopen file using the defualt text editor
						IEditorPart part = getPart().getSite().getPage().openEditor(new FileEditorInput((IFile) resource), IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID, true, IWorkbenchPage.MATCH_NONE);
						if (part != null && part instanceof AbstractDecoratedTextEditor)
							return (AbstractDecoratedTextEditor)part;
					}
				}
			} catch (PartInitException e) {
				// ignore
			}
		}
		
		//no existing editor references found, try to open a new editor for the file	
		if (resource instanceof IFile){
			try {
				IEditorDescriptor descrptr = IDE.getEditorDescriptor((IFile) resource);
				//try to open the associated editor only if its an internal editor
				if (descrptr.isInternal()){
					IEditorPart part = IDE.openEditor(getPart().getSite().getPage(), (IFile) resource);
					if (part instanceof AbstractDecoratedTextEditor)
						return (AbstractDecoratedTextEditor)part;
					
					//editor opened is not a text editor - close it
					getPart().getSite().getPage().closeEditor(part, false);
				}
				//open file in default text editor	
				IEditorPart part = IDE.openEditor(getPart().getSite().getPage(), (IFile) resource, IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
				if (part != null && part instanceof AbstractDecoratedTextEditor)
					return (AbstractDecoratedTextEditor)part;
				
			} catch (PartInitException e) {
			}
		}
	
        return null;
	}
    
    private RevisionInformation createRevisionInformation(final AnnotateBlocks annotateBlocks, IProgressMonitor monitor) {
    	Map logEntriesByRevision= new HashMap();
		final CommitterColors colors= CommitterColors.getDefault();
		RevisionInformation info= new RevisionInformation();
		HashMap sets= new HashMap();
		
		GetLogsCommand logCommand = new GetLogsCommand(this.remoteFile);
		logCommand.setRevisionStart(this.fromRevision);
		logCommand.setRevisionEnd(this.toRevision);

		try {
			logCommand.run(monitor);
			ILogEntry[] logEntries = logCommand.getLogEntries();
			
			for (int i = 0; i < logEntries.length; i++) {
				ILogEntry logEntry = logEntries[i];
				logEntriesByRevision.put(new Long(logEntry.getRevision().getNumber()), logEntry);
			}
		} catch (SVNException e) {
			SVNUIPlugin.log(e);
		}
		
		for (Iterator blocks= annotateBlocks.getAnnotateBlocks().iterator(); blocks.hasNext();) {
			final AnnotateBlock block= (AnnotateBlock) blocks.next();
			final String revisionString= Long.toString(block.getRevision());
			final LogEntry logEntry = (LogEntry) logEntriesByRevision.get(new Long(block.getRevision()));
			
			Revision revision= (Revision) sets.get(revisionString);
			if (revision == null) {
				revision= new Revision() {
					public Object getHoverInfo() {
							return "<b>" + block.getUser() + " " + revisionString + " " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(block.getDate()) + "</b>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							"<p>" + logEntry.getComment() + "</p>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					
					private String getCommitterId() {
						return block.getUser();
					}
					
					public String getId() {
						return revisionString;
					}
					
					public Date getDate() {
						return block.getDate();
					}
					
					public RGB getColor() {
						return colors.getCommitterRGB(getCommitterId());
					}
				};
				sets.put(revisionString, revision);
				info.addRevision(revision);
			}
			revision.addRange(new LineRange(block.getStartLine(), block.getEndLine() - block.getStartLine() + 1));
		}
		return info;
	}
    
    /**
	 * Returns true if the user wishes to always use the live annotate view, false otherwise.
	 * @return
	 */
	private boolean promptForQuickDiffAnnotate(){
		//check whether we should ask the user.
		final IPreferenceStore store = SVNUIPlugin.getPlugin().getPreferenceStore();
		final String option = store.getString(ISVNUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		if (option.equals(MessageDialogWithToggle.ALWAYS))
			return true; //use live annotate
		else if (option.equals(MessageDialogWithToggle.NEVER))
			return false; //don't use live annotate
		
		final MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(Utils.getShell(null), Policy.bind("AnnotateOperation_QDAnnotateTitle"),
				Policy.bind("AnnotateOperation_QDAnnotateMessage"), Policy.bind("AnnotateOperation_4"), false, store, ISVNUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		final int result = dialog.getReturnCode();
		switch (result) {
			//yes
			case IDialogConstants.YES_ID:
			case IDialogConstants.OK_ID :
			    return true;
		}
		return false;
	}
}
