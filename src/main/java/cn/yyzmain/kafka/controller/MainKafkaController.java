package cn.yyzmain.kafka.controller;

import cn.yyzmain.kafka.product.MainProduct;
import cn.yyzmain.result.MainResult;
import cn.yyzmain.result.MainResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mainKafka")
@Api(tags = "kafka")
@Slf4j
@RequiredArgsConstructor
public class MainKafkaController {

    private final MainProduct mainProduct;

    @ApiOperation("发送数据-1")
    @PostMapping("/sendMessage")
    public MainResult<String> sendMessage(String topic, String msg) {
        mainProduct.sendMessage(topic, msg);
        return MainResultGenerator.createOkResult();
    }

    @ApiOperation("发送数据-2")
    @PostMapping("/add")
    public MainResult<String> sendMessageCallback(String topic, String msg) {
        mainProduct.sendMessageCallback(topic, msg);
        return MainResultGenerator.createOkResult();
    }

}
