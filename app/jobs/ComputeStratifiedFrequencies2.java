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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;
import utils.Utils;

/**
 *
 * @author loopasam
 */
public class ComputeStratifiedFrequencies2 extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("trends computation started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        int y5 = now - 5;

        //iterate over all the years and save the values
        Logger.info("Reading index...");
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + y5).getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);

        Terms terms = SlowCompositeReaderWrapper.wrap(ireader).terms("contents");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        Map<Long, Double> frequencies = new HashMap<Long, Double>();

        while ((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            if (!term.contains("_")) {
                Logger.info("Term: " + term);
                Stopwatch time = Stopwatch.createUnstarted();
                time.start();

                Phrase phrase = Phrase.find("byValue", term).first();
                if (phrase != null) {
                    Logger.info("Term: " + phrase.value + " (" + term + ")");
                    int frequency = iterator.docFreq();
                    frequencies.put(phrase.id, (double) frequency);
                }
                time.stop();
                Logger.info("- Query time: " + time.elapsed(TimeUnit.MILLISECONDS));
            }
        }

        ireader.close();
        directory.close();

        Phrase.em().flush();
        Phrase.em().clear();
        int counter = 0;
        for (Long id : frequencies.keySet()) {
            Phrase phrase = Phrase.findById(id);
            phrase.frequency5y = frequencies.get(id);
            phrase.save();
            counter++;
            Logger.info("Counter: " + counter);

            if (counter % 1000 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }
        }

        stopwatch.stop();
        Utils.emailAdmin("Yearly frequency calculated. ", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

        Logger.info("Job done.");

    }

}
