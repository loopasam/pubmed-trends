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
public class OntologyTerm extends Model {

    public String value;

    public String termId;
    
    public String branch;
    
    public int length;

    public OntologyTerm(String value, String termId, String branch, int length) {
        this.value = value;
        this.termId = termId;
        this.branch = branch;
        this.length = length;
    }
}
