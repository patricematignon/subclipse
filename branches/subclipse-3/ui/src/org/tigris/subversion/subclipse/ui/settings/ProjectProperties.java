package org.tigris.subversion.subclipse.ui.settings;

import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.ISVNProperty;

public class ProjectProperties {
    protected String label;
    protected String message;
    protected boolean number;
    protected String url;
    protected boolean warnIfNoIssue;
    protected boolean append;

    public ProjectProperties() {
        super();
    }

    public boolean isAppend() {
        return append;
    }
    public void setAppend(boolean append) {
        this.append = append;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isNumber() {
        return number;
    }
    public void setNumber(boolean number) {
        this.number = number;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public boolean isWarnIfNoIssue() {
        return warnIfNoIssue;
    }
    public void setWarnIfNoIssue(boolean warnIfNoIssue) {
        this.warnIfNoIssue = warnIfNoIssue;
    }
    
    public String getResolvedMessage(String issue) {
        return message.replaceAll("%BUGID%", issue);
    }
    
    public String getResolvedUrl(String issue) {
        return url.replaceAll("%BUGID%", issue);
    }
    
    // Return error message if there are any problems with the issue that was entered.
    public String validateIssue(String issue) {
        if (number) {
           if (!hasOnlyDigits(issue)) return Policy.bind("CommitDialog.number", label); //$NON-NLS-1$
        }
        return null;
    }
    
    // Helper method to test for all numerics and commas.
    private boolean hasOnlyDigits(String s) {
        for (int i=0; i<s.length(); i++) if ((!(s.charAt(i) == ',')) && !Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    
    public String toString() {
       return "bugtraq:label: " + label + "\n" + //$NON-NLS-1$
              "bugtraq:message: " + message + "\n" + //$NON-NLS-1$
              "bugtraq:number: " + number + "\n" + //$NON-NLS-1$
              "bugtraq:url: " + url + "\n" + //$NON-NLS-1$
              "bugtraq:warnifnoissue: " + warnIfNoIssue + "\n" + //$NON-NLS-1$
              "bugtraq:append: " + append; //$NON-NLS-1$
    }
    
    // Get ProjectProperties for selected resource.  First looks at selected resource,
    // then works up through ancestors until a folder with the bugtraq:message property
    // is found.  If none found, returns null.
    public static ProjectProperties getProjectProperties(IResource resource) throws SVNException {
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        ISVNProperty property = svnResource.getSvnProperty("bugtraq:message"); //$NON-NLS-1$
        if (property != null) {
            String value = property.getValue();
            ProjectProperties projectProperties = new ProjectProperties();
            if (value == null) projectProperties.setMessage("Reference: %BUGID%"); //$NON-NLS-1$
            else projectProperties.setMessage(value);
            property = svnResource.getSvnProperty("bugtraq:label"); //$NON-NLS-1$
            if ((property == null) || (property.getValue() == null)) projectProperties.setLabel("Reference:"); //$NON-NLS-1$
            else projectProperties.setLabel(property.getValue());
            property = svnResource.getSvnProperty("bugtraq:url"); //$NON-NLS-1$
            if ((property != null) && (property.getValue() != null)) projectProperties.setUrl(property.getValue()); 
            property = svnResource.getSvnProperty("bugtraq:number"); //$NON-NLS-1$
            if ((property == null) || (property.getValue() == null)) projectProperties.setNumber(false);
            else projectProperties.setNumber(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$  
            property = svnResource.getSvnProperty("bugtraq:warnifnoissue"); //$NON-NLS-1$
            if ((property == null) || (property.getValue() == null)) projectProperties.setWarnIfNoIssue(false);
            else projectProperties.setWarnIfNoIssue(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$   
            property = svnResource.getSvnProperty("bugtraq:append"); //$NON-NLS-1$
            if ((property == null) || (property.getValue() == null)) projectProperties.setAppend(true);
            else projectProperties.setAppend(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$                                   
            return projectProperties;           
        }
        IResource checkResource = resource;
        while (checkResource.getParent() != null) {
            checkResource = checkResource.getParent();
            if (checkResource.getParent() == null) return null;
            svnResource = SVNWorkspaceRoot.getSVNResourceFor(checkResource);
            property = svnResource.getSvnProperty("bugtraq:message"); //$NON-NLS-1$
            if (property != null) return getProjectProperties(checkResource);
        }
        return null;
    }
}
