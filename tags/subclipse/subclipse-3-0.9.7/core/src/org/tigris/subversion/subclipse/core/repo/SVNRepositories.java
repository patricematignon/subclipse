/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion  
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.repo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.core.util.Util;

/**
 * The list of known repositories
 *
 */
public class SVNRepositories 
{
    private Map repositories = new HashMap();
    private static final String REPOSITORIES_STATE_FILE = ".svnProviderState"; //$NON-NLS-1$
    
    // version numbers for the state file (a positive number indicates version 1)
    private static final int REPOSITORIES_STATE_FILE_VERSION_1 = 1;

    /*
     * Add the repository location to the cached locations
     */
    private void addToRepositoriesCache(ISVNRepositoryLocation repository) {
        repositories.put(repository.getLocation(), repository);
        SVNProviderPlugin.getPlugin().getRepositoryResourcesManager().repositoryAdded(repository);
    }
    
    /*
     * Remove the repository location from the cached locations
     */
    private void removeFromRepositoriesCache(ISVNRepositoryLocation repository) {
        if (repositories.remove(repository.getLocation()) != null) {
            SVNProviderPlugin.getPlugin().getRepositoryResourcesManager().repositoryRemoved(repository);
        }
    }

    /**
     * Add the repository to the receiver's list of known repositories. Doing this will enable
     * password caching accross platform invokations.
     */
    public void addRepository(ISVNRepositoryLocation repository) throws SVNException {
        // Check the cache for an equivalent instance and if there is one, just update the cache
        SVNRepositoryLocation existingLocation = (SVNRepositoryLocation)repositories.get(repository.getLocation());
        if (existingLocation != null) {
            ((SVNRepositoryLocation)repository).updateCache();
        } else {
            // Cache the password and register the repository location
            addToRepositoriesCache(repository);
            ((SVNRepositoryLocation)repository).updateCache();
        }
        saveState();
    }
    
    /**
     * Dispose of the repository location
     * 
     * Removes any cached information about the repository such as a remembered password.
     */
    public void disposeRepository(ISVNRepositoryLocation repository) throws SVNException {
        ((SVNRepositoryLocation)repository).dispose();
        removeFromRepositoriesCache(repository);
    }

    /** 
     * Return a list of the know repository locations
     */
    public ISVNRepositoryLocation[] getKnownRepositories() {
        return (ISVNRepositoryLocation[])repositories.values().toArray(new ISVNRepositoryLocation[repositories.size()]);
    }

    public void refreshRepositoriesFolders() {
        ISVNRepositoryLocation[] repositories = getKnownRepositories();
        for (int i = 0; i < repositories.length;i++) {
            repositories[i].refreshRootFolder();
        }
    }

    /**
     * Create a repository instance from the given properties.
     * The supported properties are:
     * 
     *   user The username for the connection (optional)
     *   password The password used for the connection (optional)
     *   url The url where the repository resides
     * 
     * The created instance is not known by the provider and it's user information is not cached.
     * The purpose of the created location is to allow connection validation before adding the
     * location to the provider.
     * 
     * This method will throw a SVNException if the location for the given configuration already
     * exists.
     */
    public ISVNRepositoryLocation createRepository(Properties configuration) throws SVNException {
        // Create a new repository location
        SVNRepositoryLocation location = SVNRepositoryLocation.fromProperties(configuration);
        
        // Check the cache for an equivalent instance and if there is one, throw an exception
        SVNRepositoryLocation existingLocation = (SVNRepositoryLocation)repositories.get(location.getLocation());
        if (existingLocation != null) {
            throw new SVNException(new SVNStatus(SVNStatus.ERROR, Policy.bind("SVNProvider.alreadyExists"))); //$NON-NLS-1$
        }

        return location;
    }

	/**
	 * Get the repository instance which matches the given String. 
	 * The format of the String is an url
	 */
	public ISVNRepositoryLocation getRepository(String location) throws SVNException {
        
        
		Set keys = repositories.keySet();
		for(Iterator iter = keys.iterator();iter.hasNext();){
			String url = (String)iter.next();
			if(location.indexOf(url)!=-1){
				return (ISVNRepositoryLocation) repositories.get(url);
			}
        	
		}//else we couldn't find it, fall through to adding new repo.
		ISVNRepositoryLocation repository = SVNRepositoryLocation.fromString(location);
		addToRepositoriesCache(repository);
        
		return repository;
	}
    
    

    /**
     * load the state of the plugin, ie the repositories locations 
     *
     */
    private void loadState() {
        try {
            IPath pluginStateLocation = SVNProviderPlugin.getPlugin().getStateLocation().append(REPOSITORIES_STATE_FILE);
            File file = pluginStateLocation.toFile();
            if (file.exists()) {
                try {
                    DataInputStream dis = new DataInputStream(new FileInputStream(file));
                    readState(dis);
                    dis.close();
                } catch (IOException e) {
                    throw new TeamException(new Status(Status.ERROR, SVNProviderPlugin.ID, TeamException.UNABLE, Policy.bind("SVNProvider.ioException"), e));  //$NON-NLS-1$
                }
            } /* else {
                // If the file did not exist, then prime the list of repositories with
                // the providers with which the projects in the workspace are shared.
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (int i = 0; i < projects.length; i++) {
                    RepositoryProvider provider = RepositoryProvider.getProvider(projects[i], CVSProviderPlugin.getTypeId());
                    if (provider!=null) {
                        ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(projects[i]);
                        FolderSyncInfo info = folder.getFolderSyncInfo();
                        if (info != null) {
                            ICVSRepositoryLocation result = getRepository(info.getRoot());
                        }
                    }
                }
                saveState();
            }*/
        } catch (TeamException e) {
            Util.logError(Policy.bind("SVNProvider.errorLoading"), e);//$NON-NLS-1$
        }
    }
    

    /**
     * Save the state of the plugin, ie the repositories locations 
     */
    private void saveState() {
        try {
            IPath pluginStateLocation = SVNProviderPlugin.getPlugin().getStateLocation();
            File tempFile = pluginStateLocation.append(REPOSITORIES_STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
            File stateFile = pluginStateLocation.append(REPOSITORIES_STATE_FILE).toFile();
            try {
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
                writeState(dos);
                dos.close();
                if (stateFile.exists()) {
                    stateFile.delete();
                }
                boolean renamed = tempFile.renameTo(stateFile);
                if (!renamed) {
                    throw new TeamException(new Status(Status.ERROR, SVNProviderPlugin.ID, TeamException.UNABLE, Policy.bind("SVNProvider.rename", tempFile.getAbsolutePath()), null)); //$NON-NLS-1$
                }
            } catch (IOException e) {
                throw new TeamException(new Status(Status.ERROR, SVNProviderPlugin.ID, TeamException.UNABLE, Policy.bind("SVNProvider.save",stateFile.getAbsolutePath()), e)); //$NON-NLS-1$
            }
        } catch (TeamException e) {
            Util.logError(Policy.bind("SVNProvider.errorSaving"), e);//$NON-NLS-1$
        }
    }
    
    /**
     * read the state of the plugin, ie the repositories locations
     * @param dis
     * @throws IOException
     * @throws SVNException
     */
    private void readState(DataInputStream dis) throws IOException, SVNException {
        int count = dis.readInt();
        if (count == REPOSITORIES_STATE_FILE_VERSION_1) {
            count = dis.readInt();
            for(int i = 0; i < count;i++){
                ISVNRepositoryLocation root = getRepository(dis.readUTF());

            }
            
        } else {
            Util.logError(Policy.bind("SVNProviderPlugin.unknownStateFileVersion", new Integer(count).toString()), null); //$NON-NLS-1$
        }
    }
    
    /**
     * write the state of the plugin ie the repositories locations
     * @param dos
     * @throws IOException
     */
    private void writeState(DataOutputStream dos) throws IOException {
        // Write the repositories
        dos.writeInt(REPOSITORIES_STATE_FILE_VERSION_1);
        // Write out the repos
        Collection repos = repositories.values();
        dos.writeInt(repos.size());
        Iterator it = repos.iterator();
        while (it.hasNext()) {
            SVNRepositoryLocation root = (SVNRepositoryLocation)it.next();
            dos.writeUTF(root.getLocation());
        }
		dos.flush();
		dos.close();
    }

    public void startup() {
        loadState();
    }

    public void shutdown() {
        saveState();
    }

	/**
	 * Answer whether the provided repository location is known by the provider or not.
	 * The location string corresponds to the Strin returned by ICVSRepositoryLocation#getLocation()
	 */
	public boolean isKnownRepository(String location) {
		Set keys = repositories.keySet();
		for(Iterator iter = keys.iterator();iter.hasNext();){
			if(location.indexOf((String)iter.next())!=-1){
				return true;
			}
    		
		}
		return false;
	}

}
