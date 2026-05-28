package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel(value = "菜单参数")
public class MenuParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("菜单ID（修改时必传）")
    private Long id;

    @ApiModelProperty("父ID，0表示顶级")
    private Long pid;

    @NotBlank(message = "菜单标题不能为空")
    @ApiModelProperty("菜单标题")
    private String title;

    @NotBlank(message = "菜单类型不能为空")
    @ApiModelProperty("菜单类型：dir目录 menu菜单 btn按钮")
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
}
