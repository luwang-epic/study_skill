package com.wang.rpc.protobuf;

import com.google.protobuf.ProtocolStringList;
import com.wang.rpc.protobuf.news.NewsProto;
import com.wang.rpc.protobuf.news.NewsServiceGrpc;
import com.wang.rpc.protobuf.sms.SmsProto;
import com.wang.rpc.protobuf.sms.SmsServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Date;

/*
protobuf (protocol buffer) 是谷歌内部的混合语言数据标准。
通过将结构化的数据进行序列化，用于通讯协议、数据存储等领域和语言无关、平台无关、可扩展的序列化结构数据格式。
    官网：https://developers.google.cn/protocol-buffers/

protobuf的优点：
    1. 多语言，多平台。自定义源文件，存储类，有代码生成机制，可以直接生成相关的语言代码
    2. 性能好/效率高：时间开销：XML 格式化（序列化）的开销还好；但是XML解析（反序列化）的开销就不敢恭维了
        但是protobuf在这个方面就进行了优化。可以使序列化和反序列化的时间开销都减短。
        空间开销：也减少了很多
    3. 扩展性好，兼容性好。支持向后兼容和向前兼容
        当客户端和服务器同时使用一个协议时，客户端在协议中增加一个字节，并不会影响客户端的使用。
protobuf缺点
    1、二进制格式导致可读性差
        为了提高性能，protobuf采用了二进制格式进行编码。这直接导致了可读性差，影响开发测试时候的效率。
        当然，在一般情况下，protobuf非常可靠，并不会出现太大的问题。
    2、缺乏自描述
        一般来说，XML 是自描述的，而 protobuf 格式则不是。它是一段二进制格式的协议内容，
        并且不配合写好的结构体是看不出来什么作用的。
    3、通用性差
        protobuf虽然支持了大量语言的序列化和反序列化，但仍然并不是一个跨平台和语言的传输标准。
        在多平台消息传递中，对其他项目的兼容性并不是很好，需要做相应的适配改造工作。
        相比json和XML，通用性还是没那么好。


protocol工具流如下：
    client -> protobuf idl -> serialize -> compiler -> skeleton -> client protocol stack -> 网络传输
    -> server protocol stack -> skeleton -> protobuf idl -> compiler -> deserialize ->  server

protobuf主要有4中通信模式：
    1. 一元RPC通信模式，即：客户端一次请求，一个响应
    2. 服务端流式RPC通信模式，即：客户端一次请求，服务端多个响应
    3. 客户端流式RPC通信模式，即：客户端多次请求，服务端一个响应
    4. 双向流式RPC通信模式：即：客户端多次请求，服务端多次响应


也可以和spring boot集合，可以更方便的开发基于protobuf的RPC服务
        一般使用grpc-server-spring-boot-starter和grpc-client-spring-boot-starter这个第三方提供的整合包
        如：net.devh:grpc-server-spring-boot-starter:2.13.0.RELEASE

 */
/**
 * protobuf服务端
 */
public class ProtobufServerSample {

    public static void main(String[] args) {
        try {
            // 创建server对象，监听9999端口，并加入服务实现类
            Server server = ServerBuilder.forPort(9999)
                    .addService(new NewsServiceImpl())
                    .addService(new SmsServiceImpl())
                    .build();
            // 启动服务
            server.start();
            System.out.println("grpc服务端启动成功，端口号为：9999");
            server.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新闻服务类，实现具体的接口，需要继承生成的类
     */
    private static class NewsServiceImpl extends NewsServiceGrpc.NewsServiceImplBase {
        /**
         * 第一个参数是请求，第二个参数是返回值，rpc中没有将返回值放到方法的返回中，而是放在了参数中
         * @param request 参数
         * @param responseObserver 返回对象
         */
        // 一元RPC通信模式
        @Override
        public void list(NewsProto.NewsRequest request, StreamObserver<NewsProto.NewsResponse> responseObserver) {
            System.out.println("invoke NewsServiceImpl list with params: " + request);
            // 获取传入的参数中的日期date对象
            String date = request.getDate();
            NewsProto.NewsResponse newsResponse = null;
            try {
                NewsProto.NewsResponse.Builder builder = NewsProto.NewsResponse.newBuilder();
                for (int i = 1; i <= 10; i++) {
                    // mock对象，mock一条新闻
                    NewsProto.News news = NewsProto.News.newBuilder()
                            .setId(i)
                            .setContent(date + "当日新闻日期内容" + i)
                            .setTitle("new title " + i)
                            .setCreateTime(new Date().getTime())
                            .build();
                    builder.addNews(news);
                }
                newsResponse = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 将对象放入到返回值中，只有一个response表示一个请求一个返回，
                // 有多个response表示一个请求，多个返回，此时需要调用多次onNext方法
                responseObserver.onNext(newsResponse);
            }

            // 表示服务端处理完成
            responseObserver.onCompleted();
        }
    }

    private static class SmsServiceImpl extends SmsServiceGrpc.SmsServiceImplBase {
        // 服务端流式RPC通信模式
        @Override
        public void send(SmsProto.SmsRequest request, StreamObserver<SmsProto.SmsResponse> responseObserver) {
            System.out.println("invoke SmsServiceImpl send with params: " + request);

            ProtocolStringList phoneNumberList = request.getPhoneNumberList();
            for (String phoneNumber : phoneNumberList) {
                SmsProto.SmsResponse response = SmsProto.SmsResponse.newBuilder()
                        .setResult(request.getContent() + " to " + phoneNumber).build();
                // 一个请求多个响应，向每个手机号码返回一个response
                responseObserver.onNext(response);

                // 演示方便，增加1s睡眠
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 需要放到循环外面，表示服务端完成处理了
            responseObserver.onCompleted();
        }

        // 客户端流式RPC通信模式
        @Override
        public StreamObserver<SmsProto.PhoneNumberRequest> createPhone(StreamObserver<SmsProto.PhoneNumberResponse> responseObserver) {
            return new StreamObserver<SmsProto.PhoneNumberRequest>() {
                int count = 0;
                // 接受客户端发来的新的电话请求，触发onNext
                @Override
                public void onNext(SmsProto.PhoneNumberRequest phoneNumberRequest) {
                    System.out.println(phoneNumberRequest.getPhoneNumber() + " 手机号已经登记");
                    count++;
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                // 客户端传输完毕时，服务端向客户端响应
                @Override
                public void onCompleted() {
                    responseObserver.onNext(SmsProto.PhoneNumberResponse.newBuilder().setResult("本次一共导入" + count + "个员工号码....").build());
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
