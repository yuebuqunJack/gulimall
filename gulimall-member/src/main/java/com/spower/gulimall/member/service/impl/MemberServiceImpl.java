package com.spower.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spower.common.to.member.MemberUserLoginTO;
import com.spower.common.to.member.MemberUserRegisterTO;
import com.spower.common.utils.HttpUtils;
import com.spower.common.utils.R;
import com.spower.gulimall.member.dao.MemberLevelDao;
import com.spower.gulimall.member.entity.MemberLevelEntity;
import com.spower.gulimall.member.exception.PhoneException;
import com.spower.gulimall.member.exception.UsernameException;
import com.spower.gulimall.member.vo.MemberUserLoginVo;
import com.spower.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.Query;

import com.spower.gulimall.member.dao.MemberDao;
import com.spower.gulimall.member.entity.MemberEntity;
import com.spower.gulimall.member.service.MemberService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;


/**
 * @author CZQ
 */
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberUserRegisterTO vo) {

        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //设置其它的默认信息
        //检查用户名和手机号是否唯一。感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setNickname(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        //密码进行MD5加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setGender(0);
        memberEntity.setCreateTime(new Date());

        //保存数据
        this.baseMapper.insert(memberEntity);

    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {

        Integer phoneCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));

        if (phoneCount > 0) {
            throw new PhoneException();
        }

    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameException {

        Integer usernameCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));

        if (usernameCount > 0) {
            throw new UsernameException();
        }
    }

    @Override
    public MemberEntity login(MemberUserLoginTO user) {

        String loginacct = user.getLoginacct();
        String password = user.getPassword();

        //1、去数据库查询 SELECT * FROM ums_member WHERE username = ? OR mobile = ?
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or().eq("mobile", loginacct));

        if (memberEntity == null) {
            //登录失败
            return null;
        } else {
            //获取到数据库里的password
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = passwordEncoder.matches(password, password1);
            if (matches) {
                //登录成功
                return memberEntity;
            }
        }

        return null;
    }

    /**
     * gitee登录
     * <p>
     * 需要具有登录与注册逻辑？
     *
     * @param giteeInfo
     * @return
     * @throws Exception
     */
    @Override
    public MemberEntity giteeLogin(@RequestParam("giteeInfo") String giteeInfo) throws Exception {
        // 拿到accesstoken，获取用户基本信息
        JSONObject baseJson = JSON.parseObject(giteeInfo);
        Map<String, String> params = new HashMap<>();
        String accessToken = baseJson.getString("access_token");
        String expiresIn = baseJson.getString("expires_in");
        params.put("access_token", accessToken);
        /**
         * gitee api文档：根据access_token查user_id
         */
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), params);
        //存放返回的用户授权信息给OAuth2Controller处理
        MemberEntity newMember = new MemberEntity();
        /**
         * 查成功200，获得gitee用户id 再通过这个id查询数据库
         */
        if (response.getStatusLine().getStatusCode() == 200) {
            String s = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(s);
            String id = jsonObject.getString("id");

            /**
             * 通过social_uid条件查询数据库是否存在该用户，存在就是注册过了  不存在就你懂的
             */
            MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", "gitee" + "_" + id));
            System.out.println("---------------------member----------------");
            System.out.println(member);
            if (member != null) {
                // 说明已经注册过，根据用户id更新令牌因为令牌有过期时间
                newMember.setId(member.getId());
                newMember.setNickname(jsonObject.getString("name"));
                newMember.setAccessToken(accessToken);
                newMember.setExpiresIn(expiresIn);
                this.updateById(newMember);
            } else {
                // 没有查到当前社交用户对应的记录我们就需要注册一个
                newMember.setSocialUid("gitee" + "_" + id);
                newMember.setNickname(jsonObject.getString("name"));
                newMember.setAccessToken(accessToken);
                newMember.setExpiresIn(expiresIn);
                this.save(newMember);
            }

        }
        return newMember;
    }
}