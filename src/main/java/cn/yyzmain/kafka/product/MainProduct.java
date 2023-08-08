package cn.yyzmain.kafka.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainProduct {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String msg) {
        log.info("开始发送kafka数据,topic:{}, msg:{}", topic, msg);
        kafkaTemplate.send(topic, msg);
        log.info("====================end=======================");
    }

    public void sendMessageCallback(String topic, String msg) {
        log.info("开始发送kafka数据,topic:{}, msg:{}", topic, msg);
        ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, msg);
        result.addCallback(sendResult -> log.info("==>>>消息发送成功..."), throwable -> {
            log.error("==>>>消息发送失败!", throwable);
            // 兜底处理
            result.isCancelled();
        });
        log.info("====================end=======================");
    }
}
