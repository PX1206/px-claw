package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@ApiModel("安装包记录")
public class InstallPackageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String downloadCode;
    private String fileName;
    private String suffix;
    private Long fileSize;
    private String versionLabel;
    private String remark;
    private Date createTime;

    @ApiModelProperty("完整公开下载地址（可直接发给用户）")
    private String downloadUrl;
}
