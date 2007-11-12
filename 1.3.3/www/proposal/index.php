<?php  																														require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/app.class.php");	require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/nav.class.php"); 	require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/menu.class.php"); 	$App 	= new App();	$Nav	= new Nav();	$Menu 	= new Menu();		include($App->getProjectCommon());    # All on the same line to unclutter the user's desktop'

$pageTitle 		= "";
$pageKeywords	= "";
$pageAuthor		= "";

ob_start();
?>
    <div id="maincontent">
	<div id="midcolumn">

<h1>SVN Team Provider</h1>
</p>
<?php
include_once($_SERVER['DOCUMENT_ROOT'] . "/projects/fragments/proposal-page-header.php");
generate_header("svn");
?>

<h2>Introduction</h2>

<p>The SVN Team Provider Project is a proposed open source project 
under the <a href="http://www.eclipse.org/technology/">Eclipse Technology
Project</a>.</p>

<p>This proposal is in the Project Proposal Phase (as defined in the
<a href="http://www.eclipse.org/projects/dev_process/"> Eclipse Development
Process document</a>) and is written to declare its intent and scope. This 
proposal is written to solicit additional participation and  input from the 
Eclipse community. You are invited to comment on and/or join the project. Please 
send all feedback to the <a 
href="news://news.eclipse.org/eclipse.technology.svn">
news://news.eclipse.org/eclipse.technology.svn</a> newsgroup.</p>

<h2>Background</h2>

<p>Subversion (SVN) is very popular and rivals CVS as the most 
widely used open source version control system. Many hosting services are 
providing SVN access, and several major open source projects have converted
their CVS repositories to SVN. Eclipse has great support for CVS, and users
are looking for similar support for SVN.  Eclipse has no support for SVN out
of the box and many new users are unaware that quality third-party SVN
team providers are available.  This perceived lack of SVN support makes
Eclipse look inferior to other IDEs such as Netbeans and IntelliJ that ship with
SVN support in their base installations.  The Bugzilla entry to add SVN
support to Eclipse, <a 
href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=37154">#37154</a>, has over
100 votes and has been in the top-5 for most votes for a very long time.</p>

<p>In addition to adding SVN support to Eclipse, there is the issue of providing
a solid Team API that can be leveraged by commercial and open source SCM
providers.  The current Team API is very capable, but at the same time it is 
also hindered by having only one concrete implementation.  In many instances
the API has become very CVS-centric.  Having the platform support a second team 
provider would provide further validation of Team APIs before they are released
and consumed by other team providers.</p>

<p>Finally, there is the issue of providing support for SVN to projects hosted
by Eclipse.org.  The code for this project should be self-hosted in an SVN 
repository and it is likely that there are other projects currently hosted at
Eclipse.org that would like to change their repositories from CVS to SVN.  For
reference purposes, there are existing Bugzilla entries to add support for 
hosting projects in Subversion repositories (see 
<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=71735">#71735</a> and <a
href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=131096">#131096</a>).
Part of this proposal is to provide resources and expertise that will support
the Eclipse Foundation in their efforts to provide these services to the
community.  There are many individuals involved in the Subclipse community, and
the wider Subversion ecosystem, who would be willing to volunteer time
towards assisting the Eclipse organization on setting up its own
Subversion repository, as a complement to its existing CVS repository.
There are also commercial entities in the Subversion ecosystem, and some
Eclipse members, who could offer commercial assistance on this regard.</p>

<h2>History</h2>

<p>One of the main problems with CVS that Subversion sought to address was
the lack of an official API.  Subversion was designed as a set of layered
libraries with well-defined interfaces, designed to be called by
other applications.  These libraries are delivered as native C libraries in
order to achieve the maximum portability across both operating systems and
programming languages, as most languages have a facility for accessing C
libraries.  JavaHL is the name for the official Java language binding to these
libraries.  JavaHL is similar to SWT in that it accesses the native libraries
via JNI while providing a standardized interface to Java clients.  Since
virtually all SVN clients use these same libraries, there is great 
compatibility when using multiple clients on the same local working copy.  The
Subversion API also has very strictly defined compatibility rules and the
Subversion client and server are both forward and backward compatible with
each other.</p>

<p>The Subclipse project has figured out how to deliver the native
libraries for JavaHL for the Windows platform only.  Currently, an Eclipse user
running on Linux has to install Subversion using the packaging system for their
distribution.  This then installs all of the required native libraries, 
including the JavaHL library.  There are similar facilities available for OS X
users.  We would be interested in working with the Eclipse platform project to
see if there are changes that could be made to Eclipse to enable native 
libraries to be delivered by a plug-in fragment when those libraries have 
dependencies on other libraries.  In the short term, users on non-Windows
platforms would have to obtain the JavaHL native library for their operating
system.  All of the major Linux distributions now make this very easy to do
and really it is not much different than how a Linux user that uses KDE has to
make sure they have the GTK2 libraries installed before they can use SWT and
Eclipse.</p>

<p>There is also a third-party library named JavaSVN that has been developed
by a company named TMate.  <a href="http://tmate.org/svn/">
http://tmate.org/svn/</a>.  They have reverse-engineered the SVN network
protocols and working copy format and have produced a 100% pure Java
implementation of the JavaHL interface; making it a drop in replacement for
the native library version of JavaHL.  The Subclipse distribution currently
provides an adapter option for this library.  The JavaSVN library <a
href="http://tmate.org/svn/licensing/index.html">license</a> is not compatible
with the EPL, so we will not provide this library or its adapter as part
of the project.  We will work with TMate to assist them in providing a
plug-in that is downloadable via their web site that adds JavaSVN as an adapter
option via our public extension point.  This has always been the option that
TMate has expressed as their preference, and they have asked to be listed
as both a supporter and interested party of this project.  External packagers
of Eclipse plug-ins, such as Linux distributions (Fedora, Debian) and/or <a 
href="http://www.yoxos.com/">Yoxos</a> would likely be able to include JavaSVN
in their distributions as the JavaSVN license would be compatible with what 
they are doing.</p>

<p>In the process of putting together this proposal, as well as in prior
related discussions, we have received a lot of feedback about the issue of
using native libraries (JavaHL).  It is our position that using the native
libraries provided by Subversion is the right approach to take.  The existing
CVS plug-in took the approach of creating a custom pure Java library.  However,
it needs to be taken into account that:</p>

<ol>
<li>There really was/is no official CVS "API" that could have been used.</li>
<li>CVS development was already largely dormant before Eclipse development
even started.</li>
</ol>

<p>Despite this second point, even to this day there is still the occasional
incompatibility between the Eclipse CVS client and other CVS clients or
servers.  Subversion, in contrast to CVS, has an official API that was
designed to be used by multiple languages, including Java, and more importantly
Subversion is actively developed and frequently sees major new features being
added.  It would be a mistake to think that all of the work that has gone into
Subversion, and continues to go into it to this day could be easily replicated
and managed.  It would be better for Eclipse, as a platform for application
development, to put this effort into making it easier to use native libraries
in Eclipse plug-ins and applications.  Surely any problems in this area are
solvable, and Eclipse would just be that much better if it did solve those
problems.</p>

<h2>Scope</h2>
<p>The objectives of the SVN Team Provider project are to: </p>
<ul>
	<li>Provide support for accessing and manipulating projects that are hosted
	in a Subversion repository.</li>
	<li>Provide UI and features that are familiar to existing Eclipse CVS users,
	while at the same time providing an appropriate user interface for accessing
	Subversion specific features.</li>
	<li>Provide a simple API for accessing Subversion functions, as well as
	support headless operations of Team provider functions.  These items will
	make it easier for other plug-ins to add Subversion functionality as 
	needed.</li>
	<li>Provide accurate and up to date documentation on using the features of
	the SVN provider.</li>
	<li>Support, implement and follow the latest best practices for Eclipse UI
	development	as appropriate for the current Eclipse release.</li>
    <li>Contribute to the development and refinement of the Eclipse Core Team
    API.</li>
    <li>Provide real world use cases for development of the official JavaHL SVN
    client interface.</li>
	<li>Assist resources at the Eclipse Foundation in offering SVN hosting to
	projects.</li>
	<li>Assist the release engineering team to add SVN support to their tools
	and process.</li>
</ul>
	
<h2>Description</h2>

<p>The initial code for this project will be based on a code contribution from
the <a href="http://subclipse.tigris.org/">Subclipse</a> project.  Subclipse was
recently re-licensed under the terms of the EPL and provides a robust and stable
implementation of Subversion functionality as an Eclipse Team Provider.  In
addition, this project will adhere to common Eclipse development best practices,
most of which are already in place in the Subclipse code base.</p>

<ul>
<li>Separation into core and ui plug-ins.</li>
<li>JUnit test suite.</li>
<li>Internal packages for non-public API.</li>
<li>Export and use of extension points.</li>
<li>Separate help plugin for documentation as well as use of context
sensitive help.</li>
<li>Localization support.  The Subclipse community has contributed localizations
for Japanese (ja), Chinese (zh), and Traditional Chinese (zh_TW).</li>
</ul>

<p>The Subclipse code and architecture is based on the Eclipse CVS provider, 
so it should be easy for the existing Team and CVS developers to understand,
should they decide to contribute.</p>

<p>
<img src="architecture.png" />
</p>

<p>The foundation for this project is a common Subversion API layer called the
SVN Client Adapter (<b>org.eclipse.subversion.client</b>).  This adapter gives
clients full access to any SVN repository (even those not under direct control
of the SVN Team Provider).  The adapter serves to create an abstraction on top
of all client adapter implementations (JavaHL, JavaSVN and the SVN command line
client).  This adapter defines a standard Eclipse extension point that 
provider implementations can contribute to in order to provide an 
implementation.  The user can then select which provider implementation they
wish to use in their client preferences.</p>

<p>The default SVN Client Adapter will be JavaHL, which is the official Java
language binding for the Subversion native libraries.  JavaHL is developed,
maintained and provided as part of the Subversion project and is licensed under
the <a href="http://subversion.tigris.org/project_license.html">Subversion
license.</a></p>

<p>The core plugin (<b>org.eclipse.team.svn.core</b>) is the Eclipse Team 
provider implementation for Subversion.  It provides access to the SVN
instances that are managed by the current workspace.  The core plugin allows
the team provider to function in a headless environment.  This headless 
environment is already being used by the 
<a href="http://www.eclipse.org/buckminster/">Buckminster</a> project.</p>

<p>Finally, the UI plugin (<b>org.eclipse.team.svn.ui</b>) uses the core
team support to provide a friendly interface to the user.  The existing
Subclipse UI plugin already implements many of the advanced UI elements that
have been added to the CVS UI plugin such as: Quick Diff, Live Annotations,
Common History View, Synchronize View including Change Sets and Console view.
This is in addition to the more basic use of the Team, Compare and Replace
menus.  This similarity in the UI makes it easier for existing CVS users to
make the transition to SVN, and also smoothes the path by providing the same
nice extras that users have become accustomed to.  We would like to explore
ways to further integrate the UI of these plugins, such as providing a common
Repository view and perspective that could be used to house CVS, SVN and any
other repositories in one location.  There is also the possibility to build
common dialogs for operations like Commit.  Finally, Subclipse has also gone
to great lengths to make the handling of Branches and Tags as familiar as
possible to CVS users.  See: <a 
href="http://subclipse.tigris.org/branch_tag.html">
http://subclipse.tigris.org/branch_tag.html</a>.  Subversion and CVS handle
branches and tags very differently and the work that has been done by
Subclipse in this area has been looked at by the Subversion development
community as a possible way to make the handling of tags in Subversion
friendlier to CVS users.</p>

<h2>Organization</h2>

<p>The SVN Team provider project will be organized as follows:</p>
	
<p><b>Initial committers</b></p>
<p>The initial committers will focus on refactoring the existing Subclipse code
base to meet the initial objectives of this project, as well as aligning the
project with changes in the Platform and Team API.  The initial committers
are (in alphabetical order):</p>

<ul>
	<li>Daniel Bradby</li>
	<li>Cédric Chabanois</li>
	<li><a href="http://www.collab.net/">CollabNet</a>: Considering providing
	development resources</li>
	<li>Stephen Elsemore (<a href="http://www.softlanding.com/">SoftLanding
	Systems</a>)</li>
	<li>Brock Janiczak</li>
	<li>Alexander Kitaev (<a href="http://tmate.org/svn/">TMate</a>)</li>
	<li>Eugene Kuleshov</li>
	<li>Martin Letenay</li>
	<li>Mark Phippard (<a href="http://www.softlanding.com/">SoftLanding 
	Systems</a>):  Project Lead</li>
	<li>Jesper Steen Møller</li>
	<li>Paul Thiel (<a href="http://www.softlanding.com/">SoftLanding
	Systems</a>): Documentation</li>
</ul>
	
<p><b>Subversion committers</b></p>
<p>Unlike CVS, Subversion is an actively developed open source project.  It
is important to participate in and coordinate with the Subversion project to
help insure that the needs of Eclipse users are taken into consideration as 
future enhancements are planned and delivered.  This proposal includes the
following members that have committer access to the Subversion project:</p>

<ul>
	<li>Paul Burba (<a href="http://www.softlanding.com/">SoftLanding
	Systems</a>): Subversion Full Committer</li>
	<li>Mark Phippard (<a href="http://www.softlanding.com/">SoftLanding 
	Systems</a>):  Subversion JavaHL Committer</li>
	<li>Daniel Rall (<a href="http://www.collab.net/">CollabNet
	</a>): Subversion Full Committer</li>
</ul>

<p><b>Interested parties</b></p>
<p>The following Eclipse projects have previously worked with the Subclipse
project and have expressed an interest in this proposal.</p>
<ul>
	<li><a href="http://www.eclipse.org/mylar/">Mylar</a></li>
	<li><a href="http://www.eclipse.org/buckminster/">Buckminster</a></li>
	<li><a href="http://www.eclipse.org/alf/">ALF</a></li>
</ul>

<p>The following individuals and their organizations have expressed their
interest in and support for this proposal.</p>
<ul>
	<li>Karl Fogel (<a href="http://www.google.com/">Google</a>)</li>
	<li>Ben Konrath (<a href="http://www.redhat.com/">Red Hat</a>)</li>
	<li>Andrew Overholt (<a href="http://www.redhat.com/">Red Hat</a>)</li>
</ul>

<p><b>Developer community</b></p>
<p>The Subclipse team has a proven track record of operating in an open and
transparent manner.  Even this proposal has been developed collaboratively
by the community using the mailing lists and repository to work as a team.
We also have a proven track record of being able to attract and
support contributions from the developer community.  The Subclipse 1.0 release
listed thirty-seven individuals that made code contributions to the project.  
We expect to continue this tradition and likely expand it greatly as the 
visibility of the project in the Eclipse community is likely to increase as a 
result of this process.</p>

<p>The Subclipse team has also made several contributions to Platform Team, 
Mylar and ALF with the aim of producing a better Team Platform for everyone.  
Examples include:</p>
<ul>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=89806">
[API] Add a 'Team' quick diff that delegates to team provider's implementation
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=138583">
Common Team Repository browser view
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=123867">
[History View] Allow GenericHistoryView to show history of non IResources
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=143354">
Unable to Apply Patch that contains new file
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=130571">
[Contributions] 3.2M5A Problem Associating Team Provider with Project
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=143954">
Provide better support for Subversion patch file format
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=109166">
Provide a hook to capture copy event
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=144130">
GenericHistory view does not support drag-n-drop folders
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=149712">
Provide better support for using native libraries via JNI
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=142875">
Support SVN active changesets
	</a></li>
	<li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=148512">
Commit support for team providers
	</a></li>
</ul>

<p><b>User community</b></p>
<p>As with the Developer community, Subclipse has a proven track record of 
having a vibrant user community that has always provided valuable assistance to
the development process as well as assistance in supporting one another in a 
respectful and professional manner.  The Subversion project itself for that 
matter has set a high standard of excellence in this area, which other projects
have followed.</p>

<p><b>Subversive Proposal</b></p>
<p>In March 2006 a second SVN plug-in for Eclipse emerged in the form of
a product named Subversive from a company named <a 
href="http://www.polarion.org/">Polarion</a>.  This company has also submitted
a proposal to the Eclipse Foundation.  See: <a 
href="http://www.eclipse.org/proposals/subversive/">
http://www.eclipse.org/proposals/subversive/</a>.</p>

<p>We believe that our proposal represents the best option for the Eclipse
community.  The Subclipse project has a 3-year history of operating 
transparently and is well past the "1.0" stage of the development process.  We
even have an Eclipse 3.2-specific release available for Callisto users --  a
release that leverages several Eclipse 3.2 features.  This proposal has the 
backing of two long time Eclipse Foundation members, <a 
href="http://www.softlanding.com/">SoftLanding Systems</a> and <a 
href="http://www.collab.net/">CollabNet</a>, both of whom have a proven track
record of supporting open source development in general, and Subversion in 
particular.  We have worked with and have the support of several
existing Eclipse projects and just as importantly, we have the backing
of the Subversion developer community.  Even the primary developer of the
JavaSVN library, Alexander Kitaev, has asked to be listed as an interested
party and potential contributor to our proposal.</p>

<p>We strongly believe that any Eclipse.org provided support for Subversion 
should work closely with the Subversion development community.  Any proposal
that is not based on the official Subversion libraries does not do this.</p>

<p>We invite Polarion to join this proposal and devote their resources to
helping us create a single great option that satisfies the needs of the Eclipse
and Subversion communities.  They have implemented some good ideas in
Subversive, and we would encourage them to work with our community to get
these features incorporated into our code base, and where necessary, into
the Subversion libraries themselves.</p>

<h2>Tentative Plan</h2>
	
<ul>
	<li>September 2006: Refactored and re-licensed code contribution based on
	Subclipse</li>
	<li>December 2006: Initial release as an Eclipse project, supporting
	Eclipse 3.2.</li>
	<li>January 2007: Join the release train for Eclipse 3.3</li>
	<li>July 2007: Possibly move project to Eclipse Platform</li>
</ul>

      </div>
  </div>
<?php
	$html = ob_get_contents();
	ob_end_clean();

	# Generate the web page
	$App->generatePage($theme, $Menu, $Nav, $pageAuthor, $pageKeywords, $pageTitle, $html);
?>
