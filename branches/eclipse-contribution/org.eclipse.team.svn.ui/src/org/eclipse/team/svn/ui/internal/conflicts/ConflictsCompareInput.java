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
package org.eclipse.team.svn.ui.internal.conflicts;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.compare.internal.BufferedResourceNode;
import org.eclipse.team.svn.ui.internal.compare.internal.Utilities;

/**
 * CompareEditorInput to resolve conflicts.
 * 
 * <p>
 * We start by copying the content of mine resource to destination resource
 * (without saving) and we tell that save is needed. So each time user edits
 * conflicts, content of left panel is in fact content of mine resource. <br>
 * We could probably detect if destination resource contains " < < < < < < < <
 * .mine" and if not, don't copy the content of mine resource to destination
 * resource.
 * </p>
 */
public class ConflictsCompareInput extends CompareEditorInput {
    
    /**
     * This class is only here so we can make the fireChange method public.
     * We want to invoke this when we do a save so that the synchronization
     * markers get updated. 
     */
    public static class MyDiffNode extends DiffNode {
        public MyDiffNode(IDiffContainer parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
            super(parent, kind, ancestor, left, right);
        }
         
        public void fireChange() {
            super.fireChange();
        }
    }
    
    private Object fRoot;

    private BufferedResourceNode fAncestor;

    private BufferedResourceNode fLeft;

    private BufferedResourceNode fRight;

    private IFile fAncestorResource;

    private IFile fMineResource;

    private IFile fTheirsResource;

    private IFile fDestinationResource;

    // we use this trick because we can't use setDirty which does not work as I
    // expected
    private boolean neverSaved = true;
    
    // use this to avoid recursion in saveChanges
    private boolean isSaving = false;

    /**
     * Creates an compare editor input for the given selection.
     */
    public ConflictsCompareInput(CompareConfiguration config) {
        super(config);
    }

    public void setResources(IFile ancestor, IFile mine, IFile theirs,
            IFile destination) {

        fAncestorResource = ancestor;
        fMineResource = mine;
        fTheirsResource = theirs;
        fDestinationResource = destination;

        initializeCompareConfiguration();
    }

    private String getType() {
        String s = fDestinationResource.getFileExtension();
        if (s != null)
            return s;
        return ITypedElement.UNKNOWN_TYPE;
    }

    /**
     * Initializes the labels in the compare configuration.
     */
    private void initializeCompareConfiguration() {
        CompareConfiguration cc = getCompareConfiguration();

        String leftLabel = "Merged - " + fDestinationResource.getName();
        String rightLabel = "Theirs - " + fTheirsResource.getName();
        String ancestorLabel = "Ancestor -" + fAncestorResource.getName();

        cc.setLeftLabel(leftLabel);

        cc.setRightLabel(rightLabel);

        cc.setAncestorLabel(ancestorLabel);
    }    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected Object prepareInput(IProgressMonitor pm)
            throws InvocationTargetException, InterruptedException {
        try {
            pm
                    .beginTask(
                            Utilities.getString("ResourceCompare.taskName"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

            fMineResource.refreshLocal(IResource.DEPTH_ZERO, Policy
                    .subMonitorFor(pm, IProgressMonitor.UNKNOWN));
            fTheirsResource.refreshLocal(IResource.DEPTH_ZERO, Policy
                    .subMonitorFor(pm, IProgressMonitor.UNKNOWN));
            fAncestorResource.refreshLocal(IResource.DEPTH_ZERO, Policy
                    .subMonitorFor(pm, IProgressMonitor.UNKNOWN));
            fDestinationResource.refreshLocal(IResource.DEPTH_ZERO, Policy
                    .subMonitorFor(pm, IProgressMonitor.UNKNOWN));

            fAncestor = new BufferedResourceNode(fAncestorResource) {
                public String getType() {
                    return ConflictsCompareInput.this.getType();
                }

                public boolean isEditable() {
                    return false;
                }
            };
            fLeft = new BufferedResourceNode(fDestinationResource) {
                public String getType() {
                    return ConflictsCompareInput.this.getType();
                }
            };
            
            InputStream mineContents = fMineResource.getContents();
            byte[] contents;
            try {
                contents = new byte[mineContents.available()];
                mineContents.read(contents);
            } finally {
                mineContents.close();
            }
            
            fLeft.setContent(contents);

            // add after setting contents, otherwise we end up in a loop
            // makes sure that the diff gets re-run if we right-click and select Save on the left pane.
            // Requires that we have a isSaving flag to avoid recursion
            fLeft.addContentChangeListener( new IContentChangeListener() {            
                public void contentChanged(IContentChangeNotifier source) {
                    if (!isSaving) {
                        try {                    
                            saveChanges(new NullProgressMonitor());
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                }            
            });

            fRight = new BufferedResourceNode(fTheirsResource) {
                public String getType() {
                    return ConflictsCompareInput.this.getType();
                }

                public boolean isEditable() {
                    return false;
                }
            };

            String title = "Conflicts on " + fDestinationResource.getName();
            setTitle(title);

            // Override the default difference visit method to use MyDiffNode 
            // instead of just DiffNode
            Differencer d = new Differencer()
            {
                protected Object visit(Object data, int result, Object ancestor, Object left, Object right) {
                    return new MyDiffNode((IDiffContainer) data, result, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
                }
            };

            fRoot = d.findDifferences(true, pm, null, fAncestor, fLeft, fRight);
            return fRoot;
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            pm.done();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.compare.CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void saveChanges(IProgressMonitor pm) throws CoreException {
        try {
            isSaving = true;
            super.saveChanges(pm);
            fLeft.commit(pm);
            neverSaved = false;
            ((MyDiffNode)fRoot).fireChange();
        } finally {
            isSaving = false;
        }
    }

    public boolean isSaveNeeded() {
        if (neverSaved) {
            return true;
        } else {
            return super.isSaveNeeded();
        }
    }

}