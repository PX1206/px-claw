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
@ApiModel("AI客服归档会话")
@TableName("ai_cs_chat_session")
public class AiCsChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @ApiModelProperty("客户端会话 id")
    private String clientSessionId;

    private String title;

    private String providerId;

    private Long clientUpdatedAt;

    private Date createTime;
    private Date updateTime;
}
