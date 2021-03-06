package com.spower.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.spower.gulimall.product.service.CategoryBrandRelationService;
import com.spower.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.Query;

import com.spower.gulimall.product.dao.CategoryDao;
import com.spower.gulimall.product.entity.CategoryEntity;
import com.spower.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author CZQ
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

//    private Map<String,Object> cache = new HashMap<>();

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1?????????????????????
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2?????????????????????????????????

        //2.1?????????????????????????????????
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1????????????????????????????????????????????????????????????

        //????????????
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * ?????????????????????????????????
     * 1. @CacheEvict????????????????????????
     * 2. @Cachingevict ????????????????????????
     * 3. @CacheEvict(value = "category", allEntries = true)??????category?????????????????????
     * 4. @CachePut ???????????????updateCascade()????????? ???????????????????????????redis???
     *
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),  //????????????????????????
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        System.out.println("---------------updateCascade---------------");
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        //???????????????DB????????????????????????????????????
        //redis.de("catelogJSON");??????????????????????????????
    }

    /**
     * ????????????????????????
     * SELECT * FROM `pms_category` WHERE parent_cid  = 0
     *
     * @return
     * @Cacheable ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????category??????
     * SpEL???#root.method.name???????????????https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-spel-context
     *
     * spring-Cache?????????
     *    1.?????????
     *      ????????????
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true) //????????????????????????
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        System.out.println("------------getLevel1Categorys---------------");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("???????????????" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("--------------------?????????????????????------------------");
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //???????????????k value???map???
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            //1???????????????????????????,???????????????????????????????????????
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2???????????????????????????Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1????????????????????????????????????????????????????????????????????????vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2????????????????????????
                            Catelog2Vo.Category3Vo catelog3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        //???????????????????????????
        return parent_cid;
    }

    /**
     * ???????????????redis????????????
     * TODO ???????????????????????????OutOfDirectMemoryError
     * ?????????
     * 1.springboot2.0??????????????????lettuce????????????redis????????????????????????netty?????????????????????
     * 2.lettuce???bug??????netty???????????????????????????netty???????????????????????????????????????-Xmx500m?????????????????????
     * ???????????????
     * 1.????????????-Dio.netty.maxDirectMemory????????????
     * 2.???????????????-Dio.netty.maxDirectMemory??????
     * 3.??????lettuce????????????????????????netty
     * 4.????????????jedis?????????????????????
     * jedis\lettuce?????????????????????redis?????????????????????spring????????????redisTemplate
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        /**
         * 1.????????????????????????????????????????????????json????????????java?????????json?????????????????????????????????????????????
         * 2.?????????????????????????????????????????????
         *   2.1??????????????????????????????????????????DB???
         *   2.2??????????????????redis???key???????????????
         *   2.3??????????????????
         */
        //1.???????????????????????????????????????JSON?????????JSON??????????????????????????????????????????
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("???????????????-----------------------?????????????????????.........................................");
            //??????????????????????????????DB????????????
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            //?????????????????????JSON?????????redis?????????
//            String s = JSON.toJSONString(catalogJsonFromDb);
//            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            return catalogJsonFromDb;
        }
        System.out.println("????????????-----------------------????????????.........................................");
        //?????????????????????????????????????????????
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return result;
    }

    /**
     * ?????????4?????????????????????redisson??????????????????
     * ?????????????????????updateCascade()??????????????????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????
     * 1.???????????? == ??????DB???????????? || ????????????????????????????????????key????????????:????????????1??????????????????2??????????????????DB?????????????????? || ?????????????????????
     * 2.???????????? == ??????DB????????????key || ?????????????????????????????????1??????????????????2 3??????????????????2?????????3?????????????????????3?????????1???2???????????????????????????????????????????????? || ?????????????????????
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        /**
         * 1.?????????????????????redis??????(????????????????????????)
         * 2.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        /**
         * ????????????........????????????
         */
        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }

        return dataFromDB;


    }

    /**
     * ?????????3
     * 1.??????????????????:??????Redis
     * 2.????????????getCatalogJsonFromDbWithLocalLock()??????????????????????????????synchronized (this) {}
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        /**
         * ??????????????????redis?????????
         * ?????????:????????????????????????key?????????????????????222??????????????????????????????????????????set lock 1111 EX 300 NX???
         * stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
         * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        if (lock) {
            System.out.println("????????????????????????-------------------");

            /**
             * ????????????........????????????
             */
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {

                /**
                 * lua?????????new DefaultRedisScript<Integer>(script,Integer.class)?????????Integer?????? | ????????????????????????key???Arrays.asList("lock")???(???KEYS[1])?????????key??? == ARGV[1]??????value???) |
                 * ?????????
                 * return 1???????????? return 0??????
                 */
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }

            /**
             * TODO ??????????????????????????????????????????????????????????????????????????????key?????????????????????222???
             *
             *  stringRedisTemplate.delete("lock")???????????????1.?????????????????????key??????????????????delete 2.?????????????????????????????????key?????????????????????????????????????????????????????????????????????
             *  ?????????????????????10s?????????1????????????????????????????????????11s???????????? ?????? ??????2????????????????????????????????????10s??????????????? ??????3...
             */
//            stringRedisTemplate.delete("lock");
            /**
             * ?????????????????? + ?????????????????????????????? = ?????????????????????
             * ???????????????http://www.redis.cn/commands/set.html ??????????????? lua??????
             */
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                //???????????????????????????
//                stringRedisTemplate.delete("lock");
//            }


            return dataFromDB;
        } else {
            System.out.println("????????????????????????-------------------????????????-------");
            //????????????.......??????.....???????????????
            //??????100ms??????
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                System.out.println(e);
            }
            return getCatalogJsonFromDbWithRedisLock();

        }

    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isNotEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return result;
        }
        System.out.println("--------------?????????????????????--------------");

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //???????????????k value???map??????????????????????????????parent_cid??????
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            //1???????????????????????????,???????????????????????????????????????
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2???????????????????????????Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1????????????????????????????????????????????????????????????????????????vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2????????????????????????
                            Catelog2Vo.Category3Vo catelog3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        //??????????????????????????????
        //cache.put("catalogJson", parent_cid);
        //?????????????????????JSON?????????redis????????????111111111111???
        System.out.println("---------------------?????????????????????---------------------");
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        //???????????????????????????
        return parent_cid;
    }


    /**
     * ?????????1???2
     * ??????????????????????????????cache??????Map????????????
     *
     * @return
     */
////    @Override
//    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
////        //map???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
////        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
////        //???????????????????????????????????????
////        if (cache.get("catalogJson") == null) {
////           //????????????
////            //????????????????????????
//
//
////        //??????:???????????????????????????????????????,??????????????????????????????
////        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
////
////        //1?????????????????????
////        //1???1???????????????????????????
////        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
////
////        //???????????????k value???map???
////        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
////
////            //1???????????????????????????,???????????????????????????????????????
////            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
////
////            //2???????????????????????????Catelog2Vo
////            List<Catelog2Vo> catelog2Vos = null;
////            if (categoryEntities != null) {
////                catelog2Vos = categoryEntities.stream().map(l2 -> {
////                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
////                    //1????????????????????????????????????????????????????????????????????????vo
////                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
////                    if (level3Catelog != null) {
////                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
////                            //2????????????????????????
////                            Catelog2Vo.Category3Vo catelog3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
////
////                            return catelog3Vo;
////                        }).collect(Collectors.toList());
////                        catelog2Vo.setCatalog3List(collect);
////                    }
////
////                    return catelog2Vo;
////                }).collect(Collectors.toList());
////            }
////
////            return catelog2Vos;
////        }));
////
////        //??????????????????????????????
////        cache.put("catalogJson", parent_cid);
////        //???????????????????????????
////        return parent_cid;
//
//
////        }
////        //???????????????????????????
////        return catalogJson;
//        /**
//         * ???????????????????????????????????????????????????????????????
//         * 1.synchronized (this)???springboot???????????????????????????????????????
//         * TODO ????????????synchronized\JUC(LOCK)??????????????????????????????????????????????????????????????????????????????????????????????????????
//         *
//         *
//         * ?????????????????????????????????synchronized????????????????????????????????????????????????????????????????????????redis??????????????????????????????????????????DB?????????????????????????????????????????????????????????????????????????????????
//         * ?????????????????????????????????????????????????????????????????????synchronized???????????????  ?????????????????????111111111111
//         */
//        synchronized (this) {
//            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
//            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
//            if (StringUtils.isNotEmpty(catalogJSON)) {
//                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//                });
//
//                return result;
//            }
//            System.out.println("--------------?????????????????????--------------");
//
//            //??????:???????????????????????????????????????
//            List<CategoryEntity> selectList = this.baseMapper.selectList(null);
//
//            //1?????????????????????
//            //1???1???????????????????????????
//            List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
//
//            //???????????????k value???map???
//            Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
//
//                //1???????????????????????????,???????????????????????????????????????
//                List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
//
//                //2???????????????????????????Catelog2Vo
//                List<Catelog2Vo> catelog2Vos = null;
//                if (categoryEntities != null) {
//                    catelog2Vos = categoryEntities.stream().map(l2 -> {
//                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
//                        //1????????????????????????????????????????????????????????????????????????vo
//                        List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
//                        if (level3Catelog != null) {
//                            List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
//                                //2????????????????????????
//                                Catelog2Vo.Category3Vo catelog3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
//
//                                return catelog3Vo;
//                            }).collect(Collectors.toList());
//                            catelog2Vo.setCatalog3List(collect);
//                        }
//
//                        return catelog2Vo;
//                    }).collect(Collectors.toList());
//                }
//
//                return catelog2Vos;
//            }));
//
//            //??????????????????????????????
//            //cache.put("catalogJson", parent_cid);
//            //?????????????????????JSON?????????redis????????????111111111111???
//            System.out.println("---------------------?????????????????????---------------------");
//            String s = JSON.toJSONString(parent_cid);
//            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
//            //???????????????????????????
//            return parent_cid;
//        }
//
//
//    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //??????parent_cid???????????????
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1?????????????????????id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    //????????????????????????????????????
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //1??????????????????
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2??????????????????
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}