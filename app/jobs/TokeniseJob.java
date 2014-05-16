/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.OntologyTerm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
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

/**
 *
 * @author loopasam
 */
public class TokeniseJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Job started");

        List<OntologyTerm> terms = OntologyTerm.findAll();
        int total = terms.size();
        int counter = 0;
        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/luceneAbstract").getRealFile());
        DirectoryReader ireader = DirectoryReader.open(directory);

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(Version.LUCENE_47, "contents", analyzer);

        Map<Long, Integer> freqs = new HashMap<Long, Integer>();

        for (OntologyTerm ontologyTerm : terms) {
            counter++;
            Logger.info("i: " + counter + "/" + total);

            if (!ontologyTerm.value.contains("/")) {

                Query query = parser.parse("\"" + ontologyTerm.value + "\"");
                ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
                freqs.put(ontologyTerm.id, hits.length);
                Logger.info("Query: " + ontologyTerm.value + " - " + hits.length);
            }

        }

        counter = 0;
        for (Long id : freqs.keySet()) {
            counter++;
            OntologyTerm term = OntologyTerm.findById(id);
            Logger.info("i save: " + counter);
            int freq = freqs.get(id);
            term.updateFrequency(freq);

            if (counter % 50 == 0) {
                OntologyTerm.em().flush();
                OntologyTerm.em().clear();
            }

        }

        Logger.info("Job finished");

    }

}
