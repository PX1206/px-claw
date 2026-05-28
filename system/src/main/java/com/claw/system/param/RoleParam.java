package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel(value = "角色参数")
public class RoleParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("角色ID（修改时必传）")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @ApiModelProperty("角色名称")
    private String name;

    @NotBlank(message = "角色标识不能为空")
    @ApiModelProperty("角色标识")
    private String code;

    @ApiModelProperty("描述")
    private String description;
}
