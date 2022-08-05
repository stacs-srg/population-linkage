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

# clear all keys in global environemtn apart from those in keep_strings_column
clear_env_apart_from <- function(keep_strings_column) {
  print(keep_strings_column)
  keep_strings_column <- cbind(keep_strings_column, "clear_env_apart_from")
  print(keep_strings_column)
  rm(list = ls(envir = .GlobalEnv)[!(ls(envir = .GlobalEnv) %in% keep_strings_column)], envir = .GlobalEnv)
}

# Load the data from the file filename into the global env unless already defined
conditionLoadIntoGlobal <- function(filename, name, sep = ",") {
  if (isdefined(name)) {
    print(paste("ERROR", name, "already defined in global environment"))
  } else {
    data <- read.table(filename, sep = sep, header = TRUE, stringsAsFactors = FALSE)
    assign(name, data, envir = .GlobalEnv)
  }
}

clear <- function() {
  cat("\014")
}
