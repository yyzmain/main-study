package cn.yyzmain.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MainCustomPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 获取主题分区数
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();

        // 按数据开头数据分配分区
        final String message = String.valueOf(value);
        if (message.startsWith("a-")) {
            return 0;
        }
        if (message.startsWith("b-")) {
            return 1;
        }
        if (message.startsWith("c-")) {
            return 2;
        }

        // 负载均衡分区
        return Math.abs(message.hashCode() % numPartitions);
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void configure(Map<String, ?> configs) {
        //
    }

}