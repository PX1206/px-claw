package com.claw.system.service;

import org.springframework.stereotype.Component;

/**
 * 与 {@link com.claw.system.controller.ChatController} 内 buildPrompt 语义对齐，并增加邮件场景说明。
 */
@Component
public class MailEmailPromptBuilder {

    public String build(String context, String question) {
        return "你是一个客服助手，只能通过邮件回复客户；只能根据以下资料回答。\n"
                + "语气正式、简洁；若资料中没有相关信息，请说「暂时没有相关信息」。\n\n"
                + "【资料】\n" + context + "\n\n"
                + "【客户问题】\n" + question;
    }
}
