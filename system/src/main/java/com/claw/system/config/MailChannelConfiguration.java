package com.claw.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailChannelConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mail.smtp", name = "enabled", havingValue = "true")
    public JavaMailSender javaMailSender(MailChannelProperties mailChannelProperties) {
        MailChannelProperties.Smtp smtp = mailChannelProperties.getSmtp();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtp.getHost());
        sender.setPort(smtp.getPort());
        sender.setUsername(smtp.getUsername());
        sender.setPassword(smtp.getPassword());

        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", smtp.isStarttls() ? "true" : "false");
        p.put("mail.smtp.ssl.enable", smtp.isSsl() ? "true" : "false");
        sender.setJavaMailProperties(p);
        return sender;
    }
}
