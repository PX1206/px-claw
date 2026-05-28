package com.claw.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("菜单VO")
public class MenuVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("菜单ID")
    private Long id;

    @ApiModelProperty("父ID")
    private Long pid;

    @ApiModelProperty("菜单标题")
    private String title;

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

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("子菜单")
    private List<MenuVO> children;
}
