package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("邮件建议回复详情")
public class MailSuggestedReplyDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long inboundId;
    private String status;
    private String suggestedBody;
    private String retrievedContext;
    private String lastError;
    private Long reviewedByUserId;
    private Date reviewedAt;
    private Date sentAt;
    private Date createTime;

    @ApiModelProperty("入站主题")
    private String inboundSubject;

    @ApiModelProperty("客户发件人")
    private String inboundFrom;

    @ApiModelProperty("入站正文（纯文本）")
    private String inboundBodyText;

    @ApiModelProperty("原始线程头 JSON")
    private String rawHeadersJson;
}
