/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.code.morphia.annotations.Entity;
import play.modules.morphia.Model;

/**
 *
 * @author loopasam
 */
@Entity
public class MorphiaPhrase extends Model {

    public String value;

    //Document frequency for t(now)
    public int frequencyNow;

    //Frequency of documents for t(now - 1 year)
    public double frequencyThen;
    
    public double trend;
    
    public double volumetricTrend;

    public MorphiaPhrase(String value, int frequency) {
        this.value = value;
        this.frequencyNow = frequency;
    }

}
