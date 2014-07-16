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
            if(phrase.frequencyThen != 0){
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
                double stdThen = (double) phrase.frequencyThen / totalDocsThen;
                Logger.info("frequencyThen: " + phrase.frequencyThen);
                Logger.info("stdThen: " + stdThen);
                //trend(c, delta) = ( stdNow - stdThen ) / stdThen
                double trend = (stdNow - stdThen) / stdThen * 100;
                Logger.info("Trend: " + trend);
                double volumetricTrend = trend * phrase.frequencyNow;
                Logger.info("Volumetric trend: " + volumetricTrend);
                phrase.trend = trend;
                phrase.volumetricTrend = volumetricTrend;
                phrase.displayTrend = new DecimalFormat("#.00").format(trend);
            }else{
                phrase.isNew = true;
            }

            phrase.save();
        }

        //Compute the rank
        int rank = 1;

        Logger.info("Computing rank...");
        List<MorphiaPhrase> rankPhrases = MorphiaPhrase.q().filter("isNew", false).order("-trend").asList();
        for (MorphiaPhrase phrase : rankPhrases) {
            phrase.rank = rank;
            phrase.save();
            rank++;
        }

        Logger.info("Job done.");
        stopwatch.stop();
        Utils.emailAdmin("Trends computed", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

    }


}
