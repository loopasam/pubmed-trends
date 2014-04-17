package jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.Citation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
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
import utils.CustomStandardAnalyzer;

public class LuceneIndexing extends Job {

	public void doJob() throws IOException, ParseException{

		Logger.info("Indexing started...");		

		Analyzer analyzer = new CustomStandardAnalyzer(Version.LUCENE_47);
		
		ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);

		Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);

				List<Citation> citations = Citation.findAll();
				int total = citations.size();
				int counter = 1;
				
				for (Citation citation : citations) {
					Logger.info("Document: " + counter + "/" + total);
					counter++;
					
					Document doc = new Document();
					if(citation.abstractText != null){
						doc.add(new Field("abstract", citation.abstractText, TextField.TYPE_STORED));
					}
					doc.add(new Field("title", citation.title, TextField.TYPE_STORED));
					doc.add(new Field("date", DateTools.dateToString(citation.created, DateTools.Resolution.MINUTE), TextField.TYPE_STORED));
					iwriter.addDocument(doc);
				}

//		Document doc = new Document();
//		doc.add(new Field("title", "Suppression of v-Src transformation by andrographolide via " +
//				"degradation, of the v-Src protein and attenuation of " +
//				"the Erk signaling pathway.", TextField.TYPE_STORED));
//		iwriter.addDocument(doc);

		iwriter.close();

		Logger.info("index done.");
	}

}
