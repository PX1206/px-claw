package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.claw.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("install_package")
@ApiModel(value = "安装包分发")
public class InstallPackage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("公开下载随机码")
    private String downloadCode;

    @ApiModelProperty("展示文件名(含后缀)")
    private String fileName;

    @ApiModelProperty("存储子目录 yyyy-MM-dd")
    private String relativePath;

    @ApiModelProperty("后缀含点")
    private String suffix;

    @ApiModelProperty("字节")
    private Long fileSize;

    @ApiModelProperty("SHA-512 Base64，用于桌面自动更新")
    private String sha512;

    @ApiModelProperty("版本说明")
    private String versionLabel;

    @ApiModelProperty("备注")
    private String remark;

    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;
    private Boolean delFlag;
}
