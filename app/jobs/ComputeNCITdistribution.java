/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;

import java.util.List;
import java.util.concurrent.TimeUnit;
import models.MorphiaOntologyTerm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;
import utils.CustomStopWordsStandardAnalyzer;
import utils.Utils;

/**
 * TO KEEP
 * Iterates over the concepts of the NCIT and searches for the number of
 * documents indexed. Important in order to estimate how often curated words are present
 * in the wild.
 * @author loopasam
 */
public class ComputeNCITdistribution extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        List<MorphiaOntologyTerm> terms = MorphiaOntologyTerm.findAll();
        
        int total = terms.size();
        int counter = 0;
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-2013").getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);

        //Just chunck the words - no stop word removal - such concept will not give any result in principle
        Analyzer analyzer = new CustomStopWordsStandardAnalyzer(Version.LUCENE_47);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(Version.LUCENE_47, "contents", analyzer);

        for (MorphiaOntologyTerm ontologyTerm : terms) {
            counter++;
            
            Logger.info("i: " + counter + "/" + total);
            Stopwatch timeQuery = Stopwatch.createUnstarted();
            timeQuery.start();
            Query query = parser.parse("\"" + ontologyTerm.value + "\"");
            ScoreDoc[] hits = isearcher.search(query, null, 100000000).scoreDocs;
            timeQuery.stop();
            Logger.info("Query time: " + timeQuery.elapsed(TimeUnit.MILLISECONDS));

            Stopwatch timeUpdate = Stopwatch.createUnstarted();
            timeUpdate.start();
            ontologyTerm.frequency = hits.length;
            ontologyTerm.save();
            timeUpdate.stop();
            Logger.info("Update time: " + timeUpdate.elapsed(TimeUnit.MILLISECONDS));
            Logger.info("Query: " + ontologyTerm.value + " - " + hits.length);
        }

        stopwatch.stop();
        Utils.emailAdmin("Distribution completed", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
        Logger.info("Job finished");

    }

}
