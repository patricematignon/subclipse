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
package org.tigris.subversion.svnclientadapter.javahl;

import java.util.Date;

import org.tigris.subversion.javahl.BlameCallback;
import org.tigris.subversion.svnclientadapter.Annotations;

/**
 * JavaHL specific subclass of {@link Annotations}.
 * It implements a {@link org.tigris.subversion.javahl.BlameCallback}
 * as means of constructing the annotation records.  
 * 
 */
public class JhlAnnotations extends Annotations implements BlameCallback {
	
    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.BlameCallback#singleLine(java.util.Date, long, java.lang.String, java.lang.String)
     */
    public void singleLine(Date changed, long revision, String author,
                           String line) {
    	addAnnotation(new Annotation(revision, author, changed, line));
    }
}
