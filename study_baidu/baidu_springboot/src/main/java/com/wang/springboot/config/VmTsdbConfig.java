package com.wang.springboot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: wanglu51
 * @Date: 2022/11/28 21:02
 */
@Data
@Component
public class VmTsdbConfig {

    @Value("#{${project.tsdb.vm.client.headers:{:}}}")
    private Map<String, String> headers;


    @Value("${project.tsdb.vm.client.url22:}")
    private String url22;

}
