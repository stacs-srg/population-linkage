## CS5199 Project Documentation

### How to run threshold analysis
1. Prior to starting the analysis, run all of the builders as some analysers depend on other linkages to count FNOTs
2. Run . src/main/java/uk/ac/standrews/cs/population_linkage/aleks/analysers/run_threshold_analysis.sh (May take hours as the threshold will be automatically maximised)
3. Multiple CSV files will be created which should contain all the quality and open triangle measurements
4. optimal_threshold.py can then be used to generate the graphs and threshold estimates for the desired algorithm. The script requires the following arguments
- --max: maximum number of fields for a linkage
- --min: minimum number of fields for a linkage
- --file: generic file name of all csv files
- --save: optional tag if a graph needs to be saved
- Example: --max 8 --min 4 --file birthbirth --save

### How to run resolvers
1. Run . src/main/java/uk/ac/standrews/cs/population_linkage/aleks/analysers/run_resolvers.sh (preferably on a machine with large amount of memory and cores)

### How to run end-to-end
To run all the analysers, builders and resolvers:
1. Run  . src/main/scripts/endtoend/runall.sh
