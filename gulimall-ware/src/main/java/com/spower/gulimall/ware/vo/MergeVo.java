package com.spower.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author CZQ
 */
@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;

}
