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
package org.tigris.subversion.subclipse.ui.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.util.LinkList;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public class ProjectProperties {
    protected String label = "Issue Number:"; //$NON-NLS-1$
    protected String message;
    protected boolean number = false;
    protected String url;
    protected boolean warnIfNoIssue = false;
    protected boolean append = true;
    protected String logregex;
    
    private static final String URL = "://"; //$NON-NLS-1$

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
    
	public String getLogregex() {
		return logregex;
	}

	public void setLogregex(String logregex) {
		this.logregex = logregex;
	}

	public String getResolvedMessage(String issue) {
        if (message == null) return null;
        return message.replaceAll("%BUGID%", issue); //$NON-NLS-1$
    }
    
    public String getResolvedUrl(String issue) {
        if (url == null) return null;
        return url.replaceAll("%BUGID%", issue); //$NON-NLS-1$
    }
    
    // Retrieve hyperlink ranges and url's from commit message.
    public LinkList getLinkList(String commitMessage) {
        ArrayList links = new ArrayList();
        ArrayList urls = new ArrayList();
        ArrayList texts = new ArrayList();
        String bugID = "%BUGID%"; //$NON-NLS-1$    	
    	
        if( logregex != null ) {
    		String[] resplit = logregex.split("\n");
    		String re1 = resplit[0].trim();
    		String re2 = resplit.length > 1 ? resplit[1].trim() : null;

    		Pattern pre1 = Pattern.compile(re1);
    		Matcher matcher1 = pre1.matcher(commitMessage);
    		if (re2 == null) {
    			while (matcher1.find()) {
    				for (int i = 0; i < matcher1.groupCount(); i++) {
    			        int range[] = {matcher1.start(i+1), matcher1.end(i+1)-matcher1.start(i+1)};
    			        String url = getResolvedUrl(matcher1.group(i+1));
    			        if ((url != null) && (url.trim().length() > 0)) { 
    			            links.add(range);
    			            urls.add(url);
    			            texts.add(matcher1.group(i+1));
    			        }
    				}
    			}
    		} else {
    			Pattern pre2 = Pattern.compile(re2);
    			while (matcher1.find()) {
    				Matcher matcher2 = pre2.matcher(matcher1.group());
    				while (matcher2.find()) {
    					for (int i = 0; i < matcher2.groupCount(); i++) {
        			        int range[] = {matcher2.start(i+1) + matcher1.start(), matcher2.end(i+1)-matcher2.start(i+1)};
        			        String url = getResolvedUrl(matcher2.group(i+1));
        			        if ((url != null) && (url.trim().length() > 0)) { 
        			            links.add(range);
        			            urls.add(url);
        			            texts.add(matcher2.group(i+1));
        			        }
    					}
    				}
    			}
    		}
        } else if (message != null) {
	        int index = message.indexOf(bugID);
	        if (index != -1) {
	        	String remainder = null;
	        	if (message.length() > index + bugID.length())
	        		remainder = message.substring(index + bugID.length());
	        	else
	        		remainder = "";
		        String tag = message.substring(0, index);
		        index = commitMessage.indexOf(tag);
		        if (index != -1) {
			        index = index + tag.length();
			        int start = index;
			        StringBuffer issue = new StringBuffer();
			        while (index < commitMessage.length()) {
			            if (commitMessage.substring(index, index + 1).equals(",")) { //$NON-NLS-1$
			                int range[] = {start, issue.length()};
			                String url = getResolvedUrl(issue.toString());
			                if ((url != null) && (url.trim().length() > 0)) {
			                    links.add(range);
			                    urls.add(url);
			                }
			                start = index + 1;
			                issue = new StringBuffer();
			            } else {
			                if (commitMessage.substring(index, index + 1).equals("\n") || commitMessage.substring(index, index + 1).equals("\r")) break; //$NON-NLS-1$ //$NON-NLS-2$
			                if (commitMessage.substring(index).trim().equals(remainder.trim())) break;
			                if (commitMessage.substring(index).startsWith(remainder + "\n")) break;
			                if (commitMessage.substring(index, index + 1).equals(" ")) {
			                    int lineIndex = commitMessage.indexOf("\n", index);
			                    if (lineIndex == -1) lineIndex = commitMessage.indexOf("\r", index);
			                    if (lineIndex != -1) {
			                        if (commitMessage.substring(index, lineIndex - 1).trim().length() == 0)
			                        break;
			                    }
			                }
			                issue.append(commitMessage.substring(index, index + 1));
			            }
			            index++;
			        }  
			        int range[] = {start, issue.length()};
			        String url = getResolvedUrl(issue.toString());
			        if ((url != null) && (url.trim().length() > 0)) { 
			            links.add(range);
			            urls.add(url);
			            texts.add(issue.toString());
			        }
		        }
	        }
        }
        LinkList urlLinks = getUrls(commitMessage);
        int[][] urlRanges = urlLinks.getLinkRanges();
        String[] urlUrls = urlLinks.getUrls();
        for (int i = 0; i < urlRanges.length; i++) {
            links.add(urlRanges[i]);
            urls.add(urlUrls[i]);
        }
        int[][] linkRanges = new int[links.size()][2];
        links.toArray(linkRanges);
        String[] urlArray = new String[urls.size()];
        urls.toArray(urlArray);
        String[] textArray = new String[texts.size()];
        texts.toArray(textArray);
        LinkList linkList = new LinkList(linkRanges, urlArray, textArray);
        return linkList;
    }
    
    public static LinkList getUrls(String s) {
        int max = 0;
        int i = -1;
        if (s != null) {
            max = s.length();
            i = s.indexOf(URL);
        }
    	ArrayList linkRanges = new ArrayList();
    	ArrayList links = new ArrayList();
    	while (i != -1) {
    	    while (i != -1) {
    	        if (Character.isWhitespace(s.charAt(i)) || s.substring(i, i + 1).equals("\n")) { //$NON-NLS-1$
    	            i++;
    	            break;
    	        }
    	        i--;
    	    }
    		int start = (i < 0) ? 0 : i;
    		// look for the first whitespace character
    		boolean found = false;
    		i += URL.length();
    		while (!found && i < max) {
    			found = (Character.isWhitespace(s.charAt(i)) || s.substring(i, i + 1).equals("\n")); //$NON-NLS-1$
    			i++;
    		}
    		if (i!=max) i--;
    		linkRanges.add(new int[] {start, i - start});
    		links.add(s.substring(start, i));
    		i = s.indexOf(URL, i);
    	}
    	return new LinkList(
    			(int[][])linkRanges.toArray(new int[linkRanges.size()][2]),
    			(String[])links.toArray(new String[links.size()]), null);
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
       return "bugtraq:label: " + label + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
              "bugtraq:logregex: " + logregex + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
       		  "bugtraq:message: " + message + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
              "bugtraq:number: " + number + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
              "bugtraq:url: " + url + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
              "bugtraq:warnifnoissue: " + warnIfNoIssue + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
              "bugtraq:append: " + append; //$NON-NLS-1$
    }
    
    private static ISVNProperty getSvnProperty(File file, String name) throws SVNException {
    	ISVNClientAdapter svnClient = null;
		try {
			svnClient = SVNProviderPlugin.getPlugin().getSVNClient();
	        SVNProviderPlugin.disableConsoleLogging(); 
			ISVNProperty prop = svnClient.propertyGet(file, name);
	        return prop;
		} catch (SVNClientException e) {
	        throw SVNException.wrapException(e); 
		} finally {
	        SVNProviderPlugin.enableConsoleLogging();
	        SVNProviderPlugin.getPlugin().getSVNClientManager().returnSVNClient(svnClient);
		}
    }
    
    private static ProjectProperties getProjectProperties(File file, ISVNLocalResource svnResource) throws SVNException {
    	if (file == null) return null;
    	ISVNProperty property = null;
    	boolean hasBugtraq = false;
        try {
        	property = getSvnProperty(file, "bugtraq:message"); //$NON-NLS-1$
            if( property != null && !"".equals(property.getValue().trim()) ) {
            	hasBugtraq = true;
            }
			property = getSvnProperty(file, "bugtraq:logregex"); //$NON-NLS-1$
            if( property != null && !"".equals(property.getValue().trim()) ) {
            	hasBugtraq = true;
            }
		} catch (SVNException e) {
			if (e.getMessage() != null && e.getMessage().indexOf("Path is not a working copy") != -1) { //$NON-NLS-1$
				return null;
			}
		}
        if (hasBugtraq) {
            ProjectProperties projectProperties = new ProjectProperties();
            property = getSvnProperty(file, "bugtraq:message"); //$NON-NLS-1$
            if (property != null) projectProperties.setMessage(property.getValue()); 
            property = getSvnProperty(file, "bugtraq:label"); //$NON-NLS-1$
            if (property != null) projectProperties.setLabel(property.getValue()); 
            property = getSvnProperty(file, "bugtraq:url"); //$NON-NLS-1$
            if (property != null) projectProperties.setUrl(resolveUrl(property.getValue(), svnResource));
            property = getSvnProperty(file, "bugtraq:number"); //$NON-NLS-1$
            if ((property != null) && (property.getValue() != null)) projectProperties.setNumber(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$  
            property = getSvnProperty(file, "bugtraq:warnifnoissue"); //$NON-NLS-1$
            if ((property != null) && (property.getValue() != null)) projectProperties.setWarnIfNoIssue(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$   
            property = getSvnProperty(file, "bugtraq:append"); //$NON-NLS-1$
            if ((property != null) && (property.getValue() != null)) projectProperties.setAppend(property.getValue().equalsIgnoreCase("true")); //$NON-NLS-1$                                   
            property = getSvnProperty(file, "bugtraq:logregex"); //$NON-NLS-1$
            if ((property != null) && (property.getValue() != null)) projectProperties.setLogregex(property.getValue()); //$NON-NLS-1$                                   
            return projectProperties;           
        }
        File checkFile = file;
        while (checkFile.getParentFile() != null) {
            checkFile = checkFile.getParentFile();
            try {
	            return getProjectProperties(checkFile, svnResource);
            } catch (SVNException e) {}
        }
        return null;
    }
    
    // Get ProjectProperties for selected resource.  First looks at selected resource,
    // then works up through ancestors until a folder with the bugtraq:message property
    // is found.  If none found, returns null.
    public static ProjectProperties getProjectProperties(IResource resource) throws SVNException {
        if (resource == null || resource.getLocation() == null) return null;
    	ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
    	if (svnResource != null) {
    		return getProjectProperties(new File(resource.getLocation().toString()), svnResource);
    	}
        return null;
    }
    
    private static String resolveUrl(String url, ISVNLocalResource svnResource) {
    	String resolvedUrl = null;
    	
    	// Relative to repository root, with navigators.
    	if (url.startsWith("^/")) {
    		SVNUrl repositoryUrl = svnResource.getRepository().getUrl();
    		String path = url.substring(1);
    		while (path.startsWith("/..")) {
    			if (repositoryUrl.getParent() == null) break;
    			repositoryUrl = repositoryUrl.getParent();
    			path = path.substring(3);
    		}
    		resolvedUrl = repositoryUrl + path;
    	}
    	
    	// Relative to host.
    	else if (url.startsWith("/")) {
    		String resourceUrl = svnResource.getUrl().toString();
    		String protocol = svnResource.getUrl().getProtocol();
    		int start = protocol.length();
    		while (resourceUrl.substring(start, start + 1).equals(":") || resourceUrl.substring(start, start + 1).equals("/"))
    			start++;
    		int end = resourceUrl.indexOf("/", start);
    		if (end == -1) resolvedUrl = resourceUrl + url;
    		else resolvedUrl = resourceUrl.substring(0, end) + url;
    	}
    	
    	//  Non-relative
    	else resolvedUrl = url;
    	
    	return resolvedUrl;
    }
    
    public static ProjectProperties getProjectProperties(ISVNRemoteResource remoteResource)  throws SVNException {
		return getProjectProperties(remoteResource.getResource());
    }
}
