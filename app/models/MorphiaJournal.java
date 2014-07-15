/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embedded;
import play.modules.morphia.Model;

/**
 *
 * @author loopasam
 */
@Entity
public class MorphiaJournal extends Model {

    public String title;

    public String iso;

    public String displayOIF;

    @Indexed
    public String issn;

    public double openImpactFactor;

    //http://en.wikipedia.org/wiki/Standard_deviation#Uncorrected_sample_standard_deviation
    //http://www.wikihow.com/Calculate-Standard-Deviation
    //Population standard deviation - entire population considered
    public double deviationIF;

    //Flag to see if a journal qualifies to receive an impact factor
    public boolean isOldEnough;

    @Embedded
    public List<Integer> counts;

    public MorphiaJournal(String title, String iso, String issn) {
        this.title = title;
        this.iso = iso;
        this.issn = issn;
        this.openImpactFactor = 0;
        this.deviationIF = 0;
        this.isOldEnough = false;
        this.counts = new ArrayList<Integer>();
    }

}
