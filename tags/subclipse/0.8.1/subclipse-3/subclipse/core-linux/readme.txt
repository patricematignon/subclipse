Greetings,

This library was linked against libraries present a debian Linux 2.4 install and svn 0.3.1 (which itself linked against berkeley db4.1 ). YMMV.  Below you will
find the output of ldd on my system. You will need all of these libraries present on your system
to use this library.  The ones you probably don't have by default are asterisked below.

The libsvnjavahl.so depends on the following libraries. Check your ldconfig output if svn complains about 
not being able to find certain symbols.  ldd might come in handy for you too.

-m@loonsoft.com

Libraries to be aware of:
	*libneon.so.24  //must be 24, 23 won't do.
	libxml2.so.2 
	libz.so.1 
	libpthread.so.0
	*libaprutil-0.so.0 //stuff from apache portable runtime
	*libapr-0.so.0 //apache portable runtime 
	libexpat.so.1 
	librt.so.1 
	libcrypt.so.1
	libnsl.so.1 
	libdl.so.2 
	*libdb-4.1.so //berkeley db 4.1 
	/** svn libraries, these should be present if you have installed subversion **/
	libsvn_client-1.so.0 
	libsvn_delta-1.so.0 
	libsvn_fs-1.so.0 
	libsvn_ra-1.so.0
	libsvn_ra_dav-1.so.0 
	libsvn_ra_local-1.so.0 
	libsvn_ra_svn-1.so.0 
	libsvn_repos-1.so.0 
	libsvn_subr-1.so.0 
	libsvn_wc-1.so.0 
	libsvn_diff-1.so.0 

	libstdc++.so.5 /* the rest of these are likely to exist on any standard system */
	libm.so.6
	libc.so.6 
	libgcc_s.so.1 
	libssl.so.0.9.7 
	libcrypto.so.0.9.7 
	ld-linux.so.2
	libldap.so.2 
	liblber.so.2 
	libresolv.so.2 
	libsasl2.so.2 
	libgnutls.so.7 
	libtasn1.so.0 
	libgcrypt.so.1
