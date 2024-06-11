package com.cl.server.pojo.DTO;

import lombok.Data;



@Data
public class StatusDTO {
    
    private String metric;
    
    private String endpoint;
    
    private Long timestamp;
    
    private Long step;
    
    private Double value;

}

