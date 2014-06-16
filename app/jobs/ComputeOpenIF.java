/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import models.Citation;
import models.Journal;
import play.Logger;
import play.db.jpa.GenericModel;
import play.jobs.Job;

/**
 *
 * @author loopasam
 */
public class ComputeOpenIF extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job open IF started...");
        //IF(2013) = articles published in 2011 and 2012
        //A = # of times articles published in journal in 2011 and 2012 are cited - ideally calculated in very beginning 2014, when all citations that happened
        //in 2013 have been considered.
        //B = # of articles published by the journal in 2011 and 2012.
        //IF(2013) = A/B

        //foreach journal:
        List<Journal> journals = Journal.findAll();
        int counter = 0;
        int total = journals.size();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date start2011 = sdf.parse("01/01/2011");
        Date end2011 = sdf.parse("31/12/2011");

        Date start2012 = sdf.parse("01/01/2012");
        Date end2012 = sdf.parse("31/12/2012");

        for (Journal journal : journals) {
            counter++;
            Logger.info(journal.title + ": " + counter + "/" + total);
            List<Citation> citations2011 = Citation.find("journalAbbreviation = ? and created between ? and ?", journal.issn, start2011, end2011).fetch();
            Logger.info("Article 2011: " + citations2011.size());
            int citationCount = 0;
            for (Citation citation : citations2011) {
                citationCount += citation.citationCount;
            }
            Logger.info("Total citations: " + citationCount);
            double openIF = 0;
            if (citationCount > 0) {
                openIF = (double) citationCount / citations2011.size();
                Logger.info("IF: " + openIF);
            }
            journal.openImpactFactor = openIF;
            journal.save();
        }

        //Get the list of articles published in 2011
        //count number of articles in 2011 --> B'
        //Get the list of articles published in 2012
        //count number of articles in 2012 --> B"
        //if one of them if zero, theoretically does not qualify for impact factor --> warn about it
        //otherwise B = B' + B"
        //Sum the number of citations and compute standard deviation
    }

}
