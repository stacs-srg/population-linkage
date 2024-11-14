package uk.ac.standrews.cs.population_linkage.aleks;

import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_records.RecordRepository;

public abstract class SiblingOpenTriangleResolver {
    protected static NeoDbCypherBridge bridge;
    protected static RecordRepository record_repository;

    //Various constants for predicates
    protected static final int MAX_AGE_DIFFERENCE  = 24;
    protected static final double DATE_THRESHOLD = 0.5;
    protected static final double NAME_THRESHOLD = 0.5;
    protected static final int BIRTH_INTERVAL = 270;

    protected static String CREATE_SIBLING_QUERY;
    protected static String DELETE_SIBLING_QUERY;



    public SiblingOpenTriangleResolver(String sourceRepo) {
        bridge = Store.getInstance().getBridge();
        record_repository= new RecordRepository(sourceRepo);
    }
}
