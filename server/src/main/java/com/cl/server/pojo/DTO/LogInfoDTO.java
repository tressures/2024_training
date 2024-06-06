package com.cl.server.pojo.DTO;

import lombok.Data;

import java.util.List;
@Data
public class LogInfoDTO {

    private String hostname;

    private String file;

    private List<String> logs;
}
