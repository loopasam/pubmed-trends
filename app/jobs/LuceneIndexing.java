package jobs;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.TimeUnit;
import models.Citation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;

public class LuceneIndexing extends Job {

    private final static int STEP = 10000;

    @Override
    public void doJob() throws Exception {

        Logger.info("Indexing started...");

//        Analyzer analyzer = new CustomStandardAnalyzer(Version.LUCENE_47);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 5);

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/luceneAbstract").getRealFile());
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
        //config.setRAMBufferSizeMB(64);
        IndexWriter iwriter = new IndexWriter(directory, config);

        //Iterate over the citations by packs of 1000
        //The total number as now is: 23772097
        //long totalCitations = Citation.count();

        int totalCitations = 23772097;
        //Add time information for when the data is fetched from the database        
        for (int i = 0; i < 20000; i += STEP) {

            Logger.info("i: " + i + "/" + totalCitations);
            Stopwatch stopwatchdb = Stopwatch.createUnstarted();
            stopwatchdb.start();
            List<Citation> citations = Citation.all().from(i).fetch(STEP);
            stopwatchdb.stop();
            Logger.info("Time to query the DB: " + stopwatchdb.elapsed(TimeUnit.SECONDS));

            Stopwatch stopwatchindex = Stopwatch.createUnstarted();
            stopwatchindex.start();

            for (Citation citation : citations) {

                Document doc = new Document();
                String contents = "";

                if (citation.abstractText != null) {
                    contents += citation.abstractText;
//                    doc.add(new Field("abstract", citation.abstractText, TextField.TYPE_STORED));
                }

                if (citation.title != null) {
                    contents += citation.title;
                    //doc.add(new Field("title", citation.title, TextField.TYPE_STORED));
                }

                if (!contents.equals("")) {
                    doc.add(new Field("contents", contents, TextField.TYPE_STORED));
                }
                
                doc.add(new Field("date", DateTools.dateToString(citation.created, DateTools.Resolution.MINUTE), TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }

            stopwatchindex.stop();
            Logger.info("Time to index the documents: " + stopwatchindex.elapsed(TimeUnit.SECONDS));
            
        }

        iwriter.close();

        Logger.info("index done.");
    }
}
