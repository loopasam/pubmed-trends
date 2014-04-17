package jobs;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
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
import play.vfs.VirtualFile;

public class LuceneQuery extends Job {
	
	String query;

	public LuceneQuery(String query) {
		this.query = query;
	}

	public void doJob() throws IOException, ParseException{
		Logger.info("Runnign query...");

		Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
		DirectoryReader ireader = DirectoryReader.open(directory);

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		// Parse a simple query that searches for "text":
		QueryParser parser = new QueryParser(Version.LUCENE_47, "title", analyzer);
		Query query = parser.parse(this.query);
		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		System.out.println("results: " + hits.length);
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(hitDoc.get("title"));
		}
		ireader.close();
		directory.close();
	}



}

