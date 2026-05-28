package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 公开接口：当前可用于 Web 展示的「最新 Windows 桌面安装包」元信息（与 latest.yml 选取规则一致）。
 */
@Data
@Accessors(chain = true)
@ApiModel("公开-最新桌面安装包信息")
public class OpenInstallPackageLatestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("语义化版本（从版本说明或文件名解析，与自动更新一致）")
    private String version;

    @ApiModelProperty("管理员填写的版本说明原文")
    private String versionLabel;

    @ApiModelProperty("原始文件名")
    private String fileName;

    @ApiModelProperty("公开下载随机码")
    private String downloadCode;

    @ApiModelProperty("文件大小（字节）")
    private Long fileSize;
}
