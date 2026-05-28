package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** OpenAI Chat Completions 兼容响应摘要。 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiChatCompletionResponse {

    private List<Choice> choices;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private MessageRole message;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageRole {
            private String role;
            private String content;
        }
    }

    public String firstAssistantContentTrimmed() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        Choice c = choices.get(0);
        if (c == null || c.message == null) {
            return "";
        }
        String t = c.message.getContent();
        return t == null ? "" : t.trim();
    }
}
