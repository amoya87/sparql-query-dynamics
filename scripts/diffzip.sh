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
        java -jar diffSortedRDFGraph.jar -l "${last}" -r "${db}" -o1 zdiff${lasti}-${dbi}-minus.nt.gz -o2 zdiff${lasti}-${dbi}-plus.nt.gz -igz -ogz
    else
        java -jar diffSortedRDFGraph.jar -l "${last}" -r "${db}" -o1 zdiff${lasti}-${dbi}-minus.nt.gz -o2 zdiff${lasti}-${dbi}-plus.nt.gz -igz -ogz &
    fi
    last=$db
    ((i++))
done
