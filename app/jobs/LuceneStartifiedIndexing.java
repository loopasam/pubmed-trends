/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import models.Citation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
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
import play.vfs.VirtualFile;
import utils.Utils;

/**
 * Prepare multiple indexes to perform the trend computation faster afterwards.
 *
 * @author loopasam
 */
public class LuceneStartifiedIndexing extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Stratified indexing started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 5);
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));

        //TODO could possibly do less indexes
        for (int t = now; t >= now - 10; t--) {
            //Create a folder for the index
            VirtualFile.fromRelativePath("/indexes/index-" + t).getRealFile().mkdir();
            Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/indexes/index-" + t).getRealFile());
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
            IndexWriter iwriter = new IndexWriter(directory, config);

            //Retrieve the citations given a year t
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date start = sdf.parse("01/01/" + t);
            Date end = sdf.parse("31/12/" + t);

            //Get all the citations given a year
            List<Citation> citations = Citation.find("created between ? and ?", start, end).fetch();
            int total = citations.size();
            int counter = 0;

            //Iterate over the citations and create the index
            for (Citation citation : citations) {
                counter++;
                Logger.info("i (" + t + "): " + counter + "/" + total);
                Document doc = new Document();
                String contents = "";

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
        Logger.info("Job done!");
        stopwatch.stop();
        Utils.emailAdmin("Stratified index built", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

    }
}
