/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.ArrayList;
import java.util.List;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author Jianheng Huang jh377@st-andrews.ac.uk
 *         Bolun Wang bw93@st-andrews.ac.uk
 */
public class EstablishGTLinks {
    private final RecordRepository record_repository;
    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final IBucket marriages;
    private final IBucket deaths;
    private final List<Birth> birthList = new ArrayList<>();
    private final List<Marriage> marriageList = new ArrayList<>();
    private final List<Death> deathList = new ArrayList<>();

    public static final String BIRTHS_BUCKET_NAME = "birth_records";
    public static final String MARRIAGE_BUCKET_NAME = "marriage_records";
    public static final String DEATHS_BUCKET_NAME = "death_records";

    public EstablishGTLinks(String source_repo_name, NeoDbCypherBridge bridge) throws BucketException {
        this.record_repository = new RecordRepository(source_repo_name);
        this.bridge = bridge;
        this.births = record_repository.getBucket(BIRTHS_BUCKET_NAME);
        this.marriages = record_repository.getBucket(MARRIAGE_BUCKET_NAME);
        this.deaths = record_repository.getBucket(DEATHS_BUCKET_NAME);
        initializeRecordsToList();
    }

    private void initializeRecordsToList() throws BucketException {
        checkConnections( record_repository );

        record_repository.getBirths().forEach(birthList::add);
        record_repository.getMarriages().forEach(marriageList::add);
        record_repository.getDeaths().forEach(deathList::add);
    }

    private void checkConnections(RecordRepository record_repository) throws BucketException {
        if( record_repository.getBucket(BIRTHS_BUCKET_NAME).size() == 0 ) {
            System.out.println( "Zero birth records found - giving up!" );
            throw new RuntimeException("No records found");
        }
        if( record_repository.getBucket(MARRIAGE_BUCKET_NAME).size() == 0 ) {
            System.out.println( "Zero marriage records found - giving up!" );
            throw new RuntimeException("No records found");
        }
        if( record_repository.getBucket(DEATHS_BUCKET_NAME).size() == 0 ) {
            System.out.println( "Zero death records found - giving up!" );
            throw new RuntimeException("No records found");
        }
    }

    public void runLinkageCreation() {
        //BM
        matchBirthGroomIdentity();  // break
        matchBirthBrideIdentity();  // break
        matchDeathBirth();  // Birth Death Death Birth not break

        matchBirthMotherIdentity(); // break
        matchDeathBrideOwnMarriage();// break
        matchDeathGroomOwnMarriageIdentity();// break

        matchFatherGroomIdentity(); // not break marriage birth done
        matchBirthParentsMarriage();// not break marriage birth
        matchBirthDeathSibling();   // not break  Death Birth
        matchBirthDeathHalfSibling();
        matchBrideGroomSiblingLinkage();    // not break
        matchGroomBrideSiblingLinkage();    //not break
        matchBrideGroomHalfSiblingLinkage();    // not break
        matchGroomBrideHalfSiblingLinkage();    //not break

        matchBirthSiblingLinkage(); // not break  Birth Birth
        matchBirthHalfSiblingLinkage(); // not break  Birth Birth

        matchBrideBrideSiblingLinkage();    // not break Marriage Marriage
        matchBrideBrideHalfSiblingLinkage();

        matchGroomGroomSibling();   // not break
        matchGroomGroomHalfSibling();

        matchDeathSiblingLinkage(); // not break
        matchDeathHalfSiblingLinkage();


    }

    private void matchBirthGroomIdentity() {
        System.out.println("Creating BirthGroomIdentity linkage...");
        for (Birth birth : birthList) {
            String childId = birth.getString(Birth.CHILD_IDENTITY);
            String CHILD_IDENTITY = "child_identity";
            Query.createBirthGroomOwnMarriageReference(bridge, CHILD_IDENTITY, childId);
        }
        System.out.println("BirthGroomIdentity linkage finished!");
    }

    private void matchBirthBrideIdentity() {
        System.out.println("Creating BirthBrideIdentity linkage...");
        for (Birth birth : birthList) {
            String childId = birth.getString(Birth.CHILD_IDENTITY);
            String CHILD_IDENTITY = "child_identity";
            Query.createBirthBrideOwnMarriageReference(bridge, CHILD_IDENTITY, childId);
        }
        System.out.println("BirthBrideIdentity linkage finished!");
    }

    // for Birth-Marriage match
    private void matchFatherGroomIdentity() {
        System.out.println("Creating FatherGroomIdentity linkage...");
        for (Birth birth : birthList) {
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            String FATHER_IDENTITY = "father_identity";
            String standardisedId = birth.getString(Birth.STANDARDISED_ID);
            String STANDARDISED_ID = "standardised_id";
            Query.createFatherGroomIdentityReference(bridge, FATHER_IDENTITY, STANDARDISED_ID, fatherId, standardisedId);

        }
        System.out.println(" FatherGroomIdentity linkage finished!");
    }

    // for Birth-Death match
    private void matchDeathBirth() {
        System.out.println("Creating DeathBirthIdentity linkage...");
        for (Birth birth : birthList) {
            String childId = birth.getString(Birth.CHILD_IDENTITY);
            String CHILD_IDENTITY = "child_identity";
            Query.createBirthDeathIdentityReference(bridge, CHILD_IDENTITY, childId);
        }
        System.out.println("DeathBirthIdentity linkage finished!");

        System.out.println("Creating BirthDeathIdentity linkage...");
        for (Birth birth : birthList) {
            String childId = birth.getString(Birth.CHILD_IDENTITY);
            String CHILD_IDENTITY = "child_identity";
            Query.createDeathBirthIdentityReference(bridge, CHILD_IDENTITY, childId);
        }
        System.out.println("BirthDeathIdentity linkage finished!");
    }

    private void matchDeathBrideOwnMarriage() {
        System.out.println("Creating DeathBrideOwnMarriage linkage...");
        for (Death death : deathList) {
            String deceasedId = death.getString(Death.DECEASED_IDENTITY);
            String DECEASED_IDENTITY = "deceased_identity";
            Query.createDeathBrideOwnMarriageReference(this.bridge, DECEASED_IDENTITY, deceasedId);
        }
        System.out.println("DeathBrideOwnMarriage linkage finished!");
    }

    // for Marriage-Death match
    private void matchDeathGroomOwnMarriageIdentity() {
        System.out.println("Creating DeathGroomOwnMarriageIdentity linkage...");
        for (Death death : deathList) {
            String deceasedId = death.getString(Death.DECEASED_IDENTITY);
            String DECEASED_IDENTITY = "deceased_identity";
            Query.createDeathGroomOwnMarriageIdentityReference(this.bridge, DECEASED_IDENTITY, deceasedId);
        }
        System.out.println("DeathGroomOwnMarriageIdentity linkage finished!");
    }

    // for Birth-Birth match
    private void matchBirthMotherIdentity() {
        System.out.println("Creating BirthMotherIdentity linkage...");
        for (Birth birth : birthList) {
            String childId = birth.getString(Birth.CHILD_IDENTITY);
            String CHILD_IDENTITY = "child_identity";
            Query.createBirthMotherIdentityReference(bridge, CHILD_IDENTITY, childId);
        }
        System.out.println("BirthMotherIdentity linkage finished!");
    }

    private void matchBirthParentsMarriage() {
        System.out.println("Creating BirthParentsMarriage linkage...");
        for (Birth birth : birthList) {
            String motherId = birth.getString(Birth.MOTHER_IDENTITY);
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {

                String standardisedId = birth.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBirthParentsMarriageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
    }

    private void matchBirthDeathSibling() {
        System.out.println("Creating BirthDeathSibling linkage...");
        for (Birth birth : birthList) {
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            String motherId = birth.getString(Birth.MOTHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {
                String standardisedId = birth.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBirthDeathSiblingReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
    }

    private void matchBirthDeathHalfSibling() {
        System.out.println("Creating BirthDeathHalfSibling linkage...");
        for (Birth birth : birthList) {
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            String motherId = birth.getString(Birth.MOTHER_IDENTITY);
            if(!fatherId.equals("") || !motherId.equals("")) {
            String standardisedId = birth.getString(Birth.STANDARDISED_ID);
            String STANDARDISED_ID = "standardised_id";
            Query.createBirthDeathHalfSiblingReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
    }

    private void matchBirthSiblingLinkage() {
        System.out.println("Creating BirthSibling linkage...");
        for (Birth birth : birthList) {
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            String motherId = birth.getString(Birth.MOTHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {
                String standardisedId = birth.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBirthSiblingLinkageReference1(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BirthSibling linkage finished!");
    }

    private void matchBirthHalfSiblingLinkage() {
        System.out.println("Creating BirthHalfSibling linkage...");
        for (Birth birth : birthList) {
            String fatherId = birth.getString(Birth.FATHER_IDENTITY);
            String motherId = birth.getString(Birth.MOTHER_IDENTITY);
            if (!(fatherId.equals("") && motherId.equals("")) && (fatherId.equals("") || motherId.equals(""))) {
                String standardisedId = birth.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBirthHalfSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BirthHalfSibling linkage finished!");
    }

    private void matchBrideBrideSiblingLinkage() {
        System.out.println("Creating BrideBrideSibling linkage...");
        for (Marriage marriage : marriageList) {
            String fatherId = marriage.getString(Marriage.BRIDE_FATHER_IDENTITY);
            String motherId = marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {
                String standardisedId = marriage.getString(Marriage.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBrideBrideSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BrideBrideSibling linkage finished!");
    }

    private void matchBrideBrideHalfSiblingLinkage() {
        System.out.println("Creating BrideBrideHalfSibling linkage...");
        for (Marriage marriage : marriageList) {
            String fatherId = marriage.getString(Marriage.BRIDE_FATHER_IDENTITY);
            String motherId = marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY);
            if (!(fatherId.equals("") && motherId.equals("")) && (fatherId.equals("") || motherId.equals(""))) {
                String standardisedId = marriage.getString(Marriage.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBrideBrideHalfSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BrideBrideHalfSibling linkage finished!");
    }

    private void matchBrideGroomSiblingLinkage() {
        System.out.println("Creating BrideGroomSibling linkage...");
        for (Marriage marriage : marriageList) {
            String brideId = marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY);
            String brideFatherId = marriage.getString(Marriage.BRIDE_FATHER_IDENTITY);
            if (!brideId.equals("") && !brideFatherId.equals("")) {
                String standardisedId = marriage.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBrideGroomSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BrideGroomSibling linkage finished!");
    }

    private void matchBrideGroomHalfSiblingLinkage() {
        System.out.println("Creating BrideGroomHalfSibling linkage...");
        for (Marriage marriage : marriageList) {
            String brideId = marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY);
            String brideFatherId = marriage.getString(Marriage.BRIDE_FATHER_IDENTITY);
            if (!(brideId.equals("") && brideFatherId.equals("")) && (brideFatherId.equals("") || brideId.equals(""))) {
                String standardisedId = marriage.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createBrideGroomHalfSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("BrideGroomHalfSibling linkage finished!");
    }

    private void matchGroomBrideSiblingLinkage() {
        System.out.println("Creating GroomBrideSibling linkage...");
        for (Marriage marriage : marriageList) {
            String groomId = marriage.getString(Marriage.GROOM_MOTHER_IDENTITY);
            String groomFatherId = marriage.getString(Marriage.GROOM_FATHER_IDENTITY);
            if (!groomId.equals("") && !groomFatherId.equals("")) {
                String standardisedId = marriage.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createGroomBrideSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("GroomBrideSibling linkage finished!");
    }

    private void matchGroomBrideHalfSiblingLinkage() {
        System.out.println("Creating GroomBrideSibling linkage...");
        for (Marriage marriage : marriageList) {
            String groomId = marriage.getString(Marriage.GROOM_MOTHER_IDENTITY);
            String groomFatherId = marriage.getString(Marriage.GROOM_FATHER_IDENTITY);
            if (!(groomId.equals("") && groomFatherId.equals("")) && (groomFatherId.equals("") || groomId.equals(""))) {
                String standardisedId = marriage.getString(Birth.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createGroomBrideHalfSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("GroomBrideSibling linkage finished!");
    }

    private void matchGroomGroomSibling() {
        System.out.println("Creating GroomGroomSibling linkage...");
        for (Marriage marriage : marriageList) {
            String fatherId = marriage.getString(Marriage.GROOM_FATHER_IDENTITY);
            String motherId = marriage.getString(Marriage.GROOM_MOTHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {
                String standardisedId = marriage.getString(Marriage.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createGroomGroomSiblingReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("GroomGroomSibling linkage finished!");
    }

    private void matchGroomGroomHalfSibling() {
        System.out.println("Creating GroomGroomHalfSibling linkage...");
        for (Marriage marriage : marriageList) {
            String fatherId = marriage.getString(Marriage.GROOM_FATHER_IDENTITY);
            String motherId = marriage.getString(Marriage.GROOM_MOTHER_IDENTITY);
            if (!(fatherId.equals("") && motherId.equals("")) && (fatherId.equals("") || motherId.equals(""))) {
                String standardisedId = marriage.getString(Marriage.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createGroomGroomHalfSiblingReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("GroomGroomHalfSibling linkage finished!");
    }

    private void matchDeathSiblingLinkage() {
        System.out.println("Creating DeathSibling linkage...");
        for (Death death : deathList) {
            //createDeathSiblingLinkage(death);
            String fatherId = death.getString(Death.FATHER_IDENTITY);
            String motherId = death.getString(Death.MOTHER_IDENTITY);
            if (!fatherId.equals("") && !motherId.equals("")) {
                String standardisedId = death.getString(Death.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createDeathSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("DeathSibling linkage finished!");
    }

    private void matchDeathHalfSiblingLinkage() {
        System.out.println("Creating DeathHalfSibling linkage...");
        for (Death death : deathList) {
            String fatherId = death.getString(Death.FATHER_IDENTITY);
            String motherId = death.getString(Death.MOTHER_IDENTITY);
            if (!(fatherId.equals("") && motherId.equals("")) && (fatherId.equals("") || motherId.equals(""))) {
                String standardisedId = death.getString(Death.STANDARDISED_ID);
                String STANDARDISED_ID = "standardised_id";
                Query.createDeathHalfSiblingLinkageReference(bridge, STANDARDISED_ID, standardisedId);
            }
        }
        System.out.println("DeathHalfSibling linkage finished!");
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            EstablishGTLinks linkageMatch = new EstablishGTLinks("Umea",bridge); // it should read the configuration file, not hard code

            linkageMatch.runLinkageCreation();
            System.out.println("finished!");
        } catch (Exception e) {
            System.out.println( "Fatal exception during linkage creation");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            System.exit(0); // all good
        }
    }
}