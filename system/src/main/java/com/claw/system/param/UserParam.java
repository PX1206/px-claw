package com.claw.system.param;

import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 用户
 *
 * @author Sakura
 * @since 2024-12-04
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户信息参数")
public class UserParam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像")
    private String headImg;

    @ApiModelProperty("性别：1男 2女")
    private Integer sex;

    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("常用地址")
    private String address;

}
