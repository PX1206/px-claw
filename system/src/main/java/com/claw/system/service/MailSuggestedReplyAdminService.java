package com.claw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.exception.BusinessException;
import com.claw.common.pagination.Paging;
import com.claw.common.tool.LoginUtil;
import com.claw.common.tool.StringUtil;
import com.claw.system.entity.MailInbound;
import com.claw.system.entity.MailSuggestedReply;
import com.claw.system.enums.MailSuggestedReplyStatus;
import com.claw.system.mapper.MailInboundMapper;
import com.claw.system.mapper.MailSuggestedReplyMapper;
import com.claw.system.param.MailSuggestedReplyPageParam;
import com.claw.system.vo.MailSuggestedReplyDetailVO;
import com.claw.system.vo.MailSuggestedReplyListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MailSuggestedReplyAdminService {

    @Autowired
    private MailSuggestedReplyMapper mailSuggestedReplyMapper;

    @Autowired
    private MailInboundMapper mailInboundMapper;

    @Autowired
    private MailSendService mailSendService;

    public Paging<MailSuggestedReplyListVO> pageList(MailSuggestedReplyPageParam param) {
        LambdaQueryWrapper<MailSuggestedReply> w = new LambdaQueryWrapper<>();
        if (StringUtil.isNotBlank(param.getStatus())) {
            w.eq(MailSuggestedReply::getStatus, param.getStatus().trim());
        }
        w.orderByDesc(MailSuggestedReply::getCreateTime);

        Page<MailSuggestedReply> page = new Page<>(param.getPageIndex(), param.getPageSize());
        IPage<MailSuggestedReply> ip = mailSuggestedReplyMapper.selectPage(page, w);

        List<MailSuggestedReplyListVO> records = new ArrayList<>();
        for (MailSuggestedReply r : ip.getRecords()) {
            MailSuggestedReplyListVO vo = new MailSuggestedReplyListVO();
            vo.setId(r.getId());
            vo.setInboundId(r.getInboundId());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreateTime());
            MailInbound in = mailInboundMapper.selectById(r.getInboundId());
            if (in != null) {
                vo.setSubject(in.getSubject());
                vo.setFromAddr(in.getFromAddr());
            }
            records.add(vo);
        }
        return new Paging<>(ip, records);
    }

    public MailSuggestedReplyDetailVO getDetail(Long id) {
        MailSuggestedReply r = mailSuggestedReplyMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("记录不存在");
        }
        MailInbound in = mailInboundMapper.selectById(r.getInboundId());
        if (in == null) {
            throw new BusinessException("关联入站邮件不存在");
        }
        MailSuggestedReplyDetailVO vo = new MailSuggestedReplyDetailVO();
        vo.setId(r.getId());
        vo.setInboundId(r.getInboundId());
        vo.setStatus(r.getStatus());
        vo.setSuggestedBody(r.getSuggestedBody());
        vo.setRetrievedContext(r.getRetrievedContext());
        vo.setLastError(r.getLastError());
        vo.setReviewedByUserId(r.getReviewedByUserId());
        vo.setReviewedAt(r.getReviewedAt());
        vo.setSentAt(r.getSentAt());
        vo.setCreateTime(r.getCreateTime());
        vo.setInboundSubject(in.getSubject());
        vo.setInboundFrom(in.getFromAddr());
        vo.setInboundBodyText(in.getBodyText());
        vo.setRawHeadersJson(in.getRawHeadersJson());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBody(Long id, String suggestedBody) {
        MailSuggestedReply r = mailSuggestedReplyMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("记录不存在");
        }
        if (!MailSuggestedReplyStatus.PENDING_REVIEW.name().equals(r.getStatus())
                && !MailSuggestedReplyStatus.FAILED.name().equals(r.getStatus())) {
            throw new BusinessException("当前状态不可编辑");
        }
        MailSuggestedReply upd = new MailSuggestedReply();
        upd.setId(id);
        upd.setSuggestedBody(suggestedBody);
        mailSuggestedReplyMapper.updateById(upd);
    }

    @Transactional(rollbackFor = Exception.class)
    public void discard(Long id) {
        MailSuggestedReply r = mailSuggestedReplyMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("记录不存在");
        }
        if (!MailSuggestedReplyStatus.PENDING_REVIEW.name().equals(r.getStatus())
                && !MailSuggestedReplyStatus.FAILED.name().equals(r.getStatus())) {
            throw new BusinessException("当前状态不可丢弃");
        }
        MailSuggestedReply upd = new MailSuggestedReply();
        upd.setId(id);
        upd.setStatus(MailSuggestedReplyStatus.DISCARDED.name());
        mailSuggestedReplyMapper.updateById(upd);
    }

    @Transactional(rollbackFor = Exception.class)
    public void send(Long id) {
        MailSuggestedReply r = mailSuggestedReplyMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("记录不存在");
        }
        if (!MailSuggestedReplyStatus.PENDING_REVIEW.name().equals(r.getStatus())
                && !MailSuggestedReplyStatus.FAILED.name().equals(r.getStatus())) {
            throw new BusinessException("仅待审核或失败记录可发送");
        }
        String body = r.getSuggestedBody();
        if (body == null || body.trim().isEmpty()) {
            throw new BusinessException("建议正文为空，无法发送");
        }
        MailInbound in = mailInboundMapper.selectById(r.getInboundId());
        if (in == null) {
            throw new BusinessException("关联入站邮件不存在");
        }

        Long userId = LoginUtil.getUserId();
        Date now = new Date();

        try {
            mailSendService.sendReply(in, body.trim());
            MailSuggestedReply upd = new MailSuggestedReply();
            upd.setId(id);
            upd.setStatus(MailSuggestedReplyStatus.SENT.name());
            upd.setReviewedByUserId(userId);
            upd.setReviewedAt(now);
            upd.setSentAt(now);
            upd.setLastError(null);
            mailSuggestedReplyMapper.updateById(upd);
        } catch (Exception e) {
            MailSuggestedReply upd = new MailSuggestedReply();
            upd.setId(id);
            upd.setStatus(MailSuggestedReplyStatus.FAILED.name());
            upd.setLastError(e.getMessage() != null && e.getMessage().length() > 1000
                    ? e.getMessage().substring(0, 1000) : e.getMessage());
            mailSuggestedReplyMapper.updateById(upd);
            throw new BusinessException("发送失败: " + e.getMessage());
        }
    }
}
