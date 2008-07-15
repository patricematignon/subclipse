package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;

public class RevisionGraphEditorInput implements IEditorInput {
	
	private IResource resource;

	public RevisionGraphEditorInput(IResource resource) {
		this.resource = resource;
	}
	
	public IResource getResource() {
		return resource;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getEditorRegistry()
			.getImageDescriptor(resource.getName());
	}

	public String getName() {
		return resource.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}

	public Object getAdapter(Class adapter) {
		if(adapter == IResource.class) {
			return resource;
		}
		return resource.getAdapter(adapter);
	}

}
