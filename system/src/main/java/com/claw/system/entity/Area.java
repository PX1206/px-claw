package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * @author Sakura
 * @since 2024-08-12
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Area对象")
public class Area implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer parentId;

    private String initial;

    private String initials;

    private String pinyin;

    private String extra;

    private String suffix;

    private String code;

    private String areaCode;

    private Integer areaOrder;

}
