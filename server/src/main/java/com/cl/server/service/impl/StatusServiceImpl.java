package com.cl.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.cl.server.entity.Status;
import com.cl.server.enums.IsDeletedFlagEnum;
import com.cl.server.pojo.DTO.StatusQueryDTO;
import com.cl.server.pojo.VO.StatusResp;
import com.cl.server.pojo.VO.Values;
import com.cl.server.exception.BaseException;
import com.cl.server.mapper.StatusDao;
import com.cl.server.util.RedisUtil;
import com.cl.server.service.StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (Status)表服务实现类
 *
 * @author tressures
 * @date:  2024-05-26 17:06:02
 */
@Service("cpuStatusService")
@Slf4j
public class StatusServiceImpl implements StatusService {
    @Resource
    private StatusDao statusDao;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void uploadMetrics(List<Status> statusList) {
        String endPoint= statusList.get(0).getEndpoint();
        String cpuKey = redisUtil.buildKey(endPoint+"cpu.used.percent");
        String memKey = redisUtil.buildKey(endPoint+"mem.used.percent");
        long currentTime = statusList.get(0).getTimestamp();
        String cpuMember = currentTime + ":" + statusList.get(0).getValue();
        String memMember = currentTime + ":" + statusList.get(1).getValue();
        redisUtil.zAdd(cpuKey,cpuMember,currentTime);
        redisUtil.zAdd(memKey,memMember,currentTime);
        if (redisUtil.countZset(cpuKey)>10) removeOldest(cpuKey);
        if (redisUtil.countZset(memKey)>10) removeOldest(memKey);
        for(Status status : statusList){
            status.setCreateTime(LocalDateTime.now());
            status.setIsDelete(IsDeletedFlagEnum.UN_DELETED.getCode());
        }
        log.info("CpuStatusServiceImpl.uploadMetrics.cpuStatusList:{}", JSON.toJSONString(statusList));
        statusDao.insertBatch(statusList);
    }

    private void removeOldest(String key){
        Set<String> members = redisUtil.rangeZset(key,0,-1);
        String oldestMember = null;
        //ZSet集合默认按照score升序排列,集合第一个就是最老的元素
        if (CollectionUtils.isNotEmpty(members)) {
            Iterator<String> iterator = members.iterator();
            oldestMember = iterator.next();
            redisUtil.removeZset(key,oldestMember);
        }
    }

    @Override
        public List<StatusResp> queryMetrics(StatusQueryDTO statusQueryDTO) {
        Status status = new Status();
        status.setEndpoint(statusQueryDTO.getEndpoint());
        long size = statusDao.count(status);
        if(size==0L){
            throw new BaseException("主机不存在");
        }
        List<StatusResp> statusRespList = new ArrayList<>();
        //查指定类型利用率
        if(!StringUtils.isEmpty(statusQueryDTO.getMetric())){
            StatusResp statusResp = new StatusResp();
            List<Values> valueList = new ArrayList<>();
            //查Redis
            String key = redisUtil.buildKey(statusQueryDTO.getEndpoint()+statusQueryDTO.getMetric());
            Set<String> members = redisUtil.rangeByScore(key,statusQueryDTO.getStart_ts(),statusQueryDTO.getEnd_ts());
            Long timeEnd=Long.MIN_VALUE;
            if(CollectionUtils.isNotEmpty(members)){
                //包装data
                for(String item : members){
                    Values values = decorate(item);
                    valueList.add(values);
                    //找出Redis中最老的时间
                    if(timeEnd< values.getTimeStamp()) timeEnd= values.getTimeStamp();
                }
            }
            //先查出Redis中符合的数据，再根据Redis最早的数据时间查数据库里这个时间之前所有数据
            if(timeEnd==Long.MIN_VALUE){
                timeEnd = statusQueryDTO.getEnd_ts();
            }
            //查数据库
            List<Status> statusList = statusDao.queryAllByTimeStamp(statusQueryDTO.getEndpoint(),statusQueryDTO.getMetric()
                    ,statusQueryDTO.getStart_ts(),timeEnd);
            if(CollectionUtils.isNotEmpty(statusList)) {
                //包装data
                List<Values> values = statusList.stream().map(item -> {
                    Values value = new Values();
                    value.setTimeStamp(item.getTimestamp());
                    value.setValue(item.getValue());
                    return value;
                }).collect(Collectors.toList());
                valueList.addAll(values);
                statusResp.setMetric(statusQueryDTO.getMetric());
                statusResp.setValues(valueList);
                statusRespList.add(statusResp);
                }
            log.info("CpuStatusServiceImpl.queryMetrics.statusRespList:{}", JSON.toJSONString(statusRespList));
            return statusRespList;
            //查全部类型
        }else{
            StatusResp cpuStatusResp = new StatusResp();
            StatusResp memStatusResp = new StatusResp();
            List<Values> cpuValueList = new ArrayList<>();
            List<Values> memValueList = new ArrayList<>();
            String cpukey = redisUtil.buildKey(statusQueryDTO.getEndpoint()+"cpu.used.percent");
            String memkey = redisUtil.buildKey(statusQueryDTO.getEndpoint()+"mem.used.percent");
            Set<String> cpuMembers = redisUtil.rangeByScore(cpukey,statusQueryDTO.getStart_ts(),statusQueryDTO.getEnd_ts());
            Set<String> memMembers = redisUtil.rangeByScore(memkey,statusQueryDTO.getStart_ts(),statusQueryDTO.getEnd_ts());
            Long timeEnd=Long.MIN_VALUE;
            if(CollectionUtils.isNotEmpty(cpuMembers)&&CollectionUtils.isNotEmpty(memMembers)){
                //包装data
                for(String item : cpuMembers){
                    Values values = decorate(item);
                    cpuValueList.add(values);
                }
                for(String item : memMembers){
                    Values values = decorate(item);
                    memValueList.add(values);
                    //找出Redis中最老的时间
                    if(timeEnd<values.getTimeStamp()) timeEnd=values.getTimeStamp();
                }
            }
            //先查出Redis中符合的数据，再根据Redis最早的数据时间查数据库里这个时间之前所有数据
            if(timeEnd==Long.MIN_VALUE){
                timeEnd = statusQueryDTO.getEnd_ts();
            }
            //查数据库
            List<Status> statusList = statusDao.queryAllByTimeStamp(statusQueryDTO.getEndpoint(),statusQueryDTO.getMetric()
                    ,statusQueryDTO.getStart_ts(),timeEnd);
            if(CollectionUtils.isNotEmpty(statusList)) {
                //根据指标分组
                Map<String,List<Status>> map = statusList.stream()
                        .collect(Collectors.groupingBy(Status::getMetric));
                //包装data
                for(String key:map.keySet()){
                    List<Status> cs = map.get(key);
                    List<Values> values = cs.stream().map(item -> {
                        Values value = new Values();
                        value.setTimeStamp(item.getTimestamp());
                        value.setValue(item.getValue());
                        return value;
                    }).collect(Collectors.toList());
                    if("cpu.used.percent".equals(key)){
                        cpuValueList.addAll(values);
                        cpuStatusResp.setMetric("cpu.used.percent");
                        cpuStatusResp.setValues(cpuValueList);
                        statusRespList.add(cpuStatusResp);
                    }else{
                        memValueList.addAll(values);
                        memStatusResp.setMetric("mem.used.percent");
                        memStatusResp.setValues(memValueList);
                        statusRespList.add(memStatusResp);
                    }
                }
            }
            log.info("CpuStatusServiceImpl.queryMetrics.statusRespList:{}", JSON.toJSONString(statusRespList));
            return statusRespList;
        }
    }

    private Values decorate(String item){
        String[] parts = item.split(":");
        long timeStamp = Arrays.stream(parts[0].split(""))
                .mapToLong(Long::parseLong)
                .sum();
        double value = Arrays.stream(parts[1].split("\\\\."))
                .mapToDouble(Double::parseDouble)
                .sum();
        return new Values(timeStamp,value);
    }
}
