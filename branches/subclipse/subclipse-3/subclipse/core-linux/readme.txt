Greetings,

This library was linked against libraries present a Debian Linux 2.4.22
install and svn 0.3.2.1 (which itself linked against berkeley db4.1
). YMMV.  

The libsvnjavahl.so dependencies are many and varied. Use ldd to see what I
linked against, and make sure these libraries are present on your system
(ldconfig -p). 

-m@loonsoft.com

