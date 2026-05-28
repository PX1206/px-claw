package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.claw.common.api.Update;
import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 文件表
 *
 * @author Sakura
 * @since 2022-08-22
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("file")
@ApiModel(value = "File对象")
public class LocalFile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("文件编码，随机生成")
    private String code;

    @ApiModelProperty("文件名称")
    private String name;

    @ApiModelProperty("文件类型 1.图片 2.文档 3.视频 4.音频 5.其它")
    private Integer type;

    @ApiModelProperty("前缀")
    private String domain;

    @ApiModelProperty("路径")
    private String path;

    @ApiModelProperty("文件后缀")
    private String suffix;

    @ApiModelProperty("来源：用户 2商家 3管理")
    private Integer source;

    @ApiModelProperty("文件大小 单位Kb")
    private Integer size;

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

    @ApiModelProperty("所属同步目录ID")
    private Long syncDirectoryId;

    @ApiModelProperty("在同步目录内的相对路径")
    private String relativePath;

}
