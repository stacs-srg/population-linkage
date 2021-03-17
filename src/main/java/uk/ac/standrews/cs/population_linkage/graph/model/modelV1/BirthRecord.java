/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

/**
 * A Birth Record class suitable for inclusion in Neo4J graph model
 * This is a transcription of the LXP model used elsewhere and may be constructed from trhat representation
 */
@NodeEntity
public class BirthRecord extends VitalEventRecord {

    @Property
    public String ORIGINAL_ID;
    @Property
    public String STANDARDISED_ID;
    @Property
    public String LXP_ID;
    @Property
    public String FORENAME;
    @Property
    public String CHANGED_FORENAME;
    @Property
    public String SURNAME;
    @Property
    public String CHANGED_SURNAME;
    @Property
    public String SEX;
    @Property
    public String FATHER_FORENAME;
    @Property
    public String FATHER_SURNAME;
    @Property
    public String MOTHER_FORENAME;
    @Property
    public String MOTHER_SURNAME;
    @Property
    public String MOTHER_MAIDEN_SURNAME;
    @Property
    public String CHANGED_MOTHER_MAIDEN_SURNAME;
    @Property
    public String MOTHER_OCCUPATION;
    @Property
    public String FATHER_OCCUPATION;
    @Property
    public String YEAR_OF_REGISTRATION;
    @Property
    public String ENTRY;
    @Property
    public String REGISTRATION_DISTRICT_SUFFIX;
    @Property
    public String REGISTRATION_DISTRICT_NUMBER;
    @Property
    public String CORRECTED_ENTRY;
    @Property
    public String IMAGE_QUALITY;
    @Property
    public String BIRTH_DAY;
    @Property
    public String BIRTH_MONTH;
    @Property
    public String BIRTH_YEAR;
    @Property
    public String BIRTH_ADDRESS;
    @Property
    public String ILLEGITIMATE_INDICATOR;
    @Property
    public String ADOPTION;
    @Property
    public String PARENTS_DAY_OF_MARRIAGE;
    @Property
    public String PARENTS_MONTH_OF_MARRIAGE;
    @Property
    public String PARENTS_YEAR_OF_MARRIAGE;
    @Property
    public String PARENTS_PLACE_OF_MARRIAGE;
    @Property
    public String PLACE_OF_BIRTH;
    @Property
    public String INFORMANT_DID_NOT_SIGN;
    @Property
    public String INFORMANT;
    @Property
    public String FAMILY;
    @Property
    public String DEATH;
    @Property
    public String FORENAME_CLEAN;
    @Property
    public String SURNAME_CLEAN;
    @Property
    public String FATHER_FORENAME_CLEAN;
    @Property
    public String FATHER_SURNAME_CLEAN;
    @Property
    public String MOTHER_FORENAME_CLEAN;
    @Property
    public String MOTHER_SURNAME_CLEAN;
    @Property
    public String CHILD_IDENTITY;
    @Property
    public String MOTHER_IDENTITY;
    @Property
    public String FATHER_IDENTITY;
    @Property
    public String DEATH_RECORD_IDENTITY;
    @Property
    public String PARENT_MARRIAGE_RECORD_IDENTITY;
    @Property
    public String FATHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String MOTHER_BIRTH_RECORD_IDENTITY;

    public BirthRecord() {
        super();
    }

    public BirthRecord(LXP lxp_rep) throws PersistentObjectException {
        this();
        this.ORIGINAL_ID = lxp_rep.getString( Birth.ORIGINAL_ID );
        this.STANDARDISED_ID = lxp_rep.getString( Birth.STANDARDISED_ID );
        this.LXP_ID = lxp_rep.getThisRef().toString();
        this.FORENAME = lxp_rep.getString( Birth.FORENAME );
        this.CHANGED_FORENAME = lxp_rep.getString( Birth.CHANGED_FORENAME );
        this.SURNAME = lxp_rep.getString( Birth.SURNAME );
        this.CHANGED_SURNAME = lxp_rep.getString( Birth.CHANGED_SURNAME );
        this.SEX = lxp_rep.getString( Birth.SEX );
        this.FATHER_FORENAME = lxp_rep.getString( Birth.FATHER_FORENAME );
        this.FATHER_SURNAME = lxp_rep.getString( Birth.FATHER_SURNAME );
        this.MOTHER_FORENAME = lxp_rep.getString( Birth.MOTHER_FORENAME );
        this.MOTHER_SURNAME = lxp_rep.getString( Birth.MOTHER_SURNAME );
        this.MOTHER_MAIDEN_SURNAME = lxp_rep.getString( Birth.MOTHER_MAIDEN_SURNAME );
        this.CHANGED_MOTHER_MAIDEN_SURNAME = lxp_rep.getString( Birth.CHANGED_MOTHER_MAIDEN_SURNAME );
        this.MOTHER_OCCUPATION = lxp_rep.getString( Birth.MOTHER_OCCUPATION );
        this.FATHER_OCCUPATION = lxp_rep.getString( Birth.FATHER_OCCUPATION );
        this.YEAR_OF_REGISTRATION = lxp_rep.getString( Birth.YEAR_OF_REGISTRATION );
        this.ENTRY = lxp_rep.getString( Birth.ENTRY );
        this.REGISTRATION_DISTRICT_SUFFIX = lxp_rep.getString( Birth.REGISTRATION_DISTRICT_SUFFIX );
        this.REGISTRATION_DISTRICT_NUMBER = lxp_rep.getString( Birth.REGISTRATION_DISTRICT_NUMBER );
        this.CORRECTED_ENTRY = lxp_rep.getString( Birth.CORRECTED_ENTRY );
        this.IMAGE_QUALITY = lxp_rep.getString( Birth.IMAGE_QUALITY );
        this.BIRTH_DAY = lxp_rep.getString( Birth.BIRTH_DAY );
        this.BIRTH_MONTH = lxp_rep.getString( Birth.BIRTH_MONTH );
        this.BIRTH_YEAR = lxp_rep.getString( Birth.BIRTH_YEAR );
        this.BIRTH_ADDRESS = lxp_rep.getString( Birth.BIRTH_ADDRESS );
        this.ILLEGITIMATE_INDICATOR = lxp_rep.getString( Birth.ILLEGITIMATE_INDICATOR );
        this.ADOPTION = lxp_rep.getString( Birth.ADOPTION );
        this.PARENTS_DAY_OF_MARRIAGE = lxp_rep.getString( Birth.PARENTS_DAY_OF_MARRIAGE );
        this.PARENTS_MONTH_OF_MARRIAGE = lxp_rep.getString( Birth.PARENTS_MONTH_OF_MARRIAGE );
        this.PARENTS_YEAR_OF_MARRIAGE = lxp_rep.getString( Birth.PARENTS_YEAR_OF_MARRIAGE );
        this.PARENTS_PLACE_OF_MARRIAGE = lxp_rep.getString( Birth.PARENTS_PLACE_OF_MARRIAGE );
        this.PLACE_OF_BIRTH = lxp_rep.getString( Birth.PLACE_OF_BIRTH );
        this.INFORMANT_DID_NOT_SIGN = lxp_rep.getString( Birth.INFORMANT_DID_NOT_SIGN );
        this.INFORMANT = lxp_rep.getString( Birth.INFORMANT );
        this.FAMILY = lxp_rep.getString( Birth.FAMILY );
        this.DEATH = lxp_rep.getString( Birth.DEATH );
        this.FORENAME_CLEAN = lxp_rep.getString( Birth.FORENAME_CLEAN );
        this.SURNAME_CLEAN = lxp_rep.getString( Birth.SURNAME_CLEAN );
        this.FATHER_FORENAME_CLEAN = lxp_rep.getString( Birth.FATHER_FORENAME_CLEAN );
        this.FATHER_SURNAME_CLEAN = lxp_rep.getString( Birth.FATHER_SURNAME_CLEAN );
        this.MOTHER_FORENAME_CLEAN = lxp_rep.getString( Birth.MOTHER_FORENAME_CLEAN );
        this.MOTHER_SURNAME_CLEAN = lxp_rep.getString( Birth.MOTHER_SURNAME_CLEAN );
        this.CHILD_IDENTITY = lxp_rep.getString( Birth.CHILD_IDENTITY );
        this.MOTHER_IDENTITY = lxp_rep.getString( Birth.MOTHER_IDENTITY );
        this.FATHER_IDENTITY = lxp_rep.getString( Birth.FATHER_IDENTITY );
        this.DEATH_RECORD_IDENTITY = lxp_rep.getString( Birth.DEATH_RECORD_IDENTITY );
        this.PARENT_MARRIAGE_RECORD_IDENTITY = lxp_rep.getString( Birth.PARENT_MARRIAGE_RECORD_IDENTITY );
        this.FATHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Birth.FATHER_BIRTH_RECORD_IDENTITY );
        this.MOTHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Birth.MOTHER_BIRTH_RECORD_IDENTITY );

    }

    public boolean equals(Object o) {
        return o instanceof BirthRecord &&
                ((BirthRecord)o).getId() == this.getId();
    }

    public int hashCode() {
        return this.getId().hashCode();
    }
}
