package com.wang.springboot.service;

import com.wang.springboot.config.InfoFlowConfig;
import com.wang.springboot.pojo.AlertManagerBody;
import com.wang.springboot.pojo.InfoFlowMessageRequest;
import com.wang.springboot.pojo.InfoFlowMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * 如流相关
 *
 * @Author: wanglu51
 * @Date: 2023/5/19 15:25
 */
@Service
public class InfoFlowService {
    private static final String TEXT_MESSAGE_TYPE = "TEXT";

    private InfoFlowConfig infoFlowConfig;

    private RestTemplate restTemplate;

    @Autowired
    public InfoFlowService(InfoFlowConfig infoFlowConfig) {
        this.infoFlowConfig = infoFlowConfig;
        this.restTemplate = new RestTemplate();
    }


    /**
     * 发送消息到如流群
     * @param body
     */
    public void sendAlertManagerToInfoFlowGroup(AlertManagerBody body) {
        InfoFlowMessageRequest infoFlowMessageRequest = buildDefaultInfoFlowMessageRequest(body);
        InfoFlowMessageResponse response = restTemplate.postForObject(
                infoFlowConfig.getUrl(), infoFlowMessageRequest, InfoFlowMessageResponse.class);
        if (response.isSuccess()) {
            return;
        }
        throw new RuntimeException(response.getErrMsg());
    }


    /**
     * 构建默认的如流发送对象
     * @return InfoFlowMessageRequest
     */
    InfoFlowMessageRequest buildDefaultInfoFlowMessageRequest(AlertManagerBody alertBody) {
        InfoFlowMessageRequest.MessageHeader header =
                new InfoFlowMessageRequest.MessageHeader(infoFlowConfig.getGroupIds());
        String content = new StringBuilder()
                .append("报警名称: ").append(alertBody.getGroupLabels().get("alertgroup")).append("\n")
                .append("报警内容: ").append(alertBody.getCommonAnnotations()).append("\n")
                .append("报警标签: ").append(alertBody.getCommonLabels()).append("\n")
                .append("规则名称: ").append(alertBody.getGroupLabels().get("alertname")).append("\n")
                .append("规则地址: ").append(alertBody.getExternalURL()).append("\n")
                .toString();
        InfoFlowMessageRequest.MessageBody body = new InfoFlowMessageRequest.MessageBody(TEXT_MESSAGE_TYPE, content);
        InfoFlowMessageRequest.Message request =
                new InfoFlowMessageRequest.Message(header, Collections.singletonList(body));
        return new InfoFlowMessageRequest(request);
    }

}
