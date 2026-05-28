package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("话术分页")
public class ChatScriptPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("当前页数据")
    private List<ChatScriptVO> records;

    @ApiModelProperty("总条数")
    private long total;

    @ApiModelProperty("每页条数")
    private long size;

    @ApiModelProperty("当前页码（从 1 起）")
    private long current;

    @ApiModelProperty("总页数")
    private long pages;
}
