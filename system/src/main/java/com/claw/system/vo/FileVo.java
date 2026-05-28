package com.claw.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "文件信息")
public class FileVo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文件编码")
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

    @ApiModelProperty("请求路径")
    private String url;

    @ApiModelProperty("来源：用户 2商家 3管理")
    private Integer source;

    @ApiModelProperty("文件大小 单位字节")
    private Integer size;

    @ApiModelProperty("所属同步目录ID")
    private Long syncDirectoryId;

    @ApiModelProperty("在同步目录内的相对路径")
    private String relativePath;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("最后同步时间（无更新记录时同创建时间）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSyncTime;

}
