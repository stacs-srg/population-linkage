#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module population-linkage.
#
# population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with population-linkage. If not, see
# <http://www.gnu.org/licenses/>.
#

library("ggplot2")
library("stringr")
library("scales")

INPUT_DIRECTORY_PATH <- "~/Desktop/data"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"

X_IMAGE_WIDTH <- 20
Y_IMAGE_WIDTH <- 20
IMAGE_SIZE_UNITS <- "cm"
IMAGE_DPI <- 320

# From http://www.cookbook-r.com/Graphs/Colors_(ggplot2)/
PALETTE <- c('#ffcc00', '#CC66FF', '#99cc00', '#cc9900', '#ff6633',
             '#333300', '#ff3366', '#336633', '#ffff33', '#cc3399',
             '#3399ff', '#33cccc', '#ff00CC', '#00ffcc', '#000000')

INPUT_FILE_NAME_ROOTS <- c("UmeaBirthGroomIdentity", "UmeaBirthSibling", "UmeaBirthBrideIdentity", "UmeaBirthDeathIdentity", "UmeaBirthFatherIdentity",
                           "UmeaBirthMotherIdentity", "UmeaBrideBrideSibling", "UmeaBrideGroomSibling", "UmeaDeathSibling", "UmeaGroomGroomSibling")

DISTANCE_MEASURE_NAME_PREFIX <- "Normalised mean of field distances using:"

RAW_DISTANCE_MEASURE_NAMES <- c(paste(DISTANCE_MEASURE_NAME_PREFIX, "BagDistance"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Cosine"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Damerau-Levenshtein"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Dice"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Jaccard"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Jaro"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "JaroWinkler"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "JensenShannon"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Levenshtein"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "LongestCommonSubstring"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "Metaphone-Levenshtein"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "NYSIIS-Levenshtein"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "NeedlemanWunsch"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "SED"),
                                paste(DISTANCE_MEASURE_NAME_PREFIX, "SmithWaterman"))

DISPLAY_DISTANCE_MEASURE_NAMES <- c("Bag Distance", "Cosine", "Damerau-Levenshtein", "Dice", "Jaccard", "Jaro", "Jaro-Winkler",
                                    "Jensen-Shannon", "Levenshtein", "Longest Common Substring", "Metaphone-Levenshtein", "NYSIIS-Levenshtein", "Needleman-Wunsch", "SED", "Smith-Waterman")

DISTANCE_MEASURE_NAME_MAP <- setNames(as.list(DISPLAY_DISTANCE_MEASURE_NAMES), RAW_DISTANCE_MEASURE_NAMES)
DIAGRAM_FILE_TYPE <- ".eps"
