package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 目录树节点（同步目录 + 子目录）
 */
@Data
@ApiModel("目录树节点")
public class DirectoryTreeNodeVo {

    @ApiModelProperty("同步目录ID")
    private Long syncDirectoryId;

    @ApiModelProperty("相对路径，根为空字符串")
    private String relativePath;

    @ApiModelProperty("显示名称")
    private String displayName;

    @ApiModelProperty("所属用户昵称（管理员可见）")
    private String ownerName;

    @ApiModelProperty("所属用户ID（管理员可见）")
    private Long userId;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("最后同步时间")
    private String lastSyncTime;

    @ApiModelProperty("子节点")
    private List<DirectoryTreeNodeVo> children = new ArrayList<>();
}
