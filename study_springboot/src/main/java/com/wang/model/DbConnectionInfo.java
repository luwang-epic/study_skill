package com.wang.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
@Data
public class DbConnectionInfo {
    private String username;
    private String password;
    private Integer timeout;
}
