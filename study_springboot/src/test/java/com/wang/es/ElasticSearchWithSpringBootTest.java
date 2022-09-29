package com.wang.es;

import com.wang.model.UserEsDocument;
import com.wang.service.UserElasticsearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Optional;

/*
es的聚合怎么做的 （参考：https://www.cnblogs.com/huangying2124/p/12717369.html）
    ES的聚合一共有4种类型，Bucket 、Metric、Pipeline是经常使用的3中，还有一种是Matrix聚合

    倒排索引，类似JAVA中Map的k-v结构，k是分词后的关键词，v是doc文档编号，检索关键字特别容易，
        但要找到aggs的value值，必须全部搜索v才能得到，性能比较低。
    正排索引，也类似JAVA中Map的k-v结构，k是doc文档编号，v是doc文档内容，
        只要有doc编号作参数，提取相应的v即可，搜索范围小得多，性能比较高。
        正排索引本质上是一个序列化的链表，里面的数据类型都是一致的（各个文档的同一列），
        压缩时可以大大减少磁盘空间、提高访问速度（cache内存需要大些，cache够大才能提升正排索引的缓存和查询效率）

    聚合查询肯定不能用倒排索引了，那就用正排索引，建立的数据结构将变成这样：
    如果聚合查询里有带过滤条件或检索条件，先由倒排索引完成搜索，确定文档范围，
        再由正排索引提取field，最后做聚合计算。

    Elasticsearch的聚合查询时，如果数据量较多且涉及多个条件聚合，会产生大量的bucket，
    并且需要从这些bucket中挑出符合条件的，那该怎么对这些bucket进行挑选是一个值得考虑的问题，
    挑选方式好，事半功倍，效率非常高，挑选方式不好，可能OOM，有深度优先和广度优两中方式
    使用深度优先还是广度优先，要考虑实际的情况，广度优先仅适用于每个组的聚合数量远远小于当前总组数的情况

    如果你对去重结果的精准度没有特殊要求，使用cardinality聚合函数
        AggregationBuilders.cardinality("deviceCount").field("deviceID").precisionThreshold(自定义一个精度范围100-40000)
            说明：cardinality 度量是一个近似算法。 它是基于 HyperLogLog++ （HLL）算法的。
                HLL 会先对我们的输入作哈希运算，然后根据哈希运算的结果中的 bits 做概率估算从而得到基数。
                （HLL算法参考：https://www.cnblogs.com/linguanh/p/10460421.html）
            优点：性能快，亿级别的记录在1秒内完成
            缺点：存在只能保证最大40000条记录内的精确，超过的存在5%的误差，不适合需要精确去重场景
    如果你对去重结果要求精确，使用termsagg聚合（类似group by）
            AggregationBuilders.terms("deviceCount").field("deviceID").size(Integer.MAX_VALUE);
            说明：默认只聚合10个桶，size(Integer.MAX_VALUE)可以指定桶个数
            优点：结果精确
            缺点：只适合聚合少量桶场景（100以内），否则性能极差（十万级的桶需要分钟级完成）
    针对海量数据去重（多桶）场景，可能需要考虑用第三方分析工具，将把es作为存储


    1. Bucket就是桶的意思，即按照一定的规则将文档分配到不同的桶中，达到分类分析的目的，类似于sql的group。
        包括：Terms Aggregation  根据字段值项分组聚合
            {
                "aggs":{
                    "age_terms":{
                        "terms":{
                            "field":"age"
                        }
                    }
                }
            }
         Range Aggregation 范围分组聚合
            {
                "aggs":{
                    "age_range":{
                        "range":{
                            "field":"age",
                            "keyed":true,
                            "ranges":[
                                {
                                    "to":20,
                                    "key":"TW"
                                },
                                {
                                    "from":25,
                                    "to":40,
                                    "key":"TH"
                                },
                                {
                                    "from":60,
                                    "key":"SIX"
                                }
                            ]
                        }
                    }
                }
            }
         还有直方图聚合，嵌套聚合等

    2. 指标聚合，使用聚合函数 max min sum avg 等进行聚合，类似于sql的count
        {
          "size": 0,
          "aggs": {
            "maxage": {
              "max": {
                "field": "age"
              }
            }
          }
        }

    3. pipeline聚合：多个聚合，在之前的绝活基础上进行聚合

    参考：
        博客：https://my.oschina.net/bingzhong/blog/1917915

es的搜索推荐（suggest）怎么做的
    suggester基本运作原理是：将输入的文本分解为token，然后在索引的字典中查找相似的term并且返回。
    根据使用场景不同，elasticsearch中涉及了 4种类别的suggester。分别是：
    1. Term Suggester
        提供一种基于单个词项的拼写纠错方法。
        {
          "suggest": {
            "suggest_name": {
              "text": "test1 test2 ", // 会分为两个单词，每个单词给相关的推荐
              "term": {
                "field": "title"
              }
            }
          }
        }
    2. Phrase Suggester
        可以返回完整的短语建议而不是单个词项的建议。
        Phrase suggester在Term suggester的基础上，会考量多个term之间的关系，比如是否同时出现在索引的原文里，相邻程度，以及词频等
            {
              "suggest": {
                "text": "test1 test2", // 给一个整体的推荐结果
                "phrase_name": {
                  "phrase": {
                    "field": "title",
                    "analyzer": "whitespace",
                    "gram_size": 3,
                    "direct_generator": [
                      {
                        "field": "title",
                        "suggest_mode": "always"
                      }
                    ],
                    "highlight": {
                      "pre_tag": "<em>",
                      "post_tag": "</em>"
                    }
                  }
                }
              }
            }
    3. Completion Suggester
        它主要针对的应用场景就是"Auto Completion"。此场景下用户每输入一个字符的时候，
        就需要即时发送一次查询请求到后端查找匹配项，在用户输入速度较高的情况下对后端响应速度要求比较苛刻。
        因此实现上它和前面两个Suggester采用了不同的数据结构，索引并非通过倒排来完成，
        而是将analyze过的数据编码成FST和索引一起存放。对于一个open状态的索引，
        FST会被ES整个装载到内存里的，进行前缀查找速度极快。但是FST只能用于前缀查找，
        这也是Completion Suggester的局限所在。
        因为使用基于内存的FST(FST的空间会比trie少很多，具体网上搜索)，因此需要对suggest的字段单独设置为completion类型
            {
              "suggest": {
                "suggest_name": {
                  "prefix": "test",
                  "completion": {
                    "field": "title.keyword"
                  }
                }
              }
            }
    4. Context Suggester （参考博客：https://www.cnblogs.com/Neeo/articles/10695031.html）
        是基于Completion Suggester的，在此基础上进行一些筛选，例如定义如下的索引字段
            "field_name": {
                "type": "completion",
                "contexts": {
                  "name": "typeName",
                  "type": "category"  // context suggest目前只支持category（分类）和geo（地址位置）两种
                }
              }
        这个时候搜索可以在completion中加入typeName信息，在指定的context中搜索，如：
            {
              "suggest":{
                "suggest_name":{
                  "prefix":"test",
                  "completion":{
                    "field":"title.keyword",
                    "size": 10,
                    "contexts":{
                      "typeName":["HiveTable", "Topic"]
                    }
                  }
                }
              }
            }

    参考：
        博客：https://blog.csdn.net/allensandy/article/details/109239331

Es的内存管理（堆内和堆外）
    堆内内存主要包括：
        1. segments cache
            segments FST数据的缓存，为了加速查询，FST永驻堆内内存，
            无法被GC回收。
        2. filter cache （(5.x里叫做Request cache)）
            node级别的filter过滤器结果缓存，每个节点有一个，被所有 shard 共享，
            filter query 结果要么是 yes 要么是no，不涉及 scores 的计算。使用LRU淘汰策略
            内存无法被GC。
            集群中每个节点都要配置，indices.queries.cache.size: 10%，默认为 10%。
            index.queries.cache.enabled 用来控制具体索引是否启用缓存，默认是开启的。
            属于index级别配置，只用在索引创建时候或者关闭的索引上设置。
        3. field data cache
            OLAP场景，用于排序、聚合使用的字段的缓存，
            无法被GC回收
            对于Text类型的字段，如果要对其进行聚合和排序，则需要打开字段的Filed data属性。
            Field data是延迟加载。如果你从来没有聚合一个分析字符串，就不会加载field data到内存中。
            如果没有足够的内存保存field data时，es会不断地从磁盘加载数据到内存，并剔除掉旧的内存数据。
            剔除操作会造成严重的磁盘I/O，并且引发大量的GC，会严重影响es的性能。
        4. bulk queue
            一般来说，Bulk queue不会消耗很多的heap，但是见过一些用户为了提高bulk的速度，客户端设置了很大的并发量，
            并且将bulk Queue设置到不可思议的大，比如好几千。 Bulk Queue是做什么用的？
            当所有的bulk thread都在忙，无法响应新的bulk request的时候，将request在内存里排列起来，然后慢慢清掉。
        5. indexing buffer
            用于存储新写入的文档，当其被填满时，缓冲区中的文档被写入磁盘中的 segments 中。节点上所有 shard 共享。
            可以被GC的
        6. cluster state buffer
            这个cluster state包含诸如集群有多少个node，多少个index，每个index的mapping是什么？有多少shard，每个shard的分配情况等等
            在一个规模很大的集群，这个状态信息可能会非常大的，耗用的内存空间就不可忽视了
            无法被GC回收
        7. 超大搜索聚合结果集的fetch 和 对高cardinality字段做terms aggregation
            无论是搜索，还是聚合，如果返回结果的size设置过大，都会给heap造成很大的压力，特别是数据汇聚节点
            比如本来是想计算cardinality，却用了terms aggregation + size:0这样的方式; 对大结果集做深度分页；一次性拉取全量数据等
            所谓高cardinality，就是该字段的唯一值比较多。 比如client ip，可能存在上千万甚至上亿的不同值。
            对这种类型的字段做terms aggregation时，需要在内存里生成海量的分桶，内存需求会非常高。
            如果内部再嵌套有其他聚合，情况会更糟糕。
            可以被GC

    堆外内存主要包括：
        1. Segment Memory
            通过Segment Cache的FST关联出Segment数据，加载到Off Heap用于加速查询
            ES还有Off-heap内存，由Lucene管理，负责缓存倒排索引（Segment Memory）。
            Lucene 中的倒排索引 segments 存储在文件中，为提高访问速度，都会把它加载到内存中，从而提高 Lucene 性能。

   那么有哪些途径减少data node上的segment memory占用呢？ 总结起来有三种方法:
        1.  删除不用的索引
        2.  关闭索引 （文件仍然存在于磁盘，只是释放掉内存）。需要的时候可以重新打开。
        3.  定期对不再更新的索引做optimize (ES2.0以后更改为force merge api)。
            这optimize的实质是对segment file强制做合并，可以节省大量的segment memory。

   对我们的堆内存建立起来完整的监控，避免OOM问题：
        1. 倒排词典的索引需要常驻内存，无法GC，需要监控data node上segment memory增长趋势。
        2. 各类缓存，field cache, filter cache, indexing cache, bulk queue等等，要设置合理的大小，
            并且要应该根据最坏的情况来看heap是否够用，也就是各类缓存全部占满的时候，
            还有heap空间可以分配给其他任务吗？避免采用clear cache等“自欺欺人”的方式来释放内存。
        3. 避免返回大量结果集的搜索与聚合。确实需要大量拉取数据的场景，可以采用scan & scroll api来实现。
        4. cluster stats驻留内存并无法水平扩展，超大规模集群可以考虑分拆成多个集群通过tribe node连接。
        5. 想知道heap够不够，必须结合实际应用场景，并对集群的heap使用情况做持续的监控。
        6. 根据监控数据理解内存需求，合理配置各类circuit breaker（es官方为我们提供的内存保护机制-熔断器，参考：https://blog.csdn.net/star1210644725/article/details/123767568）
            将内存溢出风险降低到最低。


flush merge refresh区别
    在ES中，当写入一个新文档时，首先被写入到内存缓存中，
    默认每1秒将in-memory index buffer中的文档生成一个新的段并清空原有in-memory index buffer，
    新写入的段变为可读状态，但是还没有被完全提交。该新的段首先被写入文件系统缓存，
    保证段文件可以正常被正常打开和读取，后续再进行刷盘操作。由此可以看到，
    ES并不是写入文档后马上就可以搜索到，而是一个近实时的搜索（默认1s后）。
    因此，refresh后文档被写入一个新的段，处于searchable状态，但是仍是未提交状态
    虽然refresh是一个较轻量的操作，但也是有一定的资源消耗的，必要时刻可以手动执行refresh api保证文档可立即被读到。
    生产环境建议正确使用refresh api，接受ES本身1s后可读的近实时特性。
    在生产环境中，若我们需要创建一个大索引，可设置该参数为-1，数据导入后开始使用时再开启参数，减少创建索引时refresh的消耗

    即使通过每秒refresh实现了近实时搜索，但refresh无法保障数据安全，我们仍然需要经常进行完整提交来确保能从失败中恢复。
    flush就是一次完全提交的过程，一次完整的提交会将段刷到磁盘，并写入一个包含所有段列表的提交点。
    Elasticsearch 在启动或重新打开一个索引的过程中使用这个提交点来判断哪些段隶属于当前分片，保证数据的安全。
    将translog中所有的段进行全量提交并对translog进行截断的操作叫做flush，flush操作期间会做的事项主要有（默认5s）：
        1. 强制refresh，将内存缓冲区所有文档写入一个新的段，写入到文件系统缓存并将旧的内存缓冲区被清空（refresh）
        2. 将文件系统缓存中的段通过fsync进行刷盘
        3. 将最新的commit point写入磁盘
        4. 删除老的translog，启动新translog

    每次refresh操作都会生成一个新的segment，随着时间的增长segmengt会越来越多，
    这就出现一个比较严重的问题是每次search操作必须依次扫描所有的segment，导致查询效率变慢，
    为了避免该问题es会定期多这个segment进行合并操作。
    同时merge操作会将已经打.del标签的文档从文件系统进行物理删除。merge属于一个后台操作。
    在es中每个delete操作其实都是对将要删除的文档打一个.del的标签，
    同理update操作就是将原文档进行.del打标然后插入新文档，
    只有merge操作才会将这些已经打标的.del文件真正进行物理删除。
    merge操作不适用于频繁更新的动态索引，相反他更适合只有index的日志型索引，定期将历史索引segment进行合并，加快search效率

 */
@SpringBootTest
public class ElasticSearchWithSpringBootTest {

    @Resource
    private UserElasticsearchRepository userElasticsearchRepository;


    @Test
    public void addDocument() {
        UserEsDocument user = new UserEsDocument();
        user.setUsername("lisi");
        user.setAge(22);
        userElasticsearchRepository.save(user);
    }

    @Test
    public void getDocument() {
        Optional<UserEsDocument> user = userElasticsearchRepository.findById("lisi");
        System.out.println("document ---->" + user);
    }

}
