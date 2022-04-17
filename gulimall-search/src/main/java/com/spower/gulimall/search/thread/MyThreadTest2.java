package com.spower.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author cenziqiang
 * @create 2022/4/4 22:54
 */
public class MyThreadTest2 {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...........start..............");
//        CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, executor);

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).whenComplete((res, exection) -> {
//            //可以得到异常信息 但是 无法修改返回数据
//            System.out.println("异步任务成功完成了....结果是：" + res + "异常是：" + exection);
//        }).exceptionally(throwable -> {
//            //可以感知异常，同时返回默认值
//            return 10;
//        });

        //handle最终处理：方法完成后处理
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = future.get();
//        System.out.println("main...........end..............." + integer);


//        //线程串行化：一个任务干完了干下一个任务
//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).thenAcceptAsync(res -> {
//            System.out.println("任务1启动了........" + res);
//        });
//        System.out.println("main...........end...............");
//    }

        //线程串行化：一个任务干完了干下一个任务
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).thenApplyAsync(res -> {
//            System.out.println("任务1启动了........" + res);
//            return res;
//        }, executor);
//        System.out.println("main...........end..............." + future.get());
//    }


        //两任务都要完成才能进行下一步
        //任务1
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1开始" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结束：" + i);
//            return i;
//        }, executor);
//        //任务2
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2开始" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//                System.out.println("任务2结束");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return "Hello";
//        }, executor);

        //任务1 2 完成后任务3才开始
//        future01.runAfterBothAsync(future02, () -> {
//            System.out.println("任务3开始");
//        }, executor);

        //任务1 2 完成后任务3可以查看1 2 结果
//        future02.thenAcceptBothAsync(future01, (f1, f2) -> {
//            System.out.println("任务3开始...可以查看之前的结果：" + f1 + "||||" + f2);
//        }, executor);
        //任务1 2 完成后任务3可以查看1 2 结果并进行下一步操作升级版
//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + "|||||||" + f2 + "|||";
//        }, executor);
//        System.out.println("dddddddddddddddd:" + future.get());

        //当其中一个任务完成后执行操作
//        future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("任务3开始...");
//        }, executor);

        //当其中一个任务完成后执行操作 并有返回值
//        future02.acceptEitherAsync(future01, (res) -> {
//            System.out.println("任务3开始得到返回结果：" + res);
//        }, executor);

        //当其中一个任务完成后执行操作 有返回结果 自己也可以感知结果
//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, res -> {
//            System.out.println("--------------------返回结果：" + res);
//            return res.toString() + "hahahahaha";
//        }, executor);
//        System.out.println("|||||||||||||||||||||" + future.get());

        //多任务组合 allof等待所有任务完成
        CompletableFuture<String> futureImage = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色+256G";
        }, executor);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {

            try {
                Thread.sleep(3000);
                System.out.println("查询商品的描述");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "华为";
        }, executor);

//        futureImage.get();
//        futureAttr.get();
//        futureDesc.get();
        //优化上面的三句代码 allOf全部都要完成才结束
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImage, futureAttr, futureDesc);
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImage, futureAttr, futureDesc);
        //等待三个结果完成
//        allOf.get();
//        allOf.join();
//        System.out.println("-----------------:" + futureImage.get() + "--" + futureAttr.get() + "--" + futureDesc.get());
        System.out.println("-----------------:" + anyOf.get());


        System.out.println("main........start..........");
    }
}
