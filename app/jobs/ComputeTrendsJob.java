/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import models.MorphiaCitation;
import models.MorphiaPhrase;
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
import utils.Utils;

/**
 * TO KEEP Compute the frequencies of terms for the previous year, as well as
 * the trends.
 *
 * @author loopasam
 */
public class ComputeTrendsJob extends Job {

    IndexSearcher isearcher;
    QueryParser parser;

    @Override
    public void doJob() throws Exception {
        Logger.info("Trends computation started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        //Retrieve the citations given a year t
        Logger.info("Query for documents year " + now + "...");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date start = sdf.parse("01/01/" + now);
        Date end = sdf.parse("31/12/" + now);
        long totalDocsNow = MorphiaCitation.q().filter("created <=", end).filter("created >=", start).count();
        Logger.info("Number of documents: " + totalDocsNow);

        int then = now - 1;
        Logger.info("Query for documents year " + then + "...");
        start = sdf.parse("01/01/" + then);
        end = sdf.parse("31/12/" + then);
        long totalDocsThen = MorphiaCitation.q().filter("created <=", end).filter("created >=", start).count();
        Logger.info("Number of documents: " + totalDocsThen);

        Logger.info("Previous year: " + then);

        Logger.info("Reading index...");
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + then).getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        this.isearcher = new IndexSearcher(ireader);
        this.parser = new QueryParser(Version.LUCENE_47, "contents", analyzer);

        //Retrieve all the phrases in the database, and compute
        Logger.info("Retrieving phrases...");

        List<MorphiaPhrase> phrases = MorphiaPhrase.findAll();
        int total = phrases.size();
        int counter = 0;

        for (MorphiaPhrase phrase : phrases) {
            Stopwatch time = Stopwatch.createUnstarted();
            time.start();
            counter++;
            Logger.info("i: " + counter + "/" + total + " (" + phrase.value + ")");
            int frequencyThen = query("\"" + phrase.value + "\"");
            time.stop();
            Logger.info("- Query time: " + time.elapsed(TimeUnit.MILLISECONDS));

            if (frequencyThen == 0) {
                phrase.isNew = true;
            }

            //TODO put in other class SaveIndexJob
            //std(c, t) = doc(c, t) / doc(t)
            //Trend: ( std(c, now) - std(c, then) ) / std(c, then)
            //Volumetric: trend(c, delta) * doc(c, now)
            //doc(now) = totalDocsNow
            //doc(then) = totalDocsThen
            //doc(c, now) = frequencyNow
            //doc(c, then) = frequencyThen
            //std(c, now) = frequencyNow / totalDocsNow = stdNow
            double stdNow = (double) phrase.frequencyNow / totalDocsNow;
            Logger.info("phrase.frequencyNow: " + phrase.frequencyNow);
            Logger.info("stdNow: " + stdNow);
            //std(c, then) = frequencyThen / totalDocsThen = stdThen
            double stdThen = (double) frequencyThen / totalDocsThen;
            Logger.info("frequencyThen: " + frequencyThen);
            Logger.info("stdThen: " + stdThen);
            //trend(c, delta) = ( stdNow - stdThen ) / stdThen
            double trend = (stdNow - stdThen) / stdThen * 100;
            Logger.info("Trend: " + trend);
            double volumetricTrend = trend * phrase.frequencyNow;
            Logger.info("Volumetric trend: " + volumetricTrend);

            phrase.trend = trend;
            phrase.frequencyThen = frequencyThen;
            phrase.volumetricTrend = volumetricTrend;
            phrase.displayTrend = new DecimalFormat("#.00").format(trend);
            phrase.save();
        }

        ireader.close();
        directory.close();

        //Compute the rank
        int rank = 1;

        Logger.info("Computing rank...");
        phrases = MorphiaPhrase.q().filter("trend exists", true).order("-trend").asList();
        for (MorphiaPhrase phrase : phrases) {
            phrase.rank = rank;
            phrase.save();
            rank++;
        }

        Logger.info("Job done.");
        stopwatch.stop();
        Utils.emailAdmin("Trends computed", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

    }

    private int query(String queryString) throws Exception {
        Query query = this.parser.parse(queryString);
        ScoreDoc[] hits = this.isearcher.search(query, 10000000).scoreDocs;
        int numbers = hits.length;
        return numbers;
    }

}
