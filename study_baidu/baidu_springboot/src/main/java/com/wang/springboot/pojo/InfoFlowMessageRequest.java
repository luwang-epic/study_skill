package com.wang.springboot.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 如流发送消息请求对象
 *
 * @Author: wanglu51
 * @Date: 2023/5/19 15:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoFlowMessageRequest {
    private Message message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private MessageHeader header;
        @JsonProperty("body")
        private List<MessageBody> msgs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageHeader {
        @JsonProperty("toid")
        private List<Long> toIds;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageBody {
        private String type;
        private String content;
    }
}
