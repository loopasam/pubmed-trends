/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
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
 * TO KEEP
 *Iterate over the whole index and compute the frequency distribution of all the shingles.
 * This step is there to know how the shingles distribute and what threshold should be considered.
 * @author loopasam
 */
public class ComputeIndexDistribution extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job started...");
        
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-2013").getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);

        //Returns an error is the field does not exists
        //Do the same for abstract (first if possible)
        Terms terms = SlowCompositeReaderWrapper.wrap(ireader).terms("contents");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        Map<Integer, Integer> distribution = new HashMap<Integer, Integer>();

        while ((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);

            if (!term.contains("_")) {

                int frequency = iterator.docFreq();

                if (distribution.get(frequency) != null) {
                    Integer oldValue = distribution.get(frequency);
                    Integer newValue = oldValue + 1;
                    distribution.put(frequency, newValue);
                } else {
                    distribution.put(frequency, 1);
                }

                Logger.info("Term: " + term + " - freq: " + frequency);
            }
        }

        File file = new File("data/distribution-frequency-in-corpus-2013-shingles.csv");
        StringBuilder sb = new StringBuilder();
        for (Integer frequency : distribution.keySet()) {
            sb.append(frequency).append("\t").append(distribution.get(frequency)).append("\n");
        }
        FileUtils.writeStringToFile(file, sb.toString());
        Logger.info("Job finished.");
    }

}
