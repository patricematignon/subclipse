/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 
package org.tigris.subversion.svnclientadapter;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


public class StringUtils {

	/**
	 * we can't use String.split as it is a JDK 1.4 method.
	 * @param str
	 * @param separator
	 * @return
	 */
	static public String[] split(String str, char separator) {
		int pos = 0;
		List list = new LinkedList();
		int length = str.length();
		for (int i = 0; i < length;i++) {
			char ch = str.charAt(i);
			if (ch == separator) {
				list.add(str.substring(pos,i));
				pos = i+1;
			}
		}
		if (pos != length) {
			list.add(str.substring(pos,length));
		}
		return (String[])list.toArray(new String[0]);
	}
	// Splitting
	//--------------------------------------------------------------------------
    
	/**
	 * Splits the provided text into a list, using whitespace as the separator.
	 * The separator is not included in the returned String array.
	 *
	 * @param str  the string to parse
	 * @return an array of parsed Strings 
	 */
	public static String[] split(String str) {
		return split(str, null, -1);
	}

	/**
	 * @see #split(String, String, int)
	 */
	public static String[] split(String text, String separator) {
		return split(text, separator, -1);
	}

	/**
	 * Splits the provided text into a list, based on a given separator.
	 * The separator is not included in the returned String array.
	 * The maximum number of splits to perfom can be controlled.
	 * A null separator will cause parsing to be on whitespace.
	 *
	 * <p>This is useful for quickly splitting a string directly into
	 * an array of tokens, instead of an enumeration of tokens (as
	 * <code>StringTokenizer</code> does).
	 *
	 * @param str The string to parse.
	 * @param separator Characters used as the delimiters. If
	 * <code>null</code>, splits on whitespace.
	 * @param max The maximum number of elements to include in the
	 * list.  A zero or negative value implies no limit.
	 * @return an array of parsed Strings 
	 */
	public static String[] split(String str, String separator, int max) {
		StringTokenizer tok = null;
		if (separator == null) {
			// Null separator means we're using StringTokenizer's default
			// delimiter, which comprises all whitespace characters.
			tok = new StringTokenizer(str);
		} else {
			tok = new StringTokenizer(str, separator);
		}

		int listSize = tok.countTokens();
		if (max > 0 && listSize > max) {
			listSize = max;
		}

		String[] list = new String[listSize];
		int i = 0;
		while (tok.hasMoreTokens()) {
			if (max > 0 && i == listSize - 1) {
				// In the situation where we hit the max yet have
				// tokens left over in our input, the last list
				// element gets all remaining text.
				StringBuffer buf = new StringBuffer((int) 1.2 * str.length() * (listSize - i) / listSize);
				while (tok.hasMoreTokens()) {
					buf.append(tok.nextToken());
					if (tok.hasMoreTokens()) {
						buf.append(separator);
					}
				}
				list[i] = buf.toString();
				break;
			} else {
				list[i] = tok.nextToken();
			}
			i++;
		}
		return list;
	}

}
