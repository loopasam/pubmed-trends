package jobs;

import java.sql.*;
import models.Citation;
import models.Journal;

import play.Logger;
import play.jobs.Job;
import play.test.Fixtures;

public class MedlineImportJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Getting data...");

        Fixtures.delete(Citation.class);
        Fixtures.delete(Journal.class);

        String url = "jdbc:oracle:thin:@ora-vm5-015.ebi.ac.uk:1551:LITPUB";
        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();

        String user = (String) play.Play.configuration.get("litpub.user");
        String password = (String) play.Play.configuration.get("litpub.password");

        Connection c = DriverManager.getConnection(url, user, password);

        //Get all the citations of the last 5 years
        //TODO compute date dynamically from configuration
        PreparedStatement pstmt = c.prepareStatement("SELECT TITLE_SPECIAL_CHAR, TITLE, ABSTRACT, ABSTRACT_LONG, EXTERNAL_ID, CREATED, ISO_ABBREVIATION, CITATION_COUNT "
                + "FROM CDB.CITATIONS c, CDB.JOURNAL_ISSUES ji, CDB.CV_JOURNALS j, CDB.CN_METRICS m "
                + "WHERE source = 'MED' "
                + "AND CREATED < '01-JAN-14' "
                + "AND CREATED > '31-DEC-10' "
                + "AND c.JOURNAL_ISSUE_ID = ji.ID "
                + "AND ji.JOURNAL_ID = j.ID "
                + "AND m.CITATION_ID = c.ID");

        //Set an arbitrary limit
        //int total = 1000000;
        //pstmt.setMaxRows(total);
        ResultSet rs = pstmt.executeQuery();
        int counter = 1;
        
        int total = 3244360;

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
            
            String journalAbbreviation = rs.getString("ISO_ABBREVIATION");
            
            String citationCount = rs.getString("CITATION_COUNT");
            
            Logger.info("Record (PMID: " + pmid + ") - " + counter + "/" + total);
            counter++;

            new Citation(pmid, title, abstractText, created, journalAbbreviation, citationCount).save();
            if (counter % 1000 == 0) {
                Citation.em().flush();
                Citation.em().clear();
            }

        }
        pstmt.close();
        rs.close();

        Logger.info("Job done.");
        counter = 0;

        PreparedStatement pstmtJournals = c.prepareStatement("SELECT JOURNALTITLE, ISSN, ISO_ABBREVIATION "
                + "FROM CDB.CV_JOURNALS");

        ResultSet rsJournals = pstmtJournals.executeQuery();

        while (rsJournals.next()) {

            String title = rsJournals.getString("JOURNALTITLE");

            String issn = rsJournals.getString("ISSN");

            String iso = rsJournals.getString("ISO_ABBREVIATION");

            Logger.info("Journal (" + title + "): " + counter);
            counter++;

            new Journal(title, issn, iso).save();

            if (counter % 100 == 0) {
                Journal.em().flush();
                Journal.em().clear();
            }

        }

        c.close();

    }

}
