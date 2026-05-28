package com.claw.system.service;

import com.claw.common.tool.StringUtil;
import com.claw.system.config.AiChatProperties;
import com.claw.system.dto.FaissReloadRequest;
import com.claw.system.dto.FaissSearchRequest;
import com.claw.system.dto.FaissSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 调用 Python FAISS 检索服务（语义向量检索）。
 */
@Slf4j
@Service
public class FaissRetrievalClient {

    @Autowired
    private AiChatProperties aiChatProperties;

    @Autowired
    @Qualifier("retrievalRestTemplate")
    private RestTemplate retrievalRestTemplate;

    /**
     * @param ownerUserId 登录用户 id，与话术文件 faq_{ownerUserId}.txt 一致；为 null 时 Python 可走旧版单文件
     */
    public Optional<String> retrieve(String question, Long ownerUserId) {
        if (!aiChatProperties.isRetrievalEnabled() || StringUtil.isBlank(aiChatProperties.getRetrievalBaseUrl())) {
            return Optional.empty();
        }
        String q = question != null ? question.trim() : "";
        if (StringUtil.isBlank(q)) {
            return Optional.empty();
        }
        String base = aiChatProperties.getRetrievalBaseUrl().trim().replaceAll("/+$", "");
        String url = base + "/search";
        FaissSearchRequest req = new FaissSearchRequest();
        req.setQuery(q);
        req.setTopK(Math.max(1, aiChatProperties.getRagLimit()));
        req.setOwnerUserId(ownerUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FaissSearchRequest> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<FaissSearchResponse> resp =
                    retrievalRestTemplate.exchange(url, HttpMethod.POST, entity, FaissSearchResponse.class);
            FaissSearchResponse body = resp.getBody();
            if (body == null || body.getOk() == null || !Boolean.TRUE.equals(body.getOk())) {
                log.debug("FAISS 返回不可用: {}", body != null ? body.getMessage() : null);
                return Optional.empty();
            }
            List<String> passages = body.getPassages();
            if (passages == null || passages.isEmpty()) {
                return Optional.empty();
            }
            List<String> cleaned = passages.stream()
                    .filter(StringUtil::isNotBlank)
                    .collect(Collectors.toList());
            if (cleaned.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(String.join("\n", cleaned));
        } catch (Exception e) {
            log.warn("FAISS 检索服务不可用，回退关键词检索: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 通知 Python 检索服务重建索引（话术文件更新后调用）。
     */
    public void reloadRemoteIndex(Long ownerUserId) {
        if (!aiChatProperties.isRetrievalEnabled() || StringUtil.isBlank(aiChatProperties.getRetrievalBaseUrl())) {
            return;
        }
        String base = aiChatProperties.getRetrievalBaseUrl().trim().replaceAll("/+$", "");
        String url = base + "/reload";
        try {
            FaissReloadRequest body = new FaissReloadRequest(ownerUserId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FaissReloadRequest> entity = new HttpEntity<>(body, headers);
            retrievalRestTemplate.postForEntity(url, entity, Object.class);
            log.info("已请求 FAISS 服务重建索引: {} ownerUserId={}", url, ownerUserId);
        } catch (RestClientException e) {
            log.warn("FAISS /reload 调用失败（可稍后重启 Python 服务）: {}", e.getMessage());
        }
    }
}
