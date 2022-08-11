package com.wang.execution_scheduling;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 参考： https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling
 */
@Component
public class TaskExecutorExample {
    private class MessagePrinterTask implements Runnable {

        private String message;

        public MessagePrinterTask(String message) {
            this.message = message;
        }

        public void run() {
            System.out.println(message);
        }
    }

    @Resource
    private TaskExecutor taskExecutor;

    /**
     * 需要在启动类中使用EnableScheduling注解激活，单位ms
     */
    @Scheduled(fixedDelay = 500000)
    public void printMessages() {
        for(int i = 0; i < 25; i++) {
            taskExecutor.execute(new MessagePrinterTask("Message" + i));
        }
    }

    /**
     * 当调用时，异步执行该方法
     */
    @Async
    public void doSomething() {
        System.out.println("doSomething...");
    }
}
