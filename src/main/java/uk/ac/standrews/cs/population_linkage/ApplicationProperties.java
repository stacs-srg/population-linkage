/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads configuration information from external properties file.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class ApplicationProperties extends Properties {

    private static final long serialVersionUID = 15472546L;
    private static final Path DEFAULT_CONFIG_DIRECTORY_PATH = Paths.get("properties");
    private static final String APPLICATION_PROPERTIES_FILE_NAME = "application.properties";

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static Properties properties;
    private static final String STORE_PATH_KEY = "store_path";
    private static final String REPOSITORY_NAME_KEY = "repository_name";
    private static final String TEMP_STORE_PATH_PREFIX = "";

    static {

        try {
            properties = new ApplicationProperties();

        } catch (Exception e) {
            ErrorHandling.exceptionError(e);
        }
    }

    private ApplicationProperties() {

        try (InputStream input_stream = Files.newInputStream(DEFAULT_CONFIG_DIRECTORY_PATH.resolve(APPLICATION_PROPERTIES_FILE_NAME))) {

            load(input_stream);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not load properties: " + e.getMessage());
        }
    }

    public static Path getStorePath() {

        final Path store_path_from_properties = getStorePathFromProperties();
        return store_path_from_properties != null ? store_path_from_properties : getTempStorePath();
    }

    public static String getRepositoryName() {

        return properties.getProperty(REPOSITORY_NAME_KEY);
    }

    private static Path getStorePathFromProperties() {

        final String store_path_string = properties.getProperty(STORE_PATH_KEY);
        return store_path_string != null ? Paths.get(store_path_string) : null;
    }

    private static Path getTempStorePath() {

        try {
            return Files.createTempDirectory(TEMP_STORE_PATH_PREFIX);
        }
        catch (final IOException e) {
            throw new RuntimeException("Couldn't create temp output directory: " + e.getMessage());
        }
    }
}
