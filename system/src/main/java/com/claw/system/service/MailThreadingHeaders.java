package com.claw.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * 将关键线程头序列化入 mail_inbound.raw_headers_json，发信时再读出。
 */
public final class MailThreadingHeaders {

    private MailThreadingHeaders() {
    }

    public static String toJson(Message m, ObjectMapper om) throws MessagingException {
        ObjectNode n = om.createObjectNode();
        putHeader(m, n, "Message-ID");
        putHeader(m, n, "References");
        putHeader(m, n, "In-Reply-To");
        try {
            return om.writeValueAsString(n);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static void putHeader(Message m, ObjectNode n, String name) throws MessagingException {
        String[] v = m.getHeader(name);
        if (v != null && v.length > 0 && v[0] != null && !v[0].trim().isEmpty()) {
            n.put(name, v[0].trim());
        }
    }

    public static String getFirstHeader(String json, String headerName, ObjectMapper om) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode n = om.readTree(json);
            JsonNode x = n.get(headerName);
            return x != null && !x.isNull() ? x.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
