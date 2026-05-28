package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 重置密码参数
 *
 * @author Sakura
 * @since 2024-12-10
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "重置密码参数")
public class ResetPasswordParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "密码（密码长度不能小于8位必须由数字和字母组成且不能出现连续四位相同字符），通过rsa2加密传输", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

}
