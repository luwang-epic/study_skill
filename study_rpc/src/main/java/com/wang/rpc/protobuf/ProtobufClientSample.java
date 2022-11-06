package com.wang.rpc.protobuf;

import com.wang.rpc.protobuf.news.NewsProto;
import com.wang.rpc.protobuf.news.NewsServiceGrpc;
import com.wang.rpc.protobuf.sms.SmsProto;
import com.wang.rpc.protobuf.sms.SmsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * protobuf客户端
 */
public class ProtobufClientSample {

    public static void main(String[] args) throws Exception {
        // 创建通信管道
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                .usePlaintext()
                // 无需加密或认证
                .build();

        // 获取服务类对象，采用阻塞的同步IO通信
        NewsServiceGrpc.NewsServiceBlockingStub newsServiceBlockingStub = NewsServiceGrpc.newBlockingStub(channel);
        // 构建请求参数
        NewsProto.NewsRequest.Builder builder = NewsProto.NewsRequest.newBuilder();
        builder.setDate("20221106");

        // 调用新闻服务
        NewsProto.NewsResponse newsResponse = newsServiceBlockingStub.list(builder.build());
        System.out.println("invoke list result: " + newsResponse.getNewsList());
        // 对中文需要使用utf8解码
        System.out.println(new String(newsResponse.getNewsList().get(0).getContent().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        // 调用消息服务
        SmsServiceGrpc.SmsServiceBlockingStub smsServiceBlockingStub = SmsServiceGrpc.newBlockingStub(channel);
        SmsProto.SmsRequest smsRequest = SmsProto.SmsRequest.newBuilder().setContent("study protobuf")
                .addPhoneNumber("11111")
                .addPhoneNumber("22222")
                .addPhoneNumber("33333")
                .build();
        // 发送消息
        Iterator<SmsProto.SmsResponse> iterator = smsServiceBlockingStub.send(smsRequest);
        while (iterator.hasNext()) {
            SmsProto.SmsResponse next = iterator.next();
            System.out.println("invoke send result: " + next.getResult());
        }

        // 客户端流式通信，需要使用异步通信客端户
        SmsServiceGrpc.SmsServiceStub smsServiceStub = SmsServiceGrpc.newStub(channel);

        // 监听服务端返回的响应
        StreamObserver<SmsProto.PhoneNumberResponse> responseStreamObserver = new StreamObserver<SmsProto.PhoneNumberResponse>() {
            // 接受服务端处理完毕的响应消息
            @Override
            public void onNext(SmsProto.PhoneNumberResponse phoneNumberResponse) {
                System.out.println("invoke createPhone result: " + phoneNumberResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("服务端处理完毕....");
            }
        };

        // 实例化StreamObserver，发起请求
        StreamObserver<SmsProto.PhoneNumberRequest> requestStreamObserver = smsServiceStub.createPhone(responseStreamObserver);
        // 批量向服务端发起请求
        for (int i = 1; i < 4; i++) {
            // 创建请求对象
            SmsProto.PhoneNumberRequest request = SmsProto.PhoneNumberRequest.newBuilder().setPhoneNumber("10000" + i).build();
            // 发送请求，服务端会接受请求
            requestStreamObserver.onNext(request);

            // 睡眠1s，方便观察
            Thread.sleep(1000);
        }
        // 通知服务端，所有请求都发完了
        requestStreamObserver.onCompleted();
        // 确保消息收到后才关闭通道
        Thread.sleep(1000);

        // 双向流式RPC通信模式，就是将服务端流式和客户端流式集合起来就可以了

        // 关闭通道
        channel.shutdown();
    }
}
