#!/usr/bin/env bash

# Learners must learn the same set of values in total order

diff $1 $2 > test1.out

if [[ -s test1.out ]]; then
	echo "test 1: Failed!"
else
	echo "test 1: OK"
	rm test1.out
fi
