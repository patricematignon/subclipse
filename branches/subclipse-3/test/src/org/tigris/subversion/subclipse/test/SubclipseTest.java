/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.test;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;


public abstract class SubclipseTest extends TestCase {
	protected SVNRepositories repositories;
	protected ISVNRepositoryLocation repositoryLocation;
	protected String url;
	protected String user;
	protected String pass;
	protected String remoteHttpsUrl;
	protected String remoteHttpUrl;
	protected String testRepoArchive;
	public SubclipseTest(String name) {
		super(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		url = "file://" + System.getProperty("java.io.tmpdir") + "/trepo";
		testRepoArchive = System.getProperty("svn.repo.arch");
		remoteHttpUrl = System.getProperty("remote.http.url");//in case we're
		// testing auth or
		// something.
		remoteHttpsUrl = System.getProperty("remote.https.url");
		user = System.getProperty("svn.user");
		pass = System.getProperty("svn.pass");
		String mode = System.getProperty("svn.mode");
		File archFile = new File(testRepoArchive);
		if (testRepoArchive == null)
			throw new RuntimeException(
					"svn.repo.arch property must be an absolute path to trepo.zip");
		if (!archFile.exists())
			throw new RuntimeException("can't find file: "
					+ archFile.getAbsolutePath());
		if (!url.startsWith("file://"))
			throw new RuntimeException("svn.url must be a file:// url.");
		extractArchive(archFile);
		SVNProviderPlugin plugin = SVNProviderPlugin.getPlugin();
		// do we use javahl or command line ?
		if (mode == null || mode.equalsIgnoreCase("javahl")) {
			plugin.setSvnClientInterface(SVNClientAdapterFactory.JAVAHL_CLIENT);
			if (plugin.getSvnClientInterface() != SVNClientAdapterFactory.JAVAHL_CLIENT) {
				System.out.println("Warning : Can't use Javahl");
			}
		} else if (mode.equalsIgnoreCase("cli")) {
			plugin
					.setSvnClientInterface(SVNClientAdapterFactory.COMMANDLINE_CLIENT);
			if (plugin.getSvnClientInterface() != SVNClientAdapterFactory.JAVAHL_CLIENT) {
				System.out
						.println("Warning : Can't use command line interface");
			}
		} else {
			throw new RuntimeException("Unknown svn.mode: " + mode);
		}
		// get the ISVNRepositoryLocation corresponding to our repository
		repositories = plugin.getRepositories();
		Properties properties = new Properties();
		if (url == null)
			throw new RuntimeException(
					"You must set at least set the svn.url property to run these tests.");
		properties.setProperty("url", url);
		if (user != null)
			properties.setProperty("user", user);
		if (pass != null)
			properties.setProperty("password", pass);
		
		repositoryLocation = repositories.createRepository(properties);
	}
	/**
	 * @param archFile
	 * @todo Generated comment
	 */
	private void extractArchive(File archFile) throws IOException {
		ZipFile zf = new ZipFile(archFile);
		Enumeration entries = zf.getEntries();
		String tmp = System.getProperty("java.io.tmpdir");
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (!entry.isDirectory()) {
				int len;
				OutputStream out = new BufferedOutputStream(new FileOutputStream(
						new File(tmp, entry.getName())));
				InputStream in = zf.getInputStream(entry);
				byte[] buffer = new byte[1024];
				while ((len = in.read(buffer)) >= 0)
					out.write(buffer, 0, len);
				in.close();
				out.close();
			} else {
				File newdir = new File(tmp, entry.getName());
				if (!newdir.exists() && !newdir.mkdirs()) {
					throw new RuntimeException("Failed to create: "
							+ System.getProperty("java.io.tmpdir") + " :"
							+ entry.getName());
				}
			}
		}
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		TestProject.waitForIndexer();
		// delete all the projects we created in the test
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			SVNTeamProvider teamProvider = (SVNTeamProvider) RepositoryProvider
					.getProvider(project, SVNProviderPlugin.getTypeId());
			if (teamProvider != null) {
				unshareProject(project);
			}
			Exception e = null;
			int retry = 0;
			try {
				// delete the project
				project.delete(true, true, null);
			} catch (Exception ex) {
				e = ex;
			}
		}
		// remove all the repositories
		ISVNRepositoryLocation[] svnRepositoryLocations = repositories
				.getKnownRepositories();
		for (int i = 0; i < svnRepositoryLocations.length; i++) {
			repositories.disposeRepository(svnRepositoryLocations[i]);
		}
	}
	/**
	 * create a project with a Class
	 * @param projectName
	 * @return
	 * @throws CoreException
	 */
	protected TestProject createProjectWithAClass(String projectName) throws CoreException {
		TestProject testProject = new TestProject(projectName);
		
		// create a file
		IPackageFragment package1 = testProject.createPackage("pack1");
		IType type = testProject.createJavaType(package1,"AClass.java",
			"public class AClass { \n" +
			"  public void m() {}\n" +
			"}");
		
		return testProject;
	}

	/**
	 * share the project using svn
	 * 
	 * @throws Exception
	 */
	protected void shareProject(IProject project) throws TeamException {
		SVNWorkspaceRoot.shareProject(repositoryLocation, project, null, null);
	}
	/**
	 * unshare the project (do not delete .svn directories)
	 * 
	 * @throws TeamException
	 */
	protected void unshareProject(IProject project) throws TeamException {
		RepositoryProvider.unmap(project);
	}
	


	/**
	 * @return
	 */
	protected void addAndCommit(IProject project, IResource resource,
			String comment) throws SVNException, TeamException {
		SVNTeamProvider provider = getProvider(project);
		// add it to repository
		provider.add(new IResource[]{resource}, IResource.DEPTH_ZERO, null);
		// commit it
		provider.checkin(new IResource[]{resource}, comment,
				IResource.DEPTH_ZERO, null);
	}
	/**
	 * @return
	 */
	public ISVNRepositoryLocation getRepositoryLocation() {
		return repositoryLocation;
	}
	public SVNTeamProvider getProvider(IProject project) {
		return (SVNTeamProvider) RepositoryProvider.getProvider(project,
				SVNProviderPlugin.getTypeId());
	}
}
