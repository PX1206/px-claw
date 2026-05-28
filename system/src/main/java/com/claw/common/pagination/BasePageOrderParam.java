package com.claw.common.pagination;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 可排序查询参数对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("可排序查询参数对象")
public abstract class BasePageOrderParam extends BasePageParam {
    private static final long serialVersionUID = 57714391204790143L;

//    @ApiModelProperty("排序")
//    private List<OrderItem> pageSorts;

//    public void defaultPageSort(OrderItem orderItem) {
//        this.defaultPageSorts(Arrays.asList(orderItem));
//    }
//
//    public void defaultPageSorts(List<OrderItem> pageSorts) {
//        if (CollectionUtils.isEmpty(pageSorts)) {
//            return;
//        }
//        this.pageSorts = pageSorts;
//    }

}
