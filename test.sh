#!/bin/sh
echo "Hello World"

while IFS='' read -r line || [[ -n "$line" ]]; do
   for STEP in 20 40 60 80 100
do
	echo "RUNNING WITH STEP SIZE : " $STEP
	
	java -jar redecheck-jar-with-dependencies.jar --oracle $line/index --test $line/0 --step $STEP --start 400 --end 1300 --preamble file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/
done
done < "$1"

