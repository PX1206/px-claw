package com.claw.system.param;

import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "短信验证码登录参数")
public class SMSLoginParam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^$|^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(16[0-8])|(147))\\d{8}$", message = "手机号码格式错误")
    private String mobile;

    @ApiModelProperty(value = "短信验证码", required = true)
    private String smsCode;

}
