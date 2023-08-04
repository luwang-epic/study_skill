package com.wang.springboot.config;

import lombok.Data;

import java.util.Map;

/**
 * http metric client config
 *
 * @Author: wanglu51
 * @Date: 2022/11/28 20:03
 */
@Data
public class HttpClientConfig {
    private Map<String, String> headers;
    private String url;
    private boolean needCompress;
    private int connTimeoutMillis;
    private int socketTimeoutMillis;
    private int maxConnTotal;
    private int maxConnPerRoute;
}
