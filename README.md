# 中年大叔

##  场景题

### 申请单回填金额到期自动关闭

在企业实名系统中，企业实名完成打款认证流程需要回填金额，回填金额有时间限制，就需要把打款状态设置为回填金额失败状态。

订单的到期关闭的实现有很多方式。
* 被动关闭（不推荐）

在解决这类问题的时候，有一种比较简单的方式，那就是通过业务上的被动方式来进行关单操作。

简单点说，就是订单创建好了以后，我们系统上不做主动关单，什么时候用户来访问这个订单了，再去判断时间是不是超过了过期时间，如果过了时间那就进行关单操作，然后再提示用户。
这种操作是最简单的，基本不需要开发定时关闭的功能，但是他的缺点也很明显，那就是如果用户一直不来查看这个订单，那么就会有很多脏数据冗余在数据库中一直无法被关单。

还有一个缺点，那就是需要在用户的查询过程中进行写的操作，一般写操作都会比读操作耗时更长，而且有失败的可能，一旦关单失败，就会导致系统处理起来比较复杂。



* 定时任务

定时任务定时扫描所有到期的订单，然后执行关单动作。
但是有以下几个问题。
1. 时间不精确。

2. 无法处理大量订单量。

3. 对数据库造成压力。

4. 分库分表问题。

   

* DelayQueue（不推荐，基于内存，无法持久化）

使用DelayQueue实现超时关单的方案，实现起来简单，不需要依赖第三方的框架和类库，JDK原生就支持。

当然这个方案，如果订单量过大，能导致OOM的问题。DelayQueue是基于JVM内存，一旦机器重启，数据就丢失。基于JDK的DelayQueue方案只适合单机。



* 时间轮（不推荐，基于内存，无法持久化）

因为DelayQueue插入和删除操作的平均时间复杂度-O(nlog(n)),虽然已经挺好，但是时间轮的方案可以插入和删除操作的时间复杂度都降为O(1).

基于Netty的HashedWheelTimer可以帮助我们快速的实现一个时间轮，这种方式和DelayQueue类似，缺点都是基于内存、集群扩展麻烦、内存有限制等等。



* kafka（MQ方案不推荐，大量无效调度）

kafka内部有很多延时性操作，如延时生产，延时拉取，延时数据删除等，这些延时功能由内部的延时操作管理器来做专门的处理，其底层是采用时间轮实现的。

为了解决有一些时间跨度大的延时任务，kafka还引入了层级时间轮，能更好控制时间力度。

kafka中的时间轮，在实现方式上优点复杂，需要依赖kafka，但是他的稳定性和性能都要更高一些，而且适合用在分布式场景中。



* RocketMQ延迟消息（MQ方案不推荐，大量无效调度）

RocketMQ支持延迟消息，只是支持固定时长的延迟消息1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h



* RabbitMQ死信队列（MQ方案不推荐，大量无效调度）

当RabbitMQ中的一条正常的消息，因为过了存活时间（TTL过期）、队列长度超限、被消费者拒绝等原因无法被消费时，就会变成Dead Message。

基于RabbitMQ的死信队列，可以实现延迟消息，非常灵活的实现定时关单，并且借助RabbitMQ的集群扩展性，可以实现高可用，以及处理大并发。缺点是可能消息阻塞。



* Redis过期监听（不推荐，容易丢消息）

redis加入配置过期监听，实现监听器就可以监听key的过期消息。redis并不保证key在过期的时候就能被立即删除，更不保证这个消息能被立即发出。所以，消息延迟是必然存在的。




* Redis的ZSet（不推荐，可能会重复消费）

订单超时时间的时间戳（下单时间+超时时长）与订单号分别设置为 score 和 member。

使用redis zset来实现订单关闭的功能的优点是可以借助redis的持久化、高可用机制。避免数据丢失。但是这个方案也有缺点，那就是在高并发场景中，有可能有多个消费者同时获取到同一个订单号，一般采用加分布式锁解决，这样做也会降低吞吐量。



* Redisson（推荐，可以用）

Redission定义了分布式延迟队列RDelayedQueue，这是一种基于我们前面介绍过的zset结构实现的延时队列，它允许以指定的延时时长将元素放到目标队列中。

基于Redission的实现方式，是可以解决基于zset方案中的并发重复问题，而且还能实现方式也比较简单，稳定性、性能都比较高。




##  线上问题排查

### RocketMQ消费堆积问题排查

目前对RocketMQ未曾了解






##  项目难点&亮点

### 引入分布式锁解决并发问题

我们有一个企业实名的申请单，这个申请单进行定时审核、申请单审核结果通知发送短信的过程中，采用分布式锁方式来解决。
技术选型：
分布式锁的方式，主要有基于数据库、zookeeper以及redis。最简单的是数据库的方式，缺点太依赖数据库，高并发下，可能对数据库造成压力，并且性能也不好。

redis在我们系统已经使用，但是zk的话我们没有引入，搭建一个zk集群成本高。

在redis中，最开始使用senx方案，后来出现并发，发现因为有的时候，我们设置的解锁超时时间太短，导致锁提前释放。但是拉长超时时间又会降低并发度。后来redisson其实
实现了看门狗，可以帮助我们自动续期，就选用了redisson的方案。

但是其实这种方案也存在一定的问题，那就是redis的主节点如果出现问题，可能会导致锁失效，后来使用了redis的redlock来解决这个问题，redlock可以借助集群的投票机制，超过板书写入就算加锁成功，这样可解决单点故障的问题。




##  大厂实践

### 数据库能抗秒杀的原理

1. 减少行级锁的并发。（热点行数据排序执行）

2. 减少B+树的索引遍历。（通过缓存的方式执行）

3. 减少事务提交的次数。（通过SQL排序执行）

   

### 秒杀系统

1. 库存拆分。（将库存分为多个库，同时要考虑库融合的方式。）

2. 缓存+数据库。（redis+mysql（阿里插件））

3. 数据库。（mysql（阿里插件））

   

##  Java基础

### Java语言特点

1. Java是解释性语言
2. Java是跨平台
3. Java自动内存管理
4. Java是值传递
5. Java是单继承


##  集合类

### Java集合有哪些？如何分类？

Java的整个集合框架中有List、Set、Queue、Stack、Map等五种数据结果，前四种是单一元素，Map是K-V结构

List可以有链表和数组实现方式，链表增删快，查询慢，数组查询快，增删慢，Queue可以分为优先队列和双端队列。Map可以分为HashMap和可以排序的TreeMap。

Set相对于List是一个无需的可以去重的列表，一定有equals、hashcode、comareTo方法。

#### Collections和Collection区别

Collections是集合的工具类，Collection是集合的顶级接口。

#### 遍历集合

1. for
2. iterator
3. foreach
4. Enumeration
5. Stream

#### Iterable和Iterator

Iterable表示可以遍历
Iterator表示遍历的方法具体实现

##  Java并发

### 什么是多线程上下文切换

线程上下文切换是指cpu从一个线程切换到另外一个线程，需要记录当前线程的线程状态，并且恢复另外一个线程的线程状态。线程状态包括程寄栈，程序计数器，寄存器，栈指针。

在多线程中，上下文切换的开销比直接用单线程大，因为在多线程中，需要保存和恢复更多的上下文信息。过多的上下文切换会降低系统的运行效率，因此需要尽可能的减少上下文切换的次数。

##  Jvm

#### 什么是平台无关性

一次编译，到处执行

#### 平台无关性的实现

Java语言规范、Class文件、Jvm。

在Java平台中，想要把Java文件，编译成二进制文件，需要经过两步编译，前段编译和后端编译。

Java中，我们所熟知的javac的编译就是前端编译。除了这个以外，都内置了前端编译器。主要功能就是把java代码转换成class代码。

后端编译主要是将中间代码再编译成机器语言。java中，这一步骤就是java虚拟机来执行的。

#### Java中基本数据类型大小都是确定的吗？

单个的boolean长度为4个byte，数组类型boolean为8个字节。

##  Spring
##  MySQL
##  MyBatis
##  Tomcat
##  Netty
##  微服务
##  分布式
##  Redis
##  Dubbo
##  Kafka
##  RocketMQ
##  RabbitMQ
##  配置中心
##  ElasticSearch
##  Zookeeper
##  高性能
##  高可用
##  高并发
##  本地缓存
##  分库分表
##  定时任务
##  文件处理
##  DDD
##  Maven&Git
##  IDEA
##  日志
##  设计模式
##  单元测试
##  云计算
##  计算机网络
##  网络安全
##  操作系统
##  数据结构
##  容器
##  架构设计
##  编程题
##  智商题
##  非技术问题
##  其他
##  其他专属内容
##  面经实战