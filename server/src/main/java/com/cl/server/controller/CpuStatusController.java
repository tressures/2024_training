package com.cl.server.controller;

import com.alibaba.fastjson.JSON;
import com.cl.server.convert.StatusConverter;
import com.cl.server.entity.CpuStatus;
import com.cl.server.pojo.DTO.StatusDTO;
import com.cl.server.pojo.DTO.StatusQueryDTO;
import com.cl.server.pojo.VO.StatusResp;
import com.cl.server.entity.Result;
import com.cl.server.exception.BaseException;
import com.cl.server.service.CpuStatusService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * (CpuStatus)表控制层
 *
 * @author tressures
 * @date  2024-05-26 17:05:56
 */
@Slf4j
@RestController
@RequestMapping("/api/metric")
public class CpuStatusController {

    @Resource
    private CpuStatusService cpuStatusService;

    @PostMapping("/upload")
    public Result uploadMetric(@RequestBody List<StatusDTO> statusDTOS){
        try {
            Preconditions.checkNotNull(statusDTOS,"获取指标为空");
        }catch (Exception e){
            throw new BaseException(e.getMessage());
        }
        log.info("CpuStatusController.upload.cpuStatusList:{}", JSON.toJSONString(statusDTOS));
        List<CpuStatus> cpuStatusList = new ArrayList<>();
        for(StatusDTO statusDTO : statusDTOS){
            CpuStatus cpuStatus = new CpuStatus();
            cpuStatus.setMetric(statusDTO.getMetric());
            cpuStatus.setEndpoint(statusDTO.getEndpoint());
            cpuStatus.setTimestamp(statusDTO.getTimestamp());
            cpuStatus.setStep(statusDTO.getStep());
            cpuStatus.setValue(statusDTO.getValue());
            cpuStatusList.add(cpuStatus);
        }
        cpuStatusService.uploadMetrics(cpuStatusList);
        return Result.ok();
    }

    @GetMapping("/query")
    public Result<List<StatusResp>> queryMetrics(@RequestBody StatusQueryDTO statusQueryDTO){
        try{
            Preconditions.checkNotNull(statusQueryDTO.getEndPoint(),"机器名称不能为空");
            Preconditions.checkNotNull(statusQueryDTO.getStart_ts(),"起始时间不能为空");
            Preconditions.checkNotNull(statusQueryDTO.getEnd_ts(),"结束时间不能为空");
        }catch (Exception e){
            throw new BaseException("参数为空");
        }
        log.info("CpuStatusController.query.statusQueryDTO:{}", JSON.toJSONString(statusQueryDTO));
        List<StatusResp> list = cpuStatusService.queryMetrics(statusQueryDTO);
        return Result.ok(list);
    }
}

