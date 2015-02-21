#!/usr/bin/env bash

# Learners must learn every value that was proposed

cat $1 >  prop
cat $2 >> prop

sort prop > prop.sorted
sort $3 > $3.sorted
sort $4 > $4.sorted

diff prop.sorted $3.sorted > test2.out

if [[ -s test2.out ]]; then
	echo "test 2: Failed!"
	exit
fi

diff prop.sorted $4.sorted > test2.out
	
if [[ -s test2.out ]]; then
	echo "test 2: Failed!"
else
	echo "test 2: OK"
	rm test2.out prop $3.sorted $4.sorted
fi
