#!/bin/bash
for filename in ${1}/*; do
    echo "contando ${filename}" 
    start=`date +%s`
    #LANG=C sort -S 24G --parallel=12 -T ~/ --compress-program=gzip ${filename} | gzip > ${filename}.sorted.gz
    zcat ${filename} | uniq | wc -l
    #~/scripts/load/run_query.sh ${filename}
    end=`date +%s`
    runtime=$((end-start))
    echo "${filename} ${runtime}"
done
