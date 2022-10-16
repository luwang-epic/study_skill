package com.wang.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户的访问数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {
    // 用户名
    private String user;
    // 访问地址
    private String url;
    // 访问时间
    private Long timestamp;
}
