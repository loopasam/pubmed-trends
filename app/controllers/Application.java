package controllers;

import jobs.ComputeIndexDistribution;
import jobs.ComputeNCITdistribution;
import jobs.ComputeTrends;
import jobs.DumpIndex;
import jobs.LoadOntologyJob;
import jobs.LuceneIndexing;
import jobs.LuceneIndexingInDb;
import jobs.LuceneQuery;
import jobs.LuceneStartifiedIndexing;
import jobs.SampleJob;
import jobs.SimpleIndexJob;
import play.mvc.*;

public class Application extends Controller {

    public static void conceptDistribution() {
        new ComputeNCITdistribution().now();
        index();
    }

    public static void index() {
        render();
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
        new SampleJob().now();
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

    public static void simpleIndex() {
        new SimpleIndexJob().now();
        index();
    }

    public static void indexInDb() {
        new LuceneIndexingInDb().now();
        index();
    }

    public static void computeTrends() {
        new ComputeTrends().now();
        index();
    }

}
