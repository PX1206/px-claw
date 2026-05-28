package com.claw.system.param;

import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-24
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "新增用户信息参数")
public class AddUserParam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账号", required = true)
    @NotBlank(message = "账号不能为空")
    private String username;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @ApiModelProperty(value = "昵称", required = true)
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @ApiModelProperty("头像")
    private String headImg;

    @ApiModelProperty("性别：1男 2女")
    private Integer sex;

    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("常用地址")
    private String address;

    @ApiModelProperty(value = "密码（密码长度不能小于8位必须由数字和字母组成且不能出现连续四位相同字符），通过rsa2加密传输", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty("角色ID列表，不传则默认普通用户")
    private List<Long> roleIds;

    @ApiModelProperty("同步空间配额（GB），默认 5，范围 1～2048")
    private Integer syncQuotaGb;

}
