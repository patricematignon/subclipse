package org.tigris.subversion.subclipse.core.properties;

/**
 * @author Brock Janiczak
 */
public class SVNPropertyDefinition {
    
    private final String name;
    private final String description;

    public SVNPropertyDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    
    public String getDescription() {
        return this.description;
    }
    
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.name;
    }
}
