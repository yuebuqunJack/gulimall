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

        //??????????????????
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //???????????????????????????
        //?????????????????????????????????????????????????????????????????????
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setNickname(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        //????????????MD5??????
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setGender(0);
        memberEntity.setCreateTime(new Date());

        //????????????
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

        //1????????????????????? SELECT * FROM ums_member WHERE username = ? OR mobile = ?
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or().eq("mobile", loginacct));

        if (memberEntity == null) {
            //????????????
            return null;
        } else {
            //????????????????????????password
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //??????????????????
            boolean matches = passwordEncoder.matches(password, password1);
            if (matches) {
                //????????????
                return memberEntity;
            }
        }

        return null;
    }

    /**
     * gitee??????
     * <p>
     * ????????????????????????????????????
     *
     * @param giteeInfo
     * @return
     * @throws Exception
     */
    @Override
    public MemberEntity giteeLogin(@RequestParam("giteeInfo") String giteeInfo) throws Exception {
        // ??????accesstoken???????????????????????????
        JSONObject baseJson = JSON.parseObject(giteeInfo);
        Map<String, String> params = new HashMap<>();
        String accessToken = baseJson.getString("access_token");
        String expiresIn = baseJson.getString("expires_in");
        params.put("access_token", accessToken);
        /**
         * gitee api???????????????access_token???user_id
         */
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), params);
        //????????????????????????????????????OAuth2Controller??????
        MemberEntity newMember = new MemberEntity();
        /**
         * ?????????200?????????gitee??????id ???????????????id???????????????
         */
        if (response.getStatusLine().getStatusCode() == 200) {
            String s = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(s);
            String id = jsonObject.getString("id");

            /**
             * ??????social_uid?????????????????????????????????????????????????????????????????????  ?????????????????????
             */
            MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", "gitee" + "_" + id));
            System.out.println("---------------------member----------------");
            System.out.println(member);
            if (member != null) {
                // ????????????????????????????????????id???????????????????????????????????????
                newMember.setId(member.getId());
                newMember.setNickname(jsonObject.getString("name"));
                newMember.setAccessToken(accessToken);
                newMember.setExpiresIn(expiresIn);
                this.updateById(newMember);
            } else {
                // ????????????????????????????????????????????????????????????????????????
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