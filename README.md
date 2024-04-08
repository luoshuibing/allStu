# 不屈日记

elasticsearch
* 一个开源的分布式搜索引擎，可以用来实现搜索、日志统计、分析、系统监控等功能

elastic stack
* elasticsearch为核心的技术栈，包括Logstash、kibana、elasticsearch

Lucene
* Apache的开源搜索引擎类库

倒排索引
* 文档：每条数据就是一个文档
* 词条：文档按照语义分成的词语

文档和词条
* 每一条数据就是一个文档
* 对文档中的内容分词，得到的词语就是词条

正向索引
* 基于文档id创建索引。查询词条时必须先找到文档，而后判断是否包含词条

倒排索引
* 对文档内容分词，对词条创建索引，并记录词条所在文档的信息。查询时现根据词条查询到文档id，而后获取到文档

索引
* 相当于文档的集合

映射
* 索引中文档的字段约束信息，类似表的结构约束

架构
* MySQL：擅长事务类型操作
* ElasticSearch:擅长海量数据的搜索、分析、计算

[IK分词器](https://github.com/infinilabs/analysis-ik)

分词器的作用
* 创建倒排索引时对文档分词
* 用户搜索时，对输入的内容分词

IK分词器模式
* ik_smart:粗粒度
* ik_max_word:细粒度

IK分词器如果扩展词条，停用词
* 利用config目录下的IKAnalyzer.cfg.xml文件添加拓展词典和停用词典
* 在词典中添加拓展词条和停用词条

mapping属性
> mapping是对索引库中文档的约束
* type:字段数据类型
  * 字符串：text(可分词的文本)、keyword(精确值)
  * 数值：long、integer、short、byte、double、float
  * 布尔：boolean
  * 日期：date
  * 对象：object
* index：是否创建索引，默认为true。可以参与搜索
* analyzer：使用哪种分词器
* properties:该字段的子字段


```

创建索引库
PUT /heima
{
  "mappings":{
    "properties": {
      "info":{
        "type":"text",
        "analyzer":"ik_smart"
      },
      "email":{
        "type":"keyword",
        "index":false
      },
      "name":{
        "type":"object",
        "properties":{
          "firstName":{
            "type":"keyword"
          },
          "lastName":{
            "type":"keyword"
          }
        }
      }
    }
  }
}

修改索引库
PUT /heima/_mapping
{
  "properties":{
    "age":{
      "type":"integer"
    }
  }
}

删除索引库
DELETE /heima

添加文档
POST /heima/_doc/1
{
  "info":"黑马程序员Java讲师",
  "email":"zy@itcast.cn",
  "name":{
    "firstName":"云",
    "lastName":"赵"
  }
}

删除文档
DELETE /heima/_doc/1

查询文档
GET /heima/_doc/1

全量修改和新增都可以
PUT /heima/_doc/1
{
  "info":"黑马程序员Java讲师1",
  "email":"zy1@itcast.cn",
  "name":{
    "firstName":"云1",
    "lastName":"赵1"
  }
}
局部修改文档
POST /heima/_update/1
{
  "doc":{
    "info":"黑马1程序员Java讲师1"
  }

}

复杂索引创建
PUT /hotel
{
  "mappings":{
    "properties": {
      "id":{
        "type": "keyword"
      },
      "name":{
        "type":"text",
        "analyzer": "ik_max_word",
        "copy_to": "all"
      },
      "address":{
        "type": "keyword",
        "index":false
      },
      "price":{
        "type": "integer"
      },
      "score":{
        "type":"integer"
      },
      "brand":{
        "type":"keyword",
        "copy_to": "all"
      },
      "city":{
        "type":"keyword"
      },
      "starName":{
        "type":"keyword"
      },
      "business":{
        "type":"keyword",
        "copy_to": "all"
      },
      "location":{
        "type":"geo_point"
      },
      "pic":{
        "type":"keyword",
        "index":false
      },
      "all":{
        "type":"text",
        "analyzer":"ik_max_word"
      }
    }
  }
}

查询所有
GET /hotel/_search
{
  "query": {
    "match_all": {
    }
  }
}

虚拟全部字段查询copy_to
GET /hotel/_search
{
  "query": {
    "match": {
      "all":"四川北路商业区"
    }
  }
}

复合查询
GET /hotel/_search
{
  "query": {
    "multi_match": {
      "query": "7天酒店", 
      "fields": ["brand","name"]
    }
  }
}

精准匹配
GET /hotel/_search
{
  "query": {
    "term": {
      "city": {"value":"上海"}
    }
  }
}

范围查询
GET /hotel/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 10,
        "lte": 2000
      }
    }
  }
}

地理位置查询
GET /hotel/_search
{
  "query": {
    "geo_distance":{
      "distance":"5km",
      "location":"31.21, 121.5"
    }
  }
}

带权重查询
GET /hotel/_search
{
  "query": {
    "function_score": {
      "query": {
        "match": {
          "all": "外滩"
        }
      },
      "functions": [
        {
          "filter": {
            "term":{
              "brand": "如家"
            }
          },
          "weight": 10
        }
      ],
      "boost_mode": "multiply"
    }
  }
}

boolean查询
GET /hotel/_search
{
  "query":{
    "bool":{
      "must": [
        {
          "match": {
            "name": "如家"
          }
        }
      ],
      "must_not": [
        {
          "range": {
            "price": {
              "gt": 400
            }
          }
        }
      ],
      "filter": [
        {
          "geo_distance": {
            "distance": "10km",
            "location": {
              "lat": 31.21,
              "lon": 121.5
            }
          }
        }
      ]
    }
  }
}

排序
GET /hotel/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "score":"desc",
      "price":"asc"
    }
  ]
}

排序
GET /hotel/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "_geo_distance": {
        "location": "31.21, 121.5",
        "order": "asc",
        "unit":"km"
      }
    }
  ]
}

排序
GET /hotel/_search
{
  "query": {
    "match_all": {}
  },
  "from": 100,
  "size": 10,
  "sort": [
    {
      "price": "asc"
    }
  ]
}
高亮
GET /hotel/_search
{
  "query": {
    "match": {
      "name": "传媒"
    }
  },
  "highlight": {
    "fields": {
      "name":{
        "pre_tags": "<em>",
        "post_tags":"</em>"
      }
    }
  }
}

高亮
GET /hotel/_search
{
  "query": {
    "match": {
      "all": "传媒"
    }
  },
  "highlight": {
    "fields": {
      "name":{
        "require_field_match": "false"
      }
    }
  }
}


















```

[DSL文档地址](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html)

相关性算法

![img.png](img.png)

FSQ

![img_1.png](img_1.png)

bool查询种类
* must：必须匹配
* should：选择性匹配
* must_not:必须不匹配
* filter：必须匹配

分页

from+size
* 优点：支持随机翻页
* 缺点：深度分页问题，默认查询上限是10000
* 场景：百度、京东、谷歌、淘宝这样的随机翻页搜索

after search：
* 优点：没有查询上限
* 缺点：只能向后逐页翻页
* 场景：没有随机翻页需求的搜索

scroll:
* 优点：没有查询上限
* 缺点：会有额外内存消耗，并且搜索结果是非实时的
* 场景：海量数据的获取或迁移

高亮：
> 在搜索结果中把搜索关键字突出显示

* 将搜索结果中的关键字用标签标记出来
* 在页面中给标签添加css样式

