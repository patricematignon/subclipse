package org.tigris.subversion.subclipse.core.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brock Janiczak
 */
public class SVNPropertyGroupDefinition {

    private final String groupName;
    private final String description;

    private List properties = new ArrayList();
    
    public SVNPropertyGroupDefinition(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
        
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getGroupName() {
        return this.groupName;
    }
    
    public void addProperty(SVNPropertyDefinition property) {
        properties.add(property);
    }
    
    public SVNPropertyDefinition[] getProperties() {
        return (SVNPropertyDefinition[]) properties.toArray(new SVNPropertyDefinition[properties.size()]);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.groupName;
    }
}
