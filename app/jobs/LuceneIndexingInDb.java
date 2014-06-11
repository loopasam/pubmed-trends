/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import models.Citation;
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

/**
 *
 * @author loopasam
 */
public class LuceneIndexingInDb extends Job {

    //Defined from analysis over term frequency distribution
    //Limits to just under 2'000'000

    private static final int FREQ_TRESHOLD = 15;

    @Override
    public void doJob() throws Exception {

        Logger.info("Saving index in DB...");

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/luceneAbstract").getRealFile());

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
            
            if (frequency > FREQ_TRESHOLD && !term.contains("_") && !term.matches(".*\\d{4}.*")) {
                new Phrase(term, frequency).save();
                Logger.info("Term: " + term + " - freq: " + frequency);
            }

            if (counter % 1000 == 0) {
                Phrase.em().flush();
                Phrase.em().clear();
            }

        }

        Logger.info("index done and saved.");
    }
}
