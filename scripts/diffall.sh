#!/bin/bash
dbs=$(ls ${1})
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
    qs=$(ls ${1}/${db})    
    for q in ${qs}; do
        qi=$(echo $q | grep -o -E '[0-9]+')
        diff=$(diff "${1}/${last}/${q}" "${1}/${db}/${q}")
	v=0
        if echo "$diff" | grep -q ">"; then
            ((v++))
        fi
	if echo "$diff" | grep -q "<"; then
            v=$((v|2))
        fi
	printf "$v,"
        ((j++))
    done
    printf "\n"
    last=$db
    ((i++))
done
