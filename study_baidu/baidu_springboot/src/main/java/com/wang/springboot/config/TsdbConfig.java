package com.wang.springboot.config;

import lombok.Data;

/**
 * tsdb config
 *
 * @Author: wanglu51
 * @Date: 2022/11/29 14:19
 */
@Data
public class TsdbConfig {
    private int maxBatchSize;
    private int maxBatchCount;
    private int lingerMillSeconds;
    private int sendThreadCount;
}
