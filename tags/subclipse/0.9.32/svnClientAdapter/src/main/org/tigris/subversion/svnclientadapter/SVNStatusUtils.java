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

/**
 * Some static methods to deal with svn status
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class SVNStatusUtils {
    
    public static boolean isManaged(SVNStatusKind textStatus) {
        return ((!textStatus.equals(SVNStatusKind.UNVERSIONED))
                && (!textStatus.equals(SVNStatusKind.NONE)) && (!textStatus
                .equals(SVNStatusKind.IGNORED)));
    }
    
    /**
     * Returns if is managed by svn (added, normal, modified ...)
     * 
     * @return if managed by svn
     */    
    public static boolean isManaged(ISVNStatus status) {
        return isManaged(status.getTextStatus());
    }

    public static boolean hasRemote(SVNStatusKind textStatus) {
        return ((isManaged(textStatus)) && (!textStatus.equals(SVNStatusKind.ADDED)));
    }
    
    /**
     * Returns if the resource has a remote counter-part
     * 
     * @return has version in repository
     */
    public static boolean hasRemote(ISVNStatus status) {
        return hasRemote(status.getTextStatus());
    }    

    public static boolean isAdded(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.ADDED);
    }

    public static boolean isDeleted(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.DELETED);
    }

    public static boolean isIgnored(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.IGNORED);
    }

    public static boolean isTextMerged(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.MERGED);
    }

    public static boolean isTextModified(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.MODIFIED);
    }

    public static boolean isTextConflicted(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.CONFLICTED);
    }

    public static boolean isPropModified(ISVNStatus status) {
        return status.getPropStatus().equals(SVNStatusKind.MODIFIED);
    }

    public static boolean isPropConflicted(ISVNStatus status) {
        return status.getPropStatus().equals(SVNStatusKind.CONFLICTED);
    }    
    
}