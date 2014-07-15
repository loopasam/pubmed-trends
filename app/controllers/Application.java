package controllers;

import java.util.List;
import java.util.regex.Pattern;
import jobs.BuildIndexJob;
import jobs.ComputeIndexDistribution;
import jobs.ComputeMorphiaIF;
import jobs.ComputeNCITdistribution;
import jobs.ComputeOpenIF;
import jobs.ComputeStratifiedFrequencies;
import jobs.ComputeStratifiedFrequencies2;
import jobs.ComputeTrendsJob;
import jobs.DumpIndex;
import jobs.ImportMedlineJob;
import jobs.LoadMongoDb;
import jobs.LoadOntologyJob;
import jobs.LuceneIndexing;
import jobs.LuceneIndexingInDb;
import jobs.LuceneQuery;
import jobs.LuceneStartifiedIndexing;
import jobs.MedlineImportJob;
import jobs.SaveIndexJob;
import models.MorphiaJournal;
import models.MorphiaPhrase;
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

    public static void triggerSearch(String searchtype, String query) {
        Logger.info("Query: " + query + " - Type: " + searchtype);
        //Performs the query on either of the collections depending on searchtype
        MorphiaPhrase concept = MorphiaPhrase.find("value", query).first();
        concept(concept.getIdAsStr());
    }
    
    public static void searchPhrases(String query) {
        //TODO determine how many to keep
        List<MorphiaPhrase> concepts = MorphiaPhrase.find("value", Pattern.compile("^" + query, Pattern.CASE_INSENSITIVE)).limit(3).asList();
        renderJSON(concepts);
    }

    public static void searchJournals(String query) {
        //TODO determine how many to keep
        List<MorphiaJournal> journals = MorphiaJournal.find("issn", Pattern.compile("^" + query, Pattern.CASE_INSENSITIVE)).limit(3).asList();
        renderJSON(journals);
    }

    public static void concept(String id) {
        MorphiaPhrase concept = MorphiaPhrase.findById(id);

        List<MorphiaPhrase> nexts = MorphiaPhrase.filter("trend >", concept.trend).limit(5).asList();
        render(concept, nexts);
    }

    public static void trends(String attr, String sort) {
        String direction = "";
        if (sort.equals("desc")) {
            direction = "-";
        }
        String property = "trend";
        if (attr.equals("volume")) {
            property = "volumetricTrend";
        }

        List<MorphiaPhrase> concepts = MorphiaPhrase.q().filter("trend exists", true).filter("isNew", false).order(direction + property).limit(50).asList();
        render(concepts, attr, sort);
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
        new BuildIndexJob().now();
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
        new ImportMedlineJob().now();
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
        new SaveIndexJob().now();
        index();
    }

    public static void computeTrends() {
        new ComputeTrendsJob().now();
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
