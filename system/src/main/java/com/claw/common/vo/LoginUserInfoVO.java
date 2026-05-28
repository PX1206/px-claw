package com.claw.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("登录用户详细信息")
public class LoginUserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户编号")
    private String userNo;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("头像")
    private String headImg;

    @ApiModelProperty("生日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @ApiModelProperty("性别 1男 2女 0未知")
    private Integer sex;

    @ApiModelProperty("角色：admin/user")
    private String role;

    @ApiModelProperty("状态：0注销 1正常 2禁用 3冻结 4临时冻结")
    private Integer status;

    @ApiModelProperty("登录认证token, 请求时请在Header配置成：Authorization")
    private String token;

    @ApiModelProperty("权限code列表")
    private List<String> permissions;

}
