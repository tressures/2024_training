package com.cl.server.handler.storage.impl;

import com.cl.server.entity.LogAddress;
import com.cl.server.entity.LogInfo;
import com.cl.server.enums.IsDeletedFlagEnum;
import com.cl.server.mapper.LogAddressDao;
import com.cl.server.mapper.LogInfoDao;
import com.cl.server.pojo.DTO.LogInfoDTO;
import com.cl.server.pojo.DTO.LogQueryDTO;
import com.cl.server.pojo.VO.LogInfoVO;
import com.cl.server.enums.StorageTypeEnum;
import com.cl.server.handler.storage.StorageTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MYSQL存储方式
 *
 * @author: tressures
 * @date: 2024/6/5
 */
@Component
@Slf4j
public class MysqlStorageHandler implements StorageTypeHandler {

    @Resource
    private LogAddressDao logAddressDao;

    @Resource
    private LogInfoDao logInfoDao;

    @Override
    public StorageTypeEnum getHandlerType() {
        return StorageTypeEnum.MYSQL;
    }

    @Override
    public void upload(List<LogInfoDTO> logInfoDTOS) {
        for(LogInfoDTO logInfoDTO : logInfoDTOS){
            LogAddress logAddress = new LogAddress();
            logAddress.setHostName(logInfoDTO.getHostname());
            logAddress.setFile(logInfoDTO.getFile());
            logAddress.setCreateTime(LocalDateTime.now());
            logAddress.setIsDelete(IsDeletedFlagEnum.UN_DELETED.getCode());
            logAddressDao.insert(logAddress);
            //执行完插入语句后，自动将自增id赋值给对象的属性id
            int addressId = logAddress.getId();
            //日志和日志路径地址的绑定
            for(String log : logInfoDTO.getLogs()) {
                LogInfo logInfo = new LogInfo();
                logInfo.setLogAddressId(addressId);
                logInfo.setInfo(log);
                logInfo.setCreateTime(LocalDateTime.now());
                logInfo.setIsDelete(IsDeletedFlagEnum.UN_DELETED.getCode());
                logInfoDao.insert(logInfo);
            }
        }
    }

    @Override
    public LogInfoVO query(LogQueryDTO logQueryDTO) {
        LogInfoVO logInfoVO = new LogInfoVO();
        LogAddress logAddress = new LogAddress();
        logAddress.setHostName(logQueryDTO.getHostname());
        logAddress.setFile(logQueryDTO.getFile());
        logAddress.setIsDelete(IsDeletedFlagEnum.UN_DELETED.getCode());
        int addressId = logAddressDao.queryByLimit(logAddress);
        LogInfo logInfo = new LogInfo();
        logInfo.setLogAddressId(addressId);
        List<LogInfo> logInfos = logInfoDao.queryAllByLimit(logInfo);
        //按日志插入时间大小降序排序
        List<String> logs = logInfos.stream()
                .sorted(Comparator.comparing(LogInfo::getCreateTime).reversed())
                .map(LogInfo::getInfo)
                .collect(Collectors.toList());
        logInfoVO.setHostname(logAddress.getHostName());
        logInfoVO.setFile(logAddress.getFile());
        logInfoVO.setLogs(logs);
        return logInfoVO;
    }
}
