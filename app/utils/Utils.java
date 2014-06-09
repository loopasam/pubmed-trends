/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import play.Logger;
import play.libs.Mail;

/**
 *
 * @author loopasam
 */
public class Utils {

    public static void emailAdmin(String subject, String message) {

        SimpleEmail email = new SimpleEmail();
        try {
            email.setFrom("super.cool.bot@gmail.com");
            String address = (String) play.Play.configuration.get("mail.admin");
            email.addTo(address);
            email.setSubject(subject);
            email.setMsg(message);
            Mail.send(email);
        } catch (EmailException e) {
            Logger.error(e.getLocalizedMessage());
        }
    }

}
