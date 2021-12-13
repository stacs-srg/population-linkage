/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph;

import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.time.LocalDateTime;

public class CreateIndices {

    private static final String CREATE_ID_UNIQUENESS_CONSTRAINT = "CREATE CONSTRAINT ON (n:STANDARDISED_ID) ASSERT n.propertyName IS UNIQUE";

    public static final String BIRTHS_INDEX_NAME = "BirthsIndex";
    public static final String MARRIAGES_INDEX_NAME = "MarriagesIndex";
    public static final String DEATHS_INDEX_NAME = "DeathsIndex";

    private static final String CREATE_BIRTHS_UNIQUENESS_CONSTRAINT = makeCreateUniquenessConstraintQuery(BIRTHS_INDEX_NAME, "Birth");
    private static final String CREATE_MARRIAGES_UNIQUENESS_CONSTRAINT = makeCreateUniquenessConstraintQuery(MARRIAGES_INDEX_NAME, "Marriage");
    private static final String CREATE_DEATHS_UNIQUENESS_CONSTRAINT = makeCreateUniquenessConstraintQuery(DEATHS_INDEX_NAME, "Death");

    public static final String BIRTH_RECORD_IDENTITY_INDEX_NAME = "BIRTH_RECORD_IDENTITY_INDEX";
    public static final String BIRTH_CHILD_IDENTITY_INDEX_NAME = "BIRTH_CHILD_IDENTITY_INDEX";
    public static final String BIRTH_MOTHER_IDENTITY_INDEX_NAME = "BIRTH_MOTHER_IDENTITY_INDEX";
    public static final String BIRTH_FATHER_IDENTITY_INDEX_NAME = "BIRTH_FATHER_IDENTITY_INDEX";

    public static final String DEATH_DECEASED_IDENTITY_INDEX_NAME = "DEATH_DECEASED_IDENTITY_INDEX";
    public static final String DEATH_MOTHER_IDENTITY_INDEX_NAME = "DEATH_MOTHER_IDENTITY_INDEX";
    public static final String DEATH_FATHER_IDENTITY_INDEX_NAME = "DEATH_FATHER_IDENTITY_INDEX";

    public static final String MARRIAGE_BRIDE_IDENTITY_INDEX_NAME = "MARRIAGE_BRIDE_IDENTITY_INDEX";
    public static final String MARRIAGE_GROOM_IDENTITY_INDEX_NAME = "MARRIAGE_GROOM_IDENTITY_INDEX";
    public static final String MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX_NAME = "MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX";
    public static final String MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX_NAME = "MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX";
    public static final String MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX_NAME = "MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX";
    public static final String MARRIAGE_GROOM_FATHER_IDENTITY_INDEX_NAME = "MARRIAGE_GROOM_FATHER_IDENTITY_INDEX";

    private static final String CREATE_BIRTH_RECORD_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (b:Birth) on (b.BIRTH_RECORD_IDENTITY)", BIRTH_RECORD_IDENTITY_INDEX_NAME);
    private static final String CREATE_BIRTH_CHILD_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (b:Birth) on (b.CHILD_IDENTITY)", BIRTH_CHILD_IDENTITY_INDEX_NAME);
    private static final String CREATE_BIRTH_MOTHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (b:Birth) on (b.MOTHER_IDENTITY)", BIRTH_MOTHER_IDENTITY_INDEX_NAME);
    private static final String CREATE_BIRTH_FATHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (b:Birth) on (b.FATHER_IDENTITY)", BIRTH_FATHER_IDENTITY_INDEX_NAME);

    private static final String CREATE_DEATH_DECEASED_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (d:Death) on (d.DECEASED_IDENTITY)", DEATH_DECEASED_IDENTITY_INDEX_NAME);
    private static final String CREATE_DEATH_FATHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (d:Death) on (d.FATHER_IDENTITY)", DEATH_FATHER_IDENTITY_INDEX_NAME);
    private static final String CREATE_DEATH_MOTHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (d:Death) on (d.MOTHER_IDENTITY)", DEATH_MOTHER_IDENTITY_INDEX_NAME);

    private static final String CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_IDENTITY)", MARRIAGE_BRIDE_IDENTITY_INDEX_NAME);
    private static final String CREATE_MARRIAGE_GROOM_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_IDENTITY)", MARRIAGE_GROOM_IDENTITY_INDEX_NAME);
    private static final String CREATE_MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_MOTHER_IDENTITY)", MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX_NAME);
    private static final String CREATE_MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_MOTHER_IDENTITY)", MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX_NAME);
    private static final String CREATE_MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_FATHER_IDENTITY)", MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX_NAME);
    private static final String CREATE_MARRIAGE_GROOM_FATHER_IDENTITY_INDEX = String.format("CREATE INDEX %s IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_FATHER_IDENTITY)", MARRIAGE_GROOM_FATHER_IDENTITY_INDEX_NAME);

    public static void main(String[] args) {

        doQueries(
                CREATE_ID_UNIQUENESS_CONSTRAINT, CREATE_BIRTHS_UNIQUENESS_CONSTRAINT, CREATE_MARRIAGES_UNIQUENESS_CONSTRAINT, CREATE_DEATHS_UNIQUENESS_CONSTRAINT,

                CREATE_BIRTH_RECORD_IDENTITY_INDEX, CREATE_BIRTH_CHILD_IDENTITY_INDEX, CREATE_BIRTH_FATHER_IDENTITY_INDEX, CREATE_BIRTH_MOTHER_IDENTITY_INDEX,

                CREATE_DEATH_DECEASED_IDENTITY_INDEX, CREATE_DEATH_FATHER_IDENTITY_INDEX, CREATE_DEATH_MOTHER_IDENTITY_INDEX,

                CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX, CREATE_MARRIAGE_GROOM_IDENTITY_INDEX, CREATE_MARRIAGE_GROOM_FATHER_IDENTITY_INDEX,
                CREATE_MARRIAGE_GROOM_MOTHER_IDENTITY_INDEX, CREATE_MARRIAGE_BRIDE_FATHER_IDENTITY_INDEX, CREATE_MARRIAGE_BRIDE_MOTHER_IDENTITY_INDEX);
    }

    private static void doQueries(String... queries) {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); Session session = bridge.getNewSession()) {

            System.out.println("Creating indices @ " + LocalDateTime.now());

            for (String query : queries) {
                session.run(query);
            }

            System.out.println("Complete @ " + LocalDateTime.now());
        }
    }

    private static String makeCreateUniquenessConstraintQuery(String constraint_name, String label_name) {

        return String.format("CALL db.createUniquePropertyConstraint(%s, [%s], [\"STANDARDISED_ID\"], \"native-btree-1.0\")", quote(constraint_name), quote(label_name));
    }

    private static String quote(String s) {

        return "\"" + s + "\"";
    }
}
