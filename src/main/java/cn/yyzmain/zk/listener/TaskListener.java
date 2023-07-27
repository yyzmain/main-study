package cn.yyzmain.zk.listener;

import cn.yyzmain.zk.config.ZkBootStrap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * zk监听器
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class TaskListener implements CommandLineRunner {


    @Value("${zk.listenerNodeName:/home}")
    private String listenerNodeName;

    @Override
    public void run(String... args) {
        log.info("开启节点监听---");
        addListener();
    }

    private void addListener() {
        try {
            //监听任务节点变化，若当前没有任务为空，那么调用competeTask进行任务执行
            ZkBootStrap.ZK.addChildrenListener(listenerNodeName, (client, event) -> {
                PathChildrenCacheEvent.Type type = event.getType();
                //新增和更新时检查线程执行
                log.info("zk节点发生改变，变化类型:{}", type);
                List<String> tasks = client.getChildren().forPath(listenerNodeName);
                log.info("获取到子节点信息:{}", tasks);

            });
        } catch (Exception e) {
            log.error("zk监听器异常！", e);
        }
    }
}
