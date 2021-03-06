/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import models.Citation;
import models.MorphiaCitation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.modules.morphia.Model;
import play.vfs.VirtualFile;
import utils.CustomStopWordsStandardAnalyzer;
import utils.Utils;

/**
 * TO KEEP Compute the stratified indexes, high memory job
 *
 * @author loopasam
 */
public class BuildIndexJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Computing the indexes...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        CharArraySet stopSet = CharArraySet.copy(Version.LUCENE_47, StandardAnalyzer.STOP_WORDS_SET);
        stopSet.add("we");
        stopSet.add("pmid");
        stopSet.add("were");
        stopSet.add("from");
        stopSet.add("reply");
        stopSet.add("can");
        stopSet.add("between");
        stopSet.add("using");
        stopSet.add("used");
        stopSet.add("however");
        stopSet.add("which");
        stopSet.add("our");
        stopSet.add("among");
        stopSet.add("while");
        stopSet.add("this");


        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47, stopSet);
//        Analyzer analyzer = new CustomStopWordsStandardAnalyzer(Version.LUCENE_47);
        //Does [1-5]-grams, as determined by previous graphs
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 5);
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));

        for (int t = now; t >= now - 1; t--) {
            //Create a folder for the index
            //TODO delete the folders first
            VirtualFile.fromRelativePath("/indexes/index-" + t).getRealFile().mkdir();
            Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + t).getRealFile());
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
            IndexWriter iwriter = new IndexWriter(directory, config);

            //Retrieve the citations given a year t
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date start = sdf.parse("01/01/" + t);
            Date end = sdf.parse("31/12/" + t);

            Logger.info("Query for year " + t + "...");
            List<MorphiaCitation> citations
                    = MorphiaCitation.q().filter("created <=", end).filter("created >=", start).asList();

            Logger.info("Citation size: " + citations.size());
            int total = citations.size();
            int counter = 0;

            //Iterate over the citations and create the index
            for (MorphiaCitation citation : citations) {
                counter++;
                Logger.info("i (" + t + "): " + counter + "/" + total);
                Document doc = new Document();
                String contents = "";

                doc.add(new Field("pmid", citation.pmid, TextField.TYPE_STORED));

                if (citation.abstractText != null) {
                    contents += citation.abstractText;
                }

                if (citation.title != null) {
                    contents += citation.title;
                }

                if (!contents.equals("")) {
                    doc.add(new Field("contents", contents, TextField.TYPE_STORED));
                }

                iwriter.addDocument(doc);

            }
            iwriter.close();
        }
        stopwatch.stop();
        Utils.emailAdmin("Indexing done. ", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
        Logger.info("Time to index the documents: " + stopwatch.elapsed(TimeUnit.MINUTES));
    }

}
