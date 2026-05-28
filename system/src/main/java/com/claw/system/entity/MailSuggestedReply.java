package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("邮件 AI 建议回复")
@TableName("mail_suggested_reply")
public class MailSuggestedReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inboundId;

    private String suggestedBody;

    private String retrievedContext;

    @ApiModelProperty("PENDING_REVIEW|SENT|DISCARDED|FAILED")
    private String status;

    private Long reviewedByUserId;

    private Date reviewedAt;

    private Date sentAt;

    private String smtpMessageId;

    private String lastError;

    private Date createTime;

    private Date updateTime;
}
