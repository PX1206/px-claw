package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI 客服话术（表结构）
 */
@Data
@ApiModel(value = "ChatScript")
@TableName("chat_script")
public class ChatScript implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("话术归属用户（与登录用户隔离）")
    private Long ownerUserId;

    @ApiModelProperty("问题列")
    private String question;

    @TableField("script_text")
    @ApiModelProperty("话术列")
    private String scriptText;

    @ApiModelProperty("补充（可选）")
    private String supplement;

    @ApiModelProperty("导入时间（同批共用）")
    private Date importTime;

    @ApiModelProperty("导入用户ID")
    private Long importUserId;

    @ApiModelProperty("导入用户展示名")
    private String importUsername;

    @ApiModelProperty("同一Excel批次 UUID")
    private String importBatchId;

    private Date createTime;
    private Date updateTime;
}
