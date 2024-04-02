# 不屈日记

## 缓存同步策略

缓存数据同步的常见方式有三种：
* 设置有效期：给缓存设置有效期
  * 简单、方便
  * 时效性差
  * 更新频率较低，时效性要求低的业务
* 同步双写：在修改数据库的同时，直接修改缓存
  * 时效性强，缓存与数据库强一致
  * 有代码侵入，耦合度高
  * 对一致性、时效性要求较高的缓存数据
* 异步通知：修改数据库时发送事件通知，相关服务监听到通知后修改缓存数据
  * 低耦合，可以同时通知多个缓存服务
  * 时效性一般，可能存在中间不一致状态
  * 时效性要求一般，有多个服务需要同步