package com.claw.system.service;

import com.claw.common.exception.BusinessException;
import com.claw.common.tool.StringUtil;
import com.claw.system.config.AiChatProperties;
import com.claw.system.dto.OpenAiChatCompletionRequest;
import com.claw.system.dto.OpenAiChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

/**
 * 调用 OpenAI 兼容 /chat/completions（stream=false），与 Electron 对话页的 openai_compat 数据源一致。
 */
@Slf4j
@Service
public class OpenAiCompatibleCompletionService {

    @Autowired
    private AiChatProperties aiChatProperties;

    @Autowired
    @Qualifier("completionRestTemplate")
    private RestTemplate completionRestTemplate;

    public boolean isConfigured() {
        return StringUtil.isNotBlank(aiChatProperties.getCompletionBaseUrl())
                && StringUtil.isNotBlank(aiChatProperties.getCompletionModel());
    }

    /**
     * 单轮 user 正文（通常已是拼装好的 Prompt）。
     */
    public String complete(String userContent) throws Exception {
        if (!isConfigured()) {
            throw new BusinessException("未配置 ai.chat.completion-base-url 或 ai.chat.completion-model");
        }
        String base = normalizeBaseUrl(aiChatProperties.getCompletionBaseUrl());
        String url = base + "/chat/completions";

        OpenAiChatCompletionRequest req = new OpenAiChatCompletionRequest();
        req.setModel(aiChatProperties.getCompletionModel().trim());
        req.setStream(false);
        req.setTemperature(aiChatProperties.getCompletionTemperature());
        req.setMaxTokens(Math.max(16, aiChatProperties.getMaxTokens()));

        OpenAiChatCompletionRequest.MessageBody mb = new OpenAiChatCompletionRequest.MessageBody();
        mb.setRole("user");
        mb.setContent(userContent != null ? userContent : "");
        req.setMessages(Collections.singletonList(mb));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
        String key = aiChatProperties.getCompletionApiKey();
        if (StringUtil.isNotBlank(key)) {
            headers.setBearerAuth(key.trim());
        }

        HttpEntity<OpenAiChatCompletionRequest> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<OpenAiChatCompletionResponse> resp =
                    completionRestTemplate.exchange(url, HttpMethod.POST, entity, OpenAiChatCompletionResponse.class);
            OpenAiChatCompletionResponse body = resp.getBody();
            if (body == null) {
                throw new BusinessException("推理接口返回为空");
            }
            String text = body.firstAssistantContentTrimmed();
            if (StringUtil.isBlank(text)) {
                throw new BusinessException("推理接口未返回可用正文（choices[0].message.content）");
            }
            return text;
        } catch (HttpStatusCodeException e) {
            String raw = e.getResponseBodyAsString();
            String excerpt = raw != null ? raw.trim() : "";
            if (excerpt.length() > 480) {
                excerpt = excerpt.substring(0, 480) + "…";
            }
            log.warn("OpenAI 兼容推理 HTTP {} {}", e.getRawStatusCode(), excerpt);
            throw new BusinessException("推理接口错误 HTTP "
                    + e.getRawStatusCode()
                    + (StringUtil.isNotBlank(excerpt) ? ": " + excerpt : ""));
        }
    }

    static String normalizeBaseUrl(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().replaceAll("/+$", "");
        return s;
    }
}
