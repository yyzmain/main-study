package cn.yyzmain.zk.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: zk工具类</p>
 *
 * @author yyzmain
 * @version 1.0
 */
@Slf4j
@Data
@NoArgsConstructor
public class ZkTool {

    /**
     * 客户端
     */
    private CuratorFramework client;

    /**
     * ZK连接地址
     */
    private String url;

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
     * 会话超时时间
     */
    private int sessionTimeoutMs;
    /**
     * 连接超时时间
     */
    private int connectionTimeoutMs;

    /**
     * 用于本地缓存
     */
    public TreeCache cache;

    private RetryPolicy retryPolicy;


    /**
     * volatile 关键字阻止了使用singleton代码行前后的指令重排,保证线程安全
     */
    private static ZkTool instance;

    public static ZkTool getInstance() {
        if (instance == null) {
            synchronized (ZkTool.class) {
                if (instance == null) {
                    instance = new ZkTool();
                }
            }
        }
        return instance;
    }

    public void init() {
        try {
            if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
                client = getClient(url, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
            } else {
                client = getClient(url, username, password, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
            }
            if (!Strings.isNullOrEmpty(localCachePath)) {
                initLocalCache(localCachePath);
            }
        } catch (Exception e) {
            log.error("===>>> zk client init error!", e);
        }
    }


    /**
     * 获取zk连接 用完close()
     *
     * @param url zk地址
     */
    public static CuratorFramework getClient(String url, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(url, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
        return startAndListenClient(client);
    }

    /**
     * 获取包含namespace的客户端，该客户端创建的path最终地址为namespace+path
     *
     * @param namespace 命名空间
     * @param url       zk连接地址
     */
    public static CuratorFramework getClientWithNamespace(String namespace, String url, int sessionTimeoutMs,
                                                          int connectionTimeoutMs, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .namespace(namespace)
                .connectString(url)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy)
                .build();
        return startAndListenClient(client);
    }

    /**
     * 获取zk连接（带用户名密码） 用完close()
     *
     * @param url zk地址
     */
    public static CuratorFramework getClient(String url, String username, String password, int sessionTimeoutMs,
                                             int connectionTimeoutMs, RetryPolicy retryPolicy) {

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString(url)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .authorization("digest", (username + ":" + password).getBytes())
                .retryPolicy(retryPolicy)
                .build();
        return startAndListenClient(client);
    }

    private static CuratorFramework startAndListenClient(CuratorFramework client) {
        if (client != null) {
            client.start();
            client.getConnectionStateListenable().addListener((cli, state) -> {
                if (state == ConnectionState.LOST) {
                    //连接丢失
                    log.info("===>>>lost session with zookeeper");
                } else if (state == ConnectionState.CONNECTED) {
                    //连接新建
                    log.info("===>>>connected with zookeeper");
                } else if (state == ConnectionState.RECONNECTED) {
                    log.info("===>>>reconnected with zookeeper");
                }
            });
        }
        return client;
    }

    /**
     * 初始化本地缓存
     */
    private void initLocalCache(String watchRootPath) throws Exception {
        cache = new TreeCache(client, watchRootPath);
        TreeCacheListener listener = (cli, event) -> {
            log.info("event:" + event.getType() + " |path:" + (null != event.getData() ? event.getData().getPath() : null));

            if (event.getData() != null && event.getData().getData() != null) {
                log.info("发生变化的节点内容为：" + new String(event.getData().getData()));
            }
        };
        cache.getListenable().addListener(listener);
        cache.start();
    }

    /**
     * 获取不可重入锁
     *
     * @param lockNode
     * @return
     */
    public InterProcessSemaphoreMutex getLock(String lockNode) {
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(client, lockNode);
        try {
            lock.acquire(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("===>>>获取锁异常！", e);
        }
        return lock;
    }

    /**
     * 释放不可重入锁
     *
     * @param lock
     * @return
     */
    public void releaseLock(InterProcessSemaphoreMutex lock) {
        if (lock != null && lock.isAcquiredInThisProcess()) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error("===>>>解锁异常！", e);
            }
        } else {
            log.info("===>>>锁已释放！");
        }
    }

    /**
     * 释放锁（删除节点）
     *
     * @param lockName 锁节点
     */
    public void releaseLock(String lockName) {
        try {
            if (StringUtils.isNotEmpty(lockName)) {
                delete(lockName, true, true);
            }
        } catch (Exception e) {
            log.error("===>>>锁释放异常！", e);
        }
    }

    /**
     * 获取指定节点的数据
     */
    public byte[] getNodeData(String path) {
        try {
            if (cache != null) {
                ChildData data = cache.getCurrentData(path);
                if (data != null) {
                    return data.getData();
                }
            }
            client.getData().forPath(path);
            return client.getData().forPath(path);
        } catch (Exception ex) {
            log.error("===>>>getNodeData error !", ex);
        }
        return null;
    }

    /**
     * 获取路径下的子目录
     *
     * @param path 指定路径
     */
    public static List<String> getChildren(CuratorFramework client, String path) {
        try {
            if (!isExist(client, path)) {
                return Lists.newArrayList();
            }
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            log.error("===>>>getChildren error!", e);
            return Lists.newArrayList();
        }
    }

    public List<String> getChildren(String path) {
        return getChildren(client, path);
    }

    /**
     * 关闭连接
     */
    public void stop() {
        client.close();
    }

    /**
     * 获取路径的数据
     */
    public static String getData(CuratorFramework client, String path) {
        try {
            if (isExist(client, path)) {
                byte[] bytes = client.getData().forPath(path);
                if (bytes != null) {
                    return new String(bytes);
                }
            } else {
                log.error("===>>>节点【{}】不存在！", path);
            }
        } catch (Exception e) {
            log.error("===>>>getData error!", e);
        }
        return null;
    }

    public String getData(String path) {
        return getData(client, path);
    }

    /**
     * 创建永久节点
     *
     * @param client curator 客户端
     * @param path   zk路径
     * @param data   节点数据
     * @return 节点路径
     */
    public static String createPersistentNode(CuratorFramework client, String path, String data) {
        return create(client, path, data, CreateMode.PERSISTENT);
    }

    public String createPersistentNode(String path, String data) {
        return createPersistentNode(client, path, data);
    }

    /**
     * 创建永久节点(权限控制)
     *
     * @return 节点路径
     */
    public static String createPersistentNode(CuratorFramework client, String path, String data, ArrayList<ACL> acls) {
        return create(client, path, data, CreateMode.PERSISTENT, acls);
    }

    public String createPersistentNode(String path, String data, ArrayList<ACL> acls) {
        return createPersistentNode(client, path, data, acls);
    }

    /**
     * 创建永久顺序节点
     *
     * @return 节点路径
     */
    public static String createPersistentSequentialNode(CuratorFramework client, String path, String data) {
        return create(client, path, data, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    public String createPersistentSequentialNode(String path, String data) {
        return createPersistentSequentialNode(client, path, data);
    }

    /**
     * 创建永久顺序节点(权限控制)
     *
     * @return 节点路径
     */
    public static String createPersistentSequentialNode(CuratorFramework client, String path, String data, ArrayList<ACL> acls) {
        return create(client, path, data, CreateMode.PERSISTENT_SEQUENTIAL, acls);
    }

    public String createPersistentSequentialNode(String path, String data, ArrayList<ACL> acls) {
        return createPersistentSequentialNode(client, path, data, acls);
    }

    /**
     * 创建临时节点
     *
     * @return 节点路径
     */
    public static String createEphemeralNode(CuratorFramework client, String path, String data) {
        return create(client, path, data, CreateMode.EPHEMERAL);
    }

    public String createEphemeralNode(String path, String data) {
        return createEphemeralNode(client, path, data);
    }

    /**
     * 创建临时节点(权限控制)
     *
     * @return 节点路径
     */
    public static String createEphemeralNode(CuratorFramework client, String path, String data, ArrayList<ACL> acls) {
        return create(client, path, data, CreateMode.EPHEMERAL, acls);
    }

    public String createEphemeralNode(String path, String data, ArrayList<ACL> acls) {
        return createEphemeralNode(client, path, data, acls);
    }

    /**
     * 创建临时顺序节点
     *
     * @return 节点路径
     */
    public static String createEphemeralSequentialNode(CuratorFramework client, String path, String data) {
        return create(client, path, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public String createEphemeralSequentialNode(String path, String data) {
        return createEphemeralSequentialNode(client, path, data);
    }

    /**
     * 创建临时顺序节点(权限控制)
     *
     * @return 节点路径
     */
    public static String createEphemeralSequentialNode(CuratorFramework client, String path, String data, ArrayList<ACL> acls) {
        return create(client, path, data, CreateMode.EPHEMERAL_SEQUENTIAL, acls);
    }

    public String createEphemeralSequentialNode(String path, String data, ArrayList<ACL> acls) {
        return createEphemeralSequentialNode(client, path, data, acls);
    }

    /**
     * 创建节点
     *
     * @param mode 节点类型
     *             1、PERSISTENT 持久化目录节点，存储的数据不会丢失。
     *             2、PERSISTENT_SEQUENTIAL顺序自动编号的持久化目录节点，存储的数据不会丢失
     *             3、EPHEMERAL临时目录节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除
     *             4、EPHEMERAL_SEQUENTIAL临时自动编号节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除，
     *             并且根据当前已经存在的节点数自动加 1，然后返回给客户端已经成功创建的目录节点名。
     */
    private static String create(CuratorFramework client, String path, String data, CreateMode mode) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(mode).forPath(path, Strings.isNullOrEmpty(data) ? null : data.getBytes());
        } catch (Exception e) {
            log.error("===>>>create error!", e);
            return null;
        }
    }

    /**
     * 创建节点（带权限控制）
     */
    private static String create(CuratorFramework client, String path, String data, CreateMode mode, ArrayList<ACL> acls) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(mode).withACL(acls).forPath(path, Strings.isNullOrEmpty(data) ? null : data.getBytes());
        } catch (Exception e) {
            log.error("===>>>create with acls error!", e);
            return null;
        }
    }

    /**
     * 修改数据
     */
    public static void update(CuratorFramework client, String path, String data) throws Exception {
        client.setData().forPath(path, Strings.isNullOrEmpty(data) ? null : data.getBytes());
    }

    public void update(String path, String data) throws Exception {
        if (isExist(path)) {
            update(client, path, data);
        }
    }


    /**
     * 删除节点
     *
     * @param deleteChildren 是否删除子节点
     * @param alwaysOnFailed 后台记录删除失败节点，一直删除直到删除成功
     */
    public static void delete(CuratorFramework client, String path, boolean deleteChildren, boolean alwaysOnFailed) throws Exception {
        if (isExist(client, path)) {
            if (alwaysOnFailed) {
                if (deleteChildren) {
                    client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
                } else {
                    client.delete().guaranteed().withVersion(-1).forPath(path);
                }
            } else {
                if (deleteChildren) {
                    client.delete().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
                } else {
                    client.delete().withVersion(-1).forPath(path);
                }
            }
        }
    }

    public void delete(String path, boolean deleteChildren, boolean alwaysOnFailed) throws Exception {
        delete(client, path, deleteChildren, alwaysOnFailed);
    }


    /**
     * 删除节点（包括子节点）
     */
    public static void delete(CuratorFramework client, String path) throws Exception {
        //withVersion(-1)无视版本，直接删除
        client.delete().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
    }

    /**
     * 删除节点（包括子节点）
     *
     * @param alwaysOnFailed 删除失败了也会一直在后台尝试删除 ,true or false.
     */
    public static void delete(CuratorFramework client, String path, boolean alwaysOnFailed) throws Exception {
        if (alwaysOnFailed) {
            client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
        } else {
            client.delete().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
        }
    }

    /**
     * 是否存在路径
     */
    public static boolean isExist(CuratorFramework client, String path) {
        try {
            client.sync();
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            log.error("===>>> isExist error!", e);
            return false;
        }
    }

    public boolean isExist(String path) {
        return isExist(client, path);
    }

    /**
     * 新增节点监听
     */
    public static NodeCache addListener(CuratorFramework client, String path, NodeCacheListener listener) throws Exception {
        final NodeCache cache = new NodeCache(client, path);
        cache.start();
        cache.getListenable().addListener(listener);
        return cache;
    }

    public NodeCache addListener(String path, NodeCacheListener listener) throws Exception {
        return addListener(client, path, listener);
    }

    /**
     * 新增子节点监听
     */
    public static PathChildrenCache addChildrenListener(CuratorFramework client, String path, PathChildrenCacheListener listener) throws Exception {
        final PathChildrenCache cache = new PathChildrenCache(client, path, false);
        cache.start();
        cache.getListenable().addListener(listener);
        return cache;
    }

    public PathChildrenCache addChildrenListener(String path, PathChildrenCacheListener listener) throws Exception {

        return addChildrenListener(client, path, listener);
    }

    /**
     * 新增树监听
     */
    public static TreeCache addTreeListener(CuratorFramework client, String path, TreeCacheListener listener) throws Exception {
        TreeCache.Builder builder = TreeCache.newBuilder(client, path);
        TreeCache cache = builder.build();
        cache.start();
        cache.getListenable().addListener(listener);
        return cache;
    }

    public TreeCache addTreeListener(String path, TreeCacheListener listener) throws Exception {

        return addTreeListener(client, path, listener);
    }

    /**
     * 新增树监听
     *
     * @param maxDepth 该路径下的最大监听层级
     * @param executor 执行线程池
     */
    public static TreeCache addTreeListener(CuratorFramework client, String path, int maxDepth, TreeCacheListener listener, Executor executor) throws Exception {
        TreeCache.Builder builder = TreeCache.newBuilder(client, path);
        builder.setMaxDepth(maxDepth);
        TreeCache cache = builder.build();
        cache.start();
        cache.getListenable().addListener(listener, executor);
        return cache;
    }

    public TreeCache addTreeListener(String path, int maxDepth, TreeCacheListener listener, Executor executor) throws Exception {
        return addTreeListener(client, path, maxDepth, listener, executor);
    }

    /**
     * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
     */
    ExecutorService pool = Executors.newFixedThreadPool(2);

    /**
     * 监听数据节点的变化情况
     */
    public void watchPath(String watchPath, TreeCacheListener listener) {
        //   NodeCache nodeCache = new NodeCache(client, watchPath, false);
        TreeCache cache = new TreeCache(client, watchPath);
        cache.getListenable().addListener(listener, pool);
        try {
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取读写锁
     */
    public static InterProcessReadWriteLock getReadWriteLock(CuratorFramework client, String path) {
        return new InterProcessReadWriteLock(client, path);
    }

    public InterProcessReadWriteLock getReadWriteLock(String path) {
        return getReadWriteLock(client, path);
    }
}
