/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.model.modelV1;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

/**
 * A Death Record class suitable for inclusion in Neo4J graph model
 * This is a transcription of the LXP model used elsewhere and may be constructed from trhat representation
 */
@NodeEntity
public class DeathRecord extends VitalEventRecord {

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
    public String CHANGED_MOTHERS_MAIDEN_SURNAME;
    @Property
    public String FATHER_OCCUPATION;
    @Property
    public String MOTHER_OCCUPATION;
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
    public String COD_A;
    @Property
    public String COD_B;
    @Property
    public String COD_C;
    @Property
    public String PLACE_OF_DEATH;
    @Property
    public String DATE_OF_BIRTH;
    @Property
    public String DEATH_DAY;
    @Property
    public String DEATH_MONTH;
    @Property
    public String DEATH_YEAR;
    @Property
    public String AGE_AT_DEATH;
    @Property
    public String CHANGED_DEATH_AGE;
    @Property
    public String OCCUPATION;
    @Property
    public String MARITAL_STATUS;
    @Property
    public String SPOUSE_NAMES;
    @Property
    public String SPOUSE_OCCUPATION;
    @Property
    public String MOTHER_DECEASED;
    @Property
    public String FATHER_DECEASED;
    @Property
    public String CERTIFYING_DOCTOR;
    @Property
    public String DECEASED_IDENTITY;
    @Property
    public String MOTHER_IDENTITY;
    @Property
    public String FATHER_IDENTITY;
    @Property
    public String SPOUSE_IDENTITY;
    @Property
    public String BIRTH_RECORD_IDENTITY;
    @Property
    public String PARENT_MARRIAGE_RECORD_IDENTITY;
    @Property
    public String FATHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String MOTHER_BIRTH_RECORD_IDENTITY;
    @Property
    public String SPOUSE_MARRIAGE_RECORD_IDENTITY;
    @Property
    public String SPOUSE_BIRTH_RECORD_IDENTITY;
    @Property
    public String IMMIGRATION_GENERATION;


    public DeathRecord() {
        super();
    }

    public DeathRecord(LXP lxp_rep) throws PersistentObjectException {
        this();
        this.ORIGINAL_ID = lxp_rep.getString( Death.ORIGINAL_ID );
        this.STANDARDISED_ID = lxp_rep.getString( Death.STANDARDISED_ID );
        this.LXP_ID = lxp_rep.getThisRef().toString();
        this.FORENAME = lxp_rep.getString( Death.FORENAME );
        this.CHANGED_FORENAME = lxp_rep.getString( Death.CHANGED_FORENAME );
        this.SURNAME = lxp_rep.getString( Death.SURNAME );
        this.CHANGED_SURNAME = lxp_rep.getString( Death.CHANGED_SURNAME );
        this.SEX = lxp_rep.getString( Death.SEX );
        this.FATHER_FORENAME = lxp_rep.getString( Death.FATHER_FORENAME );
        this.FATHER_SURNAME = lxp_rep.getString( Death.FATHER_SURNAME );
        this.MOTHER_FORENAME = lxp_rep.getString( Death.MOTHER_FORENAME );
        this.MOTHER_SURNAME = lxp_rep.getString( Death.MOTHER_SURNAME );
        this.MOTHER_MAIDEN_SURNAME = lxp_rep.getString( Death.MOTHER_MAIDEN_SURNAME );
        this.CHANGED_MOTHERS_MAIDEN_SURNAME = lxp_rep.getString( Death.CHANGED_MOTHERS_MAIDEN_SURNAME );
        this.FATHER_OCCUPATION = lxp_rep.getString( Death.FATHER_OCCUPATION );
        this.MOTHER_OCCUPATION = lxp_rep.getString( Death.MOTHER_OCCUPATION );
        this.YEAR_OF_REGISTRATION = lxp_rep.getString( Death.YEAR_OF_REGISTRATION );
        this.ENTRY = lxp_rep.getString( Death.ENTRY );
        this.REGISTRATION_DISTRICT_SUFFIX = lxp_rep.getString( Death.REGISTRATION_DISTRICT_SUFFIX );
        this.REGISTRATION_DISTRICT_NUMBER = lxp_rep.getString( Death.REGISTRATION_DISTRICT_NUMBER );
        this.CORRECTED_ENTRY = lxp_rep.getString( Death.CORRECTED_ENTRY );
        this.IMAGE_QUALITY = lxp_rep.getString( Death.IMAGE_QUALITY );
        this.COD_A = lxp_rep.getString( Death.COD_A );
        this.COD_B = lxp_rep.getString( Death.COD_B );
        this.COD_C = lxp_rep.getString( Death.COD_C );
        this.PLACE_OF_DEATH = lxp_rep.getString( Death.PLACE_OF_DEATH );
        this.DATE_OF_BIRTH = lxp_rep.getString( Death.DATE_OF_BIRTH );
        this.DEATH_DAY = lxp_rep.getString( Death.DEATH_DAY );
        this.DEATH_MONTH = lxp_rep.getString( Death.DEATH_MONTH );
        this.DEATH_YEAR = lxp_rep.getString( Death.DEATH_YEAR );
        this.AGE_AT_DEATH = lxp_rep.getString( Death.AGE_AT_DEATH );
        this.CHANGED_DEATH_AGE = lxp_rep.getString( Death.CHANGED_DEATH_AGE );
        this.OCCUPATION = lxp_rep.getString( Death.OCCUPATION );
        this.MARITAL_STATUS = lxp_rep.getString( Death.MARITAL_STATUS );
        this.SPOUSE_NAMES = lxp_rep.getString( Death.SPOUSE_NAMES );
        this.SPOUSE_OCCUPATION = lxp_rep.getString( Death.SPOUSE_OCCUPATION );
        this.MOTHER_DECEASED = lxp_rep.getString( Death.MOTHER_DECEASED );
        this.FATHER_DECEASED = lxp_rep.getString( Death.FATHER_DECEASED );
        this.CERTIFYING_DOCTOR = lxp_rep.getString( Death.CERTIFYING_DOCTOR );
        this.DECEASED_IDENTITY = lxp_rep.getString( Death.DECEASED_IDENTITY );
        this.MOTHER_IDENTITY = lxp_rep.getString( Death.MOTHER_IDENTITY );
        this.FATHER_IDENTITY = lxp_rep.getString( Death.FATHER_IDENTITY );
        this.SPOUSE_IDENTITY = lxp_rep.getString( Death.SPOUSE_IDENTITY );
        this.BIRTH_RECORD_IDENTITY = lxp_rep.getString( Death.BIRTH_RECORD_IDENTITY );
        this.PARENT_MARRIAGE_RECORD_IDENTITY = lxp_rep.getString( Death.PARENT_MARRIAGE_RECORD_IDENTITY );
        this.FATHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Death.FATHER_BIRTH_RECORD_IDENTITY );
        this.MOTHER_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Death.MOTHER_BIRTH_RECORD_IDENTITY );
        this.SPOUSE_MARRIAGE_RECORD_IDENTITY = lxp_rep.getString( Death.SPOUSE_MARRIAGE_RECORD_IDENTITY );
        this.SPOUSE_BIRTH_RECORD_IDENTITY = lxp_rep.getString( Death.SPOUSE_BIRTH_RECORD_IDENTITY );
        this.IMMIGRATION_GENERATION = lxp_rep.getString( Death.IMMIGRATION_GENERATION );
    }


    public boolean equals(Object o) {
        return o instanceof DeathRecord &&
                ((DeathRecord)o).getId() == this.getId();
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

}
