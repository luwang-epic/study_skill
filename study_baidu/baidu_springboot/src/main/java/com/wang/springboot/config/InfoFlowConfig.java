package com.wang.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 如流配置信息
 *
 * @Author: wanglu51
 * @Date: 2023/5/19 19:30
 */
@Data
@Component
@ConfigurationProperties(prefix = "bcm.info-flow")
public class InfoFlowConfig {
    private String url;
    private List<Long> groupIds;
}
