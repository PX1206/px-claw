package com.claw.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("话术条目")
public class ChatScriptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("问题")
    private String question;

    @ApiModelProperty("话术")
    private String scriptText;

    @ApiModelProperty("补充（可选）")
    private String supplement;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("导入时间")
    private Date importTime;

    @ApiModelProperty("导入用户登录名（展示）")
    private String importUsername;

    @ApiModelProperty("同一批次（Excel导入）")
    private String importBatchId;
}
