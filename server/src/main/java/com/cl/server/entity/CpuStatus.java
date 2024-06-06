package com.cl.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class CpuStatus implements Serializable {
    private static final long serialVersionUID = 271764723178638140L;
    
    private Integer id;
    /**
     * 指标名称
     */
    private String metric;
    /**
     * 主机名称
     */
    private String endpoint;
    /**
     * 采集时间
     */
    private Long timestamp;
    /**
     * 采集周期
     */
    private Long step;
    /**
     * 利用率
     */
    private Double value;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 是否删除
     */
    private Integer isDelete;

}

