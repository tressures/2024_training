package com.cl.server.entity;

import lombok.Data;

import java.util.List;

@Data
public class StatusResp {

    private String metric;

    private List<Values> values;

}
