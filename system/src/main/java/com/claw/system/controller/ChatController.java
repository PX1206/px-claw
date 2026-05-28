package com.claw.system.controller;

import com.claw.common.api.ApiCode;
import com.claw.common.api.ApiResult;
import com.claw.common.tool.StringUtil;
import com.claw.system.param.AiCsSessionSnapshotParam;
import com.claw.system.param.ChatRequest;
import com.claw.system.param.ChatScriptUpsertParam;
import com.claw.system.service.AiCsSessionArchiveService;
import com.claw.system.service.ChatKnowledgeImportService;
import com.claw.system.service.AiChatInferenceService;
import com.claw.system.service.ChatScriptService;
import com.claw.system.service.RagService;
import com.claw.system.vo.ChatRagContextVo;
import com.claw.system.vo.ChatReplyVo;
import com.claw.system.vo.ChatScriptImportResult;
import com.claw.system.vo.ChatScriptPageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * AI 客服：知识库检索（+ 服务端可选全流程对话，供脚本调试）；Electron 桌面端推荐使用「检索 + 本地模型配置」分流。
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@Api(value = "AI客服", tags = {"AI客服"})
public class ChatController {

    @Autowired
    private RagService ragService;

    @Autowired
    private AiChatInferenceService aiChatInferenceService;

    @Autowired
    private ChatKnowledgeImportService chatKnowledgeImportService;

    @Autowired
    private ChatScriptService chatScriptService;

    @Autowired
    private AiCsSessionArchiveService aiCsSessionArchiveService;

    /**
     * 桌面端同步 AI 客服会话全文（按用户 + 客户端会话 id 幂等），供数据库分析与排查。
     */
    @PostMapping(value = "/ai-cs/session/snapshot", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "归档 AI 客服会话快照")
    public ApiResult<Void> snapshotAiCsSession(@Validated @RequestBody AiCsSessionSnapshotParam body) {
        aiCsSessionArchiveService.saveSnapshot(body);
        return ApiResult.ok(null, "已归档");
    }

    /**
     * 话术列表分页（含导入时间、导入用户）；默认 current=1、size=20。
     */
    @GetMapping("/knowledge/scripts")
    @ApiOperation(value = "话术列表（分页）")
    public ApiResult<ChatScriptPageVO> listScripts(
            @RequestParam(value = "current", required = false, defaultValue = "1") long current,
            @RequestParam(value = "size", required = false, defaultValue = "20") long size
    ) {
        return ApiResult.ok(chatScriptService.pageVo(current, size));
    }

    /**
     * 将当前用户库中话术写入 faq 文件并请求 Python 检索服务重载缓存（多终端 / 进入聊天前可调用）。
     */
    @PostMapping("/knowledge/sync-faq")
    @ApiOperation(value = "同步话术到检索侧")
    public ApiResult<Void> syncKnowledgeToDiskAndReload() throws Exception {
        chatScriptService.syncFaqDiskAndReload();
        return ApiResult.ok(null, "话术已同步到检索服务");
    }

    /**
     * 删除单条话术
     */
    @DeleteMapping("/knowledge/scripts/{id}")
    @ApiOperation(value = "删除话术")
    public ApiResult<Void> deleteScript(@PathVariable("id") Long id) throws Exception {
        chatScriptService.deleteById(id);
        return ApiResult.ok(null, "已删除");
    }

    /**
     * 修改话术（问题 / 话术 / 补充）
     */
    @PutMapping("/knowledge/scripts/{id}")
    @ApiOperation(value = "修改话术")
    public ApiResult<Void> updateScript(
            @PathVariable("id") Long id, @Validated @RequestBody ChatScriptUpsertParam body) throws Exception {
        chatScriptService.updateRow(id, body);
        return ApiResult.ok(null, "已保存");
    }

    /**
     * Excel 导入话术：同问题覆盖更新；并请求 Python 检索服务 reload
     */
    @PostMapping(value = "/knowledge/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "导入话术 Excel", response = ChatScriptImportResult.class)
    public ApiResult<ChatScriptImportResult> importKnowledge(@RequestPart("file") MultipartFile file)
            throws Exception {
        ChatScriptImportResult r = chatKnowledgeImportService.importScriptExcel(file);
        String msg =
                String.format(
                        "新增 %d 条，更新 %d 条（若已启动 Python 检索服务会自动重建索引）",
                        r.getInserted(),
                        r.getUpdated());
        return ApiResult.ok(r, msg);
    }

    /**
     * 下载话术导入模版（.xlsx）
     */
    @GetMapping(value = "/knowledge/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @ApiOperation(value = "下载话术 Excel 模版")
    public ResponseEntity<byte[]> downloadKnowledgeTemplate() throws Exception {
        byte[] bytes = chatKnowledgeImportService.exportTemplateXlsx();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("话术导入模板.xlsx", StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(disposition);
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * 与问题参数二选一即可：query 参数 或 JSON body
     */
    @PostMapping
    @ApiOperation(value = "对话", response = ChatReplyVo.class)
    public ApiResult<ChatReplyVo> chat(
            @RequestParam(value = "question", required = false) String questionParam,
            @RequestBody(required = false) ChatRequest body
    ) throws Exception {
        String question = resolveQuestion(questionParam, body);
        if (StringUtil.isBlank(question)) {
            return ApiResult.result(ApiCode.FAIL, "问题 question 不能为空", (ChatReplyVo) null);
        }

        String context = ragService.search(question.trim());
        String prompt = buildPrompt(context, question.trim());
        String answer = aiChatInferenceService.generate(prompt);

        ChatReplyVo vo = ChatReplyVo.builder()
                .answer(answer)
                .retrievedContext(context)
                .build();
        return ApiResult.ok(vo);
    }

    /**
     * 仅返回知识库检索片段，供桌面端使用「模型配置」中的 provider 推理（与主对话同源）。
     */
    @PostMapping(value = "/rag-context", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "检索知识片段", response = ChatRagContextVo.class)
    public ApiResult<ChatRagContextVo> ragContext(@RequestBody(required = false) ChatRequest body) throws Exception {
        String question =
                body != null && StringUtil.isNotBlank(body.getQuestion()) ? body.getQuestion().trim() : null;
        if (StringUtil.isBlank(question)) {
            return ApiResult.result(ApiCode.FAIL, "问题 question 不能为空", (ChatRagContextVo) null);
        }
        String ctx = ragService.search(question);
        ChatRagContextVo vo =
                ChatRagContextVo.builder().retrievedContext(ctx != null ? ctx : "").build();
        return ApiResult.ok(vo);
    }

    /**
     * 便于浏览器或脚本直接 GET 调试
     */
    @GetMapping
    @ApiOperation(value = "对话（GET 调试）", response = ChatReplyVo.class)
    public ApiResult<ChatReplyVo> chatGet(@RequestParam("question") String question) throws Exception {
        return chat(question, null);
    }

    private static String resolveQuestion(String param, ChatRequest body) {
        if (StringUtil.isNotBlank(param)) {
            return param;
        }
        if (body != null && StringUtil.isNotBlank(body.getQuestion())) {
            return body.getQuestion();
        }
        return null;
    }

    private String buildPrompt(String context, String question) {
        return "你是一个客服助手，只能根据以下资料回答。\n"
                + "如果资料中没有相关信息，请说「暂时没有相关信息」。\n\n"
                + "【资料】\n" + context + "\n\n"
                + "【问题】\n" + question;
    }
}
