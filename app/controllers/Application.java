package controllers;

import jobs.ComputeIndexDistribution;
import jobs.ComputeMorphiaIF;
import jobs.ComputeNCITdistribution;
import jobs.ComputeOpenIF;
import jobs.ComputeStratifiedFrequencies;
import jobs.ComputeStratifiedFrequencies2;
import jobs.DumpIndex;
import jobs.LoadMongoDb;
import jobs.LoadOntologyJob;
import jobs.LuceneIndexing;
import jobs.LuceneIndexingInDb;
import jobs.LuceneQuery;
import jobs.LuceneStartifiedIndexing;
import jobs.MedlineImportJob;
import models.MorphiaJournal;
import play.Logger;
import play.mvc.*;

public class Application extends Controller {

    public static void conceptDistribution() {
        new ComputeNCITdistribution().now();
        index();
    }

    public static void index() {
        render();
    }
    
    public static void computeMorphiaIF() {
        new ComputeMorphiaIF().now();
        index();
    }
    
    public static void journal(String issn) {
        MorphiaJournal journal = MorphiaJournal.find("issn", issn).first();
        render(journal);
    }
    
    public static void loadMongo() {
        new LoadMongoDb().now();
        index();
    }

    public static void stratifiedIndex() {
        new LuceneStartifiedIndexing().now();
        index();
    }

    public static void loadontologies() {
        new LoadOntologyJob().now();
        index();
    }

    public static void computeIndexDistribution() {
        new ComputeIndexDistribution().now();
        index();
    }

    public static void samplePubLit() {
        new MedlineImportJob().now();
        index();
    }

    public static void luceneIndexing() {
        new LuceneIndexing().now();
        index();
    }

    public static void luceneQuery(String query) {
        new LuceneQuery(query).now();
        index();
    }

    public static void dumpIndex() {
        new DumpIndex().now();
        index();
    }

    public static void indexInDb() {
        new LuceneIndexingInDb().now();
        index();
    }

    public static void computeTrends() {
        new ComputeStratifiedFrequencies().now();
        index();
    }

    public static void computeStratifiedFrequencies() {
        new ComputeStratifiedFrequencies().now();
        index();
    }

    public static void computeStratifiedFrequencies2() {
        new ComputeStratifiedFrequencies2().now();
        index();
    }

    public static void computeOpenIF() {
        new ComputeOpenIF().now();
        index();
    }

}
