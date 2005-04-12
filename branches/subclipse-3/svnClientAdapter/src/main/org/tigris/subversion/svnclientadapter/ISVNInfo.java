/*
 *  Copyright(c) 2003-2004 by the authors indicated in the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tigris.subversion.svnclientadapter;

import java.io.File;
import java.util.Date;

/**
 * Give information about one subversion item (file or directory) in the
 * working copy
 */
public interface ISVNInfo
{

    /**
     * file on which we get info
     * @return file
     */
    public File getFile();

    /**
     * Retrieves the url of the item
     * @return url of the item
     */
    public SVNUrl getUrl();

    /**
     * Retrieves the uuid of the repository
     * @return  uuid of the repository
     */
    public String getUuid();

    /**
     * Retrieves the url of the repository
     * @return url of the repository
     */
    public SVNUrl getRepository();

    /**
     * Retrieves the schedule of the next commit
     * @return schedule of the next commit
     */
    public SVNScheduleKind getSchedule();

    /**
     * Retrieves the nodeKind
     * @return nodeKind
     */
    public SVNNodeKind getNodeKind();

    /**
     * Retrieves the author of the last commit
     * @return author of the last commit
     */
    public String getLastCommitAuthor();

    /**
     * Retrieves the last revision the item was updated to
     * @return last revision the item was updated to
     */
    public SVNRevision.Number getRevision();

    /**
     * Retrieves the revision of the last commit
     * @return the revision of the last commit
     */
    public SVNRevision.Number getLastChangedRevision();

    /**
     * Retrieves the date of the last commit
     * @return the date of the last commit
     */
    public Date getLastChangedDate();

    /**
     * Retrieves the last date the text content was changed
     * @return last date the text content was changed
     */
    public Date getLastDateTextUpdate();

    /**
     * Retrieves the last date the properties were changed
     * @return last date the properties were changed
     */
    public Date getLastDatePropsUpdate();

    /**
     * Retrieve if the item was copied
     * @return the item was copied
     */
    public boolean isCopied();

    /**
     * Retrieves the copy source revision
     * @return copy source revision
     */
    public SVNRevision.Number getCopyRev();

    /**
     * Retrieves the copy source url
     * @return copy source url
     */
    public SVNUrl getCopyUrl();
}
