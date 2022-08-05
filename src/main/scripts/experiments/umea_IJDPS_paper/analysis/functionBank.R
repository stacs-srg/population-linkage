library("ggplot2")
library("stringr")
library("scales")

############################################################################

# is the key defined in the global environment?
isdefined <- function(key) {
  names <- ls(envir = .GlobalEnv)
  return(key %in% names)
}

# Load the data from the file filename into the global env
loadIntoGlobal <- function(filename, name, sep = ",") {

  if (isdefined(name)) {
    rm(list = name, envir = .GlobalEnv)
  }
  data <- read.table(filename, sep = sep, header = TRUE, stringsAsFactors = FALSE)
  assign(name, data, envir = .GlobalEnv)
}

recall <- function(tp, fn) {
  return(tp / (tp + fn))
}

precision <- function(tp, fp) {
  return(tp / (tp + fp))
}

false_positive_rate <- function(tn, fp) {
  return(fp / (tn + fp))
}

true_negative_rate <- function(tn, fp) {
  return(tn / (tn + fp))
}

f_measure <- function(precision, recall) {
  return(2 * (precision * recall) / (precision + recall))
}

## Summarizes data.
## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summarized
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data = NULL, summarised_column_name, grouping_variable_column_names = NULL, na.rm = FALSE, conf.interval = .95, .drop = TRUE) {
  library(plyr)

  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function(x, na.rm = FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }

  # This does the summary. For each group's data frame, return a vector with N, mean, and sd
  datac <- ddply(data, grouping_variable_column_names, .drop = .drop,
                 .fun = function(xx, col) {
                   c(N = length2(xx[[col]], na.rm = na.rm),
                     mean = mean(xx[[col]], na.rm = na.rm),
                     sd = sd(xx[[col]], na.rm = na.rm)
                   )
                 },
                 summarised_column_name
  )

  # Rename the "mean" column
  datac <- rename(datac, c("mean" = summarised_column_name))

  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean

  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval:
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval / 2 + .5, datac$N - 1)
  datac$ci <- datac$se * ciMult

  return(datac)
}

# Returns a convergence plot, with error bars for a given distance measure and threshold.
plotFMeasureConvergence <- function(data, distance_measure_name, thresholds, x_upper_bound, x_axis_label, y_axis_label, colours) {

  data <- filter(data, distance_measure_name, thresholds)
  data <- recalculateStatistics(data)

  plot <- makeConvergencePlot(data, "f_measure", x_upper_bound, 1.0, x_axis_label, y_axis_label, "Threshold", "bottom", colours)

  return(plot)
}

# Returns a convergence plot for absolute error relative to final value, with error bars for a given distance measure and threshold.
plotFMeasureErrorConvergence <- function(data, distance_measure_name, thresholds, x_upper_bound, x_axis_label, y_axis_label, colours) {

  data <- filter(data, distance_measure_name, thresholds)
  data <- recalculateStatistics(data)
  data <- addAbsoluteErrorColumn(data)

  plot <- makeConvergencePlot(data, "absolute_error", x_upper_bound, 0.12, x_axis_label, y_axis_label, NULL, "none", colours)

  return(plot)
}

plotAllFMeasureErrorConvergence <- function(data, x_upper_bound, x_axis_label, y_axis_label, line_colour) {

  data <- recalculateStatistics(data)
  plot <- makeOverlaidConvergencePlot(data, "f_measure", x_upper_bound, x_axis_label, y_axis_label, line_colour)

  return(plot)
}

plotFMeasureVsThreshold <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  # Discard all but the final results.
  data <- data[which(data$records_processed == max(data$records_processed)),]
  data <- recalculateStatistics(data)

  plot <- makeFMeasureVsThresholdPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotSpecificityVsThreshold <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  # Discard all but the final results.
  data <- data[which(data$records_processed == max(data$records_processed)),]
  data <- recalculateStatistics(data)

  plot <- makeSpecificityVsThresholdPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotROC <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  # Discard all but the final results.
  data <- data[which(data$records_processed == max(data$records_processed)),]
  data <- recalculateStatistics(data)

  plot <- makeROCPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotPrecisionVsRecall <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  # Discard all but the final results.
  data <- data[which(data$records_processed == max(data$records_processed)),]
  data <- recalculateStatistics(data)

  plot <- makePrecisionVsRecallPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotSpecificityVsRecall <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  # Discard all but the final results.
  data <- data[which(data$records_processed == max(data$records_processed)),]
  data <- recalculateStatistics(data)

  plot <- makeSpecificityVsRecallPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

filter <- function(data, distance_measure, thresholds) {

  data <- data[which(data$distance_measure == distance_measure),]
  data <- data[which(data$threshold %in% thresholds),]

  return(data)
}

recalculateStatistics <- function(data) {

  # Recalculate since F-measure in source data is only calculated to 2 decimal places.
  data$precision <- precision(data$tp, data$fp)
  data$recall <- recall(data$tp, data$fn)
  data$false_positive_rate <- false_positive_rate(data$tn, data$fp)
  data$true_positive_rate <- data$recall
  data$true_negative_rate <- true_negative_rate(data$tn, data$fp)
  data$f_measure <- f_measure(data$precision, data$recall)

  return(data)
}

addAbsoluteErrorColumn <- function(data) {

  final_f_measure <- data[nrow(data), "f_measure"]
  data[, "absolute_error"] <- abs(final_f_measure - data[, "f_measure"])

  return(data)
}

makeConvergencePlot <- function(data, quality_measure_column_name, x_upper_bound, y_upper_bound, x_axis_label, y_axis_label, legend_label, legend_position, colours) {

  summary <- summarySE(data, summarised_column_name = quality_measure_column_name, grouping_variable_column_names = c("distance_measure", "threshold", "records_processed"))

  plot <- ggplot(summary) +
    geom_line(aes_string(x = "records_processed", y = quality_measure_column_name, colour = as.factor(summary$threshold))) +
    geom_errorbar(aes(x = records_processed, ymin = get(quality_measure_column_name) - ci, ymax = get(quality_measure_column_name) + ci, colour = as.factor(threshold))) +
    scale_x_continuous(labels = comma, limits = c(0, x_upper_bound)) +
    scale_y_continuous(n.breaks = 10, limits = c(0, y_upper_bound)) +
    labs(x = x_axis_label, y = y_axis_label, colour = legend_label) +
    theme(legend.position = legend_position, panel.background = element_rect(fill = "white"),
          panel.grid.major = element_line(size = 0.25, linetype = "solid", colour = "grey")) +
    scale_colour_manual(values = colours)

  return(plot)
}

makeOverlaidConvergencePlot <- function(data, quality_measure_column_name, x_upper_bound, x_axis_label, y_axis_label, line_colour) {

  summary <- summarySE(data, summarised_column_name = quality_measure_column_name, grouping_variable_column_names = c("distance_measure", "threshold", "records_processed"))

  plot <- ggplot()

  for (threshold in unique(summary$threshold)) {
    for (distance_measure in unique(summary$distance_measure)) {

      subset <- filter(summary, distance_measure, threshold)
      subset[, "final"] <- subset[nrow(subset), "f_measure"]

      plot <- plot + geom_line(data = subset, aes(x = records_processed, y = abs(final - get("f_measure"))), colour = line_colour)
    }
  }

  plot <- plot +
    scale_x_continuous(labels = comma, limits = c(0, x_upper_bound)) +
    scale_y_continuous(breaks = seq(0, 1, 0.01)) +
    labs(x = x_axis_label, y = y_axis_label) +
    theme(legend.position = "none", panel.background = element_rect(fill = "white"),
          panel.grid.major = element_line(size = 0.25, linetype = "solid", colour = "grey")) +
    geom_segment(aes(x = 0, xend = x_upper_bound, y = 0.01, yend = 0.01), linetype = "dashed", colour = "red")

  return(plot)
}

makeFMeasureVsThresholdPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerDistanceMeasurePlot(data, "threshold", "f_measure", x_axis_label, y_axis_label, custom_palette, faceted))
}

makeSpecificityVsThresholdPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerDistanceMeasurePlot(data, "threshold", "true_negative_rate", x_axis_label, y_axis_label, custom_palette, faceted, 0, 1, 0.99995, 1.000001))
}

makeROCPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerDistanceMeasurePlot(data, "false_positive_rate", "true_positive_rate", x_axis_label, y_axis_label, custom_palette, faceted))
}

makePrecisionVsRecallPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerDistanceMeasurePlot(data, "recall", "precision", x_axis_label, y_axis_label, custom_palette, faceted))
}

makeSpecificityVsRecallPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerDistanceMeasurePlot(data, "recall", "true_negative_rate", x_axis_label, y_axis_label, custom_palette, faceted, 0, 1, 0.99995, 1.000001))
}

makePerDistanceMeasurePlot <- function(data, x_axis_name, y_axis_name, x_axis_label, y_axis_label, custom_palette, faceted, x_lower_bound = 0, x_upper_bound = 1, y_lower_bound = 0, y_upper_bound = 1) {

  collated_data <- collateData(data)

  plot <- ggplot(collated_data, aes_string(x = x_axis_name)) +
    geom_line(aes_string(y = y_axis_name, colour = as.factor(collated_data$distance_measure))) +
    labs(x = x_axis_label, y = y_axis_label, colour = "") + # suppress legend label
    scale_colour_manual(values = custom_palette)

  if (faceted) {
    plot <- plot +
      facet_wrap(~distance_measure) +
      scale_x_continuous(limits = c(x_lower_bound, x_upper_bound), minor_breaks = NULL) +
      scale_y_continuous(limits = c(y_lower_bound, y_upper_bound), minor_breaks = NULL) +
      theme(legend.position = "bottom", panel.spacing = unit(0.75, "lines")) # space out the images a little
  }
  else {
    plot <- plot +
      scale_x_continuous(limits = c(x_lower_bound, x_upper_bound), breaks = seq(x_lower_bound, x_upper_bound, 0.1), minor_breaks = NULL) +
      scale_y_continuous(limits = c(y_lower_bound, y_upper_bound), breaks = seq(y_lower_bound, y_upper_bound, 0.1), minor_breaks = NULL) +
      theme(legend.position = "bottom", panel.background = element_rect(fill = "white"),
            panel.grid.major = element_line(size = 0.25, linetype = "solid", colour = "grey"))
  }

  return(plot)
}

collateData <- function(data) {

  collated_data <- data.frame()

  for (distance_measure in unique(data$distance_measure)) {
    for (threshold in unique(data$threshold)) {

      filtered_data <- filter(data, distance_measure, threshold)

      collated_data[nrow(collated_data) + 1, "distance_measure"] <- DISTANCE_MEASURE_NAME_MAP[[distance_measure]]
      collated_data[nrow(collated_data), "threshold"] <- threshold

      # Assignments below make sense because filtered_data only contains a single row.
      collated_data[nrow(collated_data), "false_positive_rate"] <- filtered_data$false_positive_rate
      collated_data[nrow(collated_data), "true_positive_rate"] <- filtered_data$true_positive_rate
      collated_data[nrow(collated_data), "true_negative_rate"] <- filtered_data$true_negative_rate
      collated_data[nrow(collated_data), "precision"] <- filtered_data$precision
      collated_data[nrow(collated_data), "recall"] <- filtered_data$recall
      collated_data[nrow(collated_data), "f_measure"] <- filtered_data$f_measure
    }
  }

  return(collated_data)
}

saveFMeasureVsThreshold <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotFMeasureVsThreshold(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

saveSpecificityVsThreshold <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotSpecificityVsThreshold(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

saveROC <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotROC(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

savePrecisionVsRecall <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotPrecisionVsRecall(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

saveSpecificityVsRecall <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotSpecificityVsRecall(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

inputFilePath <- function(directory_path, file_name_root, file_name_detail) {
  return(paste(directory_path, paste0(file_name_root, file_name_detail, ".csv"), sep = "/"))
}

outputFilePath <- function(directory_path, file_name_root, file_name_detail) {
  return(paste(directory_path, paste0(file_name_root, file_name_detail, DIAGRAM_FILE_TYPE), sep = "/"))
}
