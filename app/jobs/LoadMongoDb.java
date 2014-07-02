/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.util.Date;
import java.util.List;
import models.Citation;
import models.Journal;
import models.MorphiaCitation;
import models.MorphiaJournal;
import play.Logger;
import play.jobs.Job;
import play.test.MorphiaFixtures;

/**
 *
 * @author loopasam
 */
public class LoadMongoDb extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Loading mongoDB...");

//        MorphiaFixtures.delete(MorphiaCitation.class);
//        Logger.info("Getting citations...");
//        List<Citation> citations = Citation.findAll();
//        int total = citations.size();
//        int counter = 0;
//        
//        for (Citation citation : citations) {
//            counter++;
//            Logger.info(counter + "/" + total);
//            int pmid = citation.pmid;
//            String title = citation.title;
//            String abstractText = citation.abstractText;
//            Date date = citation.created;
//            String abbrev = citation.journalAbbreviation;
//            int citationCount = citation.citationCount;
//            new MorphiaCitation(pmid, title, abstractText, date, abbrev, citationCount).save();
//            
//        }
        
        MorphiaFixtures.delete(MorphiaJournal.class);
        List<Journal> journals = Journal.findAll();
        int total = journals.size();
        int counter = 0;

        for (Journal journal : journals) {
            counter++;
            Logger.info(counter + "/" + total);
            String title = journal.title;
            String iso = journal.iso;
            String issn = journal.issn;
            
            new MorphiaJournal(title, iso, issn).save();

        }

//        MorphiaCitation citation = new MorphiaCitation(1234, "title", "abstract text", new Date(), "abbrev.", 2).save();
//        Object id = citation.getId();
//        Logger.info("ID: " + id);
//        
//        MorphiaCitation cit = MorphiaCitation.findById(id);
//        Logger.info("PMID: " + cit.pmid);
        Logger.info("Job done.");
    }

}
