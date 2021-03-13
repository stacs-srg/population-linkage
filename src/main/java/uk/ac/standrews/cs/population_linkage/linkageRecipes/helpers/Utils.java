/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ReversedLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.RecordRepository;

public class Utils {
    public static void setCacheSizes(RecordRepository record_repository) {
        record_repository.setBirthsCacheSize(LinkageConfig.birthCacheSize);
        record_repository.setDeathsCacheSize(LinkageConfig.deathCacheSize);
        record_repository.setMarriagesCacheSize(LinkageConfig.marriageCacheSize);
    }

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    public static LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

        LocalDateTime next = LocalDateTime.now();
        System.out.println(prettyPrint(Duration.between(previous_time_stamp, next)) + " to " + step_description);
        return next;
    }

    public static String getLinkageClassName(LinkageRecipe linkageRecipe) {
        if(linkageRecipe instanceof ReversedLinkageRecipe) {
            return ((ReversedLinkageRecipe) linkageRecipe).getOriginalLinkageClassCanonicalName();
        } else {
            return linkageRecipe.getClass().getCanonicalName();
        }
    }
}
