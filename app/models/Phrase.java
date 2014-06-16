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
public class Phrase extends Model {
    
    public String value;
    
    public int frequency;
    
    //Frequency of documents for t - 5 years
    public double frequency5y;
    
    //Frequency of documents for t - 1 year
    public double frequency1y;
            
    public int apparitionDate;

    public Phrase(String value, int frequency) {
        this.value = value;
        this.frequency = frequency;
    }
}
