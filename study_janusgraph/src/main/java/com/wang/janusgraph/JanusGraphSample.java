package com.wang.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;

/*
JanusGraph的点表关系的类结构：(参考：https://zhuanlan.zhihu.com/p/80866491)
    在JanusGraph里面，数据元素不是点(Vertex)就是关系(Relation)。而关系又包括边（Edge）和属性（VertexProperty）。
    为什么属性也算一种关系？因为可以这么认为，属性是将一个点连接到了一个值（Property Value）上面去，
    所以属性也算作关系的一种。那为什么是叫做 VertexProperty 而且没有 EdgeProperty 呢？
    因为在JanusGraph里面，边实际上也被看成是一种特殊的点(EdgeLabelVertex)

    EdgeLabel: 即边的标签。用户可以通过标签给边分类。EdgeLabel除了最基本的给边”贴标签“功能外，还有如下特点：
        方向性(Directionality):
            JanusGraph里的边，要么是有向(directed)的，要么是单向的(uni-directed)。
            这里面可能会有点让人困惑。单向边不也是有向边吗？这两者怎么会是对立的呢？这是因为，
            JanusGraph采用的是”按边切割（Edge-Cut）“的实现方式。 简单来说就是，普通的边会在起始点和终止点各存一次。
            那么”有向(directed)边“则是这种”普通的边”。对于”有向边“，JanusGraph会在这条边的两端顶点都保存这条边的信息，
            因此两端顶点都会知道这条边的存在，从任意一个端点都能遍历到这条边。但是，对于”单向边“，
            JanusGraph只会在”起点(out-going vertex)“这一端保存这条边的信息，这样只有一个顶点知道这条边的存在，
            如果要遍历到这条边也只能从”起点“出发。在JanusGraph里，边通常都是"有向边"，也就是边都会存两份。
            用户也能声明”单向边“以降低存储空间的使用。
        多重性(Multiplicity): 简单来说就是用于限制点和点之间，同一个标签的边可以有多少条。

    VertexLabel: 点的标签。与边的标签类似，点的标签也会定义点的一些特性，
        比如点是否是partition，partition特性可以用来实现多租户

    PropertyKey: 在JanusGraph里面，点的属性(VertexProperty)的实现不是简单的Key-Value的形式。
    JanusGraph把属性的Key抽象成了PropertyKey，然后把属性的数据类型（Data Type）、Cardinality等特性定义在了PropertyKey里。
    所以，JanusGraph的StandardVertexProperty的实现是类似如下的（简化）代码：
        public class StandardVertexProperty {
            protected final PropertyKey key;    // Property的Key
            private final Object value;         // Property的Value
        }

    Schema元素也被看成是一个点（Vertex）。边的标签（EdgeLabel）的底层实现是一个点（EdgeLabelVertex），
    点的标签（VertexLabel）的底层实现也是一个点（VertexLabelVertex），
    属性的Key（PropertyKey）的底层实现也是一个点（PropertyKeyVertex）。
    甚至连后续会讲到的索引（Index）的底层实现也是一个点

JanusGraph的Hbase存储结构：(参考：https://blog.51cto.com/u_15162069/2913240）
    在JanusGraph里面，图是通过邻接表的格式存储的。点的邻接表会保存点的所有临接边（以及属性）。
    这样存储的好处是能够比较高效的遍历图，缺点则是图的边被保存了两次（在边的起始点以及终点都各保存了一次）。

    JanusGraph在将点存储到HBASE时，会使用"e" （全称是"edgestore"）作为ColumnFamily，RowKey是点的ID
    对于PropertyKeyVertex， VertexLabelVertex和Vertex都是一样的存储格式，
    但是Vertex会存储该顶点所有的边信息到列族中，因此边会多存储一份

    在JanusGraph中，以节点为中心，按切边的方式存储数据的。比如在Hbase中节点的ID作为HBase的Rowkey，
    节点上的每一个属性和每一条边，作为该Rowkey行的一个个独立的Cell。即每一个属性、每一条边，
    都是一个个独立的KCV结构(Key-Column-Value)，类似如下格式：
        hbase一行： vertex id -> property1, property2, edge1, edge2
    存储整体分为三部分：vertex id、property、edge：
        vertex id： 对应节点的唯一id，如果底层存储使用的是Hbase则代表着当前行的Rowkey，唯一代表某一个节点
        property： 代表节点的属性
        edge： 代表节点的对应的边
    排序方式分为三种：sorted by id、sorted by type、sorted by sort key：
        sorted by id： 依据vertex id在存储后端进行顺序存储
        sorted by type：此处的个人理解为针对于property 和 edge的类型进行排序，保证同种类型的属性或者边连续存储在一块便于遍历查找；
        sorted by sort key： sort key是边组成以的一部分，主要作用是，在同种类型的edge下，
            针对于sort key进行排序存储，提升针对于指定sort key的检索速度；下面edge结构部分有详细介绍
    property和edge都是作为cell存储在底层存储中；其中cell又分为column和value两部分：
        Edge的Column组成部分: label id + direction + sort key + adjacent vertex id + edge id
            label id：边类型代表的id，在创建图schema的时候janusgraph自动生成的label id，不同于边生成的唯一全局id
            direction：图的方向，out：0、in：1
            sort key：可以指定边的属性为sort key，可多个；在同种类型的edge下，针对于sort key进行排序存储，
                提升针对于指定sort key的检索速度；该key中使用的关系类型必须是属性非唯一键或非唯一单向边标签；
                存储的为配置属性的value值，可多个（只存property value是因为，已经在schema的配置中保存有当前Sort key对应的属性key了，所以没有必要再存一份）
            adjacent vertex id：target节点的节点id，其实存储的是目标节点id和源节点id的差值，这也可以减少存储空间的使用
            edge id：边的全局唯一id
        Edge的value组成部分：signature key + other properties
            signature key：边的签名key，该key中使用的关系类型必须是属性非唯一键或非唯一单向边标签；
                存储压缩后的配置属性的value值，可多个（只存property value是因为，已经在schema的配置中保存有当前signature key对应的属性key了，所以没有必要再存一份）
                主要作用提升edge的属性的检索速度，将常用检索的属性设置为signature key，提升查找速度
            other properties：边的其他属性
                注意！ 不包含配置的sort key和signature key属性值，因为他们已经在对应的位置存储过了，不需要多次存储！
                此处的属性，要插入属性key label id和属性value来标识是什么属性，属性值是什么；
                此处的property的序列化结构不同于下述所说的vertex节点的property结构，
                edge中other properties这部分存储的属性只包含：proeprty key label id + property value；不包含property全局唯一id！
        property的Column组成部分：key id，属性label对应的id，有创建schema时JanusGraph创建；
            不同于属性的唯一id（JanusGraph中key是通过PropertyKey对象来表示的，有自己的唯一id，而property的key value整体也有id）
        property的Value组成部分：property id + value
            property id：属性的唯一id，唯一代表某一个属性
            value：属性值，

    Composite Index-vertex index结构
        row key由index label id 和 properties value两大部分组成:
            index label id：标识当前索引类型
            properties value：索引中包含属性的所有属性值，可多个；
                存在压缩存储，如果超过16000个字节，则使用GZIP对property value进行压缩存储
        唯一索引Column组成部分：字节0
        唯一索引的Value组成部分：针对于rowkey + column查询到的value是vertex id，然后通过vertex id查询对应的节点
        非唯一索引Column组成部分：字节0 + vertex id，非唯一属于会有多个vertex id，这样会有多个Column
            通过vertex id可以分辨多个相同索引值对应的不同节点
        非唯一索引的Value组成部分：vertex id

    Composite Index-edge index结构
        row key和顶点的组合索引类似，由index label id 和 properties value两大部分组成
        唯一索引Column组成部分：字节0
        非唯一索引Column组成部分：字节0 + edge id
        唯一索引和非唯一索引的Value组成部分都一样：edge id +  out vertex id + type id + in vertex id
            edge id：边id
            out vertex id：边对应的出边id
            type id：edge 的label type id
            in vertex id：边对应的入边id

    Mixed Index结构
        不存在存储后端，如Hbase中，存于第三方搜索存储中，如：es


JanusGraph索引：（参考：https://liyangyang.blog.csdn.net/article/details/98513704）
    1. graph index --> composite index && mixed index
        composite index: 索引列全使用并且等值匹配、不需要后端索引存储、支持唯一性、排序在内存中成本高
            Graph Index是整个图上的全局索引结构，用户可以通过属性高效查询Vertex或Edge
            composite非常高效和快速，但只能应用对某特定的，预定义的属性key组合进行相等查询
            只支持精确匹配，不支持范围查询
            Composite Index也可以作为图的属性唯一约束使用，如果composite graph index被设置为unique()，
            则只能存在最多一个对应的属性组合。对于设置为最终一致性的后端存储，index的一致性必须被设置为允许锁定
        mixed index：索引列任何字段都可以触发索引、范围查询、全文检索、地理检索等、
            需要后端索引存储支持、不支持唯一性、排序有索引效率高无索引也在内存中排
            Mixed Index支持通过其中的 任意key的组合 查询Vertex或者Edge。Mix Index使用上更加灵活，
            而且支持范围查询等（不仅包含相等）；从另外一方面说，Mixed index效率要比Composite Index低。
            与Composite key不同，Mixed Index需要配置索引后端，例如：elasticsearch等
            不像composite index，mixed index不支持唯一性。

        注意：如果没有建索引，会进行全表扫面，此时性能非常低，可以通过配置force-index参数禁止全表扫描。

    2. vertex-centric index
        JanusGraph默认为每个属性添加该索引，组合索引满足最左匹配原则可使用，便于查询节点的边（节点存在很多边的情况下）

    如果索引是在统一事务中创建的，则在该事务中马上可以使用。
    如果该属性Key已经被使用，需要执行reindex procedure来保证索引中包含了所有数据，
    直到该过程执行完毕，否则不能使用。

使用Order时需要注意：
    composite graph index原生不支持对返回结果排序，数据会被先加载到内存中再进行排序，
        对于大数据集合来讲成本非常高
    mixed graph index本身支持排序返回，但排序中要使用的property key需要提前被加到mix index中去，
        如果要排序的property key不是index的一部分，将会导致整个数据集合加载到内存。


JanusGraph锁机制（参考：https://liyangyang.blog.csdn.net/article/details/108000791）
    在JanusGraph中主要有三种一致性修饰词(Consistency Modifier)来表示3种不同的一致性行为，
    来控制图库使用过程中的并发问题的控制程度。源码中ConsistencyModifier枚举类主要作用是
    用于控制JanusGraph在最终一致或其他非事务性后端系统上的一致性行为，其作用分别为：
        DEFAULT：默认的一致性行为，不使用分布式锁进行控制，对配置的存储后端使用由封闭事务保证的默认一致性模型，
            一致性行为主要取决于存储后端的配置以及封闭事务的（可选）配置；无需显示配置即可使用
        LOCK：在存储后端支持锁的前提下，显示的获取分布式锁以保证一致性！确切的一致性保证取决于所配置的锁实现；
            需management.setConsistency(element, ConsistencyModifier.LOCK);语句进行配置
        FORK：只适用于multi-edges和list-properties两种情况下使用；使JanusGraph修改数据时，
            采用先删除后添加新的边/属性的方式，而不是覆盖现有的边/属性，从而避免潜在的并发写入冲突；
            需management.setConsistency(element, ConsistencyModifier.FORK);进行配置
            注意edge fork仅适用于MULTI edge。具有多重性约束的边缘标签不能使用此策略，
            因为非MULTI的边缘标签定义中内置了一个唯一性约束，该约束需要显式锁定或使用基础存储后端的冲突解决机制

    我们都知道在janusgraph的底层存储中，vertexId作为Rowkey，属性和边存储在cell中，由column+value组成
    当我们修改节点的属性和边+边的属性时，很明显只要锁住对应的Rowkey + Column即可；
    在Janusgraph中，这个锁的标识的基础部分就是LockID：LockID = RowKey + Column

    JanusGraph的锁机制主要是通过本地锁+分布式锁来实现分布式系统下的数据一致性；
        本地锁是在任何情况下都需要获取的一个锁，只有获取成功后，才会进行下述分布式锁的获取
        本地锁是基于图实例维度存在的；主要作用是保证当前图实例下的操作中无冲突
        引入本地锁机制，主要目的是在图实例维度来做一层锁判断，减少分布式锁的并发冲突，减少分布式锁带来的性能消耗


JanusGraph图分区（参考：https://www.jianshu.com/p/6a80a081f46e）
    随机分区策略（默认策略）
        随机安排顶点到所有机器上。缺点：查询效率慢，因为存在大量的跨实例的通信。

    精确分区
        具有强关联性和经常访问的子图存储在相同的机器上，这样可以减少跨机器的通信成本。
        Hbase支持图自定义分区，Cassandra需要配置ByteOrderedPartitioner来支持图分区
        有edge cut和vertex cut两方面可以单独控制：

        Edge Cut(默认)
            一条边的两个顶点分别在不同的机器上，那么这条边叫做cut edge。
            包含对这条边的遍历的图查询会慢因为需要在两台设备间通信。
            对于频繁遍历的边，应该减少cut edge的存在，从而减少跨设备间的通信，提高查询效率。
            即把进行遍历的相邻顶点放在相同的分区，减少通信消耗。
            JanusGraph通过配置好的 placement strategy来控制vertex-to-partition的分配。
            一个分区就是一个有序的id区间，顶点被分配到一个分区就会为该顶点分配一个id，也就是顶点的id决定了该顶点属于哪一个分区。
            给一个顶点分配id：JanusGraph就会从顶点所属分区的id范围中选一个id值分配给该顶点。(先定分区，在分配id)
        Vertex Cut
            顶点切割，即把一个顶点进行切割，把一个顶点的邻接表分成多个子邻接表存储在图中各个分区上。
            一个拥有大量边的顶点，在加载或者访问时会造成热点问题。
            Vertex Cut通过分散压力到集群中所有实例从而缓解单顶点产生的负载。
            JanusGraph通过label来切割顶点，通过定义vertex label成partition，
            那么具有该label的所有顶点将被分配在集群中所有机器上。

    图数据小使用Random分区策略，图大时（超过10亿条边）就要使用自定义分区策略


JanusGraph的批量导入（参考：https://blog.51cto.com/u_15100534/2618646）
    JanusGraph中有许多配置选项和工具可以将大量的图数据更有效地导入。这种导入称为批量加载，
    与默认的事务性加载相反，默认的事务性加载单个事务只会添加少量数据。

    配置项：storage.batch-loading，启用该配置项，相当于打开了JanusGraph的批量导入开关；

    启用批处理加载会在许多地方禁用JanusGraph内部一致性检查，重要的是会禁用lock锁定来保证分布式一致性；
    JanusGraph假定要加载到JanusGraph中的数据与图形一致，因此出于性能考虑禁用了自己的一致性检查。
    换句话说，我们要在导入数据之前，保证要导入的数据和图中已有的数据不会产生冲突也就是保持一致性！
    特别是，启用批处理加载时，并发类型创建会导致严重的数据完整性问题。
    因此，我们强烈建议通过schema.default = none在图形配置中进行设置来禁用自动类型创建。

    我们在导入图数据时会产生大量的临时数据，这里需要我们调整一个合适的堆空间；推荐至少为8G
    如果在使用CMS发现GC过于频繁的话，我们可以考虑将垃圾收集器设置为：G1这个收集器适用于大堆空间的垃圾收集，有效的减少垃圾收集消耗的时间；


JanusGraph的批处理：
    JanusGraph从存储后端，如hbase，获取数据有两种方式：
        1. 当有一个需要从存储后端查询时，直接执行该查询然后返回结果
            该查询可以更快的获取第一条数据，而且占用更少的内存，默认的选项
        2. 维持一个存储后端查询列表，当列表到达一定大小时，执行一批次的查询，从而获取所有的结果
    配置选项query.batch，默认为false，不开启

    不使用批处理：
        在图的遍历上面，采用的深度优先的方式，
        对于有很多边的顶点，查询速度将会比较慢
        一般查询少量的顶点，应用不需要全部的结果，但是想要第一个结果尽可能快的返回，可以使用这种场景

    使用批处理：
        在图的遍历上面，采用的是广度优先的方式，
        会消耗很多的内存，如果查询比较大，会给存储后端带来一定的压力，可能会返回一些不需要的数据到内存
        在每步查询时都可能访问多个点，存储后端和图实例之间的网络带宽比较大，可以使用这种场景
        开启方式：query.batch = true
        可以通过query.limit-batch-size=true/false来选择是有限制的批查询还是无限制的批查询


JanusGraph的分布式id策略：
    当前常用的分布式id的生成策略主要分为以下四种：
        UUID
        数据库+号段模式（优化：数据库+号段+双buffer）
        基于Redis实现
        雪花算法（SnowFlake）
    还有一些其他的比如：基于数据库自增id、数据库多主模式等，这些在小并发的情况下可以使用，大并发的情况下就不太ok了
    有一些生成分布式id的开源组件，包括滴滴基于数据库+号段实现的TinyID 、百度基于SnowFlake的Uidgenerator、美团支持号段和SnowFlake的Leaf等

    在JanusGraph中，分布式id的生成采用的是数据库+号段+双buffer优化的模式； 下面我们来具体分析一下：
    分布式id生成使用的数据库就是JanusGraph当前使用的第三方存储后端，比如：Habse
    在Hbase中有column family 列族的概念； JanusGraph在初始化Hbase表时默认创建了9大列族，用于存储不同的数据
    其中有一个列族janusgraph_ids简写为i这个列族，主要存储的就是JanusGraph分布式id生成所需要的元数据
    主要分为4部分：0、count、partition、ID padding（每种顶点类型是固定值）；
    上述部分的partition + count来保证分布式节点的唯一性；
        partition id：分区id值，JanusGraph默认分了32个逻辑分区；节点分到哪个分区采用的是随机分配;
        count：每个partition都有对应的一个count范围：0-2的55次幂；JanusGraph每次拉取一部分的范围作为节点的count取值；
            JanusGraph保证了针对相同的partition，不会重复获取同一个count值！


JanusGraph如果启动过程中出现图实例已经存在的异常，可以通过如下参数解决：
 graph.graph.replace-instance-if-exists="true"


 */
public class JanusGraphSample {

    public static void main( String[] args ) {
        JanusGraph graph = JanusGraphFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "D:\\java\\local\\janusgraph-0.5.2\\file_database\\program")
                .open();

        // add a vertex by graph transaction
        JanusGraphTransaction tx = graph.newTransaction();
        tx.addVertex("user").property("name", "Jesson");
        tx.commit();

        GraphTraversalSource g = graph.traversal();
        // add a vertex by traversal
        g.addV("user").property("name", "Dennis").next();
        g.tx().commit();


        // query by traversal
        System.out.println("Vertex count = " + g.V().count().next());

        g.tx().close();
        graph.close();
    }

}
