/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import models.Phrase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import utils.Utils;

/**
 *
 * @author loopasam
 */
public class ComputeStratifiedFrequencies extends Job {

    IndexSearcher isearcher;
    QueryParser parser;

    @Override
    public void doJob() throws Exception {
        Logger.info("Frequency computation started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        int y1 = now - 1;
        Logger.info("Previous year: " + y1);

        Logger.info("Reading index...");
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + y1).getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        this.isearcher = new IndexSearcher(ireader);
        this.parser = new QueryParser(Version.LUCENE_47, "contents", analyzer);

        //Retrieve all the phrases in the database, and compute
        Logger.info("Retrieving phrases...");
        List<Phrase> phrases = Phrase.findAll();
        int total = phrases.size();
        int counter = 0;

        Map<Long, Double> frequencies = new HashMap<Long, Double>();

        for (Phrase phrase : phrases) {

            Stopwatch time = Stopwatch.createUnstarted();
            time.start();
            counter++;
            Logger.info("i: " + counter + "/" + total + " (" + phrase.value + ")");
            int frequency = query(phrase.value);
            time.stop();
            Logger.info("- Query time: " + time.elapsed(TimeUnit.MILLISECONDS));
            
            Stopwatch save = Stopwatch.createUnstarted();
            save.start();
            phrase.frequency1y = frequency;
            phrase.save();
            save.stop();
            Logger.info("- Saving time: " + save.elapsed(TimeUnit.MILLISECONDS));
            
            //Try to save it to debug
            //frequencies.put(phrase.id, (double) frequency);
        }

        //Phrase.em().flush();
        //Phrase.em().clear();
//        counter = 0;
//        for (Long id : frequencies.keySet()) {
//
//            Phrase phrase = Phrase.findById(id);
//            phrase.frequency1y = frequencies.get(id);
//            phrase.save();
//
//            counter++;
//            Logger.info("Counter: " + counter);
//
//            if (counter % 1000 == 0) {
//                Phrase.em().flush();
//                Phrase.em().clear();
//            }
//        }

        ireader.close();
        directory.close();

        Logger.info("Job done.");
        stopwatch.stop();
        Utils.emailAdmin("Stratified index built", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

    }

    private int query(String queryString) throws Exception {
        Query query = this.parser.parse(queryString);
        ScoreDoc[] hits = this.isearcher.search(query, 10000000).scoreDocs;
        int numbers = hits.length;
        return numbers;
    }

}
