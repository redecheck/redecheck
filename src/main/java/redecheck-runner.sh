#!/bin/bash

while IFS='' read -r line || [[ -n "$line" ]];
	do
		echo "$line"
		
		for COUNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
			do
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index --test $line/mutant$COUNT/mutant$COUNT --step 60 --start 400 --end 1300 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --classify
			done	    
done < "$1"