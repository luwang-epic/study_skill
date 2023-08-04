package com.wang.springboot.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 如流发送消息应答对象
 *
 * @Author: wanglu51
 * @Date: 2023/5/19 15:54
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoFlowMessageResponse {
    @JsonAlias("errcode")
    private Integer errCode;
    @JsonAlias("errmsg")
    private String errMsg;

    public boolean isSuccess() {
        return errCode == 0;
    }
}
