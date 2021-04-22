/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph.util;

import org.neo4j.ogm.session.Session;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.graph.model.BirthRecord;
import uk.ac.standrews.cs.population_linkage.graph.model.DeathRecord;
import uk.ac.standrews.cs.population_linkage.graph.model.MarriageRecord;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

import java.nio.file.Path;

public class LoadNeo4JVitalEventsFromStorr implements AutoCloseable {

    private final NeoDbOGMBridge bridge = new NeoDbOGMBridge();
    private final Session session;

    protected Iterable<LXP> birth_records;
    protected Iterable<LXP> marriage_records;
    protected Iterable<LXP> death_records;
    private final RecordRepository record_repository;
    protected final String source_repository_name;
    protected Path store_path;

    public LoadNeo4JVitalEventsFromStorr(String source_repository_name )
    {
        session = bridge.getNewSession();
        this.source_repository_name = source_repository_name;

        store_path = ApplicationProperties.getStorePath();
        this.record_repository = new RecordRepository(store_path, source_repository_name);

        birth_records = Utilities.getBirthRecords(record_repository);
        marriage_records = Utilities.getMarriageRecords(record_repository);
        death_records = Utilities.getDeathRecords(record_repository);
    }

    public void close() throws Exception
    {
        bridge.close();
    }

    public void make() throws PersistentObjectException {

        int count = 0;
        for (LXP record : birth_records) {
            BirthRecord neo_record = new BirthRecord( record  );
            session.save( neo_record );
            count++;
        }
        System.out.println( "Saved " + count + " birth records" );

        for (LXP record : death_records) {
            DeathRecord neo_record = new DeathRecord( record );
            session.save( neo_record );
            count++;
        }
        System.out.println( "Saved  " + count + " death records" );

        for (LXP record : marriage_records) {
            MarriageRecord neo_record = new MarriageRecord( record );
            session.save( neo_record );
            count++;
        }
        System.out.println( "Saved " + count + " marriage records" );
    }


    public static void main( String... args ) throws Exception
    {
        try ( LoadNeo4JVitalEventsFromStorr db = new LoadNeo4JVitalEventsFromStorr( "umea" ) )
        {
            System.out.println( "CREATING vital event records" );
            db.make();
            System.out.println( "COMPLETED creation of vital event records" );
        }
    }


}
