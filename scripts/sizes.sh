#!/bin/bash
dbs=$(ls ${1})
for db in ${dbs}; do
        echo "${db}"
        date
        java -jar -Xmx30G ../stats.jar -l "${db}" -igz -o1 "${db}.txt"
	date
done
