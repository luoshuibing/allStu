# 不屈日记

# MQ

同步调用的优点：
* 时效性强，可以立即得到结果

同步调用的问题
* 耦合度
* 性能和吞度能力下降
* 有额外的资源消耗
* 有级联失败问题

异步通信优点
* 依赖broker的可靠性、安全性、吞吐能力
* 架构复杂了，业务没有明显的流程线，不好追踪管理

[kafka高可能性原理探究](https://blog.csdn.net/u011598442/article/details/130312978)

MQ区别：公开协用吞延靠

 表头  | RabbitMQ             | ActiveMQ                      | RocketMQ |kafka
 ---- |----------------------|-------------------------------|----------| ------
 公司  | rabbit               | apache                        | alibaba  | apache
 开发语言  | erlang               | java                          | java     | scala
 协议  | AMQP、XMPP、SMTP、STOMP | OpenWire、STOMP、REST、XMPP、AMQP | 自定义协议    | 自定义协议
 可用性  | 高                    | 一般                            | 高        | 高
 吞吐量  | 一般                   | 差                             | 高        | 高
 消费延迟  | 微妙级                  | 毫秒级                           | 毫秒级      | 毫秒以内
 消息可靠性  | 高                    | 一般                            | 高        | 一般

[数据库主从模式](https://blog.csdn.net/weixin_65175398/article/details/135262931)

RabbitMq快速入门   ceqv
* channel:操作MQ的工具
* exchange:路由消息到队列
* queue：缓存消息
* virtual hosta:虚拟主机，是对queue、exchange等资源的逻辑分组

消息模型 BW广路主
* BasicQueue（基本消息队列）
* WorkQueue（工作消息队列）
* 广播
* 路由
* 主题

交换机作用
* 接受publisher发送的消息
* 将消息按照规则路由到与之绑定的队列
* 不能缓存消息，路由失败，消息丢失
* FanoutExchange的会将消息路由到每个绑定的队列

消息队列的作用：
* 异步
* 解耦
* 限流

rabbitMq生产者连接失败重试机制：
> 当网络不稳定的时候，利用重试机制可以有效提高消息发送的成功率，不过springAMQP提供的重试机制是阻塞式的重试，
> 也就是说多吃重试等待的过程中，当前线程是被阻塞的，会影响业务性能。
> 如果对于业务性能有要求的建议禁用重试机制。如果一定要使用，请合理配置等待时长和重试次数，当然也可以考虑使用
> 异步线程来执行发送消息的代码。


生产者确认机制：

RabbitMQ  Publisher Confirm和Publisher Return两种确认机制。开启确认机制后，在MQ成功收到信息后会返回确认消息给生产者。
* 消息投递到了MQ，但是路由失败。此时会通过PublisherReturn返回路由异常，然后返回ACK
* 临时消息投递到MQ，并且入队成功，返回ACK，告知投递成功
* 持久消息投递到了MQ，并且入队完成持久化，返回ACK，告知投递成功
* 其他情况都会返回NACK，告知投递失败

惰性队列：

特性：
* 接收到消息后直接存入磁盘而非内存（默认2048条）
* 消费者要消费消息时，才会从磁盘中读取并加载到内存
* 支持数百万条的消息存储

RabbitMQ如何保证消息的可靠性
* 配置交换机、队列、消息持久化到磁盘
* rabbitMQ在3.6版本引入了LazyQueue，并且在3.12版本后会成为队列的默认模式，LazyQueue会将所有消息都持久化
* 开启持久化和生产者确认时，rabbitmq只有在消息持久化完成后才会给生产者返回ACK回执


消费者确认机制

当消费者处理消息结束后，应该向rabiitMQ发送一个回执，告诉Rabbitmq自己消息处理状态。
* ack:成功处理消息，rabbitmq从队列中删除该消息
* nack:消息处理失败，rabbitmq需要再次投递消息
* reject：消息处理失败并拒绝该消息，rabbitmq从队列中删除该消息

SpringAMQP已经实现了消息确认功能，并允许我们通过配置文件选择ACK处理方式
* none:不处理。即消息投递给消费者后立刻ack，消息立刻从MQ删除。非常不安全，不建议使用
* manual：手动模式。需要自己在业务代码中调用api，发送ack或者reject，存在业务入侵，但更灵活
* auto：自动模式。SpringAMQP利用AOP对我们的消息处理逻辑做环绕增强，当业务正常执行时则自动返回ack
  * 如果是业务异常，会自动返回nack
  * 如果是消息处理或校验异常，自动返回reject   MessageConvertException

在开启重试模式后，重试次数耗尽，如果消息依然失败，则需要有MessageRecoverer接口来处理，它包含三种不同的实现
* RejectAndDontRequeueRecoverer:重试耗尽后，直接reject，丢弃消息
* ImmediateRequeueMessageRecoverer:重试耗尽后，返回nack，消息重新入队
* RepublishMessageRecoverer:重试耗尽后，将失败消息投递到指定的交换机

业务幂等性
* 通过给消息添加消息ID，并且将消息ID保存到数据库
* 通过业务乐观锁实现

延迟消息：
* 生产者发送消息时指定一个时间，消费者不会立刻收到消息，而是在指定时间之后才收到消息



死信交换机 死信
* 消费者使用basic.reject或basic.nack声明消费失败，并且消费的requeue参数设置为false
* 消息是一个过期消息，超时无人消费
* 要投递的队列消费堆积满了，最早的消息可能成为死信



































































































































































