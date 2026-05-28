package com.claw.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 客服对话返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReplyVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型回答
     */
    private String answer;

    /**
     * 本次命中的知识库原文（便于排查；无命中时为空或说明）
     */
    private String retrievedContext;
}
