package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthFather;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;

public class BirthFatherPrior extends ProcessNodes {
    public BirthFatherPrior() {
        super();
    }
    private static final String BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[r:GT_ID { actors: \"Child-Father\" } ]-(second_result:Birth) RETURN first_result,second_result";

    private static final String COUNT_BIRTH_FATHER_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Birth) WHERE " +
                    "r.FORENAME = s.FATHER_FORENAME AND " +
                    "r.SURNAME = s.FATHER_SURNAME " +
                    "RETURN count(r)";

    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthFatherPrior en = new BirthFatherPrior();

            en.provenanceComment();
            en.calculateOddsPrior(bridge,COUNT_BIRTH_FATHER_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_BIRTHS_QUERY);
            en.calculatePriors(bridge,BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, BIRTHS_SAMPLE_QUERY,"FORENAME","SURNAME","FATHER_FORENAME","FATHER_SURNAME","baby_father_first","baby_father_second");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }

}
