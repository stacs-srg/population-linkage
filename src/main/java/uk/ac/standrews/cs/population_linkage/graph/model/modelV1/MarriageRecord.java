/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;


import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

/**
 * A Marriage Record class suitable for inclusion in Neo4J graph model
 * This is a transcription of the LXP model used elsewhere and may be constructed from trhat representation
 */
@NodeEntity
class MarriageRecord extends VitalEventRecord {

    @Property
    public String ORIGINAL_ID;
    @Property
    public String STANDARDISED_ID;
    @Property
    public String LXP_ID;
    @Property
    public String BRIDE_FORENAME;
    @Property
    public String BRIDE_SURNAME;
    @Property
    public String GROOM_FORENAME;
    @Property
    public String GROOM_SURNAME;
    @Property
    public String MARRIAGE_DAY;
    @Property
    public String MARRIAGE_MONTH;
    @Property
    public String MARRIAGE_YEAR;
    @Property
    public String PLACE_OF_MARRIAGE;
    @Property
    public String YEAR_OF_REGISTRATION;
    @Property
    public String BRIDE_MOTHER_FORENAME;
    @Property
    public String BRIDE_MOTHER_MAIDEN_SURNAME;
    @Property
    public String BRIDE_FATHER_FORENAME;
    @Property
    public String GROOM_MOTHER_FORENAME;
    @Property
    public String GROOM_MOTHER_MAIDEN_SURNAME;
    @Property
    public String GROOM_FATHER_FORENAME;
    @Property
    public String BRIDE_MARITAL_STATUS;
    @Property
    public String GROOM_MARITAL_STATUS;
    @Property
    public String BRIDE_FATHER_SURNAME;
    @Property
    public String GROOM_FATHER_SURNAME;
    @Property
    public String BRIDE_ADDRESS;
    @Property
    public String BRIDE_OCCUPATION;
    @Property
    public String GROOM_ADDRESS;
    @Property
    public String GROOM_OCCUPATION;
    @Property
    public String BRIDE_MOTHER_DECEASED;
    @Property
    public String BRIDE_FATHER_DECEASED;
    @Property
    public String GROOM_FATHER_DECEASED;
    @Property
    public String GROOM_MOTHER_DECEASED;
    @Property
    public String BRIDE_FATHER_OCCUPATION;
    @Property
    public String GROOM_FATHER_OCCUPATION;
    @Property
    public String CHANGED_GROOM_FORENAME;
    @Property
    public String IMAGE_QUALITY;
    @Property
    public String ENTRY;
    @Property
    public String GROOM_DID_NOT_SIGN;
    @Property
    public String DENOMINATION;
    @Property
    public String CHANGED_GROOM_SURNAME;
    @Property
    public String BRIDE_DID_NOT_SIGN;
    @Property
    public String REGISTRATION_DISTRICT_NUMBER;
    @Property
    public String REGISTRATION_DISTRICT_SUFFIX;
    @Property
    public String BRIDE_AGE_OR_DATE_OF_BIRTH;
    @Property
    public String CHANGED_BRIDE_SURNAME;
    @Property
    public String CORRECTED_ENTRY;
    @Property
    public String CHANGED_BRIDE_FORENAME;
    @Property
    public String GROOM_AGE_OR_DATE_OF_BIRTH;
    @Property
    public String GROOM_IDENTITY;
    @Property
    public String BRIDE_IDENTITY;
    @Property
    public String GROOM_MOTHER_IDENTITY;
    @Property
    public String GROOM_FATHER_IDENTITY;
    @Property
    public String BRIDE_MOTHER_IDENTITY;
    @Property
    public String BRIDE_FATHER_IDENTITY;
    @Property
    public String GROOM_BIRTH_RECORD_IDENTITY;
    @Property
    public String BRIDE_BIRTH_RECORD_IDENTITY;
    @Property
    public String GROOM_FATHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String GROOM_MOTHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String BRIDE_FATHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String BRIDE_MOTHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String BRIDE_IMMIGRATION_GENERATION;
    @Property
    public String GROOM_IMMIGRATION_GENERATION;

    public MarriageRecord() {
        super();
    }

    public MarriageRecord(LXP lxp_rep) throws PersistentObjectException {
        this();
        this.ORIGINAL_ID = lxp_rep.getString( Marriage.ORIGINAL_ID );
        this.STANDARDISED_ID = lxp_rep.getString(Marriage.STANDARDISED_ID);
        this.LXP_ID = lxp_rep.getThisRef().toString();
        this.BRIDE_FORENAME = lxp_rep.getString(Marriage.BRIDE_FORENAME);
        this.BRIDE_SURNAME = lxp_rep.getString(Marriage.BRIDE_SURNAME);
        this.GROOM_FORENAME = lxp_rep.getString(Marriage.GROOM_FORENAME);
        this.GROOM_SURNAME = lxp_rep.getString(Marriage.GROOM_SURNAME);
        this.MARRIAGE_DAY = lxp_rep.getString(Marriage.MARRIAGE_DAY);
        this.MARRIAGE_MONTH = lxp_rep.getString(Marriage.MARRIAGE_MONTH);
        this.MARRIAGE_YEAR = lxp_rep.getString(Marriage.MARRIAGE_YEAR);
        this.PLACE_OF_MARRIAGE = lxp_rep.getString( Marriage.PLACE_OF_MARRIAGE );
        this.YEAR_OF_REGISTRATION = lxp_rep.getString( Marriage.YEAR_OF_REGISTRATION );
        this.BRIDE_MOTHER_FORENAME = lxp_rep.getString( Marriage.BRIDE_MOTHER_FORENAME );
        this.BRIDE_MOTHER_MAIDEN_SURNAME = lxp_rep.getString( Marriage.BRIDE_MOTHER_MAIDEN_SURNAME );
        this.BRIDE_FATHER_FORENAME = lxp_rep.getString( Marriage.BRIDE_FATHER_FORENAME );
        this.GROOM_MOTHER_FORENAME = lxp_rep.getString( Marriage.GROOM_MOTHER_FORENAME );
        this.GROOM_MOTHER_MAIDEN_SURNAME = lxp_rep.getString( Marriage.GROOM_MOTHER_MAIDEN_SURNAME );
        this.GROOM_FATHER_FORENAME = lxp_rep.getString( Marriage.GROOM_FATHER_FORENAME );
        this.BRIDE_MARITAL_STATUS = lxp_rep.getString( Marriage.BRIDE_MARITAL_STATUS );
        this.GROOM_MARITAL_STATUS = lxp_rep.getString( Marriage.GROOM_MARITAL_STATUS );
        this.BRIDE_FATHER_SURNAME = lxp_rep.getString( Marriage.BRIDE_FATHER_SURNAME );
        this.GROOM_FATHER_SURNAME = lxp_rep.getString( Marriage.GROOM_FATHER_SURNAME );
        this.BRIDE_ADDRESS = lxp_rep.getString( Marriage.BRIDE_ADDRESS );
        this.BRIDE_OCCUPATION = lxp_rep.getString( Marriage.BRIDE_OCCUPATION );
        this.GROOM_ADDRESS = lxp_rep.getString( Marriage.GROOM_ADDRESS );
        this.GROOM_OCCUPATION = lxp_rep.getString( Marriage.GROOM_OCCUPATION );
        this.BRIDE_MOTHER_DECEASED = lxp_rep.getString( Marriage.BRIDE_MOTHER_DECEASED );
        this.BRIDE_FATHER_DECEASED = lxp_rep.getString( Marriage.BRIDE_FATHER_DECEASED );
        this.GROOM_FATHER_DECEASED = lxp_rep.getString( Marriage.GROOM_FATHER_DECEASED );
        this.GROOM_MOTHER_DECEASED = lxp_rep.getString( Marriage.GROOM_MOTHER_DECEASED );
        this.BRIDE_FATHER_OCCUPATION = lxp_rep.getString( Marriage.BRIDE_FATHER_OCCUPATION );
        this.GROOM_FATHER_OCCUPATION = lxp_rep.getString( Marriage.GROOM_FATHER_OCCUPATION );
        this.CHANGED_GROOM_FORENAME = lxp_rep.getString( Marriage.CHANGED_GROOM_FORENAME );
        this.IMAGE_QUALITY = lxp_rep.getString( Marriage.IMAGE_QUALITY );
        this.ENTRY = lxp_rep.getString( Marriage.ENTRY );
        this.GROOM_DID_NOT_SIGN = lxp_rep.getString( Marriage.GROOM_DID_NOT_SIGN );
        this.DENOMINATION = lxp_rep.getString( Marriage.DENOMINATION );
        this.CHANGED_GROOM_SURNAME = lxp_rep.getString( Marriage.CHANGED_GROOM_SURNAME );
        this.BRIDE_DID_NOT_SIGN = lxp_rep.getString( Marriage.BRIDE_DID_NOT_SIGN );
        this.REGISTRATION_DISTRICT_NUMBER = lxp_rep.getString( Marriage.REGISTRATION_DISTRICT_NUMBER );
        this.REGISTRATION_DISTRICT_SUFFIX = lxp_rep.getString( Marriage.REGISTRATION_DISTRICT_SUFFIX );
        this.BRIDE_AGE_OR_DATE_OF_BIRTH = lxp_rep.getString( Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH );
        this.CHANGED_BRIDE_SURNAME = lxp_rep.getString( Marriage.CHANGED_BRIDE_SURNAME );
        this.CORRECTED_ENTRY = lxp_rep.getString( Marriage.CORRECTED_ENTRY );
        this.CHANGED_BRIDE_FORENAME = lxp_rep.getString( Marriage.CHANGED_BRIDE_FORENAME );
        this.GROOM_AGE_OR_DATE_OF_BIRTH = lxp_rep.getString( Marriage.GROOM_AGE_OR_DATE_OF_BIRTH );
        this.GROOM_IDENTITY = lxp_rep.getString( Marriage.GROOM_IDENTITY );
        this.BRIDE_IDENTITY = lxp_rep.getString( Marriage.BRIDE_IDENTITY );
        this.GROOM_MOTHER_IDENTITY = lxp_rep.getString( Marriage.GROOM_MOTHER_IDENTITY );
        this.GROOM_FATHER_IDENTITY = lxp_rep.getString( Marriage.GROOM_FATHER_IDENTITY );
        this.BRIDE_MOTHER_IDENTITY = lxp_rep.getString( Marriage.BRIDE_MOTHER_IDENTITY );
        this.BRIDE_FATHER_IDENTITY = lxp_rep.getString( Marriage.BRIDE_FATHER_IDENTITY );
        this.GROOM_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.GROOM_BIRTH_RECORD_IDENTITY );
        this.BRIDE_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.BRIDE_BIRTH_RECORD_IDENTITY );
        this.GROOM_FATHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.GROOM_FATHER_BIRTH_RECORD_IDENTITY );
        this.GROOM_MOTHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.GROOM_MOTHER_BIRTH_RECORD_IDENTITY );
        this.BRIDE_FATHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY );
        this.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY );
        this.BRIDE_IMMIGRATION_GENERATION = lxp_rep.getString( Marriage.BRIDE_IMMIGRATION_GENERATION );
        this.GROOM_IMMIGRATION_GENERATION = lxp_rep.getString( Marriage.GROOM_IMMIGRATION_GENERATION );
    }


    public boolean equals(Object o) {
        return o instanceof MarriageRecord &&
                ((MarriageRecord) o).getId() == this.getId();
    }

    public int hashCode() {
        return this.getId().hashCode();
    }
}

