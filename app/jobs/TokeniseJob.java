/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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

        //National Cancer Institute Thesaurus
        //BioAssay Ontology
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        List<String> result = new ArrayList<String>();
        TokenStream stream = analyzer.tokenStream(null, new StringReader("Cyclophosphamide/Prednisone/Vincristine/Zorubicin"));
        stream.reset();
        while (stream.incrementToken()) {
            result.add(stream.getAttribute(CharTermAttribute.class).toString());
        }

        Logger.info(result.toString());

    }

}
