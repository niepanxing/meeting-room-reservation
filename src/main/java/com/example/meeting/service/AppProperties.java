package com.example.meeting.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置（读取 application.yml 中 app.* 配置）
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 预约数据存放目录 */
    private String dataDir = "data";
    /** 会议室开放时间 */
    private String openTime = "09:00";
    /** 会议室关闭时间 */
    private String closeTime = "21:00";

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }
}