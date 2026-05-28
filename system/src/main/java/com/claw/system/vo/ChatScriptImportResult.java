package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("话术 Excel 导入结果")
public class ChatScriptImportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("新插入条数")
    private int inserted;

    @ApiModelProperty("按相同问题覆盖更新的条数")
    private int updated;
}
