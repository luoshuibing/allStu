package cn.itcast.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MqMetaDataConfig {

    @Bean
    public Queue lazyQueue() {
        //开启惰性队列   或者     @RabbitListener(queuesToDeclare=@Queue(name="lazy.queue",durable="true",arguments = @Argument(name="x-queue-mode",value="lazy")))
        return QueueBuilder.durable("lazy.queue").lazy().build();
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("itcast.fanout");
    }

    @Bean
    public Queue fanoutQueue1() {
        return new Queue("fanout.queue1");
    }

    @Bean
    public Queue fanoutQueue2() {
        return new Queue("fanout.queue2");
    }

    @Bean
    public Queue workQueue() {
        return new Queue("work.queue");
    }

    @Bean
    public Queue simpleQueue() {
        return new Queue("simple.queue");
    }

    @Bean
    public Binding fanoutBinding1(Queue fanoutQueue1, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    @Bean
    public Binding fanoutBinding2(Queue fanoutQueue2, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
    }

    @Bean
    public Queue objectQueue() {
        return new Queue("object.queue");
    }

    @Bean
    public DirectExchange delayExchange(){
        //设置delay的属性为true
        return ExchangeBuilder.directExchange("delay.bean.direct").delayed().durable(true).build();
    }



}
