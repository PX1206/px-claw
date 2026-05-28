package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("邮件建议回复列表项")
public class MailSuggestedReplyListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long inboundId;
    private String status;

    @ApiModelProperty("入站主题")
    private String subject;

    @ApiModelProperty("客户发件人")
    private String fromAddr;

    private Date createTime;
}
