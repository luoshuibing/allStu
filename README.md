# 不屈日记

---
## 短信登录-redis项目

---
### session共享问题：
    多台tomcat并不共享session存储空间，当请求切换到不同tomcat服务时，导致数据丢失问题。
    替代方案：
> * 数据共享
> * 内存存储
> * key.value结构