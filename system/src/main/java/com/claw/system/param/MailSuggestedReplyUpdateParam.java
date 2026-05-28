package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("更新邮件建议正文")
public class MailSuggestedReplyUpdateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty("建议回复正文（可编辑）")
    private String suggestedBody;
}
