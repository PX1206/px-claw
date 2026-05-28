package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.claw.common.base.BaseEntity;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.claw.common.api.Update;

/**
 * 菜单
 *
 * @author Sakura
 * @since 2024-12-09
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Menu对象")
public class Menu extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "id不能为空", groups = {Update.class})
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("父ID")
    private Long pid;

    @ApiModelProperty("菜单标题")
    private String title;

    @ApiModelProperty("菜单类型")
    private String type;

    @ApiModelProperty("权限code")
    private String permission;

    @ApiModelProperty("前端组件地址")
    private String component;

    @ApiModelProperty("前端菜单icon")
    private String icon;

    @ApiModelProperty("前端路由名")
    private String name;

    @ApiModelProperty("前端路由地址路径")
    private String redirect;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("是否被隐藏")
    private Boolean hidden;

    @ApiModelProperty("路径")
    private String path;

    @ApiModelProperty("是否被固定在table")
    private Boolean affix;

    @ApiModelProperty("是否缓存页面")
    private Boolean keepAlive;

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

}
