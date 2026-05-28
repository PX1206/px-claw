package com.claw.system.service;

import com.claw.common.exception.BusinessException;
import com.claw.system.config.MailChannelProperties;
import com.claw.system.entity.MailInbound;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * 人工审核通过后，通过 SMTP 回复原邮件线程。
 */
@Service
public class MailSendService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Autowired
    private MailChannelProperties mailChannelProperties;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendReply(MailInbound inbound, String editedBody) throws Exception {
        if (javaMailSender == null) {
            throw new BusinessException("SMTP 未启用：请在 application.yml 设置 mail.smtp.enabled=true 并配置 host/账号");
        }
        MailChannelProperties.Smtp smtp = mailChannelProperties.getSmtp();
        if (smtp.getFromAddress() == null || smtp.getFromAddress().trim().isEmpty()) {
            throw new BusinessException("未配置 mail.smtp.from-address");
        }
        if (inbound.getFromAddr() == null || inbound.getFromAddr().trim().isEmpty()) {
            throw new BusinessException("入站邮件缺少发件人地址");
        }

        MimeMessage mime = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setFrom(smtp.getFromAddress().trim());
        helper.setTo(inbound.getFromAddr().trim());
        helper.setSubject(buildReSubject(inbound.getSubject()));
        helper.setText(editedBody != null ? editedBody : "", false);

        String mid = MailThreadingHeaders.getFirstHeader(inbound.getRawHeadersJson(), "Message-ID", objectMapper);
        if (mid != null && !mid.trim().isEmpty()) {
            String normalized = normalizeAngleBrackets(mid.trim());
            mime.setHeader("In-Reply-To", normalized);
            String refs = MailThreadingHeaders.getFirstHeader(inbound.getRawHeadersJson(), "References", objectMapper);
            String refLine = (refs != null && !refs.trim().isEmpty() ? refs.trim() + " " : "") + normalized;
            mime.setHeader("References", refLine.trim());
        }

        javaMailSender.send(mime);
    }

    private static String buildReSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "Re: ";
        }
        String s = subject.trim();
        if (s.regionMatches(true, 0, "re:", 0, 3)) {
            return s;
        }
        return "Re: " + s;
    }

    private static String normalizeAngleBrackets(String id) {
        String t = id.trim();
        if (t.startsWith("<") && t.endsWith(">")) {
            return t;
        }
        return "<" + t.replace("<", "").replace(">", "") + ">";
    }
}
