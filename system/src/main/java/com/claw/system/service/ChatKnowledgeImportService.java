package com.claw.system.service;

import com.claw.system.vo.ChatScriptImportResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 兼容旧 Bean 名：话术 Excel 模版与入库逻辑已迁至 {@link ChatScriptService}。
 */
@Slf4j
@Service
public class ChatKnowledgeImportService {

    @Autowired
    private ChatScriptService chatScriptService;

    public byte[] exportTemplateXlsx() throws IOException {
        return chatScriptService.exportTemplateXlsx();
    }

    public ChatScriptImportResult importScriptExcel(MultipartFile file) throws Exception {
        return chatScriptService.importFromExcelMultipart(file);
    }
}
