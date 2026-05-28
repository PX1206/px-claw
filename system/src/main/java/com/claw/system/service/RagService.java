package com.claw.system.service;

import com.claw.common.tool.LoginUtil;
import com.claw.common.tool.StringUtil;
import com.claw.system.config.AiChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 知识库检索：Excel 话术行精确行首匹配 → Python FAISS → 本地关键词匹配。
 */
@Slf4j
@Service
public class RagService {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[\\s,，。、？!！?；;:\"\"''（）()\\[\\]【】]+");

    @Autowired
    private AiChatProperties aiChatProperties;

    @Autowired
    private FaissRetrievalClient faissRetrievalClient;

    @Autowired
    private ChatScriptService chatScriptService;

    /**
     * 检索优先级：① 话术行以用户问题开头（Excel 导出为「问题 话术 …」，与向量误召区分）；② Python FAISS；③ 关键词。
     *
     * @return 拼接后的资料文本；无命中时返回固定说明，供 Prompt 约束模型
     */
    public String search(String question) {
        List<String> lines = readFaqLines();

        Optional<String> exact = exactImportedLine(lines, question);
        if (exact.isPresent()) {
            return exact.get();
        }

        Optional<String> faiss = faissRetrievalClient.retrieve(question, LoginUtil.getUserId());
        if (faiss.isPresent()) {
            return faiss.get();
        }

        if (lines.isEmpty()) {
            return "无相关资料（知识库文件为空或不可读）";
        }
        return keywordSearchLines(lines, question);
    }

    /**
     * 简易关键词检索（回退路径；供调试或外层直接调用时仍单次读文件）。
     */
    public String keywordSearch(String question) {
        List<String> lines = readFaqLines();
        if (lines.isEmpty()) {
            return "无相关资料（知识库文件为空或不可读）";
        }
        return keywordSearchLines(lines, question);
    }

    private String keywordSearchLines(List<String> lines, String question) {
        List<String> result = lines.stream()
                .filter(line -> match(line, question))
                .limit(Math.max(1, aiChatProperties.getRagLimit()))
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            return "无相关资料";
        }
        return String.join("\n", result);
    }

    /**
     * Excel 导入行合并为一句「问题 话术 [补充…]」，与「问题」完全一致且作为行首时优先返回整行，
     * 避免「你是谁」在向量检索中与「你是智障…」等句式相近时被误召混入上下文。
     */
    private Optional<String> exactImportedLine(List<String> lines, String rawQuestion) {
        if (StringUtil.isBlank(rawQuestion)) {
            return Optional.empty();
        }
        String q = rawQuestion.trim();
        if (q.isEmpty()) {
            return Optional.empty();
        }
        for (String line : lines) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String trimmed = line.trim();
            // 与本行「问题」列一致并与后续列以空白分隔（与 joinKnowledgeCells 一致）
            if (trimmed.startsWith(q + " ") || trimmed.startsWith(q + "\t")) {
                return Optional.of(trimmed);
            }
            // 单行仅有问题无下文
            if (trimmed.equals(q)) {
                return Optional.of(trimmed);
            }
        }
        return Optional.empty();
    }

    private List<String> readFaqLines() {
        try {
            if (chatScriptService.countRows() > 0) {
                return chatScriptService.mergedKnowledgeLines();
            }
        } catch (Exception e) {
            log.warn("话术表不可用，改为读取 FAQ 文件: {}", e.getMessage());
        }
        Path userPath = chatScriptService.resolveFaqTxtPathForUser(LoginUtil.getUserId());
        try {
            if (Files.isRegularFile(userPath)) {
                try (Stream<String> lines = Files.lines(userPath, StandardCharsets.UTF_8)) {
                    return lines
                            .map(String::trim)
                            .filter(l -> StringUtil.isNotBlank(l) && !l.startsWith("#"))
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.warn("读取用户话术库失败，回退内置 FAQ: {}", userPath, e);
        }

        String location = aiChatProperties.getFaqFile();
        if (StringUtil.isBlank(location)) {
            return Collections.emptyList();
        }
        try {
            if (location.startsWith("classpath:")) {
                String path = location.substring("classpath:".length());
                ClassPathResource res = new ClassPathResource(path.startsWith("/") ? path.substring(1) : path);
                if (!res.exists()) {
                    log.warn("classpath FAQ 不存在: {}", location);
                    return Collections.emptyList();
                }
                try (InputStream in = res.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                     Stream<String> lines = br.lines()) {
                    return lines.collect(Collectors.toList());
                }
            }
            Path p = Paths.get(location);
            if (!Files.isRegularFile(p)) {
                log.warn("FAQ 文件不存在: {}", location);
                return Collections.emptyList();
            }
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取 FAQ 失败: {}", location, e);
            return Collections.emptyList();
        }
    }

    /**
     * 资料行与问题是否存在关键词重叠：分词后任一词命中即认为相关。
     */
    private boolean match(String text, String question) {
        if (StringUtil.isBlank(text) || StringUtil.isBlank(question)) {
            return false;
        }
        String q = question.trim();
        if (text.contains(q)) {
            return true;
        }
        List<String> tokens = tokenize(q);
        for (String t : tokens) {
            if (StringUtil.isBlank(t)) {
                continue;
            }
            // 避免单字（如「你」）命中多行话术
            if (t.codePointCount(0, t.length()) < 2 && !t.equalsIgnoreCase(q)) {
                continue;
            }
            if (text.toLowerCase(Locale.ROOT).contains(t.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<String> tokenize(String question) {
        String[] parts = TOKEN_SPLIT.split(question);
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (StringUtil.isNotBlank(p)) {
                out.add(p);
            }
        }
        if (out.isEmpty() && question.length() > 0) {
            for (int i = 0; i < question.length(); i++) {
                char c = question.charAt(i);
                if (!Character.isWhitespace(c) && !Character.isISOControl(c)) {
                    out.add(String.valueOf(c));
                }
            }
        }
        return out;
    }
}
