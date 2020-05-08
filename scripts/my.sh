#!/bin/bash
diff=$(diff file1.txt file2.txt)
v=0
	if echo "$diff" | grep -q ">"; then
            ((v++))
        fi
        if echo "$diff" | grep -q "<"; then
            v=$((v|2))
        fi
printf "$v,"
