package jobs;

import java.sql.*;

import models.Citation;

import play.Logger;
import play.jobs.Job;
import play.test.Fixtures;

public class SampleJob extends Job {

	public void doJob() {
		Logger.info("Getting data...");

		Fixtures.delete(Citation.class);
		
		Connection c = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String url = "jdbc:oracle:thin:@ora-vm5-015.ebi.ac.uk:1551:LITPUB";
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			c = DriverManager.getConnection(url, "CDB_READ_CHEMBL", "readonly");
			//pstmt = c.prepareStatement("SELECT * from CDB.CITATIONS where source = 'MED'");
			pstmt = c.prepareStatement("select * from CDB.CITATIONS where external_id = '20814568'");
			
			//pstmt.setMaxRows(1000);
			rs = pstmt.executeQuery();
			int counter = 1;
			int total = 23742757;
			
			while (rs.next()) {
				String title = rs.getString("TITLE_SPECIAL_CHAR");
				if(title == null){
					title = rs.getString("TITLE");
				}
				
				String abstractText = rs.getString("ABSTRACT");
				
				if(abstractText == null){
					abstractText = rs.getString("ABSTRACT_LONG");
				}
				String pmid = rs.getString("EXTERNAL_ID");
				String created = rs.getString("CREATED");
				
				Logger.info("Record (PMID: " + pmid + ") - " + counter + "/" + total);
				counter++;
				try{
					new Citation(pmid, title, abstractText, created).save();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				
				if (counter%1000 == 0) {
					Citation.em().flush();
					Citation.em().clear();
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception e) {};
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
			try { if (c != null) c.close(); } catch (Exception e) {};
		}

		Logger.info("Job done.");

	}

}
