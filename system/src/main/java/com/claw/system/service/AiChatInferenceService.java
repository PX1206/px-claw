package com.claw.system.service;

import com.claw.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 客服生成：优先 OpenAI 兼容 HTTP（与对话页配置同源），其次本机 llama 子进程。
 */
@Service
public class AiChatInferenceService {

    @Autowired
    private OpenAiCompatibleCompletionService openAiCompatibleCompletionService;

    @Autowired
    private LlamaService llamaService;

    public String generate(String prompt) throws Exception {
        if (openAiCompatibleCompletionService.isConfigured()) {
            return openAiCompatibleCompletionService.complete(prompt);
        }
        if (llamaService.isConfigured()) {
            return llamaService.chat(prompt);
        }
        throw new BusinessException(
                "未配置推理模型：请在 application.yml 中设置 ai.chat.completion-base-url "
                        + "与 ai.chat.completion-model（与同应用「对话」里 OpenAI 兼容源一致的地址与模型名）；"
                        + "或设置 ai.chat.llama-exe 与 ai.chat.model-path 调用本机 gguf。"
        );
    }
}
