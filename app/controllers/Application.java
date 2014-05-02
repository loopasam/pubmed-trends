package controllers;

import play.mvc.*;


import jobs.DumpIndex;
import jobs.LuceneIndexing;
import jobs.LuceneQuery;
import jobs.SampleJob;


public class Application extends Controller {

    public static void index() {
        render();
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
}