#crontab -e
#0 13 * * 5 dbBackup.sh

#dbBackup.sh
DATE=`date +%Y-%m-%d`
FILE="dump-$DATE"
FOLDER="/root/Backups/DB"
PATH="$FOLDER/$FILE"
echo "Backing up to $PATH"
/usr/bin/mongodump --out=$PATH --gzip
#mongodump --out=$PATH --gzip
echo "Backup done in $PATH"
/bin/tar -cvf - $PATH | /bin/gzip > $PATH.tar.gz
#tar -czvf $PATH.tar.gz $PATH
echo "Backup zip in $PATH.tar.gz"
java -cp "/root/Software/apache-tomcat-8.0.30/webapps3/futbol7-0.0.1-SNAPSHOT/WEB-INF/*" com.ppot14.futbol7.GoogleImporter upload $PATH.tar.gz
echo "Uploaded zip in $PATH.tar.gz"