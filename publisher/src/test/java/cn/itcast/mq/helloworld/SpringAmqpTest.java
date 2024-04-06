package cn.itcast.mq.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue() {
        String queueName = "simple.queue";
        String message = "hello, spring amqp!";
        rabbitTemplate.convertAndSend(queueName, message);
    }

    @Test
    public void testTTLmessage() {
        rabbitTemplate.convertAndSend("simple.direct", "hi", "hello",new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
    }

    @Test
    public void testWorkQueue() throws InterruptedException {
        // 队列名称
        String queueName = "work.queue";
        // 消息
        String message = "hello, message__";
        for (int i = 0; i < 50; i++) {
            // 发送消息
            rabbitTemplate.convertAndSend(queueName, message + i);
            // 避免发送太快
            Thread.sleep(20);
        }
    }


    @Test
    public void testFanoutExchange() {
        // 队列名称
        String exchangeName = "itcast.fanout";
        // 消息
        String message = "hello, everyOne__";
        // 发送消息
        rabbitTemplate.convertAndSend(exchangeName, "", message);
    }


    @Test
    public void testDirectExchange() {
        // 队列名称
        String exchangeName = "itcast.direct";
        // 消息
        String message = "hello, blue";
        // 发送消息
        rabbitTemplate.convertAndSend(exchangeName, "blue", message);
    }

    @Test
    public void testDirectTopic() {
        // 队列名称
        String exchangeName = "itcast.topic";
        // 消息
        String message = "hello, 上市";
        // 发送消息
        rabbitTemplate.convertAndSend(exchangeName, "china.weather", message);
    }

    @Test
    public void testSendObject() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("name", "张三");
        msg.put("age", 20);
        String queue = "object.queue";
        rabbitTemplate.convertAndSend(queue, msg);
    }

    @Test
    public void testPublisherConfirm() throws InterruptedException {
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        cd.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("handle message ack fail", ex);
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result.isAck()) {
                    log.debug("发送消息成功，收到ack！");
                } else {
                    log.error("发送消息失败，收到nack，reason:{}", result.getReason());
                }
            }
        });
        rabbitTemplate.convertAndSend("itcast.topic", "china213.weather12121", "hello", cd);
        Thread.sleep(2000);
    }

    @Test
    public void testPageOut(){
        Message msg = MessageBuilder.withBody("hello".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT).build();
        for (int i = 0; i < 1000000; i++) {
            rabbitTemplate.convertAndSend("lazy.queue",msg);
        }
    }































}
