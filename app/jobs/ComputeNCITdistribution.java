/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import models.OntologyTerm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
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
import utils.CustomStandardAnalyzer;
import utils.Utils;

/**
 * Iterates over the concepts of the NCIT and searches for the number of
 * documents indexed.
 *
 * @author loopasam
 */
public class ComputeNCITdistribution extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        List<OntologyTerm> terms = OntologyTerm.findAll();
        int total = terms.size();
        int counter = 0;
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/luceneAbstract").getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);

        //Just chunck the words - no stop word removal - such concept will not give any result in principle
        Analyzer analyzer = new CustomStandardAnalyzer(Version.LUCENE_47);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(Version.LUCENE_47, "contents", analyzer);

//        Map<Long, Integer> freqs = new HashMap<Long, Integer>();
        for (OntologyTerm ontologyTerm : terms) {
            counter++;
            Logger.info("i: " + counter + "/" + total);
            Stopwatch timeQuery = Stopwatch.createUnstarted();
            timeQuery.start();
            Query query = parser.parse("\"" + ontologyTerm.value + "\"");
            ScoreDoc[] hits = isearcher.search(query, null, 1000000000).scoreDocs;
            timeQuery.stop();
            Logger.info("Query time: " + timeQuery.elapsed(TimeUnit.MILLISECONDS));

//            freqs.put(ontologyTerm.id, hits.length);
            Stopwatch timeUpdate = Stopwatch.createUnstarted();
            timeUpdate.start();
            ontologyTerm.updateFrequency(hits.length);
            timeUpdate.stop();
            Logger.info("Update time: " + timeUpdate.elapsed(TimeUnit.MILLISECONDS));
            
            Logger.info("Query: " + ontologyTerm.value + " - " + hits.length);
        }

//        counter = 50;
//        for (Long id : freqs.keySet()) {
//            counter++;
//            OntologyTerm term = OntologyTerm.findById(id);
//            Logger.info("i save: " + counter);
//            int freq = freqs.get(id);
//            term.updateFrequency(freq);
//
//            if (counter % 50 == 0) {
//                OntologyTerm.em().flush();
//                OntologyTerm.em().clear();
//            }
//
//        }
        stopwatch.stop();
        Utils.emailAdmin("Distribution completed", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
        Logger.info("Job finished");

    }

}
