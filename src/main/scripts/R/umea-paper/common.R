source("functionBank.R")

X_IMAGE_WIDTH <- 20
Y_IMAGE_WIDTH <- 20
IMAGE_SIZE_UNITS <- "cm"
IMAGE_DPI <- 320

# From http://www.cookbook-r.com/Graphs/Colors_(ggplot2)/
PALETTE <- c('#ffcc00', '#CC66FF', '#99cc00', '#cc9900', '#ff6633',
             '#333300', '#ff3366', '#336633', '#ffff33', '#cc3399',
             '#3399ff', '#33cccc', '#ff00CC', '#00ffcc', '#000000')

BBI_FILE_NAME <- "UmeaBirthBrideIdentityPRFByThreshold"
BDI_FILE_NAME <- "UmeaBirthDeathIdentityPRFByThreshold"
BFI_FILE_NAME <- "UmeaBirthFatherIdentityPRFByThreshold"
BGI_FILE_NAME <- "UmeaBirthGroomIdentityPRFByThreshold"
BMI_FILE_NAME <- "UmeaBirthMotherIdentityPRFByThreshold"
BS_FILE_NAME <- "UmeaBirthSiblingPRFByThreshold"
BBS_FILE_NAME <- "UmeaBrideBrideSiblingPRFByThreshold"
BGS_FILE_NAME <- "UmeaBrideGroomSiblingPRFByThreshold"
DS_FILE_NAME <- "UmeaDeathSiblingPRFByThreshold"
GGS_FILE_NAME <- "UmeaGroomGroomSiblingPRFByThreshold"
