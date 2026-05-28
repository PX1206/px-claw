package com.claw.system.service;

import com.claw.common.exception.BusinessException;
import com.claw.common.tool.StringUtil;
import com.claw.system.config.AiChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通过本机 llama.cpp 可执行文件推理（与 {@link AiChatProperties} 中路径对应）。
 */
@Slf4j
@Service
public class LlamaService {

    @Autowired
    private AiChatProperties aiChatProperties;

    /** llama.exe 与 .gguf 均已配置时为 true */
    public boolean isConfigured() {
        return StringUtil.isNotBlank(aiChatProperties.getLlamaExe())
                && StringUtil.isNotBlank(aiChatProperties.getModelPath());
    }

    public String chat(String prompt) throws Exception {
        if (!isConfigured()) {
            throw new BusinessException("未配置 ai.chat.llama-exe 或 ai.chat.model-path，无法调用本地模型");
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(aiChatProperties.getLlamaExe());
        cmd.add("-m");
        cmd.add(aiChatProperties.getModelPath());
        cmd.add("-p");
        cmd.add(prompt);
        cmd.add("-n");
        cmd.add(String.valueOf(Math.max(16, aiChatProperties.getMaxTokens())));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }

        int timeout = Math.max(30, aiChatProperties.getProcessTimeoutSeconds());
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new BusinessException("本地模型推理超时（" + timeout + " 秒）");
        }
        int exit = process.exitValue();
        if (exit != 0) {
            log.warn("llama 进程退出码: {}", exit);
        }
        return result.toString().trim();
    }
}
