package com.cl.server.handler.storage;

import com.cl.server.pojo.DTO.LogInfoDTO;
import com.cl.server.pojo.DTO.LogQueryDTO;
import com.cl.server.pojo.VO.LogInfoVO;
import com.cl.server.enums.StorageTypeEnum;

import java.util.List;


public interface StorageTypeHandler {

    /**
     * 存储方式的识别
     * @return
     */
    StorageTypeEnum getHandlerType();

    /**
     * 上传接口
     * @param logInfoDTOS
     * @return
     */
    void upload(List<LogInfoDTO> logInfoDTOS);

    /**
     * 查询接口
     * @param logQueryDTO
     * @return
     */
    LogInfoVO query(LogQueryDTO logQueryDTO);
}
