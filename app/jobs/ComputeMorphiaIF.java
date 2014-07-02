/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.mongodb.BasicDBObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.MorphiaCitation;
import models.MorphiaJournal;
import play.Logger;
import play.jobs.Job;
import play.modules.morphia.Model.MorphiaQuery;

/**
 *
 * @author loopasam
 */
public class ComputeMorphiaIF extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("starting job...");

        Logger.info("Getting journals...");
        List<MorphiaJournal> journals = MorphiaJournal.q().filter("issn exists", true).asList();

        int total = journals.size();
        int counter = 0;
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        int y2 = now - 2;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        //Should be limited when loading
        Date start = sdf.parse("01/01/" + now);
        Logger.info("Start: " + start);
        Date end = sdf.parse("31/12/" + y2);
        Logger.info("Stop: " + end);

        for (MorphiaJournal journal : journals) {
            counter++;
            Logger.info(journal.title + ": " + counter + "/" + total);

            MorphiaQuery q = MorphiaCitation.q();

            List<MorphiaCitation> citations
                    = MorphiaCitation.q().filter("created <", end).filter("journalAbbreviation", journal.issn).asList();

            if (citations.size() > 0) {
                
                Logger.info("Citations: " + citations.size());
                journal.isOldEnough = true;

                List<Integer> counts = new ArrayList<Integer>();
                for (MorphiaCitation citation : citations) {
                    //Deal with the zero citations
                    counts.add(citation.citationCount);
                }

                double openIF = 0.0;
                double deviationIF = 0.0;
                //Compute the IF = mean citations per article published in the two previous years.
                if (citations.size() > 0) {
                    openIF = (double) sum(counts) / citations.size();
                }
                Logger.info("- IF: " + openIF);
                //Compute the standard deviation of the sample(population)
                double squaredDiff = 0.0;
                for (Integer citationCount : counts) {
                    double diff = Math.pow(citationCount - openIF, 2);
                    squaredDiff += diff;
                }

                if (citations.size() > 0) {
                    deviationIF = Math.sqrt(squaredDiff / citations.size());
                }
                Logger.info("- Deviation: " + deviationIF);
                //Save the modifications journal
                journal.openImpactFactor = openIF;
                journal.deviationIF = deviationIF;
                journal.counts = counts;
                journal.save();
            }

        }
        Logger.info("Job done.");
    }

    private int sum(List<Integer> list) {
        Integer sum = 0;
        for (Integer i : list) {
            sum = sum + i;
        }
        return sum;
    }

}
