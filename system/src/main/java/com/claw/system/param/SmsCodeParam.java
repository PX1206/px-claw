package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户表
 *
 * @author Sakura
 * @since 2023-08-14
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "短信验证码参数")
public class SmsCodeParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^$|^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(16[0-8])|(147))\\d{8}$", message = "手机号码格式错误")
    private String mobile;

    @ApiModelProperty(value = "图片验证码key", required = true)
    @NotBlank(message = "图片验证码key不能为空")
    private String key;

    @ApiModelProperty(value = "图片验证码", required = true)
    @NotBlank(message = "图片验证码不能为空")
    private String pictureCode;

}
