package com.cl.server.service;

import com.cl.server.pojo.DTO.LogInfoDTO;
import com.cl.server.pojo.DTO.LogQueryDTO;
import com.cl.server.pojo.VO.LogInfoVO;
import java.util.List;

public interface LogStorageService {

    void uploadLogs(List<LogInfoDTO> logInfoDTOS);

    LogInfoVO queryLogs(LogQueryDTO logQueryDTO);
}
