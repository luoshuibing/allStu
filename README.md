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

```



