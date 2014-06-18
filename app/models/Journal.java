/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 *
 * @author loopasam
 */
@Entity
public class Journal extends Model {
    
    public String title;
    
    public String iso;
    
    public String issn;
    
    public double openImpactFactor;
    
    //http://en.wikipedia.org/wiki/Standard_deviation#Uncorrected_sample_standard_deviation
    //http://www.wikihow.com/Calculate-Standard-Deviation
    //Population standard deviation - entire population considered
    public double deviationIF;
    
    //Flag to see if a journal qualifies to receive an impact factor
    public boolean isOldEnough;

    public Journal(String title, String iso, String issn) {
        this.title = title;
        this.iso = iso;
        this.issn = issn;
    }
}
