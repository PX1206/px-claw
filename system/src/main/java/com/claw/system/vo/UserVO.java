package com.claw.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-10
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户信息")
public class UserVO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户类型名称")
    private String typeName;

    @ApiModelProperty("行业类别ID")
    private Long industryAttributeId;

    @ApiModelProperty("行业类别")
    private String industryAttribute;

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

    @ApiModelProperty("性别：1男 2女")
    private Integer sex;

    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    @ApiModelProperty("状态：0注销 1正常 2禁用 3冻结 4临时冻结")
    private Integer status;

    @ApiModelProperty("角色：admin/user")
    private String role;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("同步空间配额（字节）")
    private Long syncQuotaBytes;

    @ApiModelProperty("已用同步空间（字节）")
    private Long syncUsedBytes;

}
