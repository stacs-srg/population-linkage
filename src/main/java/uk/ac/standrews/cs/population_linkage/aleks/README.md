## CS5199 Project Documentation

### How to run threshold analysis
1. Prior to running the threhsold analysers, the threshold for the linakge algorithm to be analysed must be set to a certain maximum, either 1.0 for weak linkages requiring less than 4 fields or 2.0 otherwise. 
2. Run run_threshold_analysis.sh
3. Multiple CSV files will be created which should contain all the quality and open triangle measuements
4. optimal_threshold.py can then be used to generate the graphs and threshold estimates for the desired algorithm. The script requires the following arguments
- --max: maximum number of fields for a linkage
- --min: minimum number of fields for a linkage
- --file: generic file name of all csv files
- --save: optional tag if a graph needs to be saved
- Example: --max 8 --min 4 --file birthbirth --save

### How to run resolvers
1. Run run_resolvers.sh
