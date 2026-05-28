package com.claw.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.claw.system.entity.MailInbound;
import com.claw.system.entity.MailSuggestedReply;
import com.claw.system.enums.MailSuggestedReplyStatus;
import com.claw.system.mapper.MailSuggestedReplyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 入站邮件入库后：RAG + 推理生成建议回复（幂等：同一 inbound 仅一条建议）。
 */
@Slf4j
@Service
public class MailSuggestedReplyOrchestrator {

    @Autowired
    private MailSuggestedReplyMapper suggestedReplyMapper;

    @Autowired
    private RagService ragService;

    @Autowired
    private AiChatInferenceService aiChatInferenceService;

    @Autowired
    private MailEmailPromptBuilder mailEmailPromptBuilder;

    public void orchestrateAfterPersistInbound(MailInbound inbound) {
        if (inbound == null || inbound.getId() == null) {
            return;
        }
        MailSuggestedReply existing = suggestedReplyMapper.selectOne(
                Wrappers.<MailSuggestedReply>lambdaQuery()
                        .eq(MailSuggestedReply::getInboundId, inbound.getId())
                        .last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        String question = inbound.getBodyText();
        if (question == null || question.trim().isEmpty()) {
            question = inbound.getSubject() != null ? inbound.getSubject() : "";
        }
        MailSuggestedReply row = new MailSuggestedReply();
        row.setInboundId(inbound.getId());
        row.setRetrievedContext("");
        row.setSuggestedBody("");
        row.setStatus(MailSuggestedReplyStatus.FAILED.name());
        try {
            String ctx = ragService.search(question.trim());
            if (ctx == null) {
                ctx = "";
            }
            row.setRetrievedContext(ctx);
            String prompt = mailEmailPromptBuilder.build(ctx, question.trim());
            String answer = aiChatInferenceService.generate(prompt);
            row.setSuggestedBody(answer != null ? answer : "");
            row.setStatus(MailSuggestedReplyStatus.PENDING_REVIEW.name());
            row.setLastError(null);
        } catch (Exception e) {
            log.warn("[mail] AI generate failed for inboundId={}", inbound.getId(), e);
            row.setStatus(MailSuggestedReplyStatus.FAILED.name());
            row.setLastError(trunc(e.getMessage()));
            row.setSuggestedBody("");
        }
        suggestedReplyMapper.insert(row);
    }

    private static String trunc(String m) {
        if (m == null) {
            return "";
        }
        return m.length() > 1000 ? m.substring(0, 1000) : m;
    }
}
