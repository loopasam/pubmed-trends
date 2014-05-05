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
    
    public double trend5years;
    
    public double volumetricTrend5years;
    
    public int apparitionDate;

    public Phrase(String value, int frequency) {
        this.value = value;
        this.frequency = frequency;
    }
}
