#!/bin/bash
#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

# Arg 1 - cluster to use
# Arg 2 - start at host number n inc.
# Arg 3 - stop at host number n inc.
# Arg 4 - script file to deploy

## now loop through the above array
for n in `seq $2 $3`; do
	
 	echo "Deploying to $1-$n.cluster - $4"
	ssh $1-$n.cluster "sh $4" &

done 

