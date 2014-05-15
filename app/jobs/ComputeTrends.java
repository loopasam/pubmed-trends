/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ComputeTrends extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("trends computation started...");

        List<Phrase> phrases = Phrase.findAll();

        //total articles t(now) - 2013
        int totalArticleTnow = query("date:[20130101 TO 20131231]");
        Logger.info("total now: " + totalArticleTnow);

        //total articles t(now - 5)
        int totalArticleT5years = query("date:[20080101 TO 20081231]");
        Logger.info("total 5 years: " + totalArticleT5years);

        int total = phrases.size();
        int counter = 0;

        //TODO First save them in memory, then in the database
        Map<Long, Double> trends = new HashMap<Long, Double>();

        for (Phrase phrase : phrases) {
//        for (int i = 0; i < 1000; i++) {
//            Phrase phrase = phrases.get(i);

            counter++;
            Logger.info("i: " + counter + "/" + total);

            //volume t(now)
            int volumeNow = query("title:\"" + phrase.value + "\" AND date:[20130101 TO 20131231]");
            //Logger.info("volume now: " + volumeNow);

            //volume t(now - 5)
            int volume5years = query("title:\"" + phrase.value + "\" AND date:[20080101 TO 20081231]");
            //Logger.info("volume 5 years: " + volume5years);

            //standardised volume t(now)
            double standardisedVolumeNow = (double) volumeNow / (double) totalArticleTnow;
            //Logger.info("standardised volume now: " + standardisedVolumeNow);

            //standardised volume t(now - 5)
            double standardisedVolume5years = (double) volume5years / (double) totalArticleT5years;
            //Logger.info("standardised volume 5 years: " + standardisedVolume5years);

            //Trend
            //Handles infinite results better than that
            if (standardisedVolume5years != 0) {
                double trend5years = (standardisedVolumeNow - standardisedVolume5years) / standardisedVolume5years;
                
                Logger.info("Trend: " + trend5years);
                trends.put(phrase.id, trend5years);
            }
        }

        total = trends.keySet().size();
        counter = 0;
        for (Long id : trends.keySet()) {
            counter++;
            Logger.info("i: " + counter + "/" + total);
            Phrase phrase = Phrase.findById(id);
            phrase.trend5years = trends.get(id);
            phrase.save();

            if (counter % 100 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }

        }
        Logger.info("Job done.");
    }

    private int query(String queryString) throws Exception {

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(Version.LUCENE_47, "<default field>", analyzer);
        Query query = parser.parse(queryString);

        ScoreDoc[] hits = isearcher.search(query, 10000000).scoreDocs;
        int numbers = hits.length;
        ireader.close();
        directory.close();
        return numbers;
    }

}
