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

/**
 *
 * @author loopasam
 */
public class ComputeStratifiedFrequencies extends Job {

    IndexSearcher isearcher5y;
    QueryParser parser;

    @Override
    public void doJob() throws Exception {
        Logger.info("trends computation started...");

        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
//        int y1 = now - 1;
        int y5 = now - 5;

        Logger.info("Reading index...");
        Directory directory5y = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + y5).getRealFile());
        DirectoryReader ireader5y = DirectoryReader.open(directory5y);
        Analyzer analyzer5y = new StandardAnalyzer(Version.LUCENE_47);
        this.isearcher5y = new IndexSearcher(ireader5y);
        this.parser = new QueryParser(Version.LUCENE_47, "contents", analyzer5y);

        //Retrieve all the phrases in the database, and compute
        Logger.info("Retrieving phrases...");
        List<Phrase> phrases = Phrase.findAll();
        int total = phrases.size();
        int counter = 0;

        Map<Long, Double> frequencies5y = new HashMap<Long, Double>();

        for (Phrase phrase : phrases) {

            Stopwatch time = Stopwatch.createUnstarted();
            time.start();
            counter++;
            Logger.info("i: " + counter + "/" + total + " (" + phrase.value + ")");
            int frequency5y = query(phrase.value);
            time.stop();
            Logger.info("- Query time: " + time.elapsed(TimeUnit.MILLISECONDS));
            frequencies5y.put(phrase.id, (double) frequency5y);
        }

        ireader5y.close();
        directory5y.close();

        //Batch saving to the database
        Phrase.em().flush();
        Phrase.em().clear();
        counter = 0;
        for (Long id : frequencies5y.keySet()) {

            Phrase phrase = Phrase.findById(id);
            phrase.frequency5y = frequencies5y.get(id);
            phrase.save();
            
            counter++;
            Logger.info("Counter: " + counter);
            
            if (counter % 1000 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }
        }

        Logger.info("Job done.");
    }

    private int query(String queryString) throws Exception {
        Query query = this.parser.parse(queryString);
        ScoreDoc[] hits = this.isearcher5y.search(query, 10000000).scoreDocs;
        int numbers = hits.length;
        return numbers;
    }

}
