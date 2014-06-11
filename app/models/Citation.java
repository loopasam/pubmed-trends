package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class Citation extends Model {

    public Citation(String pmid, String title, String abstractText, String created) {

        if (isEntirelyInBasicMultilingualPlane(abstractText)) {
            this.abstractText = abstractText;
        } else {
            Logger.warn("Abstract contains non-supported UTF8 character: " + pmid);
        }

        this.pmid = Integer.parseInt(pmid);
        this.title = title;
        try {

            this.created = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S", Locale.ENGLISH).parse(created);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public int pmid;

    @Lob
    public String title;

    @Lob
    public String abstractText;

    public Date created;

    public static boolean isEntirelyInBasicMultilingualPlane(String text) {
        if (text == null) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.isSurrogate(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
