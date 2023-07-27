package cn.yyzmain.zk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ZK配置信息
 */
@Component
@ConfigurationProperties(prefix = "zookeeper")
@Getter
@Setter
public class ZkProperties {

    /**
     * zk连接地址
     */
    private String address;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 本地缓存路径
     */
    private String localCachePath;

    /**
     * 会话超时时间：ms
     */
    private int sessionTimeoutMs = 40000;
    /**
     * 连接超时时间：ms
     */
    private int connectionTimeoutMs = 40000;
    /**
     * 重试间隔：ms
     */
    private int baseSleepTimeMs = 1000;
    /**
     * 最大重试次数
     */
    private int maxRetries = 20;

}
