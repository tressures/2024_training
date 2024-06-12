package com.cl.server.pojo.DTO;

import lombok.Data;
/**
 * 状态查询DTO
 *
 * @author tressures
 * @date:  2024/5/27
 */
@Data
public class StatusQueryDTO {

    private String endpoint;

    private String metric;

    private Long start_ts;

    private Long end_ts;
}
