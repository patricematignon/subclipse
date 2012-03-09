/*******************************************************************************
 * Copyright (c) 2003, 2006 svnClientAdapter project and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.svnclientadapter;
import java.util.Date;

/**
 * An interface describing subversion directory entry.
 * (E.g. a record returned by call to svn list)
 * 
 * @author C�dric Chabanois
 */
public interface ISVNDirEntry {

    /**
     * @return the pathname of the entry
     */
	String getPath();

    /**
     * @return the date of the last change
     */
	Date getLastChangedDate();

    /**
     * @return the revision number of the last change
     */
	SVNRevision.Number getLastChangedRevision();

    /**
     * @return true if the item has properties managed by subversion
     */
	boolean getHasProps();

    /**
     * @return the name of the author of the last change
     */
	String getLastCommitAuthor();

    /**
     * @return the kind of the node (directory or file)
     */
	SVNNodeKind getNodeKind();

    /**
     * @return length of file text, or 0 for directories
     */
	long getSize();
}
