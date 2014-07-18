/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import models.MorphiaPhrase;
import models.Phrase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.test.MorphiaFixtures;
import play.vfs.VirtualFile;
import utils.Utils;

/**
 * TO KEEP Extract the phrases from the index and store them in the database for
 * further use (trends calculation).
 *
 * @author loopasam
 */
public class SaveIndexJob extends Job {

    //Defined from analysis over term frequency distribution
    //Limits to just under 577'214 terms for the year 2013
    private static final int FREQ_TRESHOLD = 24;

    @Override
    public void doJob() throws Exception {
        Logger.info("Saving index in DB...");
        MorphiaFixtures.delete(MorphiaPhrase.class);
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        //TODO do not hardcode the date
        Directory directoryNow = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-2013").getRealFile());
        DirectoryReader ireaderNow = DirectoryReader.open(directoryNow);

        Terms termsNow = SlowCompositeReaderWrapper.wrap(ireaderNow).terms("contents");
        TermsEnum iteratorNow = termsNow.iterator(null);
        BytesRef byteRef;

        int counter = 0;

        while ((byteRef = iteratorNow.next()) != null) {
            counter++;
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            int frequency = iteratorNow.docFreq();
            //Saves only the terms with high frequency
            //Removes the terms with a _ (from shingle index)
            if (frequency > FREQ_TRESHOLD && !term.contains("_") && !term.matches(".*\\d\\s.*") && !term.matches(".*\\s\\d.*")) {
                new MorphiaPhrase(term, frequency).save();
                Logger.info("Term Now (" + counter + "): " + term + " - freq: " + frequency);
            }
        }

        ireaderNow.close();
        directoryNow.close();

        //Compares against the index of previous year
        Directory directoryThen = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-2012").getRealFile());
        DirectoryReader ireaderThen = DirectoryReader.open(directoryThen);

        Terms termsThen = SlowCompositeReaderWrapper.wrap(ireaderThen).terms("contents");
        TermsEnum iteratorThen = termsThen.iterator(null);
        BytesRef byteRefThen;

        counter = 0;

        while ((byteRefThen = iteratorThen.next()) != null) {
            counter++;
            String term = new String(byteRefThen.bytes, byteRefThen.offset, byteRefThen.length);
            int frequency = iteratorThen.docFreq();
            //Saves only the terms with high frequency
            //Removes the terms with a _ (from shingle index)
            if (frequency > FREQ_TRESHOLD && !term.contains("_") && !term.matches(".*\\d\\s.*") && !term.matches(".*\\s\\d.*")) {
                MorphiaPhrase phrase = MorphiaPhrase.find("value", term).first();
                if(phrase != null){
                    phrase.frequencyThen = frequency;
                    phrase.save();
                    //TODO compute OIF here
                    Logger.info("Term Then (" + counter + "): " + term + " - freq: " + frequency);
                }
            }
        }

        ireaderThen.close();
        directoryThen.close();

        stopwatch.stop();
        Utils.emailAdmin("Indexing in DB done. ", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

        Logger.info("index done and saved.");

    }

}
