package com.cl.server.config;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@NacosConfigurationProperties(dataId = "cfg.json", autoRefreshed = true)
public class LogStorageConfig {

    private static String log_storage;

    public String getLogStorage(){
        return this.log_storage;
    }

}
