package com.spower.gulimall.member.dao;

import com.spower.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 17:00:29
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
