/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import models.Phrase;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;
import utils.Utils;

/**
 *
 * @author loopasam
 */
public class LuceneIndexingInDb extends Job {

    //Defined from analysis over term frequency distribution
    //Limits to just under 577'214 terms for the year 2013
    private static final int FREQ_TRESHOLD = 24;

    @Override
    public void doJob() throws Exception {

        Logger.info("Saving index in DB...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-2013").getRealFile());

        DirectoryReader ireader = DirectoryReader.open(directory);
        //Returns an error is the field does not exists
        //Do the same for abstract (first if possible)
        Terms terms = SlowCompositeReaderWrapper.wrap(ireader).terms("contents");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        int counter = 0;

        while ((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);

            //Could be used later
            int frequency = iterator.docFreq();
            //Save to DB
            //check if exists alread, if yes increase the counter, otherwise create
            //Saves only the terms with high frequency
            //Removes the terms with a _ (from shigle index) and 4 decimals (dates)

            if (frequency > FREQ_TRESHOLD && !term.contains("_")) {
                new Phrase(term, frequency).save();
                Logger.info("Term: " + term + " - freq: " + frequency);
            }

            if (counter % 1000 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }

        }
        stopwatch.stop();
        Utils.emailAdmin("Indexing in DB done. ", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

        Logger.info("index done and saved.");
    }
}
