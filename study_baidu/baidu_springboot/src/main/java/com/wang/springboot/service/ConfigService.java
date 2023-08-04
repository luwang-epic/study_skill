package com.wang.springboot.service;

import com.wang.springboot.config.VmTsdbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * config service
 *
 * @Author: wanglu51
 * @Date: 2022/11/30 20:18
 */
@Service
public class ConfigService {

    private VmTsdbConfig vmTsdbConfig;

    @Autowired(required = false)
    public ConfigService(VmTsdbConfig vmTsdbConfig) {
        this.vmTsdbConfig = vmTsdbConfig;
    }

}
