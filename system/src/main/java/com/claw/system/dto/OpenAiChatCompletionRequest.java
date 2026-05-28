package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** OpenAI Chat Completions 兼容请求（非流式）。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatCompletionRequest {

    private String model;
    private Boolean stream;

    /** 等价于对话框中的采样温度 */
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private List<MessageBody> messages = new ArrayList<>();

    @Data
    public static class MessageBody {
        private String role;
        private String content;
    }
}
