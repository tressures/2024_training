package com.cl.server.handler.storage.impl;

import com.cl.server.pojo.DTO.LogInfoDTO;
import com.cl.server.pojo.DTO.LogQueryDTO;
import com.cl.server.pojo.VO.LogInfoVO;
import com.cl.server.enums.StorageTypeEnum;
import com.cl.server.handler.storage.StorageTypeHandler;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LocalStorageHandler implements StorageTypeHandler {
    @Override
    public StorageTypeEnum getHandlerType() {
        return StorageTypeEnum.LOCAL;
    }

    @Override
    public void upload(List<LogInfoDTO> logInfoDTOS) {

    }

    @Override
    public LogInfoVO query(LogQueryDTO logQueryDTO) {
        return null;
    }
}
