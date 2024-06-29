package org.luke.multi;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class helloWorld {
    public static void main(String[] args) throws InterruptedException {
        test t = new test();
        new Thread(t).start();
        new Thread(t).start();
        new Thread(t).start();
        //循环输出现有线程的状态
//        while (true){
//            Thread[] threads = new Thread[Thread.activeCount()];
//            Thread.enumerate(threads);
//            for (Thread thread : threads) {
//                log.info("thread = {}-{}", thread.getName(), thread.getState());
//            }
//            Thread.sleep(2000);
//        }
    }
}

@Slf4j
class test implements Runnable {
    ReentrantLock r = new ReentrantLock();
    int t = 2;

    @Override
    public void run() {
        log.info("Thread.currentThread().getName() = {} 在循环外准备开始循环", Thread.currentThread().getName());
        boolean flag = true;
        while (flag) {
            log.info("Thread.currentThread().getName() = {} 准备动手拿锁", Thread.currentThread().getName());
            r.lock();
            log.info("锁+{}", r.getHoldCount());
            log.info("Thread.currentThread().getName() = {} 拿到了锁", Thread.currentThread().getName());
            if (t > 0) {
                log.info("Thread.currentThread().getName() = {} 在执行 {}", Thread.currentThread().getName(), t);
                t -= 1;

            } else {
                log.info("Thread.currentThread().getName() = {} 解锁,退出循环", Thread.currentThread().getName());
                r.unlock();
                flag = false;
//                break;
            }
//            r.unlock();
        }
        log.info("Thread.currentThread().getName() = {} 执行完毕", Thread.currentThread().getName());
    }
}