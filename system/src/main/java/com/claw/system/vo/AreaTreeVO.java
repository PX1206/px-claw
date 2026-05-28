package com.claw.system.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 *
 * @author Sakura
 * @since 2024-08-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "区域树信息")
public class AreaTreeVO extends Model<AreaTreeVO> {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private Integer parentId;

    private String code;

    private List<AreaTreeVO> children;

}
