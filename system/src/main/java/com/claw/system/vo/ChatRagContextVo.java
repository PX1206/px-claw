package com.claw.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 客服：仅检索结果（桌面端用当前「模型配置」推理时拉取）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRagContextVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 本次命中的知识库片段拼接（无命中可为空）。
     */
    private String retrievedContext;
}
