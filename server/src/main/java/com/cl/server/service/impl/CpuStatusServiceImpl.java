package com.cl.server.service.impl;

import com.cl.server.entity.CpuStatus;
import com.cl.server.entity.DTO.StatusQueryDTO;
import com.cl.server.entity.VO.StatusResp;
import com.cl.server.entity.VO.Values;
import com.cl.server.mapper.CpuStatusDao;
import com.cl.server.redis.RedisUtil;
import com.cl.server.service.CpuStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (CpuStatus)表服务实现类
 *
 * @author tressures
 * @date:  2024-05-26 17:06:02
 */
@Service("cpuStatusService")
@Slf4j
public class CpuStatusServiceImpl implements CpuStatusService {
    @Resource
    private CpuStatusDao cpuStatusDao;
    @Resource
    private RedisUtil redisUtil;


    @Override
    public void uploadMetrics(List<CpuStatus> cpuStatusList) {
        String KEY = redisUtil.buildKey(cpuStatusList.get(0).getEndpoint());
        long currentTime = cpuStatusList.get(0).getTimestamp();
        String member = currentTime + ": " + cpuStatusList.get(0).getValue() + "," +cpuStatusList.get(1).getValue();
        redisUtil.zAdd(KEY,member,currentTime);
        while (redisUtil.countZset(KEY)>10){
            Set<String> members = redisUtil.rangeZset(KEY,0,-1);
            String oldestMember = null;
            //ZSet集合默认按照score升序排列,集合第一个就是最老的元素
            if (CollectionUtils.isNotEmpty(members)) {
                Iterator<String> iterator = members.iterator();
                oldestMember = iterator.next();
                redisUtil.removeZset(KEY,oldestMember);
            }
        }
        cpuStatusDao.insertBatch(cpuStatusList);
    }

    @Override
        public List<StatusResp> queryMetrics(StatusQueryDTO statusQueryDTO) {
        List<StatusResp> statusRespList = new ArrayList<>();
        //根据机器及指标查出所有
        CpuStatus cpuStatus = new CpuStatus();
        cpuStatus.setEndpoint(statusQueryDTO.getEndPoint());
        cpuStatus.setMetric(statusQueryDTO.getMetric());
        List<CpuStatus> cpuStatusList = cpuStatusDao.queryAllByLimit(cpuStatus);
        if(CollectionUtils.isNotEmpty(cpuStatusList)) {
            //过滤不符合时间段的
            List<CpuStatus> cpuStatuses = cpuStatusList.stream()
                    .filter(item -> item.getTimestamp() >= statusQueryDTO.getStart_ts() && item.getTimestamp() <= statusQueryDTO.getEnd_ts())
                    .collect(Collectors.toList());
            //根据指标分组
            Map<String,List<CpuStatus>> map = cpuStatuses.stream()
                    .collect(Collectors.groupingBy(CpuStatus::getMetric));
            //包装data
            for(String key:map.keySet()){
                List<CpuStatus> cs = map.get(key);
                List<Values> values = cs.stream().map(item -> {
                            Values value = new Values();
                            value.setTimeStamp(item.getTimestamp());
                            value.setValue(item.getValue());
                            return value;
                        })
                        .collect(Collectors.toList());
                StatusResp statusResp = new StatusResp();
                statusResp.setMetric(key);
                statusResp.setValues(values);
                statusRespList.add(statusResp);
            }
        }
        return statusRespList;
    }
}
