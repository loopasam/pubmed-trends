/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import com.google.common.base.Stopwatch;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import models.Citation;
import models.Journal;
import play.Logger;
import play.db.jpa.GenericModel;
import play.jobs.Job;
import utils.Utils;

/**
 *
 * @author loopasam
 */
public class ComputeOpenIF extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job open IF started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        //IF(2013) = articles published in 2011 and 2012
        //A = # of times articles published in journal in 2011 and 2012 are cited - ideally calculated in very beginning 2014, when all citations that happened
        //in 2013 have been considered.
        //B = # of articles published by the journal in 2011 and 2012.
        //IF(2013) = A/B
        List<Journal> journals = Journal.findAll();
        int counter = 0;
        int total = journals.size();

        //TODO do not hardcode this part
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        int y1 = now - 1;
        int y2 = now - 2;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date start2y = sdf.parse("01/01/" + y1);
        Date end2y = sdf.parse("31/12/" + y1);

        Date start1y = sdf.parse("01/01/" + y2);
        Date end1y = sdf.parse("31/12/" + y2);

        for (Journal journal : journals) {
            Logger.info("****************");
            counter++;
            Logger.info(journal.title + ": " + counter + "/" + total);
            //Get the citations for the preceeding year
            List<Citation> citations1y = Citation.find("journalAbbreviation = ? and created between ? and ?", journal.issn, start1y, end1y).fetch();
            //Logger.info("- Article " + y1 + ": " + citations1y.size());

            //Get the citations for the second preceeding year
            List<Citation> citations2y = Citation.find("journalAbbreviation = ? and created between ? and ?", journal.issn, start2y, end2y).fetch();
            //Logger.info("- Article " + y2 + ": " + citations2y.size());

            //check if the journal qualifies to receive an OpenIF value.
            if (citations1y.isEmpty() || citations2y.isEmpty()) {
                journal.isOldEnough = false;
                Logger.info("- Not old enough");
            } else {
                journal.isOldEnough = true;
            }

            //Aggregate of the citations
            List<Citation> citations = new ArrayList<Citation>();
            citations.addAll(citations1y);
            citations.addAll(citations2y);

            //List holding the citation counts (= values)
            List<Integer> citationCounts = new ArrayList<Integer>();
            for (Citation citation : citations) {
                citationCounts.add(citation.citationCount);
            }

            //Logger.info(citationCounts.toString());
            double openIF = 0.0;
            double deviationIF = 0.0;

            //Compute the IF = mean citations per article published in the two previous years.
            if (citations.size() > 0) {
                openIF = (double) sum(citationCounts) / citations.size();
            }
            Logger.info("- IF: " + openIF);

            //Compute the standard deviation of the sample (population)
            double squaredDiff = 0.0;
            for (Integer citationCount : citationCounts) {
                double diff = Math.pow(citationCount - openIF, 2);
                squaredDiff += diff;
            }

            if (citations.size() > 0) {
                deviationIF = Math.sqrt(squaredDiff / citations.size());
            }
            Logger.info("- Deviation: " + deviationIF);

            //Save the modifications
            journal.openImpactFactor = openIF;
            journal.deviationIF = deviationIF;
            journal.save();
        }
        
        stopwatch.stop();
        Utils.emailAdmin("Stratified index built", "Job finished in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
    }

    private int sum(List<Integer> list) {
        Integer sum = 0;
        for (Integer i : list) {
            sum = sum + i;
        }
        return sum;
    }

}
