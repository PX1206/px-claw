package com.claw.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件渠道配置：mail.inbound.* / mail.smtp.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailChannelProperties {

    private Inbound inbound = new Inbound();

    private Smtp smtp = new Smtp();

    @Data
    public static class Inbound {
        private boolean enabled = false;
        private String host = "";
        private int port = 993;
        private String username = "";
        private String password = "";
        private String folder = "INBOX";
        private boolean ssl = true;
        private long pollIntervalMs = 60000L;
        private int maxBatchPerPoll = 20;
    }

    @Data
    public static class Smtp {
        private boolean enabled = false;
        private String host = "";
        private int port = 587;
        private String username = "";
        private String password = "";
        private boolean ssl = false;
        private boolean starttls = true;
        private String fromAddress = "";
    }
}
