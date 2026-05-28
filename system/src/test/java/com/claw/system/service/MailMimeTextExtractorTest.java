package com.claw.system.service;

import org.junit.jupiter.api.Test;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailMimeTextExtractorTest {

    @Test
    void extractsPlainFromMultipart() throws Exception {
        Session s = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(s);
        MimeMultipart mp = new MimeMultipart("mixed");
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText("hello plain", "UTF-8");
        mp.addBodyPart(plain);
        msg.setContent(mp);
        MailMimeTextExtractor ex = new MailMimeTextExtractor();
        assertEquals("hello plain", ex.extractBodyText(msg).trim());
    }

}
