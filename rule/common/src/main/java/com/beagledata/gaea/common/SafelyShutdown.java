package com.beagledata.gaea.common;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 安全停止服务，等待正在执行的线程执行完毕，再停止服务
 *
 * Created by liulu on 2020/5/20.
 */
public class SafelyShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 停止服务前，等待线程池处理完请求的超时时间，单位：秒
     */
    private static final int SHUTDOWN_WAIT = 60;

    private volatile Connector connector;

    private int waitSeconds;

    public SafelyShutdown() {
        this(SHUTDOWN_WAIT);
    }
    public SafelyShutdown(int waitSeconds) {
        this.waitSeconds = Math.max(waitSeconds, SHUTDOWN_WAIT);
    }

    @Override
    public void customize(Connector connector) {
        if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>) {
            //解决springboot 捕捉不到上传文件过大异常
            ((AbstractHttp11Protocol) connector.getProtocolHandler()).setMaxSwallowSize(-1);
        }
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        logger.info("开始停止服务，等待{}秒执行完请求中线程", waitSeconds);
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            try {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                if (threadPoolExecutor.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
                    logger.info("服务已停止");
                } else {
                    logger.warn("等待{}秒超时，强制停止服务", waitSeconds);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
