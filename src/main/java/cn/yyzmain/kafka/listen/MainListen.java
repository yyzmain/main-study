package cn.yyzmain.kafka.listen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainListen {


    @KafkaListener(topics = "${app.main.yyzmain:yyzmain}", containerFactory = "batchFactory")
    public void handleReceiveDataTopic(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        try {
            records.forEach(v -> log.info("消费到数据: {}", v));
        } catch (Exception e) {
            log.error("处理异常，{}", e.getMessage(), e);
        } finally {
            //消息处理完毕，手动提交
            ack.acknowledge();
        }
    }

}
