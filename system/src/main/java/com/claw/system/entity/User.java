package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.claw.common.base.BaseEntity;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.claw.common.api.Update;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "User对象")
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "id不能为空", groups = {Update.class})
    @ApiModelProperty("自增ID")
    @TableId(value = "id", type = IdType.AUTO)
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

    @ApiModelProperty("性别：1男 2女")
    private Integer sex;

    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("盐")
    private String salt;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("最后登录时间")
    private Date loginTime;

    @ApiModelProperty("角色：admin/user")
    private String role;

    @ApiModelProperty("状态：0注销 1正常 2禁用 3冻结 4临时冻结")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("修改人")
    private Long updateBy;

    @ApiModelProperty("删除标识：1删除")
    private Boolean delFlag;

    @ApiModelProperty("同步文件总配额（字节），null 表示使用系统默认")
    private Long syncQuotaBytes;

}
