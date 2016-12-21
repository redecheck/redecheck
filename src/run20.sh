while IFS='' read -r line || [[ -n "$line" ]];
	do
		echo "$line"

		for COUNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
			do
			    ECHO $COUNT
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 10 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 20 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 40 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 60 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 80 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary
			  java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 100 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP --binary

				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 10 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 20 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 40 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 60 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 80 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 100 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform --binary

				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 10 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 20 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 40 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 60 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP
				java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 80 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP
			  java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 100 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniformBP

  			java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 10 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 20 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 40 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 60 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 80 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
        java -jar redecheck-jar-with-dependencies.jar --oracle $line/index/index --test $line/mutant$COUNT/mutant$COUNT --step 100 --start 400 --end 1400 --preamble /Users/thomaswalsh/Documents/Workspace/redecheck/testing/ --tool --sampling uniform
			done
done < "$1"
