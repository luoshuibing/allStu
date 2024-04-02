# 不屈日记

# redis原理

# 数据结构

## 动态字符串SDS

Redis是C语言实现的，其中SDS是一个结构体

```
struct __attribute__ ((__packed__)) sdshdr8 {
  uint8_t len; /* buf已保存的字符串字节数，不包含结束标示*/
  uint8_t alloc; /* buf申请的总的字节数，不包含结束标示*/
  unsigned char flags; /* 不同SDS的头类型，用来控制SDS的头大小
  char buf[];
};

flags对应的值
#define SDS_TYPE_5 0
#define SDS_TYPE_8 1
#define SDS_TYPE_16 2
#define SDS_TYPE_32 3
#define SDS_TYPE_64 4
```

![img.png](img.png)

SDS也叫动态字符串，动态扩容能力。

* 如果新字符串小于1M，则新空间为扩展后字符串长度的两倍+1。
* 如果新字符串大于1M，则新空间为扩展后字符串长度+1M+1.称为内存预分配。

优点：

* 获取字符串长度的时间复杂度
* 支持动态扩容
* 减少内存分配次数
* 二进制安全

## IntSet

IntSet是redis中set集合的一种实现方式，基于整数数组来实现，并且具备长度可变、有序等特征。
结构为：

```
typedef struct intset {
  uint32_t encoding; /* 编码方式，支持存放16位、32位、64位整数*/
  uint32_t length; /* 元素个数 */
  int8_t contents[]; /* 整数数组，保存集合数据*/
} intset;

/* Note that these encodings are ordered, so:
 * INTSET_ENC_INT16 < INTSET_ENC_INT32 < INTSET_ENC_INT64. 
 * 存储整数大小不同
 */
#define INTSET_ENC_INT16 (sizeof(int16_t)) /* 2字节整数，范围类似java的short*/
#define INTSET_ENC_INT32 (sizeof(int32_t)) /* 4字节整数，范围类似java的int */
#define INTSET_ENC_INT64 (sizeof(int64_t)) /* 8字节整数，范围类似java的long */


```

为了方便查找，redis会将intset中所有的整数按照升序一次保存在contents数组中

Intset可以看作是特殊的整数数组，具备一下特点：

1) redis会确保Intset中的元素唯一、有序
2) 具备类型升级机制，可以节省内存空间
3) 底层采用二分查找方式来查询

## Dict

```
typedef struct dictht {
    // entry数组
    // 数组中保存的是指向entry的指针
  dictEntry **table; 
    // 哈希表大小
  unsigned long size;   
    // 哈希表大小的掩码，总等于size - 1
    unsigned long sizemask;   
    // entry个数
    unsigned long used; 
} dictht;


typedef struct dictEntry {
  void *key; // 键
  union {
    void *val;
    uint64_t u64;
    int64_t s64;
    double d;
  } v; // 值
    // 下一个Entry的指针
  struct dictEntry *next; 
} dictEntry;


typedef struct dictht {
    // entry数组
    // 数组中保存的是指向entry的指针
  dictEntry **table; 
    // 哈希表大小
  unsigned long size;   
    // 哈希表大小的掩码，总等于size - 1
    unsigned long sizemask;   
    // entry个数
    unsigned long used; 
} dictht;


typedef struct dictEntry {
  void *key; // 键
  union {
    void *val;
    uint64_t u64;
    int64_t s64;
    double d;
  } v; // 值
    // 下一个Entry的指针
  struct dictEntry *next; 
} dictEntry;

```
Dict的结构：

* 类似java的HashTable,底层是数组加链表来解决哈希冲突
* Dict包含两个哈希表，ht[0]常用，ht[1]用来rehash

Dict的伸缩：
* 当LoadFactor大于5或者LoadFactor大于1并且没有子进程任务时，Dict扩容
* 当LoadFactor小于0.1时，Dict收缩
* 扩容大小为第一个大于等于used+1的2ⁿ
* 收缩大小为第一个大于等于used的2ⁿ
* Dict采用渐进式rehash，每次访问Dict时执行一次rehash
* rehash时ht[0]只减不增，新增操作只在ht[1]执行，其他操作在两个哈希表

## ZipList

ZipList特性：
* 压缩列表可以看做一种连续内存空间的双向链表
* 列表的节点之间不是通过指针连接，而是记录上一节点和本节点长度来寻址，内存占用较低
* 如果列表数据过多，导致链表过长，可能影响查询功能
* 增删较大数据时有可能发生连续更新问题


## QuickList
特点：

* 一个节点为ZipList的双端链表
* 节点采用ZipList,解决了创痛链表的内存占用问题
* 控制了ZipList大小，解决连续内存空间申请效率问题
* 中间节点可以压缩，进一步节省了内存



## SkipList

* 跳跃表是一个双向链表，每个节点都包含score和ele值
* 节点按照score值排序，score值一样则按照ele字典排序
* 每个节点都可以包含多层指针，层数是1到32之间的随机数
* 不同层指针到下一个节点的跨度不同，层级越高，跨度越大
* 增删改查效率与红黑树基本一致


## RedisObject
String是Redis中最常见的数据存储类型

其基本编码方式是RAW，基于简单动态字符串实现，存储上限为512mb。
如果存储的SDS长度小于44字节，则会采用EMBSTR编码，此时object head与SDS是一段连续空间。申请内存是只需要调用一次内存分配函数，效率更高。
如果存储的字符串是整数值，并且大小在LONG_MAX范围内，则采用INT编码：直接将数据保存在redisObject的ptr指针位置，不再需要SDS






## 五种数据结构

# 网络模型

# 通信协议

# 内存策略