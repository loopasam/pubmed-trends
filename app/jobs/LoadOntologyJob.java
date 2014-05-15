/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import play.libs.WS;
import uk.ac.ebi.brain.core.Brain;

/**
 *
 * @author loopasam
 */
public class LoadOntologyJob extends Job {

    @Override
    public void doJob() throws Exception {

        //http://bioportal.bioontology.org/ontologies/NCIT/?p=classes&conceptid=root
        Brain brain = new Brain();
        Logger.info("Learning...");
        brain.learn("data/NCITNCBO.owl");
        Logger.info("learnt");
        //Get the first branches
        List<String> topClasses = brain.getSubClasses("Thing", true);

        int totaltop = topClasses.size();
        int countertop = 0;

        for (String topClass : topClasses) {

            countertop++;

            List<String> subclasses = brain.getSubClasses(topClass, false);
            int total = subclasses.size();
            int counter = 0;
            String branch = topClass;
            int length = getLength(brain.getLabel(topClass));

            new OntologyTerm(brain.getLabel(topClass), topClass, branch, length).save();

            for (String subclass : subclasses) {
                counter++;
                Logger.info("branch: " + countertop + "/" + totaltop + " - i: " + counter + "/" + total);

                int lengthTerm = getLength(brain.getLabel(topClass));

                new OntologyTerm(brain.getLabel(subclass), subclass, branch, lengthTerm).save();
                if (counter % 500 == 0) {
                    OntologyTerm.em().flush();
                    OntologyTerm.em().clear();
                }

            }

        }

        brain.sleep();
        Logger.info("Job finished");
    }

    private int getLength(String label) throws IOException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        List<String> result = new ArrayList<String>();
        TokenStream stream = analyzer.tokenStream(null, new StringReader(label));
        stream.reset();
        while (stream.incrementToken()) {
            result.add(stream.getAttribute(CharTermAttribute.class).toString());
        }
        return result.size();
    }

}
