package com.spower.gulimall.cart.to;

import lombok.Data;
import lombok.ToString;


/**
 * @author CZQ
 */
@ToString
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    /**
     * 是否临时用户
     */
    private Boolean tempUser = false;

}
