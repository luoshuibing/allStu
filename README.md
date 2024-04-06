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






















































































































































































