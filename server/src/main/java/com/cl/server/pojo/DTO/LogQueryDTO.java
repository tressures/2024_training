package com.cl.server.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * 日志查询DTO
 *
 * @author tressures
 * @date:  2024/5/27
 */
@Data
@AllArgsConstructor
public class LogQueryDTO {

    private String hostname;

    private String file;
}
