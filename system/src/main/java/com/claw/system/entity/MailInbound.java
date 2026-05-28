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
@ApiModel("邮件入站快照")
@TableName("mail_inbound")
public class MailInbound implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("RFC Message-ID")
    private String messageId;

    private Long imapUid;

    private String folder;

    private String fromAddr;

    private String toAddr;

    private String subject;

    private String bodyText;

    private Date receivedAt;

    @ApiModelProperty("JSON：Message-ID、References、In-Reply-To")
    private String rawHeadersJson;

    private Date createTime;
}
