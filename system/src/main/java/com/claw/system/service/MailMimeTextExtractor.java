package com.claw.system.service;

import org.springframework.stereotype.Component;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;

/**
 * 优先提取 text/plain；若无则对 text/html 做简单去标签。
 */
@Component
public class MailMimeTextExtractor {

    public String extractBodyText(MimeMessage msg) throws Exception {
        Object content = msg.getContent();
        if (content instanceof String) {
            String s = ((String) content).trim();
            String ct = msg.getContentType();
            if (ct != null && ct.toLowerCase().contains("text/html")) {
                return stripHtml(s);
            }
            return s;
        }
        if (content instanceof Multipart) {
            return extractFromMultipart((Multipart) content).trim();
        }
        if (content instanceof InputStream) {
            return streamToString((InputStream) content).trim();
        }
        return "";
    }

    private String extractFromMultipart(Multipart mp) throws Exception {
        StringBuilder plainAcc = new StringBuilder();
        StringBuilder htmlAcc = new StringBuilder();
        collectParts(mp, plainAcc, htmlAcc);
        String plain = plainAcc.toString().trim();
        if (!plain.isEmpty()) {
            return plain;
        }
        String html = htmlAcc.toString().trim();
        if (!html.isEmpty()) {
            return stripHtml(html);
        }
        return "";
    }

    private void collectParts(Multipart mp, StringBuilder plainAcc, StringBuilder htmlAcc) throws Exception {
        int n = mp.getCount();
        for (int i = 0; i < n; i++) {
            BodyPart part = mp.getBodyPart(i);
            Object c = part.getContent();
            String ct = part.getContentType() != null ? part.getContentType().toLowerCase() : "";
            if (ct.contains("multipart/") && c instanceof Multipart) {
                collectParts((Multipart) c, plainAcc, htmlAcc);
                continue;
            }
            if (ct.contains("text/plain")) {
                String t = readPartAsString(c);
                if (t != null && !t.trim().isEmpty()) {
                    if (plainAcc.length() > 0) {
                        plainAcc.append('\n');
                    }
                    plainAcc.append(t.trim());
                }
            } else if (ct.contains("text/html")) {
                String t = readPartAsString(c);
                if (t != null && !t.trim().isEmpty()) {
                    if (htmlAcc.length() > 0) {
                        htmlAcc.append('\n');
                    }
                    htmlAcc.append(t.trim());
                }
            }
        }
    }

    private static String readPartAsString(Object c) throws Exception {
        if (c instanceof String) {
            return (String) c;
        }
        if (c instanceof InputStream) {
            return streamToString((InputStream) c);
        }
        return null;
    }

    private String stripHtml(String html) {
        String t = html.replaceAll("(?s)<script.*?</script>", "")
                .replaceAll("(?s)<style.*?</style>", "");
        return t.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private static String streamToString(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int r;
        while ((r = in.read(b)) != -1) {
            buf.write(b, 0, r);
        }
        return buf.toString("UTF-8");
    }
}
