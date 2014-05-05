/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Phrase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
public class NewConceptIdentifier extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("New concept computation started...");

        List<Phrase> phrases = Phrase.findAll();

        int total = phrases.size();
        int counter = 0;

        //TODO First save them in memory, then in the database
        Map<Long, Integer> trends = new HashMap<Long, Integer>();

        for (Phrase phrase : phrases) {
//        for (int i = 0; i < 1000; i++) {
//            Phrase phrase = phrases.get(i);

            counter++;
            Logger.info("i: " + counter + "/" + total);

            int lowerTreshold = 1990;
            //Check for each year (2013 - 1990) till it's found
            for (int i = 2012; i >= 1990; i--) {
                int previousYears = query("title:\"" + phrase.value + "\" AND date:[" + lowerTreshold + "0101 TO " + i + "1231]");

                if (previousYears == 0) {
                    //Date is found, add, save and continue
                    int apparitionDate = i + 1;
                    Logger.info("Discovery date: " + phrase.value + " - " + apparitionDate);
                    trends.put(phrase.id, apparitionDate);
                    break;
                }
            }
        }

        total = trends.keySet().size();
        counter = 0;
        for (Long id : trends.keySet()) {
            counter++;
            Logger.info("i: " + counter + "/" + total);
            Phrase phrase = Phrase.findById(id);
            phrase.apparitionDate = trends.get(id);
            phrase.save();

            if (counter % 100 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }

        }

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
