package com.claw.system.schedule;

import com.claw.system.service.MailImapIngestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 仅在启用 IMAP 且存在 {@link MailImapIngestService} Bean 时轮询。
 */
@Slf4j
@Component
@ConditionalOnBean(MailImapIngestService.class)
public class MailIngestScheduler {

    @Autowired
    private MailImapIngestService mailImapIngestService;

    @Scheduled(fixedDelayString = "${mail.inbound.poll-interval-ms:60000}")
    public void poll() {
        try {
            mailImapIngestService.pollOnce();
        } catch (Exception e) {
            log.warn("[mail] scheduled ingest error: {}", e.getMessage());
        }
    }
}
