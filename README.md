# 不屈日记

---

# springcloud组件

1) 服务注册 eurake和nacos
2) 远程调用 feign
3) 负载均衡 ribbon
4) 熔断降级 hystrx
5) 服务监控 skywalking、
6) 网管 zuul和gateway
# 微服务概念

单体架构特点

简单方便，高度耦合，扩展性差，适合小型项目

分布式架构特点

松耦合，扩展性好，但架构复杂，难度大，适合大型互联网项目

微服务

* 优点：拆分粒度更小、服务更独立、耦合度更低
* 缺点：架构非常复杂、运维、监控、部署难度提高

![img.png](img.png)

微服务组件

![img_1.png](img_1.png)



# eureka注册中心

消费者获取提供者注册信息
* 服务提供者启动时将eureka注册自己
* eureka保存信息
* 消费者根据服务名向eureka拉取提供者信息

服务消费者利用负载均衡算法
* 服务提供者每隔30秒向eurekaServer发送心跳信息，报告健康状态
* eureka会更新服务列表信息，心跳不正常会剔除
* 消费者拉取最新的信息

# ribbon负载均衡原理

![img_2.png](img_2.png)

![img_3.png](img_3.png)

Ribbon负载均衡规则：
* 规则接口是IRule
* 默认实现是ZoneAvoidanceRule，根据zone选择服务列表，然后轮询

负载均衡自定义：
* 代码方式：配置灵活
* 配置方式，无需重新打包发布，但是无法做到全局配置

饥饿加载
* 开启饥饿加载
* 指定饥饿加载的微服务名称

# nacos注册中心

Nacos启动命令   start.cmd -m standalone

NacolsRule负载均衡策略
* 优先选择同集群服务实例
* 本地集群找不到提供者，才会其他集群寻找，并且会报警
* 确定了可用实例列表后，再采用随机负载均衡挑选实例

实例的权重控制
* Nacos控制台可以设置实例的权重值，0~1之间
* 同集群内的多个实例，权重越高被访问的评率越高
* 权重设置为0则完全不会被访问

Nacos环境隔离
* namespace用来做环境隔离
* 每个namespace都有唯一ID
* 不同namespace下的服务不可见

![img_4.png](img_4.png)



Eureka和nacos共同点
* 都支持服务注册和服务发现
* 都支持服务提供者心跳方式健康检测

Nacos和Eureka的区别
* Nacos支持服务端主动检测提供者状态，临时实例采用心跳模式，非临时实例采用主动检测模式
* 临时实例心跳不正常会剔除，非临时实例则不会剔除
* Nacos支持服务列表变更的消息推送模式，服务列表更新及时
* Nacos集群默认采用AP方式，当集群中存在非临时实例时，采用CP模式；Eureka采用AP方式

Nacos配置管理步骤
* 在nacos中添加配置文件
* 在位服务中引入nacos的config依赖
* 在微服务中添加bootstrap.yml，配置nacos地址

Nacos配置自动刷新两种方式
* @RefreshScope
* @ConfigurationProperties

Nacos多环境配置共享问题
* [服务名]-[spring.profile.action].yaml，环境配置
* [服务名].yaml，默认配置，多环境共享
* [服务名]-[环境].yaml>[服务名].yaml>本地配置

集群搭建：
* 搭建MySQL集群并初始化数据库表
* 下载解压nacos
* 修改集群配置、数据库配置
* 分别启动多个nacos节点
* nginx反向代理

Fegin使用步骤
* 引入依赖
* 添加@EnableFeginClients注解
* 编写FeignClient接口
* 使用FeginClient中定义的方法替代RestTemplate

自定义Feign的配置

![img_5.png](img_5.png)

Feign的日志配置：
* 方式一配置文件，feign.client.config.xxx.loggerLevel
  * 如果xxx是default则代表全局
  * 如果xxx是服务名称，代表某个服务
* 方式二java代码配置Logger.Level这个Bean
  * 如果在@EnableFeignClients注解声明则代表全局
  * 如果在@FeignClient注解中声明则代表某服务

Feign性能优化
* 日志级别尽量用basic
* 使用HttpClient或者OkHttp代替URLConnection
  * 引入feign-httpclient依赖
  * 配置文件开启httpClient功能

Feign最佳实践
* controller和feignClient继承同一接口
* 将FeignClient、pojo、feign的默认配置都定义到一个项目中，供消费者使用
















































































































