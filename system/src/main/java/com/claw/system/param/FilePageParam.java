package com.claw.system.param;

import com.claw.common.pagination.BasePageOrderParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "文件分页参数")
public class FilePageParam extends BasePageOrderParam {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文件类型 1.图片 2.文档 3.视频 4.音频 5.其它")
    private Integer type;

    @ApiModelProperty("文件名称")
    private String name;

    @ApiModelProperty("来源：用户 2商家 3管理")
    private Integer source;

    @ApiModelProperty("提交人")
    private Long userId;

    @ApiModelProperty("同步目录ID")
    private Long syncDirectoryId;

    @ApiModelProperty("相对路径（精确匹配，用于按目录筛选文件）")
    private String relativePath;
}
