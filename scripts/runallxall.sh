#!/bin/bash
    dbs=`ls ${1}`
    echo "dbs: $dbs"
for db in ${dbs}; do
    echo "iteracion: $db"
    cd /usr/local/var/lib/virtuoso/db/
    rm virtuoso.db
    ln -s /home/amoya/virtuoso-dbs/${db} virtuoso.db
    cp virtuoso.ini backup.virtuoso.ini ; rm virtuoso* ; mv backup.virtuoso.ini virtuoso.ini
    virtuoso-t &
    echo "esperando"
    sleep 500
    echo "fin espera"
    cd ~/
    cp -r queries queries${db}
    ~/mysort.sh queries${db}/ >> runout${db}.txt
    rm queries${db}/*.rq
    sh scripts/load/virtuoso-run-script.sh scripts/load/shutdown.sql
    sleep 200
done
