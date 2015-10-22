#!/bin/sh
echo "Hello World"

while IFS='' read -r line || [[ -n "$line" ]]; do
   for STEP in 60
do
	echo $line " RUNNING WITH STEP SIZE : " $STEP
	
	java -jar redecheck-jar-with-dependencies.jar --oracle $line/index --test $line/0 --step $STEP --start 400 --end 1300 --preamble file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/
done
done < "$1"

