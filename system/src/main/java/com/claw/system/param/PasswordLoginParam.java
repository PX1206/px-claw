package com.claw.system.param;

import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-05
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "账号密码登录参数")
public class PasswordLoginParam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账号", required = true)
    @NotBlank(message = "账号不能为空")
    private String username;

    @ApiModelProperty(value = "密码（密码长度不能小于8位必须由数字和字母组成且不能出现连续四位相同字符），通过rsa2加密传输", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

}
