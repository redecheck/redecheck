#!/bin/bash

while IFS='' read -r line || [[ -n "$line" ]];
	do
		echo "$line"

        for COUNT in 1 2 3 4 5
            do
		        java -jar redecheck-jar-with-dependencies.jar --url $line --step 60 --start 400 --end 1400 --sampling uniformBP --binary --timing --timingID $COUNT --preamble /Users/thomaswalsh/Documents/PhD/fault-examples/ --browser firefox
		    done
done < "$1"