#
# Copyright 2020 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#

cd population-linkage
sh src/main/scripts/experiments/synthetic/run_linkage_job_q_instance.sh 12 "12 cluster-runs/jobQ/job-q.csv cluster-runs/record-counts.csv cluster-runs/status.txt cluster-runs/gt-link-counts.csv" > runs/job-run-`hostname`.txt