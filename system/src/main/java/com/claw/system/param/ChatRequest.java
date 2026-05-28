package com.claw.system.param;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 客服对话请求
 */
@Data
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户问题
     */
    private String question;
}
