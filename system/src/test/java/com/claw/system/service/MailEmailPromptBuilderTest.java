package com.claw.system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MailEmailPromptBuilderTest {

    @Test
    void buildsPromptContainingContextQuestionAndEmailTone() {
        MailEmailPromptBuilder b = new MailEmailPromptBuilder();
        String p = b.build("ctx-line", "用户问什么");
        assertTrue(p.contains("ctx-line"));
        assertTrue(p.contains("用户问什么"));
        assertTrue(p.contains("邮件") || p.contains("客服"));
    }
}
