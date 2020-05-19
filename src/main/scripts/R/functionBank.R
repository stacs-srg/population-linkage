library("ggplot2")
library("stringr")
library("scales")

source("utils.R")
source("dataManipulation.R")

############################################################################
# Functions in this section used in processing data for Umea papers.

RAW_METRIC_NAMES <- c("BagDistance", "Cosine", "Damerau-Levenshtein", "Dice", "Jaccard", "Jaro", "JaroWinkler",
                      "JensenShannon", "Levenshtein", "LongestCommonSubstring", "Metaphone-Levenshtein", "NYSIIS-Levenshtein", "NeedlemanWunsch", "SED", "SmithWaterman")

DISPLAY_METRIC_NAMES <- c("Bag Distance", "Cosine", "Damerau-Levenshtein", "Dice", "Jaccard", "Jaro", "Jaro-Winkler",
                          "Jensen-Shannon", "Levenshtein", "Longest Common Substring", "Metaphone-Levenshtein", "NYSIIS-Levenshtein", "Needleman-Wunsch", "SED", "Smith-Waterman")

METRIC_NAME_MAP <- setNames(as.list(DISPLAY_METRIC_NAMES), RAW_METRIC_NAMES)
DIAGRAM_FILE_TYPE <- ".eps"

# Returns a convergence plot, with error bars for a given metric and threshold.
plotFMeasureConvergence <- function(data, metric, thresholds, x_upper_bound, x_axis_label, y_axis_label, colours) {

  data <- filter(data, metric, thresholds)
  data <- recalculateStatistics(data)

  plot <- makeConvergencePlot(data, "f_measure", x_upper_bound, 1.0, x_axis_label, y_axis_label, "Threshold", "bottom", colours)

  return(plot)
}

# Returns a convergence plot for absolute error relative to final value, with error bars for a given metric and threshold.
plotFMeasureErrorConvergence <- function(data, metric, thresholds, x_upper_bound, x_axis_label, y_axis_label, colours) {

  data <- filter(data, metric, thresholds)
  data <- recalculateStatistics(data)
  data <- addAbsoluteErrorColumn(data)

  plot <- makeConvergencePlot(data, "absolute_error", x_upper_bound, 0.12, x_axis_label, y_axis_label, NULL, "none", colours)

  return(plot)
}

plotAllFMeasureErrorConvergence <- function(data, x_upper_bound, x_axis_label, y_axis_label, line_colour) {

  data <- recalculateStatistics(data)
  plot <- makeOverlaidConvergencePlot(data, x_upper_bound, x_axis_label, y_axis_label, line_colour)

  return(plot)
}

plotFMeasureVsThreshold <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  number_of_records_processed <- max(data$records.processed)
  data <- data[which(data$records.processed == number_of_records_processed),]

  data <- recalculateStatistics(data)
  plot <- makeFMeasureVsThresholdPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotROC <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  number_of_records_processed <- max(data$records.processed)
  data <- data[which(data$records.processed == number_of_records_processed),]

  data <- recalculateStatistics(data)
  plot <- makeROCPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

plotPrecisionVsRecall <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  number_of_records_processed <- max(data$records.processed)
  data <- data[which(data$records.processed == number_of_records_processed),]

  data <- recalculateStatistics(data)
  plot <- makePrecisionVsRecallPlot(data, x_axis_label, y_axis_label, custom_palette, faceted)

  return(plot)
}

filter <- function(data, metric, thresholds) {

  data <- data[which(data$metric == metric),]
  data <- data[which(data$threshold %in% thresholds),]

  return(data)
}

recalculateStatistics <- function(data) {

  # Recalculate since F-measure in source data is only calculated to 2 decimal places.
  data$precision <- precision(data$tp, data$fp)
  data$recall <- recall(data$tp, data$fn)
  data$false_positive_rate <- false_positive_rate(data$tn, data$fp)
  data$true_positive_rate <- data$recall
  data$f_measure <- fmeasure(data$precision, data$recall)

  return(data)
}

addAbsoluteErrorColumn <- function(data) {

  final_measure <- data[nrow(data), "f_measure"]
  data[, "absolute_error"] <- abs(final_measure - data[, "f_measure"])

  return(data)
}

makeConvergencePlot <- function(data, measure, x_upper_bound, y_upper_bound, x_axis_label, y_axis_label, legend_label, legend_position, colours) {

  summary <- summarySE(data, measurevar = measure, groupvars = c("metric", "threshold", "records.processed"))

  plot <- ggplot(summary) +
    geom_line(aes_string(x = "records.processed", y = measure, colour = as.factor(summary$threshold))) +
    geom_errorbar(aes(x = records.processed, ymin = get(measure) - ci, ymax = get(measure) + ci, colour = as.factor(threshold))) +
    scale_x_continuous(labels = comma, limits = c(0, x_upper_bound)) +
    scale_y_continuous(n.breaks = 10, limits = c(0, y_upper_bound)) +
    labs(x = x_axis_label, y = y_axis_label, colour = legend_label) +
    theme(legend.position = legend_position, panel.background = element_rect(fill = "white"),
          panel.grid.major = element_line(size = 0.25, linetype = "solid", colour = "grey")) +
    scale_colour_manual(values = colours)

  return(plot)
}

makeOverlaidConvergencePlot <- function(data, x_upper_bound, x_axis_label, y_axis_label, line_colour) {

  summary <- summarySE(data, measurevar = "f_measure", groupvars = c("metric", "threshold", "records.processed"))

  plot <- ggplot()

  for (thresh in unique(summary$threshold)) {
    for (metric in unique(summary$metric)) {

      subset <- filter(summary, metric, thresh)
      subset[, "final"] <- subset[nrow(subset), "f_measure"]

      plot <- plot + geom_line(data = subset, aes(x = records.processed, y = abs(final - get("f_measure"))), colour = line_colour)
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

  return(makePerMetricPlot(data, "threshold", "f_measure", x_axis_label, y_axis_label, custom_palette, faceted))
}

makeROCPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerMetricPlot(data, "false_positive_rate", "true_positive_rate", x_axis_label, y_axis_label, custom_palette, faceted))
}

makePrecisionVsRecallPlot <- function(data, x_axis_label, y_axis_label, custom_palette, faceted) {

  return(makePerMetricPlot(data, "recall", "precision", x_axis_label, y_axis_label, custom_palette, faceted))
}

makePerMetricPlot <- function(data, x_axis_name, y_axis_name, x_axis_label, y_axis_label, custom_palette, faceted) {

  collated_data <- collateData(data)

  plot <- ggplot(collated_data, aes_string(x = x_axis_name)) +
    geom_line(aes_string(y = y_axis_name, colour = as.factor(collated_data$metric))) +
    labs(x = x_axis_label, y = y_axis_label, colour = "") + # suppress legend label
    scale_colour_manual(values = custom_palette)

  if (faceted) {
    plot <- plot +
      facet_wrap(~metric) +
      scale_x_continuous(limits = c(0, 1), minor_breaks = NULL) +
      scale_y_continuous(limits = c(0, 1), minor_breaks = NULL) +
      theme(legend.position = "bottom", panel.spacing = unit(0.75, "lines")) # space out the images a little

  }
  else {
    plot <- plot +
      scale_x_continuous(limits = c(0, 1), breaks = seq(0, 1, 0.1), minor_breaks = NULL) +
      scale_y_continuous(limits = c(0, 1), breaks = seq(0, 1, 0.1), minor_breaks = NULL) +
      theme(legend.position = "bottom", panel.background = element_rect(fill = "white"),
            panel.grid.major = element_line(size = 0.25, linetype = "solid", colour = "grey"))
  }

  return(plot)
}

collateData <- function(data) {

  collated_data <- data.frame()

  for (metric in unique(data$metric)) {
    for (threshold in unique(data$threshold)) {

      filtered_data <- filter(data, metric, threshold)

      collated_data[nrow(collated_data) + 1, "metric"] <- METRIC_NAME_MAP[[metric]]
      collated_data[nrow(collated_data), "threshold"] <- threshold

      # Assignments below make sense because filtered_data only contains a single row.
      collated_data[nrow(collated_data), "false_positive_rate"] <- filtered_data$false_positive_rate
      collated_data[nrow(collated_data), "true_positive_rate"] <- filtered_data$true_positive_rate
      collated_data[nrow(collated_data), "precision"] <- filtered_data$precision
      collated_data[nrow(collated_data), "recall"] <- filtered_data$recall
      collated_data[nrow(collated_data), "f_measure"] <- filtered_data$f_measure
    }
  }

  return(collated_data)
}

saveFMeasureVsThreshold <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  # conditionLoadIntoGlobal(input_file_path, "linkage_data")
  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotFMeasureVsThreshold(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

saveROC <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  # conditionLoadIntoGlobal(input_file_path, "linkage_data")
  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotROC(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

savePrecisionVsRecall <- function(input_file_path, output_file_path, x_axis_label, y_axis_label, palette, image_dpi, x_image_width, y_image_width, image_size_units, faceted) {

  # conditionLoadIntoGlobal(input_file_path, "linkage_data")
  loadIntoGlobal(input_file_path, "linkage_data")

  plot <- plotPrecisionVsRecall(linkage_data, x_axis_label, y_axis_label, palette, faceted)
  ggsave(output_file_path, plot, dpi = image_dpi, width = x_image_width, height = y_image_width, units = image_size_units)
}

inputFilePath <- function(directory_path, file_name_root, file_name_detail) {
  return(paste(directory_path, paste0(file_name_root, file_name_detail, ".csv"), sep = "/"))
}

outputFilePath <- function(directory_path, file_name_root, file_name_detail) {
  return(paste(directory_path, paste0(file_name_root, file_name_detail, DIAGRAM_FILE_TYPE), sep = "/"))
}

############################################################################

# This returns a new data frame containing the metric,threshold, closeness, records processed derived from the data parmeter for the supplied measure
analyse_space <- function(data, measure) {

  thresholds <- c(0.4, 0.6, 0.8)

  closeness.df <- data.frame(metric = character(),
                             threshold = double(),
                             closeness = double(),
                             records.processed = integer(),
                             stringsAsFactors = FALSE)

  for (metric in unique(data$metric))
    for (threshold in thresholds) {
      current_subset <- select_records.processed.ci(data, measure, threshold, metric) # a table of records.processed and Cis of absolute differences of measue from final

      for (closeness_tolerance in seq(0, 0.1, 0.0001)) {

        number <- find_stable_close_enough_to_observed(current_subset, closeness_tolerance)
        if (!is.na(number)) {
          closeness.df[nrow(closeness.df) + 1, "metric"] <- metric
          closeness.df[nrow(closeness.df), "threshold"] <- threshold
          closeness.df[nrow(closeness.df), "closeness"] <- closeness_tolerance
          closeness.df[nrow(closeness.df), "records.processed"] <- number
        }
      }
    }
  return(closeness.df)
}

# Select the records from df with fields equal to measure, thresh and metric and calculate the confidence intervals based on final value of
select_records.processed.ci <- function(df, measure, thresh, metric) {
  # ci_s is a table in which we are interested the confidence intervals
  ci_s <- df[which(df$metric == metric),]  # select a single metric
  ci_s <- ci_s[which(ci_s$threshold == thresh),] # select a threshold

  ci_s <- ci_s[order(ci_s$records.processed),] #  order by records.processed
  final_measure <- ci_s[nrow(ci_s), measure]   # find the last value for the measure supplied as a param
  ci_s[, "abs_diff"] <- abs(final_measure - ci_s[, measure])  #add a column with the max abs differences from final_measure

  ci_s <- summarySE(ci_s, measurevar = "abs_diff", groupvars = c("metric", "threshold", "records.processed")) # calculate mean,stdev,stderr
  ci_s <- ci_s[, c("records.processed", "ci", "abs_diff")]

  return(ci_s)
}

# find the first x value for which the modelled y value is less than required_threshold
find_first_that_close_enough_to_model <- function(data, a_hat, k_hat, limit, how_close_is_close_enough) {

  for (record_count in unique(data$records.processed)) {
    vbt_value <- eval_vbt(record_count, a_hat, k_hat, limit)
    close_enough <- vbt_value - limit
    if (close_enough < how_close_is_close_enough) {
      print(paste("iterations,vbt_value,closeness: ", record_count, vbt_value, close_enough))
      return(record_count)
    }
  }
  print(paste("Cannot achive required_distance of", how_close_is_close_enough))
  return(NA)
}

# find the first x value for which the y value is less than required_threshold
find_first_close_enough_to_observed <- function(data, required_threshold, min_y_value) {

  for (record_count in unique(data$records.processed)) {
    observed_ci <- data[which(data$records.processed == record_count),]$ci   #eval_vbt( record_count,a_hat,k_hat,limit )
    if (is.null(observed_ci) &&
      is.na(observed_ci) &&
      is.nan(observed_ci)) {
      return(NA)
    }
    difference <- observed_ci - min_y_value
    if (difference < required_threshold) {
      return(record_count)
    }
  }
  return(NA)
}

# find the minimum x value after which the y value is never greater than required_threshold
find_stable_close_enough_to_observed <- function(data, required_threshold) {

  result = 0

  for (record_count in unique(data$records.processed)) {
    observed_ci <- data[which(data$records.processed == record_count),]$ci
    mean <- data[which(data$records.processed == record_count),]$abs_diff

    if (is.null(observed_ci) ||
      is.na(observed_ci) ||
      is.nan(observed_ci) ||
      is.null(mean) ||
      is.na(mean) ||
      is.nan(mean)) {
      print("got NA")
      return(NA)
    }
    top_of_error_bar <- mean + observed_ci
    if (top_of_error_bar > required_threshold) { # record the last position at which the condition doesnt hold (which is the next)
      result = record_count + 1
    }
  }
  return(result)
}

# This saves a plot the overlaps of links and non links in a big M style.
plotMacdonalds <- function(sibdata) {

  sibdata <- sibdata[which(sibdata$records.processed == max(sibdata$records.processed)),]    # extract the block of data with the highest value in records.processed

  for (i in 1:(nrow(sibdata) / 2)) { # process pairs of rows in the block

    non_links <- t(sibdata[i * 2 - 1, 8:ncol(sibdata)])  # transpose the row of counts on non_links into a column
    links <- t(sibdata[i * 2, 8:ncol(sibdata)])        # transpose the row of counts of links into a column

    non_links <- non_links / sum(non_links) # normalise non_links
    links <- links / sum(links)             # normalise links

    both <- data.frame(links, non_links)
    names(both) <- c('links', 'non_links')

    plot <- ggplot(data = both, aes(x = as.numeric(str_replace(rownames(both), "X", "")))) +
      geom_line(y = both$non_links, aes(colour = "non links")) +
      geom_line(y = both$links, aes(colour = "links")) +
      xlab("threshold") +
      ylab("count") +
      labs(colour = "Key") + #
      ylim(0, 1.1 * max(both$links, both$non_links)) +
      ggtitle(mydata[i,]$metric)

    ggsave(paste(paste("/tmp", str_replace(mydata[i,]$metric, "/", "-"), sep = "/"), "png", sep = "."), plot)
  }
}

# the parameter plotdata is a table of type record( metric, threshold, closeness,records.processed ) from analyse_space
# it returns a plot of convergence for the supplied data.
plotconvergence <- function(plotdata, title) {

  ploted <- plotdata[order(plotdata$closeness),]

  pp <- ggplot(data = ploted, aes(x = closeness, y = records.processed)) +
    ggtitle(paste(title, "records process vs error")) +
    geom_line(aes(colour = metric, linetype = as.factor(threshold))) +
    ylim(0, 20000) +
    theme(legend.position = c(1, 1), legend.justification = c(1, 1)) +
    labs(linetype = "threshold") # legend top right

  return(pp)
}

# This plots each of the the metrics on a different plot
plotmetrics <- function(df, measure, lim, metric, thresholds) {

  plotdata <- df[which(df$metric == metric),]

  str(plotdata)

  plotdata <- plotdata[which(plotdata$threshold %in% thresholds),]  # select the appropriate thresholds

  # plotdata <-plotdata[ which (plotdata$threshold == thresh), ] select a single threshold - not what we want here - aide de memoire.

  str(plotdata)

  bar_width <- 10

  plot <- ggplot(data = plotdata) +
    geom_line(aes_string(x = "records.processed", y = measure, colour = as.factor(plotdata$threshold)), show.legend = T) +
    geom_errorbar(aes(x = records.processed, ymin = get(measure) - ci, ymax = get(measure) + ci, colour = as.factor(plotdata$threshold)), width = bar_width) +
    theme(legend.position = "bottom") +
    ylab(measure) +
    xlab(paste0("Records processed (", paste0(RUNS, " replications)"))) +
    ylim(0.0, 1.1) +
    xlim(0, lim) +
    labs(colour = "Threshold") +
    ggtitle(metric)

  ggsave(paste("/Users/graham/Desktop/", metric, "-", measure, "-", lim, ".png", sep = ""), plot)
}

# This plots df relative to the final measure for the given params and saves to a filename
# lim is the xlimit
plotZeroPlots <- function(df, metric, lim, measure, thresh, filename) {

  plotdata <- df[which(df$metric == metric),]  # select a single metric
  plotdata <- plotdata[which(plotdata$threshold == thresh),]

  final_measure <- plotdata[nrow(plotdata), measure]

  plot <- ggplot(data = plotdata) +
    geom_line(aes(x = records.processed, y = final_measure - get(measure), colour = 1), show.legend = T) +
    geom_line(aes(x = records.processed, y = final_measure - get(measure) - ci, colour = 2), show.legend = T) +
    geom_line(aes(x = records.processed, y = final_measure - get(measure) + ci, colour = 3), show.legend = T) +
    scale_y_continuous(minor_breaks = seq(-1, 1, 0.001), breaks = seq(-1, 1, 0.01)) +
    xlim(0, lim) +
    ggtitle(paste(metric, thresh, measure)) +
    ggsave(paste0(filename, "-", lim, "-", metric, ".png", sep = ""), plot, dpi = 320)
}

# This plots df relative to the final measure for the given params and saves to a filename
# lim is the xlimit
# all plots are overlayed
plotAllZeroPlots <- function(plotdata, measure, filename, xlimit) {

  plot <- ggplot()
  val <- "black"  # was 1 for different colours with increment commented below.

  for (thresh in unique(plotdata$threshold)) {
    for (metric in unique(plotdata$metric)) {
      subset <- plotdata[which(plotdata$metric == metric & plotdata$threshold == thresh),]
      final <- subset[nrow(subset), measure]
      subset[, 'final'] <- final
      subset[, 'col_val'] <- val
      plot <- plot + geom_line(data = subset, aes(x = records.processed, y = abs(final - get(measure)))) # colour=col_val
      #val <- val + 1
    }
  }

  plot <- plot +
    scale_y_continuous(minor_breaks = seq(-1, 1, 0.001), breaks = seq(-1, 1, 0.01)) +
    geom_segment(aes(x = 0, xend = xlimit, y = 0.01, yend = 0.01), colour = 'red', linetype = 2) +
    scale_x_continuous(labels = comma, limits = c(0, xlimit)) +
    labs(x = "Records processed", y = "Absolute error in F-measure") +
    theme(legend.position = "none", panel.background = element_rect(fill = "white"),
          panel.grid.major = element_line(size = 0.25, linetype = 'solid', colour = "grey"))

  ggsave(paste0(filename, "-", xlimit, ".png", sep = ""), plot, dpi = 320)
}
