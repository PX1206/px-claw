package com.claw.system.param;

import com.claw.common.pagination.BasePageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("邮件建议回复分页参数")
public class MailSuggestedReplyPageParam extends BasePageParam {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("状态筛选：PENDING_REVIEW / SENT / DISCARDED / FAILED")
    private String status;
}
