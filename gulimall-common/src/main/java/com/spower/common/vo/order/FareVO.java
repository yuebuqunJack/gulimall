package com.spower.common.vo.order;

import com.spower.common.vo.ware.MemberAddressVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: wan
 */
@Data
public class FareVO {
    private MemberAddressVO address;
    private BigDecimal fare;
}
