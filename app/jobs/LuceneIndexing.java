package jobs;

import java.util.List;
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
import utils.CustomStandardAnalyzer;

public class LuceneIndexing extends Job {

    private final static int STEP = 1000;

    @Override
    public void doJob() throws Exception {

        Logger.info("Indexing started...");

        Analyzer analyzer = new CustomStandardAnalyzer(Version.LUCENE_47);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        //Iterate over the citations by packs of 1000
        //The total number as now is: 23772097
        long totalCitations = Citation.count();

        for (int i = 0; i < totalCitations; i += STEP) {

            Logger.info("i: " + i + "/" + totalCitations);
            List<Citation> citations = Citation.all().from(i).fetch(STEP);
            for (Citation citation : citations) {

                Document doc = new Document();
                if (citation.abstractText != null) {
                    doc.add(new Field("abstract", citation.abstractText, TextField.TYPE_STORED));
                }

                if (citation.title != null) {
                    doc.add(new Field("title", citation.title, TextField.TYPE_STORED));
                }
                doc.add(new Field("date", DateTools.dateToString(citation.created, DateTools.Resolution.MINUTE), TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }
        }

        iwriter.close();

        Logger.info("index done.");
    }
}
