/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.conflicts;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.ui.Policy;

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

            Differencer d = new Differencer();

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
        super.saveChanges(pm);
        fLeft.commit(pm);
        neverSaved = false;
    }

    public boolean isSaveNeeded() {
        if (neverSaved) {
            return true;
        } else {
            return super.isSaveNeeded();
        }
    }

}