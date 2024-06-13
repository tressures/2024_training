package com.cl.server.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 状态上报DTO
 *
 * @author tressures
 * @date:  2024/5/27
 */
@Data
@AllArgsConstructor
public class StatusDTO {
    
    private String metric;
    
    private String endpoint;
    
    private Long timestamp;
    
    private Long step;
    
    private Double value;
}

