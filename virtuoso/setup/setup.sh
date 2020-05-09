sudo apt-get install wget
wget https://github.com/openlink/virtuoso-opensource/releases/download/v7.2.5.1/virtuoso-opensource-7.2.5.tar.gz
sudo apt-get install dpkg-dev build-essential
sudo apt-get install autoconf automake libtool flex bison gperf gawk m4 make odbcinst libxml2-dev libssl-dev libreadline-dev libssl1.0-dev
tar xvpfz virtuoso-opensource-7.2.5.tar.gz
cd virtuoso-opensource-7.2.5/
./autogen.sh
./configure --prefix=/usr/local/ --with-readline --program-transform-name="s/isql/isql-v/"
nice make
sudo make install
cd /usr/local/var/lib/virtuoso/db/
sudo chown -R $(whoami) .
virtuoso-t &