package org.luke.multi.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Domain {
    public static void main(String[] args) throws InterruptedException {
        ReentrantLockTest t = new ReentrantLockTest();
        new Thread(t).start();
        new Thread(t).start();
        new Thread(t).start();
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
class ReentrantLockTest implements Runnable {
    ReentrantLock r = new ReentrantLock();
    int t = 2;

    @Override
    public void run() {
        log.info("ThreadName():{} 在循环外准备开始循环", Thread.currentThread().getName());
        while (true) {
            log.info("ThreadName():{} 准备动手拿锁", Thread.currentThread().getName());
            r.lock();
            log.info("ThreadName():{} 拿到了锁", Thread.currentThread().getName());
            if (t > 0) {
                log.info("ThreadName():{} 在执行 {}", Thread.currentThread().getName(), t);
                t -= 1;
            } else {
                log.info("ThreadName():{} 解锁,退出循环", Thread.currentThread().getName());
                r.unlock();
                break;
            }
        }
        log.info("ThreadName():{} 执行完毕", Thread.currentThread().getName());
    }
}