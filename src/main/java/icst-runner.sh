#!/bin/bash

while IFS='' read -r line || [[ -n "$line" ]];
	do
		echo "$line"

		java -jar redecheck-jar-with-dependencies.jar --url $line --step 60 --start 400 --end 1400 --sampling uniformBP --binary --preamble /Users/thomaswalsh/Documents/PhD/fault-examples/ --browser firefox
done < "$1"