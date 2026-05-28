package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 同步文件元数据，用于桌面端 diff（判断是否需要上传）
 */
@Data
public class SyncFileMetaDto {
    /** 下载 / 拉取到本地时使用 */
    private String code;
    private String relativePath;
    private Integer size;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
