package com.wang.bcm.sdk;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bcm.BcmClient;
import com.baidubce.services.bcm.BcmClientConfiguration;
import com.baidubce.services.bcm.model.Dimension;
import com.baidubce.services.bcm.model.ListMetricDataRequest;
import com.baidubce.services.bcm.model.ListMetricDataResponse;
import com.baidubce.services.bcm.model.MetricDataRequest;
import com.baidubce.services.bcm.model.MetricDataResponse;
import com.baidubce.services.bcm.model.PushCustomMetricDataRequest;
import com.baidubce.services.bcm.model.PushMetricDataResponse;
import com.baidubce.services.bcm.model.StatisticValue;
import com.baidubce.services.bcm.model.Statistics;
import com.baidubce.util.DateUtils;
import com.baidubce.util.JsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BcmSdkSample {


    public static void main(String[] args) {

        getMetricData();
        batchGetMetricData();
        pushCustomMonitorMetricData();

    }


    public static void getMetricData() {

        // params definition
        String endpoint = "gzbh-sandbox24-6271.gzbh.baidu.com:8869";
        String userId = "453bf9588c9e488f9ba2c984129090dc";
        String ak = "7807d1ccd46042378c9f6dfb5684496d";
        String sk = "b51dc6642339468289f946f0a6303115";
        String scope = "BCE_BCC";
        String metricName = "vCPUUsagePercent";
        String dimensions = "InstanceId:fakeid-2222-8888-1111-13a8469b1fb2";
        Statistics[] statistics = new Statistics[]{Statistics.average,Statistics.maximum};
        long now = System.currentTimeMillis();
        String startTime = DateUtils.formatAlternateIso8601Date(new Date(now - 60 * 60 * 1000));
        String endTime = DateUtils.formatAlternateIso8601Date(new Date(now));
        int periodInSecond = 60;

        // create a bcm client
        BcmClientConfiguration config = new BcmClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(ak, sk));
        config.setEndpoint(endpoint);
        BcmClient bcmClient = new BcmClient(config);

        // query metric data from bcm interface
        MetricDataRequest request = new MetricDataRequest();
        request.withUserId(userId)
                .withScope(scope)
                .withDimensions(dimensions)
                .withMetricName(metricName)
                .withStatistics(statistics)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withPeriodInSecond(periodInSecond);
        MetricDataResponse response = bcmClient.getMetricData(request);
        System.out.println(JsonUtils.toJsonString(response));
    }

    public static void batchGetMetricData() {
        // params definition
        String endpoint = "gzbh-sandbox24-6271.gzbh.baidu.com:8869";
        String userId = "453bf9588c9e488f9ba2c984129090dc";
        String ak = "7807d1ccd46042378c9f6dfb5684496d";
        String sk = "b51dc6642339468289f946f0a6303115";
        String scope ="BCE_BCC";
        String dimensions = "InstanceId:fake-1-test,InstanceId:fake-2-test";
        String[] metrics = {"vCPUUsagePercent", "WebOutBytes"};
        Statistics[] statistics = {Statistics.average, Statistics.maximum};
        long now = System.currentTimeMillis();
        String startTime = DateUtils.formatAlternateIso8601Date(new Date(now - 60 * 60 * 1000));
        String endTime = DateUtils.formatAlternateIso8601Date(new Date(now));
        int periodInSecond = 60;

        // create a bcm client
        BcmClientConfiguration config = new BcmClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(ak, sk));
        config.setEndpoint(endpoint);
        BcmClient client = new BcmClient(config);

        // query metric data from bcm interface
        ListMetricDataRequest request = new ListMetricDataRequest();
        request.withUserId(userId)
                .withScope(scope)
                .withDimensions(dimensions)
                .withMetricNames(metrics)
                .withPeriodInSecond(periodInSecond)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withStatistics(statistics);
        ListMetricDataResponse response = client.getMetricData(request);
        System.out.println(JsonUtils.toJsonString(response));
    }

    public static void pushCustomMonitorMetricData() {
        // create a bcm client
        String endpoint = "gzbh-sandbox24-6271.gzbh.baidu.com:8869";
        String userId = "453bf9588c9e488f9ba2c984129090dc";
        String ak = "7807d1ccd46042378c9f6dfb5684496d";
        String sk = "b51dc6642339468289f946f0a6303115";
        BcmClientConfiguration config = new BcmClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(ak, sk));
        config.setEndpoint(endpoint);
        BcmClient client = new BcmClient(config);

        // push custom metric data with value and no dimension
        PushCustomMetricDataRequest request = PushCustomMetricDataRequest.builder()
                .userId(userId)
                .namespace("test_wang")
                .metricName("test_api_no_dimension")
                .value(10.0)
                .timestamp(DateUtils.formatAlternateIso8601Date(new Date(System.currentTimeMillis() - 10 * 60 * 1000)))
                .build();
        PushMetricDataResponse response = client.pushCustomMonitorMetricData(request);
        System.out.println(JsonUtils.toJsonString(response));

        // push custom metric data with statistic value and dimension
        List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension().withName("dimension1").withValue("d1"));
        dimensions.add(new Dimension().withName("dimension2").withValue("d2"));
        StatisticValue value = StatisticValue.builder()
                .maximum(1.0).minimum(2.0).sum(3.0).sampleCount(1).average(4.0).build();
        request = PushCustomMetricDataRequest.builder()
                .userId(userId)
                .namespace("test_wang")
                .metricName("test_api_time")
                .dimensions(dimensions)
                .statisticValues(value)
                .timestamp(DateUtils.formatAlternateIso8601Date(new Date(System.currentTimeMillis() - 10 * 60 * 1000)))
                .build();
        response = client.pushCustomMonitorMetricData(request);
        System.out.println(JsonUtils.toJsonString(response));
    }
}