package com.claw.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 代理商分润信息（MQ推送用）
 * </p>
 **/
@Data
public class CommissionGoodsMq implements Serializable {
    private static final long serialVersionUID = -5912785220335057555L;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 售价
     */
    private BigDecimal sellPrice;

    /**
     * 数量
     */
    private Integer number;
}
