package org.tigris.subversion.subclipse.core.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

/**
 * @author Brock Janiczak
 */
public class SVNPropertyManager {

    public static final String DEFAULT_GROUP = "other";
    private static SVNPropertyManager instance;
    private Map propertyGroups = new HashMap();
    
    // Group used if no group is specified on the extension, or that group does not exist
    // TODO look at provising an isDefault attribute for groups (can't see this being useful)
    private SVNPropertyGroupDefinition defaultGroup = new SVNPropertyGroupDefinition("Other", "Unclassified properties");
    

    public static SVNPropertyManager getInstance() {
        if (instance == null) {
            instance = new SVNPropertyManager();
        }
        return instance;
    }
    
    private SVNPropertyManager() {
        registerDefaultGroup();
        loadGroupsFromExtensions();
        
        loadPropertiesFromExtensions();
        loadUserDefinedProperties();
    }

    private void registerDefaultGroup() {
        propertyGroups.put(DEFAULT_GROUP, defaultGroup);
    }
    
    private void loadGroupsFromExtensions() {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(SVNProviderPlugin.ID, SVNProviderPlugin.SVN_PROPERTY_GROUPS_EXTENSION);
        IExtension[] extensions =  extension.getExtensions();
        
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                String groupId = configElements[j].getAttribute("id"); //$NON-NLS-1$
                String groupName = configElements[j].getAttribute("name"); //$NON-NLS-1$
                String description = "";
                IConfigurationElement[] descriptionElements = configElements[j].getChildren("description");
                if (descriptionElements.length == 1) {
                    description = descriptionElements[0].getValue();
                }
                
                // TODO check if the group is already registered
                propertyGroups.put(groupId, new SVNPropertyGroupDefinition(groupName, description));
            }
        }
    }
    private void loadPropertiesFromExtensions() {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(SVNProviderPlugin.ID, SVNProviderPlugin.SVN_PROPERTY_TYPES_EXTENSION);
        IExtension[] extensions =  extension.getExtensions();
        
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                
                String groupId = configElements[j].getAttribute("groupId"); //$NON-NLS-1$
                if (groupId == null) {
                    groupId = DEFAULT_GROUP;
                }
                
                String name = configElements[j].getAttribute("name"); //$NON-NLS-1$
                String type = configElements[j].getAttribute("type"); //$NON-NLS-1$
                String description = "";
                
                IConfigurationElement[] descriptionElements = configElements[j].getChildren("description");
                if (descriptionElements.length == 1) {
                    description = descriptionElements[0].getValue();
                }
                
                SVNPropertyGroupDefinition propertyGroup = (SVNPropertyGroupDefinition) propertyGroups.get(groupId);
                SVNPropertyDefinition property = new SVNPropertyDefinition(name, description);
                if (propertyGroup != null) {
                    propertyGroup.addProperty(property);
                } else {
                    // TODO warn about missing group
                    defaultGroup.addProperty(property);
                }
                
                
            }
        }
    }
    
    private void loadUserDefinedProperties() {
        // TODO implement this
    }
    
    public SVNPropertyGroupDefinition[] getGroups() {
        return (SVNPropertyGroupDefinition[]) propertyGroups.values().toArray(new SVNPropertyGroupDefinition[propertyGroups.size()]);
    }
}
