#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#


cd population-linkage
sh src/main/scripts/experiments/synthetic/run_linkage_job_q_instance.sh 12 "linkage-job-q/linkage-jobQ.csv linkage-job-q/linkage-results.csv linkage-job-q/record-counts.csv linkage-job-q/gt-counts.csv linkage-job-q/status.txt" > runs/job-run-`hostname`.txt