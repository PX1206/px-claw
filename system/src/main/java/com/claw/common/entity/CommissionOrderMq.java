package com.claw.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 代理商分润信息（MQ推送用）
 * </p>
 **/
@Data
public class CommissionOrderMq implements Serializable {
    private static final long serialVersionUID = -5912785220335057555L;

    /**
     * 类型：1支付 2退款
     */
    private Integer type;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 金额（负数为退款）
     */
    private BigDecimal amount;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 交易日期
     */
    private Date payTime;

    /**
     * 支付方式: 1.微信 2.支付宝
     */
    private Integer payway;

    /**
     * 商品信息
     */
    private List<CommissionGoodsMq> goodsMqs;
}
