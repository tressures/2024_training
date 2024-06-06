package com.cl.server.convert;


import com.cl.server.entity.CpuStatus;
import com.cl.server.pojo.DTO.StatusDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StatusConverter {

    StatusConverter INSTANCE = Mappers.getMapper(StatusConverter.class);

    List<CpuStatus> convertDTOListToEntityList(List<StatusDTO> statusDTOS);

}
