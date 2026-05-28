package com.claw.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Sakura
 * @date 2023/8/14 14:19
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "图片验证码")
public class PictureCodeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("key")
    private String key;

    @ApiModelProperty("图片验证码 Base64格式")
    private String image;
}
