package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class JobMappersTest {

    @Test
    public void roundTripMapping() {
        JobWithExpressions jwe = JobTestUtils.buildSingularJob();
        Job job = JobMappers.map(jwe);
        JobWithExpressions jwe2 = JobMappers.map(job);

        assertThat(jwe).isEqualTo(jwe2);
    }

    @Test(expected = NotSingularJobException.class)
    public void failsOnMappingOfNonSingularJob() {
        JobWithExpressions jwe = JobTestUtils.buildJobWithExpressions();
        JobMappers.map(jwe);
    }
}