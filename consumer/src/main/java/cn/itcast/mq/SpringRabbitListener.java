package cn.itcast.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

@Component
@Slf4j
public class SpringRabbitListener {

    // @RabbitListener(queues = "simple.queue")
    // public void listenSimpleQueueMessage(String msg) throws InterruptedException {
    //     System.out.println("spring 消费者接收到消息 ：【" + msg + "】");
    //     throw new MessageConversionException("故意的");
    // }


    @RabbitListener(queues = "work.queue")
    public void listenWorkQueueMessage1(String msg) throws InterruptedException {
        System.out.println("spring 消费者1接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(20);
    }

    @RabbitListener(queues = "work.queue")
    public void listenWorkQueueMessage2(String msg) throws InterruptedException {
        System.err.println("spring 消费者2接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }

    @RabbitListener(queues = "fanout.queue1")
    public void listenFanoutQueue1(String msg) throws InterruptedException {
        System.err.println("spring 消费者1 fanout.queue1 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }

    @RabbitListener(queues = "fanout.queue2")
    public void listenFanoutQueue2(String msg) throws InterruptedException {
        System.err.println("spring 消费者2 fanout.queue2 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }


    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "direct.queue1"), exchange = @Exchange(name = "itcast.direct", type = ExchangeTypes.DIRECT), key = {"red", "blue"}))
    public void listenDirectQueue1(String msg) throws InterruptedException {
        System.err.println("spring 消费者1 direct.queue1 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "direct.queue2"), exchange = @Exchange(name = "itcast.direct", type = ExchangeTypes.DIRECT), key = {"red", "yellow"}))
    public void listenDirectQueue2(String msg) throws InterruptedException {
        System.err.println("spring 消费者2 direct.queue2 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }


    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "topic.queue1"), exchange = @Exchange(name = "itcast.topic", type = ExchangeTypes.TOPIC), key = {"china.#"}))
    public void listenTopicQueue1(String msg) throws InterruptedException {
        System.err.println("spring 消费者1 topic.queue1 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "topic.queue2"), exchange = @Exchange(name = "itcast.topic", type = ExchangeTypes.TOPIC), key = {"#.news"}))
    public void listenTopicQueue2(String msg) throws InterruptedException {
        System.err.println("spring 消费者1 topic.queue2 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }


    @RabbitListener(queues = "object.queue")
    public void listenJsonQueue(Map<String, Object> msg) throws InterruptedException {
        System.err.println("spring 消费者1 topic.queue2 接收到消息 ：【" + msg + "】" + LocalTime.now());
        Thread.sleep(200);
    }


    @RabbitListener(queues = "dlx.queue")
    public void listenDlxQueue(String msg) {
        System.out.println("dlx.queue 消费者接收到消息 ：【" + msg + "】");
    }

    @RabbitListener(bindings = @QueueBinding(value=@Queue(name="delay.queue",durable = "true"),exchange=@Exchange(name="delay.direct",delayed = "true"),key="delay"))
    public void listenDelayMessage(String msg){
        log.info("接收到delay.queue的延迟消息：{}",msg);
    }

}

