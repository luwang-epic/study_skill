package com.wang.springboot.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Prometheus报警请求体
 *
 * @Author: wanglu51
 * @Date: 2023/5/19 16:23
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertManagerBody {
    private String version;
    private String groupKey;
    private String receiver;
    private String status;
    private String externalURL;
    private Integer truncatedAlerts;
    private Map<String, String> groupLabels;
    private Map<String, String> commonLabels;
    private Map<String, String> commonAnnotations;
    private List<Alert> alerts;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alert {
        private String status;
        private String generatorURL;
        private String fingerprint;
        // 时间格式<rfc3339>
        private String startsAt;
        // 时间格式<rfc3339>
        private String endsAt;
        private Map<String, String> labels;
        private Map<String, String> annotations;
    }
}
