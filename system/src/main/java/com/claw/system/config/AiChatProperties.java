package com.claw.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 客服（llama.cpp + 本地知识库）配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties {

    /**
     * llama.cpp main.exe 或 llama-cli 可执行文件绝对路径（Windows 示例：E:/llama/main.exe）
     */
    private String llamaExe = "";

    /**
     * .gguf 模型文件路径
     */
    private String modelPath = "";

    /**
     * -n 生成的最大 token 数
     */
    private int maxTokens = 256;

    /**
     * FAQ 文本路径：classpath 前缀（如 classpath:data/faq.txt）或磁盘绝对/相对路径
     */
    private String faqFile = "classpath:data/faq.txt";

    /**
     * 检索最多返回几条资料行
     */
    private int ragLimit = 3;

    /**
     * llama 子进程最长等待时间（秒）
     */
    private int processTimeoutSeconds = 300;

    /**
     * 是否启用 Python FAISS 语义检索（启用后优先调用，失败则回退关键词检索）
     */
    private boolean retrievalEnabled = true;

    /**
     * Python 检索服务根地址，例如 http://127.0.0.1:9170
     */
    private String retrievalBaseUrl = "http://127.0.0.1:9170";

    /**
     * 连接 Python 检索服务的超时（毫秒）
     */
    private int retrievalConnectTimeoutMs = 5000;

    /**
     * 读取 Python 检索响应的超时（毫秒；首次加载嵌入模型可能较慢）
     */
    private int retrievalReadTimeoutMs = 120000;

    /**
     * 用户话术库文件（Excel 导入追加写入）。文件存在时优先作为知识库来源；可与 Python FAQ_FILE 指向同一路径以保持语义检索一致。
     */
    private String userFaqPath = "";

    // ----- 与 Electron「对话」中 openai_compat 同源：OpenAI 兼容 /v1/chat/completions（服务端非流式） -----

    /**
     * 推理接口 Base URL，一般以 /v1 结尾。例：云端 https://api.xxx/v1 ，本机 llama-server：http://127.0.0.1:8080/v1
     */
    private String completionBaseUrl = "";

    /**
     * Bearer Token，无密钥可留空
     */
    private String completionApiKey = "";

    /**
     * 模型名或兼容端要求的 model 字段（必填，与对话框里填的一致）
     */
    private String completionModel = "";

    /**
     * 采样温度
     */
    private double completionTemperature = 0.7;

    /**
     * 连接 OpenAI 兼容接口的超时（毫秒）
     */
    private int completionConnectTimeoutMs = 15000;

    /**
     * 读取推理响应的超时（毫秒；长输出可调大）
     */
    private int completionReadTimeoutMs = 180000;
}
