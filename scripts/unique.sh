#!/bin/bash
start=`date +%s`
zcat ${1} | uniq | gzip > ${1}.uniq.gz
end=`date +%s`
runtime=$((end-start))
echo "time: ${runtime}"
