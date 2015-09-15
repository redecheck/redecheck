#!/bin/sh

while IFS='' read -r line || [[ -n "$line" ]]; do
    for VARIABLE in 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19
do
	echo "RUNNING MUTANT " $VARIABLE
	java -jar redecheck-jar-with-dependencies.jar --oracle $line/index --test $line/$VARIABLE --step 60 --start 400 --end 1300 --preamble file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/
done
done < "$1"

