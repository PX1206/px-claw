package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 修改密码参数
 *
 * @author Sakura
 * @since 2023-08-27
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "修改密码参数")
public class UpdatePasswordParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "密码（密码长度不能小于8位必须由数字和字母组成且不能出现连续四位相同字符），通过rsa2加密传输", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^$|^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(16[0-8])|(147))\\d{8}$", message = "手机号码格式错误")
    private String mobile;

    @ApiModelProperty(value = "短信验证码", required = true)
    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

}
