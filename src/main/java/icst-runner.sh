#!/bin/bash

while IFS='' read -r line || [[ -n "$line" ]];
	do
		echo "$line"

		java -jar ../../../target/redecheck-jar-with-dependencies.jar --url $line  --preamble /Users/thomaswalsh/Documents/PhD/fault-examples/
done < "$1"