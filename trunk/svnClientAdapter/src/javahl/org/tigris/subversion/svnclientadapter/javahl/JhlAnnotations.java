/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
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
package org.tigris.subversion.svnclientadapter.javahl;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.callback.BlameCallback;
import org.tigris.subversion.svnclientadapter.Annotations;

/**
 * JavaHL specific subclass of {@link Annotations}.
 * It implements a {@link org.tigris.subversion.javahl.BlameCallback}
 * as means of constructing the annotation records.  
 * 
 */
public class JhlAnnotations extends Annotations implements BlameCallback {

	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	private void singleLine(Date changed, long revision, String author,
			Date merged_date, long merged_revision, String merged_author,
			String mergedPath, String line) {
		if (merged_revision == -1 || revision <= merged_revision)
			addAnnotation(new Annotation(revision, author, changed, line));
		else
			addAnnotation(new Annotation(merged_revision, merged_author, merged_date, line));
	}

	public void singleLine(long lineNum, long revision,
			Map<String, byte[]> revProps, long mergedRevision,
			Map<String, byte[]> mergedRevProps, String mergedPath, String line,
			boolean localChange) throws ClientException {

		String author = null;
		String mergedAuthor = null;
		try {
			author = new String(revProps.get("svn:author"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			author = new String(revProps.get("svn:author"));
		}
		if (mergedRevProps != null) {
			try {
				mergedAuthor = new String(mergedRevProps.get("svn:author"), "UTF8");
			} catch (UnsupportedEncodingException e) {
				mergedAuthor = new String(mergedRevProps.get("svn:author"));
			}
		}
		try {
            singleLine(
                df.parse(new String(revProps.get("svn:date"))),
                revision,
                author,
                mergedRevProps == null ? null
                    : df.parse(new String(mergedRevProps.get("svn:date"))),
                mergedRevision,
                mergedAuthor,
                mergedPath, line);
        } catch (ParseException e) {
            throw ClientException.fromException(e);
        }
	}
	
}
