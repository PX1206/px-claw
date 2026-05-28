package com.claw.system.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.claw.system.config.MailChannelProperties;
import com.claw.system.entity.MailImapCheckpoint;
import com.claw.system.entity.MailInbound;
import com.claw.system.mapper.MailImapCheckpointMapper;
import com.claw.system.mapper.MailInboundMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * IMAP 轮询拉取新邮件，入库并触发 AI 建议回复编排。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "mail.inbound", name = "enabled", havingValue = "true")
public class MailImapIngestService {

    private static final int CHECKPOINT_ID = 1;

    @Autowired
    private MailChannelProperties mailChannelProperties;

    @Autowired
    private MailInboundMapper mailInboundMapper;

    @Autowired
    private MailImapCheckpointMapper mailImapCheckpointMapper;

    @Autowired
    private MailMimeTextExtractor mailMimeTextExtractor;

    @Autowired
    private MailSuggestedReplyOrchestrator mailSuggestedReplyOrchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    public void pollOnce() {
        MailChannelProperties.Inbound inboundCfg = mailChannelProperties.getInbound();
        Store store = null;
        Folder folder = null;
        try {
            Properties props = new Properties();
            String protocol = inboundCfg.isSsl() ? "imaps" : "imap";
            if (inboundCfg.isSsl()) {
                props.put("mail.imaps.host", inboundCfg.getHost());
                props.put("mail.imaps.port", String.valueOf(inboundCfg.getPort()));
                props.put("mail.imaps.ssl.enable", "true");
            } else {
                props.put("mail.imap.host", inboundCfg.getHost());
                props.put("mail.imap.port", String.valueOf(inboundCfg.getPort()));
            }
            props.put("mail.imap.connectiontimeout", "15000");
            props.put("mail.imap.timeout", "60000");

            Session session = Session.getInstance(props);
            store = session.getStore(protocol);
            store.connect(inboundCfg.getUsername(), inboundCfg.getPassword());

            folder = store.getFolder(inboundCfg.getFolder());
            folder.open(Folder.READ_ONLY);

            MailImapCheckpoint cp = mailImapCheckpointMapper.selectById(CHECKPOINT_ID);
            if (cp == null) {
                cp = new MailImapCheckpoint();
                cp.setId(CHECKPOINT_ID);
                cp.setFolder(inboundCfg.getFolder());
                cp.setLastUid(0L);
                mailImapCheckpointMapper.insert(cp);
            }

            UIDFolder uf = (UIDFolder) folder;
            long lastUid = cp.getLastUid() == null ? 0L : cp.getLastUid();

            if (lastUid == 0) {
                if (folder.getMessageCount() > 0) {
                    Message lm = folder.getMessage(folder.getMessageCount());
                    long uid = uf.getUID(lm);
                    cp.setLastUid(uid);
                    mailImapCheckpointMapper.updateById(cp);
                    log.info("[mail] IMAP 初始化游标 last_uid={}（跳过历史邮件）", uid);
                }
                return;
            }

            Message[] raw = uf.getMessagesByUID(lastUid + 1, UIDFolder.LASTUID);
            if (raw == null || raw.length == 0) {
                return;
            }

            List<Message> msgs = new ArrayList<>();
            for (Message m : raw) {
                msgs.add(m);
            }
            msgs.sort(Comparator.comparingLong(m -> {
                try {
                    return uf.getUID(m);
                } catch (MessagingException e) {
                    return 0L;
                }
            }));
            int maxBatch = Math.max(1, inboundCfg.getMaxBatchPerPoll());
            if (msgs.size() > maxBatch) {
                msgs = msgs.subList(0, maxBatch);
            }

            long maxInBatch = lastUid;
            for (Message m : msgs) {
                long uid = uf.getUID(m);
                maxInBatch = Math.max(maxInBatch, uid);
                if (!(m instanceof MimeMessage)) {
                    continue;
                }
                MimeMessage mm = (MimeMessage) m;
                try {
                    ingestOne(mm, uid, inboundCfg.getFolder());
                } catch (Exception ex) {
                    log.warn("[mail] ingest message uid={} failed: {}", uid, ex.getMessage());
                }
            }

            cp.setLastUid(maxInBatch);
            mailImapCheckpointMapper.updateById(cp);
        } catch (Exception e) {
            log.warn("[mail] IMAP poll failed: {}", e.getMessage());
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(false);
                }
            } catch (Exception ignored) {
            }
            try {
                if (store != null) {
                    store.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void ingestOne(MimeMessage mm, long imapUid, String folderName) throws Exception {
        String messageId = firstHeader(mm, "Message-ID");
        if (messageId == null || messageId.trim().isEmpty()) {
            String from = firstAddress(mm.getFrom());
            String subj = mm.getSubject() != null ? mm.getSubject() : "";
            Date d = mm.getReceivedDate() != null ? mm.getReceivedDate() : mm.getSentDate();
            long t = d != null ? d.getTime() : 0L;
            messageId = "local-" + DigestUtil.md5Hex(from + "|" + subj + "|" + t + "|" + imapUid);
        } else {
            messageId = messageId.trim();
            if (messageId.length() > 250) {
                messageId = messageId.substring(0, 250);
            }
        }

        Integer cnt = mailInboundMapper.selectCount(
                Wrappers.<MailInbound>lambdaQuery().eq(MailInbound::getMessageId, messageId));
        if (cnt != null && cnt > 0) {
            return;
        }

        String body = mailMimeTextExtractor.extractBodyText(mm);
        String fromAddr = firstAddress(mm.getFrom());
        String toAddr = firstAddress(mm.getRecipients(Message.RecipientType.TO));
        String subject = mm.getSubject();
        Date received = mm.getReceivedDate() != null ? mm.getReceivedDate() : mm.getSentDate();
        String headersJson = MailThreadingHeaders.toJson(mm, objectMapper);

        MailInbound row = new MailInbound();
        row.setMessageId(messageId);
        row.setImapUid(imapUid);
        row.setFolder(folderName);
        row.setFromAddr(fromAddr);
        row.setToAddr(toAddr);
        row.setSubject(subject);
        row.setBodyText(body);
        row.setReceivedAt(received);
        row.setRawHeadersJson(headersJson);
        mailInboundMapper.insert(row);

        mailSuggestedReplyOrchestrator.orchestrateAfterPersistInbound(row);
    }

    private static String firstHeader(MimeMessage mm, String name) throws MessagingException {
        String[] h = mm.getHeader(name);
        if (h == null || h.length == 0) {
            return null;
        }
        return h[0];
    }

    private static String firstAddress(Address[] addrs) {
        if (addrs == null || addrs.length == 0) {
            return "";
        }
        Address a = addrs[0];
        if (a instanceof InternetAddress) {
            return ((InternetAddress) a).getAddress();
        }
        return a.toString();
    }
}
