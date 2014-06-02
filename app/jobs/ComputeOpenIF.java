/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import play.Logger;
import play.jobs.Job;

/**
 *
 * @author loopasam
 */
public class ComputeOpenIF extends Job {

    @Override
    public void doJob() throws Exception {

        Logger.info("Job open IF started...");
        //Add the open citations and journal for each Citation in the DB
        //Impact factor formula: 
    }

}
