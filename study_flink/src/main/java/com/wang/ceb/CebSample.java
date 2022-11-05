package com.wang.ceb;

import com.wang.pojo.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Map;


/*
不过在实际应用中，还有一类需求是要检测以特定顺序先后发生的一组事件，进行统计或做报警提示，这就比较麻烦了。
例如，网站做用户管理，可能需要检测“连续登录失败”事件的发生，这是个组合事件，其实就是“登录失败”和“登录失败”的组合；
电商网站可能需要检测用户“下单支付”行为，这也是组合事件，“下单”事件之后一段时间内又会有“支付”事件到来，
还包括了时间上的限制。类似的多个事件的组合，我们把它叫作“复杂事件”。
Flink为我们提供了专门用于处理复杂事件的库CEP,其实就是“复杂事件处理（Complex Event Processing）”的缩写
总结起来，复杂事件处理（CEP）的流程可以分成三个步骤：
    （1）定义一个匹配规则
    （2）将匹配规则应用到事件流上，检测满足规则的复杂事件
    （3）对检测到的复杂事件进行处理，得到结果进行输出

主要应用场景为：
    1. 风险控制
        设定一些行为模式，可以对用户的异常行为进行实时检测。当一个用户行为符合了异常行为模式，
        比如短时间内频繁登录并失败、大量下单却不支付（刷单），就可以向用户发送通知信息，
        或是进行报警提示、由人工进一步判定用户是否有违规操作的嫌疑。这样就可以有效地控制用户个人和平台的风险。
    2. 用户画像
        利用CEP可以用预先定义好的规则，对用户的行为轨迹进行实时跟踪，从而检测出具有特定行为习惯的一些用户，
        做出相应的用户画像。基于用户画像可以进行精准营销，即对行为匹配预定义规则的用户实时发送相应的营销推广；
        这与目前很多企业所做的精准推荐原理是一样的。
    3. 运维监控
        对于企业服务的运维管理，可以利用 CEP 灵活配置多指标、多依赖来实现更复杂的监控模式。

在 Flink CEP 中沿用了通过设置水位线（watermark）延迟来处理乱序数据的做法。当一个事件到来时，
并不会立即做检测匹配处理，而是先放入一个缓冲区（buffer）。缓冲区内的数据，会按照时间戳由小到大排序；
当一个水位线到来时，就会将缓冲区中所有时间戳小于水位线的事件依次取出，进行检测匹配。
这样就保证了匹配事件的顺序和事件时间的进展一致，处理的顺序就一定是正确的。这里水位线的延迟时间，也就是事件在缓冲区等待的最大时间。

水位线延迟时间不可能保证将所有乱序数据完美包括进来，总会有一些事件延迟比较大，以至于等它到来的时候水位线早已超过了它的时间戳。
这时之前的数据都已处理完毕，这样的“迟到数据”就只能被直接丢弃了——这与窗口对迟到数据的默认处理一致。
Flink CEP同样提供了将迟到事件输出到侧输出流的方式：我们可以基于PatternStream直接调用.sideOutputLateData()方法，
传入一个 OutputTag，将迟到数据放入侧输出流另行处理。

 */
/**
 * 复杂时间处理
 *  检测连续登录失败
 */
public class CebSample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 1. 获取登录事件流，并提取时间戳、生成水位线
        KeyedStream<LoginEvent, String> stream = env.fromElements(
                        new LoginEvent("user_1", "192.168.0.1", "fail", 2000L),
                        new LoginEvent("user_1", "192.168.0.2", "fail", 3000L),
                        new LoginEvent("user_2", "192.168.1.29", "fail", 4000L),
                        new LoginEvent("user_1", "171.56.23.10", "fail", 5000L),
                        new LoginEvent("user_2", "192.168.1.29", "fail", 7000L),
                        new LoginEvent("user_2", "192.168.1.29", "fail", 8000L),
                        new LoginEvent("user_2", "192.168.1.29", "success", 6000L)
                )
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner(
                                        new SerializableTimestampAssigner<LoginEvent>() {
                                            @Override
                                            public long extractTimestamp(LoginEvent loginEvent, long l) {
                                                return loginEvent.timestamp;
                                            }
                                        }
                                )
                )
                .keyBy(r -> r.userId);

        // 2. 定义Pattern，连续的三个登录失败事件
        Pattern<LoginEvent, LoginEvent> pattern =
                // 以第一个登录失败事件开始
                Pattern.<LoginEvent>begin("first")//.times(1)
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent) throws Exception {
                        // 返回true表示匹配成功了
                        return loginEvent.eventType.equals("fail");
                    }
                }) //.where()  // 组合条件
                // 接着是第二个登录失败事件
                .next("second")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent) throws Exception {
                        return loginEvent.eventType.equals("fail");
                    }
                })
                // 接着是第三个登录失败事件
                .next("third")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent) throws Exception {
                        return loginEvent.eventType.equals("fail");
                    }
                });

        // times(3).consecutive()等将于上面的3个连续事件
        //Pattern<LoginEvent, LoginEvent> pattern2 =
        //        Pattern.<LoginEvent>begin("first")
        //                .where(new SimpleCondition<LoginEvent>() {
        //                    @Override
        //                    public boolean filter(LoginEvent loginEvent) throws Exception {
        //                        return loginEvent.eventType.equals("fail");
        //                    }
        //                }).times(3).consecutive();

        /*
        我们就把每个简单事件的匹配规则，叫作“个体模式”（Individual Pattern）,例如上面的first，second，third就定义了三个个体模式
        每个个体模式都以一个“连接词”开始定义的，比如 begin、next 等等，这是 Pattern 对象的一个方法（begin 是 Pattern 类的静态方法），
        返回的还是一个 Pattern。这些“连接词”方法有一个String类型参数，这就是当前个体模式唯一的名字，比如这里的“first”、“second”。
        在之后检测到匹配事件时，就会以这个名字来指代匹配事件。

        个体模式需要一个“过滤条件”，用来指定具体的匹配规则。这个条件一般是通过调用.where()方法来实现的，
        具体的过滤逻辑则通过传入的 SimpleCondition 内的.filter()方法来定义。

        个体模式后面可以跟一个“量词”，用来指定循环的次数。从这个角度分类，个体模式可以包括“单例（singleton）模式”和“循环（looping）模式”。
        默认情况下，个体模式是单例模式，匹配接收一个事件；当定义了量词之后，就变成了循环模式，可以匹配接收多个事件。

        对于每个个体模式，匹配事件的核心在于定义匹配条件，也就是选取事件的规则。
        FlinkCEP会按照这个规则对流中的事件进行筛选，判断是否接受当前的事件。
        对于条件的定义，主要是通过调用 Pattern 对象的.where()方法来实现的，主要可以分为简单条件、迭代条件、复合条件、终止条件几种类型。
        此外，也可以调用Pattern 对象的.subtype()方法来限定匹配事件的子类型。


        有了定义好的个体模式，就可以尝试按一定的顺序把它们连接起来，定义一个完整的复杂事件匹配规则了。
        这种将多个个体模式组合起来的完整模式，就叫作“组合模式”（CombiningPattern），为了跟个体模式区分有时也叫作“模式序列”（Pattern Sequence）。
        组合模式确实就是一个“模式序列”，是用诸如 begin、next、followedBy 等表示先后顺序的“连接词”将个体模式串连起来得到的。

        之前在模式序列中，我们用 begin()、next()、followedBy()、followedByAny()这样的“连接词”来组合个体模式，
        这些方法的参数就是一个个体模式的名称；而现在它们可以直接以一个模式序列作为参数，就将模式序列又一次连接组合起来了。
        这样得到的就是一个“模式组”（Groups of Patterns）。

        在 Flink CEP 中，提供了模式的“匹配后跳过策略”（After Match Skip Strategy），专门用来精准控制循环模式的匹配结果。
        这个策略可以在 Pattern 的初始模式定义中，作为 begin()的第二个参数传入：
            Pattern.begin("start", AfterMatchSkipStrategy.noSkip())  // 默认策略，不跳过
        主要有：不跳过（NO_SKIP），跳至下一个（SKIP_TO_NEXT），跳过所有子匹配（SKIP_PAST_LAST_EVENT），
                跳至第一个（SKIP_TO_FIRST[a]），跳至最后一个（SKIP_TO_LAST[a]）
         */

        // 个体模式的次数

        // 匹配事件出现 4 次
        //pattern.times(4);
        // 匹配事件出现 4 次，或者不出现
        //pattern.times(4).optional();
        // 匹配事件出现 2, 3 或者 4 次
        //pattern.times(2, 4);
        // 匹配事件出现 2, 3 或者 4 次，并且尽可能多地匹配
        //pattern.times(2, 4).greedy();
        // 匹配事件出现 2, 3, 4 次，或者不出现
        //pattern.times(2, 4).optional();
        // 匹配事件出现 2, 3, 4 次，或者不出现；并且尽可能多地匹配
        //pattern.times(2, 4).optional().greedy();
        // 匹配事件出现 1 次或多次
        //pattern.oneOrMore();
        // 匹配事件出现 1 次或多次，并且尽可能多地匹配
        //pattern.oneOrMore().greedy();
        // 匹配事件出现 1 次或多次，或者不出现
        //pattern.oneOrMore().optional();
        // 匹配事件出现 1 次或多次，或者不出现；并且尽可能多地匹配
        //pattern.oneOrMore().optional().greedy();
        // 匹配事件出现 2 次或多次
        //pattern.timesOrMore(2);
        // 匹配事件出现 2 次或多次，并且尽可能多地匹配
        //pattern.timesOrMore(2).greedy();
        // 匹配事件出现 2 次或多次，或者不出现
        //pattern.timesOrMore(2).optional();
        // 匹配事件出现 2 次或多次，或者不出现；并且尽可能多地匹配
        //pattern.timesOrMore(2).optional().greedy();


        // 组合模式的方式

        // 严格近邻条件，即下一个事件必须是什么
        //Pattern<Event, ?> strict = start.next("middle").where(...);
        // 宽松近邻条件， 即后面有即可，不一定是下一个事件
        //Pattern<Event, ?> relaxed = start.followedBy("middle").where(...);
        // 非确定性宽松近邻条件
        //Pattern<Event, ?> nonDetermin = start.followedByAny("middle").where(...);
        // 不能严格近邻条件，下一个事件不能是什么事件
        //Pattern<Event, ?> strictNot = start.notNext("not").where(...);
        // 不能宽松近邻条件，后面不能有什么事件
        //Pattern<Event, ?> relaxedNot = start.notFollowedBy("not").where(...);
        // 时间限制条件，相当于限制在这个事件窗口内
        //middle.within(Time.seconds(10));


        // 模式组的例子

        // 以模式序列作为初始模式
        //Pattern<Event, ?> start = Pattern.begin(
        //        Pattern.<Event>begin("start_start").where(...)
        //        .followedBy("start_middle").where(...));
        // 在 start 后定义严格近邻的模式序列，并重复匹配两次
        //Pattern<Event, ?> strict = start.next(
        //        Pattern.<Event>begin("next_start").where(...)
        //        .followedBy("next_middle").where(...)).times(2);
        // 在 start 后定义宽松近邻的模式序列，并重复匹配一次或多次
        //Pattern<Event, ?> relaxed = start.followedBy(
        //        Pattern.<Event>begin("followedby_start").where(...)
        //        .followedBy("followedby_middle").where(...)).oneOrMore();
        //在 start 后定义非确定性宽松近邻的模式序列，可以匹配一次，也可以不匹配
        //Pattern<Event, ?> nonDeterminRelaxed = start.followedByAny(
        //        Pattern.<Event>begin("followedbyany_start").where(...)
        //        .followedBy("followedbyany_middle").where(...)).optional();


        // 3. 将Pattern应用到流上，检测匹配的复杂事件，得到一个PatternStream
        PatternStream<LoginEvent> patternStream = CEP.pattern(stream, pattern);

        // 4. 将匹配到的复杂事件选择出来，然后包装成字符串报警信息输出
        SingleOutputStreamOperator<String> selectStream = patternStream.select(new PatternSelectFunction<LoginEvent, String>() {
            @Override
            public String select(Map<String, List<LoginEvent>> map) throws Exception {
                LoginEvent first = map.get("first").get(0);
                LoginEvent second = map.get("second").get(0);
                LoginEvent third = map.get("third").get(0);
                return first.userId + " 连续三次登录失败！登录时间：" + first.timestamp + ", " + second.timestamp + ", " + third.timestamp;
            }
        });

        /*
        将模式应用到事件流上的代码非常简单，只要调用 CEP 类的静态方法.pattern()，
        将数据流（DataStream）和模式（Pattern）作为两个参数传入就可以了。最终得到的是一个 PatternStream
        这里的 DataStream，也可以通过 keyBy 进行按键分区得到 KeyedStream，
        接下来对复杂事件的检测就会针对不同的 key 单独进行了。

        模式中定义的复杂事件，发生是有先后顺序的，这里“先后”的判断标准取决于具体的时间语义。默认情况下采用事件时间语义，
        那么事件会以各自的时间戳进行排序；如果是处理时间语义，那么所谓先后就是数据到达的顺序。
        对于时间戳相同或是同时到达的事件，我们还可以在 CEP.pattern()中传入一个比较器作为第三个参数，用来进行更精确的排序：

         */

        // 5. 输出报警信息
        selectStream.print("warning");

        env.execute();
    }
}
