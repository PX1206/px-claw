package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.base.BaseController;
import com.claw.common.log.Module;
import com.claw.common.pagination.Paging;
import com.claw.system.param.MailSuggestedReplyIdParam;
import com.claw.system.param.MailSuggestedReplyPageParam;
import com.claw.system.param.MailSuggestedReplyUpdateParam;
import com.claw.system.service.MailSuggestedReplyAdminService;
import com.claw.system.vo.MailSuggestedReplyDetailVO;
import com.claw.system.vo.MailSuggestedReplyListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mail/suggested-reply")
@Module("claw")
@Api(value = "邮件 AI 建议回复", tags = {"邮件客服"})
public class MailSuggestedReplyController extends BaseController {

    @Autowired
    private MailSuggestedReplyAdminService mailSuggestedReplyAdminService;

    @PostMapping("/getPageList")
    @ApiOperation(value = "分页列表", response = MailSuggestedReplyListVO.class)
    public ApiResult<Paging<MailSuggestedReplyListVO>> getPageList(
            @Validated @RequestBody MailSuggestedReplyPageParam param) {
        return ApiResult.ok(mailSuggestedReplyAdminService.pageList(param));
    }

    @PostMapping("/getDetail")
    @ApiOperation(value = "详情", response = MailSuggestedReplyDetailVO.class)
    public ApiResult<MailSuggestedReplyDetailVO> getDetail(@Validated @RequestBody MailSuggestedReplyIdParam param) {
        return ApiResult.ok(mailSuggestedReplyAdminService.getDetail(param.getId()));
    }

    @PostMapping("/updateBody")
    @ApiOperation(value = "更新建议正文")
    public ApiResult<Boolean> updateBody(@Validated @RequestBody MailSuggestedReplyUpdateParam param) {
        mailSuggestedReplyAdminService.updateBody(param.getId(), param.getSuggestedBody());
        return ApiResult.ok(true);
    }

    @PostMapping("/discard")
    @ApiOperation(value = "丢弃")
    public ApiResult<Boolean> discard(@Validated @RequestBody MailSuggestedReplyIdParam param) {
        mailSuggestedReplyAdminService.discard(param.getId());
        return ApiResult.ok(true);
    }

    @PostMapping("/send")
    @ApiOperation(value = "审核通过并发送邮件")
    public ApiResult<Boolean> send(@Validated @RequestBody MailSuggestedReplyIdParam param) {
        mailSuggestedReplyAdminService.send(param.getId());
        return ApiResult.ok(true);
    }
}
