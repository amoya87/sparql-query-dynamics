#!/bin/bash
dbs=$(ls ${1})
h=${2}
i=0
j=1
last="" 
for db in ${dbs}; do
    if [ $i -eq 0 ]; then
        last=$db
        ((i++))
        continue
    fi
    lasti=$(echo $last | grep -o -E '[0-9]+')
    dbi=$(echo $db | grep -o -E '[0-9]+')
#    printf  "${last}-${db},"
    if [ $((i%h)) -eq 0 ]; then
        java -jar pdyn.jar -l "${1}/${last}" -r "${1}/${db}" -o dyn${lasti}-${dbi}.txt -igz
    else
        java -jar pdyn.jar -l "${1}/${last}" -r "${1}/${db}" -o dyn${lasti}-${dbi}.txt -igz &
    fi
    last=$db
    ((i++))
done
