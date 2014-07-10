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
public class MorphiaOntologyTerm extends Model {

    public String value;

    public String termId;

    public String branch;

    //Total length of the phrase
    public int totalLength;

    //Length when stop words have been removed. If different than total length,
    //it means that stop words are present in the entry.
    public int lengthWithoutStopWords;

    public int frequency;

    public MorphiaOntologyTerm(String value, String termId, String branch, int totalLength, int lengthWithoutStopWords) {
        this.value = value;
        this.termId = termId;
        this.branch = branch;
        this.totalLength = totalLength;
        this.lengthWithoutStopWords = lengthWithoutStopWords;
    }

}
