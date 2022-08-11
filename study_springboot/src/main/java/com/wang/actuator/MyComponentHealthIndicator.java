package com.wang.actuator;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 类名需要以HealthIndicator结尾， MyComponent为监控检查的名称
 * 可以通过actuator/health接口查看，会有一个myComponent名称的监控组件，结果为：{"status":"UP","components":{"diskSpace":{"status":"UP","details":{"total":148679684096,"free":103254843392,"threshold":10485760,"exists":true}},"myComponent":{"status":"UP","details":{"code":100,"ms":100,"count":1}},"ping":{"status":"UP"}}}
 * 参考官方的实现类：DiskSpaceHealthIndicator，可以继承AbstractHealthIndicator（推荐），也可以实现HealthIndicator接口
 * 其他的如info，metrics等都可以定制
 */
@Component
public class MyComponentHealthIndicator extends AbstractHealthIndicator {

    /**
     * 真实的检查方法
     * @param builder
     * @throws Exception
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        //mongodb。  获取连接进行测试
        Map<String,Object> map = new HashMap<>();
        // 检查完成
        if(1 == 1){
            builder.up(); //健康
            builder.status(Status.UP);
            map.put("count",1);
            map.put("ms",100);
        }else {
//            builder.down();
            builder.status(Status.OUT_OF_SERVICE);
            map.put("err","连接超时");
            map.put("ms",3000);
        }


        builder.withDetail("code",100)
                .withDetails(map);

    }
}
