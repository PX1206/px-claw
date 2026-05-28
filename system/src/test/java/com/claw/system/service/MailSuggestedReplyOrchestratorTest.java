package com.claw.system.service;

import com.claw.system.entity.MailInbound;
import com.claw.system.entity.MailSuggestedReply;
import com.claw.system.enums.MailSuggestedReplyStatus;
import com.claw.system.mapper.MailSuggestedReplyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSuggestedReplyOrchestratorTest {

    @Mock
    private MailSuggestedReplyMapper suggestedReplyMapper;

    @Mock
    private RagService ragService;

    @Mock
    private AiChatInferenceService aiChatInferenceService;

    @Mock
    private MailEmailPromptBuilder mailEmailPromptBuilder;

    @InjectMocks
    private MailSuggestedReplyOrchestrator orchestrator;

    @Test
    void skipsWhenSuggestionAlreadyExists() throws Exception {
        MailInbound inbound = new MailInbound();
        inbound.setId(10L);
        inbound.setBodyText("q");

        when(suggestedReplyMapper.selectOne(any())).thenReturn(new MailSuggestedReply());

        orchestrator.orchestrateAfterPersistInbound(inbound);

        verify(ragService, never()).search(any());
        verify(aiChatInferenceService, never()).generate(any());
        verify(suggestedReplyMapper, never()).insert(any());
    }

    @Test
    void insertsPendingWhenAiSucceeds() throws Exception {
        MailInbound inbound = new MailInbound();
        inbound.setId(11L);
        inbound.setBodyText("问题");

        when(suggestedReplyMapper.selectOne(any())).thenReturn(null);
        when(ragService.search("问题")).thenReturn("ctx");
        when(mailEmailPromptBuilder.build("ctx", "问题")).thenReturn("prompt");
        when(aiChatInferenceService.generate("prompt")).thenReturn("答案");

        orchestrator.orchestrateAfterPersistInbound(inbound);

        ArgumentCaptor<MailSuggestedReply> cap = ArgumentCaptor.forClass(MailSuggestedReply.class);
        verify(suggestedReplyMapper).insert(cap.capture());
        assertEquals(MailSuggestedReplyStatus.PENDING_REVIEW.name(), cap.getValue().getStatus());
        assertEquals("答案", cap.getValue().getSuggestedBody());
        assertEquals("ctx", cap.getValue().getRetrievedContext());
    }
}
