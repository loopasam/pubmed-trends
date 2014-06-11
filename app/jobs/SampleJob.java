package jobs;

import java.sql.*;

import org.h2.engine.Database;

import models.Citation;

import play.Logger;
import play.jobs.Job;
import play.test.Fixtures;

public class SampleJob extends Job {

    @Override
    public void doJob() {
        Logger.info("Getting data...");

        //Does an iteration to get the citations
        //SELECT TITLE_SPECIAL_CHAR, TITLE, ABSTRACT, ABSTRACT_LONG, EXTERNAL_ID, CREATED, ISO_ABBREVIATION, CITATION_COUNT
//FROM CDB.CITATIONS c, CDB.JOURNAL_ISSUES ji, CDB.CV_JOURNALS j, CDB.CN_METRICS m
//WHERE source = 'MED'
//AND c.JOURNAL_ISSUE_ID = ji.ID 
//AND ji.JOURNAL_ID = j.ID
//AND m.CITATION_ID = c.ID;
        
        //Does an iteration to save journal related information
        //SELECT JOURNALTITLE, ISSN, ISO_ABBREVIATION FROM CDB.CV_JOURNALS;
        
        Fixtures.delete(Citation.class);

        Connection c = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String url = "jdbc:oracle:thin:@ora-vm5-015.ebi.ac.uk:1551:LITPUB";
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            c = DriverManager.getConnection(url, "CDB_READ_CHEMBL", "readonly");

            pstmt = c.prepareStatement("SELECT * from CDB.CITATIONS where source = 'MED'");

            //Set an arbitrary limit
            int total = 1000000;
            pstmt.setMaxRows(total);

            rs = pstmt.executeQuery();
            int counter = 1;
            //int total = 23742757;

            //TODO save more information from the articles
            while (rs.next()) {
                String title = rs.getString("TITLE_SPECIAL_CHAR");
                if (title == null) {
                    title = rs.getString("TITLE");
                }

                String abstractText = rs.getString("ABSTRACT");

                if (abstractText == null) {
                    abstractText = rs.getString("ABSTRACT_LONG");
                }
                String pmid = rs.getString("EXTERNAL_ID");
                String created = rs.getString("CREATED");

                Logger.info("Record (PMID: " + pmid + ") - " + counter + "/" + total);
                counter++;

                new Citation(pmid, title, abstractText, created).save();
                if (counter % 1000 == 0) {
                    Citation.em().flush();
                    Citation.em().clear();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            };
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            };
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            };
        }

        Logger.info("Job done.");

    }

}
