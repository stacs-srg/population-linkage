package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.investigating;

public class SyntheticBirthRecord {

    public String id;

    public String forename;
    public String surname;

    public String fathersForename;
    public String fathersSurname;

    public String mothersForename;
    public String mothersMaidenSurname;

    public String parentsYearOfMarriage;
    public String parentsPlaceOfMarriage;

    public String familyID;

    public SyntheticBirthRecord(String forename, String surname,
                                String fathersForename, String fathersSurname,
                                String mothersForename, String mothersMaidenSurname,
                                String parentsYearOfMarriage, String parentsPlaceOfMarriage, String familyID, String childID) {

        this.forename = forename;
        this.surname = surname;

        this.fathersForename = fathersForename;
        this.fathersSurname = fathersSurname;

        this.mothersForename = mothersForename;
        this.mothersMaidenSurname = mothersMaidenSurname;

        this.parentsYearOfMarriage = parentsYearOfMarriage;
        this.parentsPlaceOfMarriage = parentsPlaceOfMarriage;

        this.familyID = familyID;
        this.id = childID;

    }

    public boolean equals(Object obj) {

        if(!(obj instanceof SyntheticBirthRecord)) {
            return false;
        } else {
            return ((SyntheticBirthRecord) obj).id.equals(id);
        }

    }

    public String toString() {
        return id + " - " + forename + " " + surname + " | " + fathersForename + " " + fathersSurname + " | " +
                mothersForename + " " + mothersMaidenSurname + " | " + parentsPlaceOfMarriage + " | " +
                parentsYearOfMarriage + " | " + familyID + "\n";
    }

    public String FFN_FSN() {
        return fathersForename + "_" + fathersSurname;
    }

    public String FFN_MFN_FSN_MMSN() {
        return fathersForename + "_" + mothersForename + "_" + fathersSurname + "_" + mothersMaidenSurname;
    }

    public String FFN_MFN_FSN_MMSN_PYM() {
        return FFN_MFN_FSN_MMSN() + "_" + parentsYearOfMarriage;
    }

    public String FFN_MFN_FSN_MMSN_PYM_PPM() {
        return FFN_MFN_FSN_MMSN_PYM() + "_" + parentsPlaceOfMarriage;
    }

}
