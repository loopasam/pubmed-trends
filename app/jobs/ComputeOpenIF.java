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
import java.util.HashMap;
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
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        int y2 = now - 2;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date start = sdf.parse("01/01/" + now);
        Date end = sdf.parse("31/12/" + y2);

        Logger.info("Getting citations...");
        List<Citation> citations = Citation.find("created between ? and ?", end, start).fetch();

        int counter = 0;
        int total = citations.size();

        //Map holding the journal's journalAbbreviation and their citation counts.
        HashMap<String, List<Integer>> citationMap = new HashMap<String, List<Integer>>();

        for (Citation citation : citations) {
            counter++;
            Logger.info(counter + "/" + total);
            if (citationMap.containsKey(citation.journalAbbreviation)) {
                List<Integer> citationCounts = citationMap.get(citation.journalAbbreviation);
                citationCounts.add(citation.citationCount);
            } else {
                List<Integer> citationCounts = new ArrayList<Integer>();
                citationCounts.add(citation.citationCount);
                citationMap.put(citation.journalAbbreviation, citationCounts);
            }
            Logger.info(citation.journalAbbreviation + ": " + citationMap.get(citation.journalAbbreviation));
        }
        
        //Save the jazz

//            double openIF = 0.0;
//            double deviationIF = 0.0;
        //Compute the IF = mean citations per article published in the two previous years.
//            if (citations.size() > 0) {
//                openIF = (double) sum(citationCounts) / citations.size();
//            }
//            Logger.info("- IF: " + openIF);
        //Compute the standard deviation of the sample (population)
//            double squaredDiff = 0.0;
//            for (Integer citationCount : citationCounts) {
//                double diff = Math.pow(citationCount - openIF, 2);
//                squaredDiff += diff;
//            }
//
//            if (citations.size() > 0) {
//                deviationIF = Math.sqrt(squaredDiff / citations.size());
//            }
//            Logger.info("- Deviation: " + deviationIF);
        //Save the modifications
//            journal.openImpactFactor = openIF;
//            journal.deviationIF = deviationIF;
//            journal.save();
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
