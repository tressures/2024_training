package com.cl.server.pojo.VO;

import lombok.Data;

import java.util.List;
@Data
public class LogInfoVO {

    private String hostname;

    private String file;

    private List<String> logs;
}
