/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import models.OntologyTerm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;

/**
 *
 * @author loopasam
 */
public class OntologyTermSizeCalculation extends Job {

    @Override
    public void doJob() throws Exception {
        
        //Time it takes: started 12pm

        Logger.info("Job started...");

        List<OntologyTerm> terms = OntologyTerm.findAll();
        int total = terms.size();
        int counter = 0;

        for (OntologyTerm ontologyTerm : terms) {

            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
            List<String> result = new ArrayList<String>();
            TokenStream stream = analyzer.tokenStream(null, new StringReader(ontologyTerm.value));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            ontologyTerm.length = result.size();
            counter++;
            Logger.info("Term (" + counter + "/" + total + "): " + ontologyTerm.value + " - length: " + result.size());
            ontologyTerm.save();
        }
        Logger.info("Job done");

    }

}
