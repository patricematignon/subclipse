RSYNC_RSH=ssh

rsync --delete --exclude=.svn -avz . bender.loonsoft.com:/usr/local/www/data/loonsoft/updates/
