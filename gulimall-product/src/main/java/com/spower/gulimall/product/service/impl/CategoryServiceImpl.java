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
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
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
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
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
     * 级联更新所有关联的数据
     * 1. @CacheEvict实现缓存失效模式
     * 2. @Cachingevict 实现多种缓存操作
     * 3. @CacheEvict(value = "category", allEntries = true)删除category下的所有的数据
     * 4. @CachePut 双写模式（updateCascade()不能用 因为没有返回值写回redis）
     *
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),  //实现缓存失效模式
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        System.out.println("---------------updateCascade---------------");
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        //上面更新完DB，接下来修改缓存中的数据
        //redis.de("catelogJSON");等待下次主动查询更新
    }

    /**
     * 查询所有一级分类
     * SELECT * FROM `pms_category` WHERE parent_cid  = 0
     *
     * @return
     * @Cacheable 表示当前方法结果需要缓存，如果缓存有则不调用，没有就会调用。最后将结果放入缓存。（category区）
     * SpEL（#root.method.name）表达式：https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-spel-context
     *
     * spring-Cache的不足
     *    1.读模式
     *      缓存穿透
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true) //实现缓存失效模式
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        System.out.println("------------getLevel1Categorys---------------");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("--------------------开始查询数据库------------------");
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据为k value到map中
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果到Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类对应的子分类也就是三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
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

        //最终返回出去的数据
        return parent_cid;
    }

    /**
     * 新版本使用redis作为缓存
     * TODO 产生堆外内存溢出：OutOfDirectMemoryError
     * 原因：
     * 1.springboot2.0之后默认使用lettuce作为操作redis的客户端。它使用netty作为网络通信。
     * 2.lettuce的bug导致netty堆外内存溢出，如果netty没有指定堆外内存，默认使用-Xmx500m作为堆外内存。
     * 解决方案：
     * 1.可以通过-Dio.netty.maxDirectMemory进行设置
     * 2.不能仅设置-Dio.netty.maxDirectMemory调大
     * 3.升级lettuce客户端使其不操作netty
     * 4.切换使用jedis（老版客户端）
     * jedis\lettuce关系：都是操作redis的底层客户端。spring再次封装redisTemplate
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        /**
         * 1.何为序列化与反序列化：给缓存放入json字符串，java再拿回json字符串并你转为能用的对象类型。
         * 2.解决缓存穿透、雪崩、击穿问题：
         *   2.1）击穿：空结果缓存一下避免查DB库
         *   2.2）雪崩：设置redis中key的过期时间
         *   2.3）穿透：加锁
         */
        //1.从缓存中获取菜单（之所以用JSON是因为JSON是跨语言跨平台的兼容性良好）
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中-----------------------将要查询数据库.........................................");
            //如果缓存是空的就调从DB获取菜单
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            //查到的数据转为JSON并放入redis缓存中
//            String s = JSON.toJSONString(catalogJsonFromDb);
//            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中-----------------------直接返回.........................................");
        //逆转对象通过这步骤返回复杂类型
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return result;
    }

    /**
     * 版本：4：最终版本使用redisson作为分布式锁
     * 思考问题：如果updateCascade()更新了数据如何保持数据和缓存数据的一致性问题！！！！！！！！
     * 缓存数据一致性只是由于读到数据有延迟会获得最终一致性
     * 解决方案：（导致的问题都称之为乱序问题）
     * 1.双写模式 == 写完DB更新缓存 || 会产生暂时性脏数据问题（key会过期）:由于线程1写缓存比线程2写缓存慢，写DB快情况下出现 || 解决方案：加锁
     * 2.失效模式 == 写了DB删了缓存key || 会产生脏数据问题：线程1删缓存比线程2 3都要慢，线程2比线程3删缓存慢，线程3在线程1、2删缓存前更新了缓存就会导致问题。 || 解决方案：加锁
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        /**
         * 1.占分布式锁，去redis占坑(锁的粒度越细越好)
         * 2.锁的粒度就是每个业务取的锁的名称要详细避免业务请求量少的请求到了业务量请求大的效率就太慢了
         */
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        /**
         * 加锁成功........执行业务
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
     * 版本：3
     * 1.使用分布式锁:使用Redis
     * 2.与旧版本getCatalogJsonFromDbWithLocalLock()对比是只在本地加了个synchronized (this) {}
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        /**
         * 分布式锁，去redis占坑：
         * 方案是:占锁的同时给锁的key设置过期时间（222），使其成为一个原子的操作（set lock 1111 EX 300 NX）
         * stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
         * 如果业务太长会导致锁过期解决方案：锁的续期不做的话可以把过期时间设置长一点。
         */
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        if (lock) {
            System.out.println("获取分布式锁成功-------------------");

            /**
             * 加锁成功........执行业务
             */
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {

                /**
                 * lua需要传new DefaultRedisScript<Integer>(script,Integer.class)并返回Integer类型 | 集合类型的所有的key用Arrays.asList("lock")装(由KEYS[1])（获得key） == ARGV[1]（放value）) |
                 * 删除锁
                 * return 1删除成功 return 0反之
                 */
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }

            /**
             * TODO 如果该代码由于异常没有执行就会导致死锁：方案是给锁的key设置过期时间（222）
             *
             *  stringRedisTemplate.delete("lock")导致问题：1.业务代码太长，key过期了后执行delete 2.业务代码执行的时间超过key的过期时间就会导致删除别人持有的锁删除的可能。
             *  比如过期时间是10s，线程1执行业务代码占用了锁但是11s锁过期了 这时 线程2占用锁并执行业务代码超过10s锁也过期了 线程3...
             */
//            stringRedisTemplate.delete("lock");
            /**
             * 获取的值对比 + 对比成功执行删除方法 = 要等于原子操作
             * 解决方案：http://www.redis.cn/commands/set.html 的设计模式 lua脚本
             */
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                //才可以删除自己的锁
//                stringRedisTemplate.delete("lock");
//            }


            return dataFromDB;
        } else {
            System.out.println("获取分布式锁失败-------------------等待重试-------");
            //枷锁失败.......重试.....锁（自旋）
            //休眠100ms重试
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
        System.out.println("--------------开始查询数据库--------------");

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据为k value到map中（将所有的数据放到parent_cid中）
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果到Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类对应的子分类也就是三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
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

        //顺便将数据放入缓存中
        //cache.put("catalogJson", parent_cid);
        //查到的数据转为JSON并放入redis缓存中（111111111111）
        System.out.println("---------------------数据放入缓存中---------------------");
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        //最终返回出去的数据
        return parent_cid;
    }


    /**
     * 版本：1、2
     * 旧版本是使用数据库无cache或者Map作为缓存
     *
     * @return
     */
////    @Override
//    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
////        //map存储的任何东西都是在内存中的，下面这行代码称之为本地缓存（分布式环境不能用本地缓存，会导致数据一致性问题）
////        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
////        //如果缓存中有数据则用缓存的
////        if (cache.get("catalogJson") == null) {
////           //调用业务
////            //返回数据放入缓存
//
//
////        //优化:将数据库的多次查询变为一次,传个空就是查所有数据
////        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
////
////        //1、查出所有分类
////        //1、1）查出所有一级分类
////        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
////
////        //封装数据为k value到map中
////        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
////
////            //1、每一个的一级分类,查到这个一级分类的二级分类
////            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
////
////            //2、封装上面的结果到Catelog2Vo
////            List<Catelog2Vo> catelog2Vos = null;
////            if (categoryEntities != null) {
////                catelog2Vos = categoryEntities.stream().map(l2 -> {
////                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
////                    //1、找当前二级分类对应的子分类也就是三级分类封装成vo
////                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
////                    if (level3Catelog != null) {
////                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
////                            //2、封装成指定格式
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
////        //顺便将数据放入缓存中
////        cache.put("catalogJson", parent_cid);
////        //最终返回出去的数据
////        return parent_cid;
//
//
////        }
////        //有缓存就将缓存返回
////        return catalogJson;
//        /**
//         * 只要是同一把锁就能锁住需要这个锁的所有线程
//         * 1.synchronized (this)，springboot所有组件在容器中都是单例的
//         * TODO 本地锁：synchronized\JUC(LOCK)，在分布式情况下本地锁只能锁住当前进程，想要锁住所有需要用分布式锁。
//         *
//         *
//         * 锁的时序问题：如果只加synchronized的话会导致还是差很多次数据库：原因是：在第一次查redis并没有数据然后第一个线程去查DB结果还没有放到缓存中已经有其他线程已经去查了数据库了。
//         * 解决方案：在查询了数据库后才把数据放到缓存（在synchronized（）中做）  具体代码实现搜111111111111
//         */
//        synchronized (this) {
//            //得到锁之后再去查一下缓存，因为之前或许已经有线程已经将数据存到缓存中了
//            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
//            if (StringUtils.isNotEmpty(catalogJSON)) {
//                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//                });
//
//                return result;
//            }
//            System.out.println("--------------开始查询数据库--------------");
//
//            //优化:将数据库的多次查询变为一次
//            List<CategoryEntity> selectList = this.baseMapper.selectList(null);
//
//            //1、查出所有分类
//            //1、1）查出所有一级分类
//            List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
//
//            //封装数据为k value到map中
//            Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
//
//                //1、每一个的一级分类,查到这个一级分类的二级分类
//                List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
//
//                //2、封装上面的结果到Catelog2Vo
//                List<Catelog2Vo> catelog2Vos = null;
//                if (categoryEntities != null) {
//                    catelog2Vos = categoryEntities.stream().map(l2 -> {
//                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
//                        //1、找当前二级分类对应的子分类也就是三级分类封装成vo
//                        List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
//                        if (level3Catelog != null) {
//                            List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
//                                //2、封装成指定格式
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
//            //顺便将数据放入缓存中
//            //cache.put("catalogJson", parent_cid);
//            //查到的数据转为JSON并放入redis缓存中（111111111111）
//            System.out.println("---------------------数据放入缓存中---------------------");
//            String s = JSON.toJSONString(parent_cid);
//            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
//            //最终返回出去的数据
//            return parent_cid;
//        }
//
//
//    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //找到parent_cid是指定的值
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}