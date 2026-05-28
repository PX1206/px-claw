package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.claw.common.pagination.BasePageOrderParam;

/**
 * <pre>
 * 用户 分页参数对象
 * </pre>
 *
 * @author Sakura
 * @date 2024-12-04
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户分页参数")
public class UserPageParam extends BasePageOrderParam {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户名、昵称、手机号、企业名称模糊搜索")
    private String keyword;

}
