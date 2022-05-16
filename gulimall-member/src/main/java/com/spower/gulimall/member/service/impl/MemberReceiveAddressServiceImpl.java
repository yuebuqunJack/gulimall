package com.spower.gulimall.member.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.Query;

import com.spower.gulimall.member.dao.MemberReceiveAddressDao;
import com.spower.gulimall.member.entity.MemberReceiveAddressEntity;
import com.spower.gulimall.member.service.MemberReceiveAddressService;


/**
 * @author CZQ
 */
@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<MemberReceiveAddressEntity> getAddress(Long memberId) {

        List<MemberReceiveAddressEntity> addressList = this.baseMapper.selectList
                (new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));

        return addressList;
    }

}