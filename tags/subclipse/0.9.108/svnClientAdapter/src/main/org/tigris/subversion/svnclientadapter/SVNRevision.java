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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to specify a revision in a svn command.
 * This class has been copied directly from javahl and renamed to SVNRevision
 * the static method getRevision has then been added to the class
 *
 */
public class SVNRevision 
{
	// See chapter 3 section 3.3 of the SVN book for valid date strings 
	protected static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
    protected int revKind;

    public SVNRevision(int kind)
    {
        revKind = kind;
    }

    public int getKind()
    {
        return revKind;
    }

    public String toString()
    {
        switch(revKind) {
            case Kind.unspecified : return "START";
            case Kind.base : return "BASE";
            case Kind.committed : return "COMMITTED";
            case Kind.head : return "HEAD";
            case Kind.previous : return "PREV";
            case Kind.working : return "WORKING";
        }
        return super.toString();
    }

    public boolean equals(Object target) {
        if (this == target)
            return true;
        if (!(target instanceof SVNRevision))
            return false;

        return ((SVNRevision)target).revKind == revKind;        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
    	return revKind;
    }

    public static final SVNRevision HEAD = new SVNRevision(Kind.head);
    public static final SVNRevision START = new SVNRevision(Kind.unspecified);
    public static final SVNRevision COMMITTED = new SVNRevision(Kind.committed);
    public static final SVNRevision PREVIOUS = new SVNRevision(Kind.previous);
    public static final SVNRevision BASE = new SVNRevision(Kind.base);
    public static final SVNRevision WORKING = new SVNRevision(Kind.working);
    public static final int SVN_INVALID_REVNUM = -1;    
    public static final SVNRevision.Number INVALID_REVISION = new SVNRevision.Number(SVN_INVALID_REVNUM);


    public static class Number extends SVNRevision
    {
        protected long revNumber;

        public Number(long number)
        {
            super(Kind.number);
            revNumber = number;
        }

        public long getNumber()
        {
            return revNumber;
        }

        public String toString() {
            return Long.toString(revNumber);
        }
        
        public boolean equals(Object target) {
            if (!super.equals(target))
                return false;

            return ((SVNRevision.Number)target).revNumber == revNumber;        
        }
        
        public int hashCode()
        {
        	return (int) revNumber;
        }
    }

    public static class DateSpec extends SVNRevision
    {
        protected Date revDate;
        public DateSpec(Date date)
        {
            super(Kind.date);
            revDate = date;
        }
        
        public Date getDate()
        {
            return revDate;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return '{' + dateFormat.format(revDate)+ '}';
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object target) {
            if (!super.equals(target))
                return false;

            return ((SVNRevision.DateSpec)target).revDate.equals(revDate);        
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
        	return revDate.hashCode();
        }
        
    }

    /** Various ways of specifying revisions.
     *
     * Various ways of specifying revisions.
     *
     * Note:
     * In contexts where local mods are relevant, the `working' kind
     * refers to the uncommitted "working" revision, which may be modified
     * with respect to its base revision.  In other contexts, `working'
     * should behave the same as `committed' or `current'.
     */
    public static final class Kind
    {
        /** No revision information given. */
        public static final int unspecified = 0;

        /** revision given as number */
        public static final int number = 1;

        /** revision given as date */
        public static final int date = 2;

        /** rev of most recent change */
        public static final int committed = 3;

        /** (rev of most recent change) - 1 */
        public static final int previous = 4;

        /** .svn/entries current revision */
        public static final int base = 5;

        /** current, plus local mods */
        public static final int working = 6;

        /** repository youngest */
        public static final int head = 7;

    }
    
    /**
     * get a revision from a string
     * revision can be :
     * - a date with the following format : MM/DD/YYYY HH:MM AM_PM
     * - a revision number
     * - HEAD, BASE, COMMITED or PREV
     * 
     * @param revision
     * @return Revision
     */
    public static SVNRevision getRevision(String revision) throws ParseException {

    	if ((revision == null) || (revision.equals("")))
    		return null;
    	
        // try special KEYWORDS
        if (revision.compareToIgnoreCase("HEAD") == 0)
            return SVNRevision.HEAD; // latest in repository
        else
        if (revision.compareToIgnoreCase("BASE") == 0)
            return new SVNRevision(SVNRevision.Kind.base); // base revision of item's working copy
        else
        if (revision.compareToIgnoreCase("COMMITED") == 0)
            return new SVNRevision(SVNRevision.Kind.committed); // revision of item's last commit
        else
        if (revision.compareToIgnoreCase("PREV") == 0) // revision before item's last commit
            return new SVNRevision(SVNRevision.Kind.previous);
        
        // try revision number
        try
        {
            int revisionNumber = Integer.parseInt(revision);
            if (revisionNumber >= 0)
                return new SVNRevision.Number(revisionNumber); 
        } catch (NumberFormatException e)
        {
        }
        
        // try date
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
        try
        {
            Date revisionDate = df.parse(revision);
            return new SVNRevision.DateSpec(revisionDate);
        } catch (ParseException e)
        {
        }
        
        throw new ParseException("Invalid revision. Revision should be a number, a date in MM/DD/YYYY HH:MM AM_PM format or HEAD, BASE, COMMITED or PREV",0);
    }    
    
}
