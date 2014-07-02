/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import java.util.Date;
import play.modules.morphia.Model;

/**
 *
 * @author loopasam
 */
@Entity
public class MorphiaCitation extends Model {

    public MorphiaCitation(int pmid, String title, String abstractText, Date created, String journalAbbreviation, int citationCount) {
        this.pmid = pmid;
        this.title = title;
        this.abstractText = abstractText;
        this.created = created;
        this.journalAbbreviation = journalAbbreviation;
        this.citationCount = citationCount;
    }
    

    public int pmid;

    public String title;

    public String abstractText;

    public Date created;

    //Indexed to build the impact factors
    @Indexed
    public String journalAbbreviation;

    public int citationCount;

}
