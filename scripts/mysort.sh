#!/bin/bash
# param1 folder
# param2 bufferSize k/M/G
# param3 parallelism
# param4 destfolder
for filename in ${1}/*; do
    echo "ordenando ${filename}"
    start=`date +%s`
    gzip -dc ${filename} | LANG=C sort -u -S ${2} --parallel=${3} -T ~/ --compress-program=gzip | gzip > ${filename}.uniqSorted.gz
    end=`date +%s`
    runtime=$((end-start))
    echo "${filename} ${runtime}"
    mv ${filename}.uniqSorted.gz ${4}
done