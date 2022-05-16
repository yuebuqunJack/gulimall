package com.spower.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author CZQ
 **/

@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;

}
