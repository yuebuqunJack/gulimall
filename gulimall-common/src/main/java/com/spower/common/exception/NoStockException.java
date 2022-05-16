package com.spower.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author CZQ
 * @Description: 无库存抛出的异常
 **/

public class NoStockException extends RuntimeException {

    @Getter
    @Setter
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id：" + skuId + "库存不足！");
    }

    public NoStockException(String msg) {
        super(msg);
    }


}
