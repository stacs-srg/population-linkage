# is the key defined in the global environment?
isdefined <- function(key) {
  names <- ls(envir = .GlobalEnv)
  return(key %in% names)
}

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

# Load the data from the file filename into the global env
loadIntoGlobal <- function(filename, name, sep = ",") {

  if (isdefined(name)) {
    rm(list = name, envir = .GlobalEnv)
  }
  data <- read.table(filename, sep = sep, header = TRUE, stringsAsFactors = FALSE)
  assign(name, data, envir = .GlobalEnv)
}

clear <- function() {
  cat("\014")
}
