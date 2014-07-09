/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.MorphiaCitation;
import models.MorphiaJournal;
import play.Logger;
import play.jobs.Job;
import play.test.MorphiaFixtures;

/**
 *
 * @author loopasam
 */
public class ImportMedlineJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Getting data from PUB_LIT...");
        //Delete the existing entities
        MorphiaFixtures.delete(MorphiaJournal.class);
        MorphiaFixtures.delete(MorphiaCitation.class);

        //Connection parameters
        String url = "jdbc:oracle:thin:@ora-vm5-015.ebi.ac.uk:1551:LITPUB";
        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        String user = (String) play.Play.configuration.get("litpub.user");
        String password = (String) play.Play.configuration.get("litpub.password");
        Connection c = DriverManager.getConnection(url, user, password);

        //Get all the citations of the last 3 years (tnow, t-1, t-2 - e.g. 2013, 2012, 2011)
        int now = Integer.parseInt((String) play.Play.configuration.get("analysis.year"));
        String endDigits = Integer.toString(now - 3).substring(2);
        String startDigit = Integer.toString(now + 1).substring(2);

        Logger.info("Retrieving citations...");

        String citationQuery = "SELECT TITLE_SPECIAL_CHAR, TITLE, ABSTRACT, ABSTRACT_LONG, EXTERNAL_ID, CREATED "
                + "FROM CDB.CITATIONS c "
                + "WHERE source = 'MED' "
                + "AND CREATED < '01-JAN-" + startDigit + "' "
                + "AND CREATED > '31-DEC-" + endDigits + "'";

        PreparedStatement pstmt = c.prepareStatement(citationQuery);
        ResultSet rsCitations = pstmt.executeQuery();

        int counter = 1;

        while (rsCitations.next()) {

            String title = rsCitations.getString("TITLE_SPECIAL_CHAR");
            if (title == null) {
                title = rsCitations.getString("TITLE");
            }

            String abstractText = rsCitations.getString("ABSTRACT");

            if (abstractText == null) {
                abstractText = rsCitations.getString("ABSTRACT_LONG");
            }
            String pmid = rsCitations.getString("EXTERNAL_ID");
            String created = rsCitations.getString("CREATED");

            //Do other queries to retrieve the rest
            String complementQuery = "SELECT ISO_ABBREVIATION, CITATION_COUNT "
                    + "FROM CDB.CITATIONS c, CDB.JOURNAL_ISSUES ji, CDB.CV_JOURNALS j, CDB.CN_METRICS m "
                    + "WHERE c.EXTERNAL_ID = '" + pmid + "' "
                    + "AND c.JOURNAL_ISSUE_ID = ji.ID "
                    + "AND ji.JOURNAL_ID = j.ID "
                    + "AND m.CITATION_ID = c.ID";

            PreparedStatement pstmtComplement = c.prepareStatement(complementQuery);
            ResultSet rsComplement = pstmtComplement.executeQuery();
            String journalAbbreviation = null;
            String citationCount = null;
            while (rsComplement.next()) {
                journalAbbreviation = rsComplement.getString("ISO_ABBREVIATION");
                citationCount = rsComplement.getString("CITATION_COUNT");
            }
            pstmtComplement.close();
            rsComplement.close();

            Logger.info("Record (PMID: " + pmid + ") - " + counter);
            counter++;
            new MorphiaCitation(pmid, title, abstractText, created, journalAbbreviation, citationCount).save();
        }
        pstmt.close();
        rsCitations.close();

        Logger.info("Citations imported.");

        //Retrieves the journals
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
            new MorphiaJournal(title, issn, iso).save();
        }

        pstmtJournals.close();
        rsJournals.close();
        c.close();
        Logger.info("Import job done.");
    }

}
