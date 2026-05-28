package com.claw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.exception.BusinessException;
import com.claw.common.tool.LoginUtil;
import com.claw.common.tool.StringUtil;
import com.claw.system.config.AiChatProperties;
import com.claw.system.entity.ChatScript;
import com.claw.system.mapper.ChatScriptMapper;
import com.claw.system.param.ChatScriptUpsertParam;
import com.claw.system.util.ChatKnowledgeMerger;
import com.claw.system.vo.ChatScriptImportResult;
import com.claw.system.vo.ChatScriptPageVO;
import com.claw.system.vo.ChatScriptVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 话术表 CRUD；与 faq.txt + FAISS 同步；有数据后检索以数据库为准重写文件。
 */
@Slf4j
@Service
public class ChatScriptService {

    private static final DataFormatter FORMATTER = new DataFormatter();

    @Autowired
    private ChatScriptMapper chatScriptMapper;

    @Autowired
    private AiChatProperties aiChatProperties;

    @Autowired
    private FaissRetrievalClient faissRetrievalClient;

    /** 当前登录用户名下话术条数 */
    public long countRows() {
        return chatScriptMapper.selectCount(qwOwner());
    }

    private LambdaQueryWrapper<ChatScript> qwOwner() {
        return Wrappers.<ChatScript>lambdaQuery()
                .eq(ChatScript::getOwnerUserId, LoginUtil.getUserId());
    }

    public byte[] exportTemplateXlsx() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("话术");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("问题");
            header.createCell(1).setCellValue("话术");
            header.createCell(2).setCellValue("补充（可选）");
            sheet.setColumnWidth(0, 24 * 256);
            sheet.setColumnWidth(1, 42 * 256);
            sheet.setColumnWidth(2, 24 * 256);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** 当前登录用户话术合并后的 faq.txt 等价行序 */
    public List<String> mergedKnowledgeLines() {
        List<ChatScript> rows =
                chatScriptMapper.selectList(qwOwner().orderByAsc(ChatScript::getId));
        return rows.stream().map(ChatScriptService::mergedLineEntity).collect(Collectors.toList());
    }

    private static String mergedLineEntity(ChatScript cs) {
        return ChatKnowledgeMerger.mergeCells(
                cs.getQuestion(), cs.getScriptText(), cs.getSupplement());
    }

    /**
     * 话术列表分页（按导入时间、id 倒序，新导入在前）。
     */
    public ChatScriptPageVO pageVo(long current, long size) {
        long pg = Math.max(1L, current);
        long sz = Math.min(100L, Math.max(1L, size));
        Page<ChatScript> page = new Page<>(pg, sz);
        LambdaQueryWrapper<ChatScript> w =
                qwOwner()
                        .orderByDesc(ChatScript::getImportTime)
                        .orderByDesc(ChatScript::getId);
        IPage<ChatScript> mp = chatScriptMapper.selectPage(page, w);
        List<ChatScriptVO> records =
                mp.getRecords().stream().map(ChatScriptService::toVo).collect(Collectors.toList());
        return ChatScriptPageVO.builder()
                .records(records)
                .total(mp.getTotal())
                .size(mp.getSize())
                .current(mp.getCurrent())
                .pages(mp.getPages())
                .build();
    }

    private static ChatScriptVO toVo(ChatScript e) {
        return ChatScriptVO.builder()
                .id(e.getId())
                .question(e.getQuestion())
                .scriptText(e.getScriptText())
                .supplement(e.getSupplement())
                .importTime(e.getImportTime())
                .importUsername(e.getImportUsername())
                .importBatchId(e.getImportBatchId())
                .build();
    }

    /**
     * 将当前库中话术重写用户 faq 文件（非空）；空库则删除文件以避免与旧数据混淆。
     */
    public void syncFaqDiskAndReload() throws IOException {
        Long uid = LoginUtil.getUserId();
        Path target = resolveFaqTxtPathForUser(uid);
        Files.createDirectories(target.getParent());

        List<String> lines = mergedKnowledgeLines();
        if (lines.isEmpty()) {
            Files.deleteIfExists(target);
            log.info("当前用户话术表为空，已删除: {}", target);
        } else {
            StringBuilder block = new StringBuilder();
            for (String line : lines) {
                block.append(line).append('\n');
            }
            Files.write(target, block.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("已重写用户话术文件 {} ({} 行)", target, lines.size());
        }
        faissRetrievalClient.reloadRemoteIndex(uid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) throws Exception {
        if (id == null) {
            throw new BusinessException("记录不存在");
        }
        int n =
                chatScriptMapper.delete(
                        Wrappers.<ChatScript>lambdaQuery()
                                .eq(ChatScript::getId, id)
                                .eq(ChatScript::getOwnerUserId, LoginUtil.getUserId()));
        if (n == 0) {
            throw new BusinessException("记录不存在");
        }
        syncFaqDiskAndReload();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRow(Long id, ChatScriptUpsertParam p) throws Exception {
        ChatScript cs =
                chatScriptMapper.selectOne(
                        Wrappers.<ChatScript>lambdaQuery()
                                .eq(ChatScript::getId, id)
                                .eq(ChatScript::getOwnerUserId, LoginUtil.getUserId()));
        if (cs == null) {
            throw new BusinessException("记录不存在");
        }
        cs.setQuestion(p.getQuestion().trim());
        cs.setScriptText(p.getScriptText().trim());
        cs.setSupplement(StringUtil.isNotBlank(p.getSupplement()) ? p.getSupplement().trim() : null);
        cs.setUpdateTime(new Date());
        chatScriptMapper.updateById(cs);
        syncFaqDiskAndReload();
    }

    /**
     * Excel 导入若干行：同一归属用户下若「问题」与已有记录相同（完全匹配），则更新话术与补充并刷新导入信息。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatScriptImportResult importFromExcelMultipart(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择 Excel 文件");
        }
        String name = file.getOriginalFilename();
        if (name == null
                || (!name.toLowerCase().endsWith(".xlsx") && !name.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException("仅支持 .xlsx 或 .xls");
        }
        List<MergedTriple> triples = parseExcelTriples(file);
        if (triples.isEmpty()) {
            throw new BusinessException("未解析到有效话术行");
        }
        Date now = new Date();
        Long uid = LoginUtil.getUserId();
        String unm = LoginUtil.getNickname();
        String batchId = UUID.randomUUID().toString();

        int inserted = 0;
        int updated = 0;
        for (MergedTriple tr : triples) {
            ChatScript existing = findOwnedByQuestion(uid, tr.q);
            if (existing != null) {
                existing.setScriptText(tr.s);
                existing.setSupplement(tr.e);
                existing.setImportTime(now);
                existing.setImportUserId(uid);
                existing.setImportUsername(unm);
                existing.setImportBatchId(batchId);
                existing.setUpdateTime(now);
                chatScriptMapper.updateById(existing);
                updated++;
            } else {
                ChatScript row = new ChatScript();
                row.setOwnerUserId(uid);
                row.setQuestion(tr.q);
                row.setScriptText(tr.s);
                row.setSupplement(tr.e);
                row.setImportTime(now);
                row.setImportUserId(uid);
                row.setImportUsername(unm);
                row.setImportBatchId(batchId);
                row.setCreateTime(now);
                row.setUpdateTime(now);
                chatScriptMapper.insert(row);
                inserted++;
            }
        }
        syncFaqDiskAndReload();
        log.info("Excel 话术导入 batch={} inserted={} updated={}", batchId, inserted, updated);
        return new ChatScriptImportResult(inserted, updated);
    }

    /** 当前用户名下按问题文案精确匹配的一条（多条时取 id 最小）。 */
    private ChatScript findOwnedByQuestion(Long ownerUserId, String question) {
        String q = question != null ? question : "";
        List<ChatScript> list =
                chatScriptMapper.selectList(
                        Wrappers.<ChatScript>lambdaQuery()
                                .eq(ChatScript::getOwnerUserId, ownerUserId)
                                .eq(ChatScript::getQuestion, q)
                                .orderByAsc(ChatScript::getId)
                                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    private static final class MergedTriple {
        /** 补充分列，空则 null 写入库 */
        final String q;
        final String s;
        final String e;

        private MergedTriple(String q, String s, String e) {
            this.q = q;
            this.s = s;
            this.e = e;
        }
    }

    /** 结构化解析后可同时写库字段与合并校验 */
    private List<MergedTriple> parseExcelTriples(MultipartFile file) throws Exception {
        List<MergedTriple> out = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                return out;
            }
            int first = sheet.getFirstRowNum();
            int last = sheet.getLastRowNum();
            boolean skipHeader = false;
            Row row0 = sheet.getRow(first);
            if (row0 != null) {
                String a0 = cellString(row0.getCell(0));
                if (a0 != null
                        && (a0.contains("问题")
                                || a0.contains("话术")
                                || a0.contains("标题")
                                || "question".equalsIgnoreCase(a0))) {
                    skipHeader = true;
                }
            }
            int start = skipHeader ? first + 1 : first;
            for (int r = start; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String c0 = cellString(row.getCell(0));
                String c1 = cellString(row.getCell(1));
                String c2 = cellString(row.getCell(2));
                String line = ChatKnowledgeMerger.mergeCells(c0, c1, c2);
                if (line == null) {
                    continue;
                }
                String sup = StringUtil.isNotBlank(c2) ? c2.trim() : null;
                out.add(
                        new MergedTriple(
                                StringUtil.isNotBlank(c0) ? c0.trim() : "",
                                StringUtil.isNotBlank(c1) ? c1.trim() : "",
                                sup));
            }
        }
        return out;
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        String s = FORMATTER.formatCellValue(cell);
        return s == null ? "" : s.trim();
    }

    /**
     * 与 Python 检索服务约定：每台机器用户目录 ~/.px-claw/faq_{ownerUserId}.txt。
     * 配置了 user-faq-path 时：若为目录则在目录下生成；若为文件则在同目录生成 faq_{ownerUserId}.txt。
     */
    public Path resolveFaqTxtPathForUser(Long ownerUserId) {
        String custom = aiChatProperties.getUserFaqPath();
        if (StringUtil.isNotBlank(custom)) {
            Path p = Paths.get(custom).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                return p.resolve("faq_" + ownerUserId + ".txt");
            }
            Path parent = p.getParent();
            if (parent != null) {
                return parent.resolve("faq_" + ownerUserId + ".txt");
            }
        }
        return Paths.get(System.getProperty("user.home"), ".px-claw", "faq_" + ownerUserId + ".txt")
                .toAbsolutePath()
                .normalize();
    }
}
