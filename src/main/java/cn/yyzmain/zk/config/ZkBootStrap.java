package cn.yyzmain.zk.config;

import cn.yyzmain.zk.utils.ZkTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * zk初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZkBootStrap implements ApplicationRunner {

    private final ZkProperties zkProperties;

    public static final ZkTool ZK = ZkTool.getInstance();

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("========>>>zk client is starting...");
            //重连策略：间隔1秒重连，重连20次
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(zkProperties.getBaseSleepTimeMs(), zkProperties.getMaxRetries());
            ZK.setUrl(zkProperties.getAddress());
            ZK.setUsername(zkProperties.getUsername());
            ZK.setPassword(zkProperties.getPassword());
            ZK.setSessionTimeoutMs(zkProperties.getSessionTimeoutMs());
            ZK.setConnectionTimeoutMs(zkProperties.getConnectionTimeoutMs());
            ZK.setRetryPolicy(retryPolicy);
            ZK.init();
            log.info("========>>>zk client is started..");
        } catch (Exception e) {
            log.error("========>>>zk client is start error..", e);
        }
    }
}
