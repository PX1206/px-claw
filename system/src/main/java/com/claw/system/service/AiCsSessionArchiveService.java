package com.claw.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.AiCsChatMessage;
import com.claw.system.entity.AiCsChatSession;
import com.claw.system.mapper.AiCsChatMessageMapper;
import com.claw.system.mapper.AiCsChatSessionMapper;
import com.claw.system.param.AiCsSessionSnapshotParam;
import com.claw.system.param.AiCsSessionSnapshotParam.AiCsSnapshotMessageItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 归档桌面端 AI 客服会话，供运营与问题排查（与本地 localStorage 快照对应）。
 */
@Slf4j
@Service
public class AiCsSessionArchiveService {

    @Autowired
    private AiCsChatSessionMapper aiCsChatSessionMapper;

    @Autowired
    private AiCsChatMessageMapper aiCsChatMessageMapper;

    /**
     * Upsert 会话并全量替换该会话消息（与客户端当前列表一致）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSnapshot(AiCsSessionSnapshotParam p) {
        Long uid = LoginUtil.getUserId();
        AiCsChatSession sess =
                aiCsChatSessionMapper.selectOne(
                        Wrappers.<AiCsChatSession>lambdaQuery()
                                .eq(AiCsChatSession::getUserId, uid)
                                .eq(AiCsChatSession::getClientSessionId, p.getClientSessionId()));
        Date now = new Date();
        if (sess == null) {
            sess = new AiCsChatSession();
            sess.setUserId(uid);
            sess.setClientSessionId(p.getClientSessionId());
            sess.setCreateTime(now);
            aiCsChatSessionMapper.insert(sess);
        }
        sess.setTitle(p.getTitle());
        sess.setProviderId(p.getProviderId());
        sess.setClientUpdatedAt(p.getClientUpdatedAt());
        sess.setUpdateTime(now);
        aiCsChatSessionMapper.updateById(sess);

        aiCsChatMessageMapper.delete(
                Wrappers.<AiCsChatMessage>lambdaQuery()
                        .eq(AiCsChatMessage::getSessionId, sess.getId()));

        int seq = 0;
        for (AiCsSnapshotMessageItem m : p.getMessages()) {
            if (m == null) {
                continue;
            }
            String role = m.getRole();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            AiCsChatMessage row = new AiCsChatMessage();
            row.setSessionId(sess.getId());
            row.setRole(role);
            row.setContent(m.getContent());
            row.setRagContext(m.getRagContext());
            row.setSortOrder(++seq);
            Long ca = m.getCreatedAt();
            row.setCreatedAtMs(ca);
            row.setCreateTime(now);
            aiCsChatMessageMapper.insert(row);
        }
        log.debug(
                "AI客服快照已保存 user={} clientSession={} messages={}",
                uid,
                p.getClientSessionId(),
                seq);
    }
}
