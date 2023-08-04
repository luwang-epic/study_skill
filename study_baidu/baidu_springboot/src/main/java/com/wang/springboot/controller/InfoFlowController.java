package com.wang.springboot.controller;

import com.wang.springboot.pojo.AlertManagerBody;
import com.wang.springboot.service.InfoFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @Author: wanglu51
 * @Date: 2023/5/22 17:08
 */
@RestController
@RequestMapping("/csm/api/v1/infoFlow")
public class InfoFlowController {

    @Autowired
    private InfoFlowService infoFlowService;

    /**
     * 发送alter manager告警消息到如流群
     */
    @PostMapping("/alertManager")
    public void sendGrafanaAlertToInfoFlowGroup(@RequestBody AlertManagerBody body) {
        infoFlowService.sendAlertManagerToInfoFlowGroup(body);
    }

}
