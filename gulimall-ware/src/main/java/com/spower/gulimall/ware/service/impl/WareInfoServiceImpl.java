package com.spower.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.Query;
import com.spower.common.utils.R;
import com.spower.gulimall.ware.dao.WareInfoDao;
import com.spower.gulimall.ware.entity.WareInfoEntity;
import com.spower.gulimall.ware.feign.MemberFeignService;
import com.spower.gulimall.ware.service.WareInfoService;
import com.spower.gulimall.ware.vo.FareVo;
import com.spower.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


/**
 * @author CZQ
 */
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }


        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 计算运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();

        //收获地址的详细信息
        R addrInfo = memberFeignService.info(addrId);

        MemberAddressVo memberAddressVo = addrInfo.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {});

        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            //截取用户手机号码最后一位作为我们的运费计算
            //1558022051
            String fare = phone.substring(phone.length() - 10, phone.length()-8);
            BigDecimal bigDecimal = new BigDecimal(fare);

            fareVo.setFare(bigDecimal);
            fareVo.setAddress(memberAddressVo);

            return fareVo;
        }
        return null;
    }

//    /**
//     * 计算运费
//     *
//     * @param addrId
//     * @return
//     */
//    @Override
//    public FareVo getFare(Long addrId) {
//
//        FareVo fareVo = new FareVo();
//
//        //收获地址的详细信息
//        R addrInfo = memberFeignService.info(addrId);
//
//        MemberAddressVo memberAddressVo = addrInfo.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
//        });
//
//        if (memberAddressVo != null) {
//            String phone = memberAddressVo.getPhone();
//            //截取用户手机号码最后一位作为我们的运费计算
//            //1558022051
//            String fare = phone.substring(phone.length() - 10, phone.length() - 8);
//            BigDecimal bigDecimal = new BigDecimal(fare);
//
//            fareVo.setFare(bigDecimal);
//            fareVo.setAddress(memberAddressVo);
//
//            return fareVo;
//        }
//        return null;
//    }

    public static void main(String[] args) {
        String phone = "1558022051";
        String fare = phone.substring(phone.length() - 10, phone.length() - 8);
        System.out.println(fare);
    }

}