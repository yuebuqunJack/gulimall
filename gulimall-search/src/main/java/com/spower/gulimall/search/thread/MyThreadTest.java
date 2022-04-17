package com.spower.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author cenziqiang
 * @create 2022/4/4 22:54
 */
public class MyThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...........start..............");
//        CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, executor);

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }, executor).whenComplete((res, exection) -> {
            //可以得到异常信息 但是 无法修改返回数据
            System.out.println("异步任务成功完成了....结果是：" + res + "异常是：" + exection);
        }).exceptionally(throwable -> {
            //可以感知异常，同时返回默认值
            return 10;
        });

        Integer integer = future.get();
        System.out.println("main...........end..............." + integer);
    }
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        System.out.println("main...........start..............");
//        /**
//         * 1.继承Thread
//         * 2.实现Runnable
//         * 3.实现Callable 配合FutureTask使用可以返回结果并处理异常
//         * 4.使用线程池:由于默认线程池有OOM的问题，所以建议自定义线程池
//         * 总结：1、2不能得到返回值 3可以
//         *      1、2、3都不能控制资源
//         *      4、可以控制资源 性能相对稳定
//         */
////---------------------------1.继承Thread---------------------------------------------------
////        Thread01 thread01 = new Thread01();
////        thread01.start();
//
////        new Thread(()-> System.out.println("hello")).start();
//
////---------------------------2.实现Runnable-------------------------------------------------
////        Runnable02 thread02 = new Runnable02();
//////        thread02.run();
////        new Thread(thread02).start();
//
////        FutureTask<Integer> vFutureTask = new FutureTask<>(new Runnable02(),2);
////        new Thread(vFutureTask).start();
////        Integer integer = vFutureTask.get();
////        System.out.println("----------------------:"+integer);
//
////----------------------------3.实现Callable 配合FutureTask使用可以返回结果并处理异常--------------
////        //实现Callable是一个阻塞等待线程，需要按照顺序执行代码结束后再返回执行结果
////        FutureTask<Integer> futureTask = new FutureTask<>(new Callable03());
////        new Thread(futureTask).start();
////        //等待整个线程结束 最后返回结果
////        Integer integer = futureTask.get();
////        System.out.println("main...........end.............." + integer);
//
////----------------------------4.使用线程池:由于默认线程池有OOM的问题，所以建议自定义线程池[ExecutorService]-
//        //原生提交线程池
////        service.submit(new Runnable02());
////        service.execute(new Runnable02());
//        /**
//         *      七大参数：
//         *         int corePoolSize,                  核心线程大小（创建就准备就绪，等待异步任务就去执行，相当于Thread aa = new Thread(); aa.start()了5个。核心线程数一直存在除非设置了allowCoreThreadTimeOut）
//         *         int maximumPoolSize,               最大线程池大小
//         *         long keepAliveTime,                非核心空闲线程存活时间
//         *         TimeUnit unit,                     非核心空闲线程存活的时间单位
//         *         BlockingQueue<Runnable> workQueue, 阻塞队列：线程无法执行完所有任务，将任务放到阻塞队列中，当有线程执行完任务就会从阻塞队列中取出并执行任务
//         *         ThreadFactory threadFactory,       线程的创建工厂
//         *         RejectedExecutionHandler handler   拒绝策略：阻塞队列爆满就触发自己指定的拒绝策略 拒绝执行任务。
//         */
//        /**
//         * Creates a new {@code ThreadPoolExecutor} with the given initial
//         * parameters and default thread factory and rejected execution handler.
//         * It may be more convenient to use one of the {@link Executors} factory
//         * methods instead of this general purpose constructor.
//         *
//         * @param corePoolSize the number of threads to keep in the pool, even
//         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
//         * @param maximumPoolSize the maximum number of threads to allow in the
//         *        pool
//         * @param keepAliveTime when the number of threads is greater than
//         *        the core, this is the maximum time that excess idle threads
//         *        will wait for new tasks before terminating.
//         * @param unit the time unit for the {@code keepAliveTime} argument
//         * @param workQueue the queue to use for holding tasks before they are
//         *        executed.  This queue will hold only the {@code Runnable}
//         *        tasks submitted by the {@code execute} method.
//         * @throws IllegalArgumentException if one of the following holds:<br>
//         *         {@code corePoolSize < 0}<br>
//         *         {@code keepAliveTime < 0}<br>
//         *         {@code maximumPoolSize <= 0}<br>
//         *         {@code maximumPoolSize < corePoolSize}
//         * @throws NullPointerException if {@code workQueue} is null
//         */
//        /**
//         * 线程池执行流程：
//         * 1.线程池创建，创建并就绪核心线程准备执行任务
//         * 2.当调用execute()添加一个请求任务，线程池会做如下判断：
//         *   2.1 当正在运行的线程数量 < 核心线程数 则立马创建线程执行这个任务
//         *   2.2 当正在运行的线程数量 >= 核心线程数 则将任务放入队列（让核心线程做）
//         *     2.2.1 当队列满了 & 正在运行的线程数量 < 最大线程数量 则创建非核心线程立刻执行任务（队列满了再让非核心线程做）
//         *     2.2.2 当队列满了 $ 正在运行的线程数量 >= 最大线程数量 则启用拒绝策略来解决
//         * 3.当一个线程执行完任务会取下另一个任务执行，当线程没有任务可以取时候会触发keepAliveTime，线程池会做如下判断：
//         *   3.1 当正在运行的线程数量 > 核心线程，那么这个线程就会被停掉
//         *   3.2 当所有任务都执行完，线程池最终收缩为核心线程的大小。
//         *
//         *
//         *   new LinkedBlockingQueue<>()默认是Interger的最大值 会导致OOM
//         *   new LinkedBlockingQueue<>(100000) 这样就可以限制大小
//         *
//         *   一个线程池 core：7 max：20 queue：50      100并发进来怎么分配：
//         *      7个任务立刻执行，50个任务会进入队列，其中会再开13个非核心线程执行任务。 100 - （7+50+13） = 30
//         *      如果不想抛弃还要执行只要线程池没关就用CallerRunsPolice
//         *
//         *   快速创建线程池：
//         *         Executors.newFixedThreadPool();       //core = 0   可回收线程
//         *         Executors.newCachedThreadPool();      //core = max 不可回收
//         *         Executors.newScheduledThreadPool();   //定时任务
//         *         Executors.newSingleThreadExecutor()   //单线程 core = maxmumPoolSize =1 一个一个任务执行
//         *
//         * 使用线程池的好处：
//         *    1.提高响应速度 eg：1.线程池没有超过上限时，有的线程会等待任务执行，无需创建新的线程就能立马执行任务。 2.假如单核cpu有1000个线程需要执行，如果线程池控制200个线程执行并发执行就会降低由于cpu时间片内切换线程执行导致的消耗。
//         *    2.降低资源的消耗  重复利用已经创建好的线程，避免重复创建与销毁
//         *    3.提高现成的可管理性  像城池可以根据系统的特点进行配置优化，减少开销提高稳定性，使用线程池统一分配
//         */
//
//
//
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//                5,
//                200,
//                10,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(100000),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy());
//
//        System.out.println("main...........end..............");
//    }

    //1.
    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    //2.
    public static class Runnable02 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    //3.
    public static class Callable03 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
