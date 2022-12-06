package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;

public class BirthDeathPrior extends ProcessNodes {
    public BirthDeathPrior() {
        super();
    }
    private static final String BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[rel:GT_ID { actors: \"Child-Deceased\" } ]-(second_result:Death) RETURN first_result,second_result";
    private static final String COUNT_BIRTH_DEATH_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Death) WHERE " +
                    "r.FORENAME = s.FORENAME AND " +
                    "r.SURNAME = s.SURNAME AND " +
                    "r.FATHER_FORENAME = s.FATHER_FORENAME AND " +
                    "r.FATHER_SURNAME = s.FATHER_SURNAME AND " +
                    "r.MOTHER_FORENAME = s.MOTHER_FORENAME AND " +
                    "r.MOTHER_MAIDEN_SURNAME = s.MOTHER_MAIDEN_SURNAME " +
                    "RETURN count(r)";

    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!
    private static final String DEATHS_SAMPLE_QUERY = "MATCH (result:Death) WITH result WHERE rand() < 0.5 RETURN result";  // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";
    private static final String COUNT_DEATHS_QUERY = "MATCH (r:Death) return count(r)";
    private static final String COUNT_MARRIAGES_QUERY = "MATCH (r:Marriage) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthDeathPrior en = new BirthDeathPrior();
            en.provenanceComment();

            en.calculateOddsPrior(bridge,COUNT_BIRTH_DEATH_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_DEATHS_QUERY);
            en.calculatePriors(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"FORENAME","SURNAME","FORENAME","SURNAME","baby_deceased_first","baby_deceased_second");
            en.calculatePriors(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","baby_groom_mother_first","baby_deceased_mother_surname");
            en.calculatePriors(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"FATHER_FORENAME","FATHER_SURNAME","FATHER_FORENAME","FATHER_SURNAME","baby_deceased_father_first","baby_deceased_father_surname");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }

}
