package com.cl.collector.service.impl;

import com.cl.collector.service.StatusCollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@Service("statusCollectorService")
@Slf4j
public class StatusCollectorServiceImpl implements StatusCollectorService {


    @Override
    public Double getCpuUsage() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
        String line;
        long totalTime = 0;
        long idleTime = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("cpu ")) {
                String[] parts = line.split("\\s+");
                totalTime = Arrays.stream(parts)
                        .skip(1)
                        .mapToLong(Long::parseLong)
                        .sum();
                idleTime = Long.parseLong(parts[4]);
            }
        }
        reader.close();
        return Double.parseDouble(String.format("%.2f",100 * ((totalTime-idleTime)*1.0 / totalTime)));
    }

    @Override
    public Double getMemUsage() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
        String line;
        long totalMemory = 0,freeMemory = 0,availableMemory=0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("MemTotal:")) {
                totalMemory = Long.parseLong(line.split("\\s+")[1]);
            } else if (line.startsWith("MemFree:")) {
                freeMemory = Long.parseLong(line.split("\\s+")[1]);
            }else if (line.startsWith("MemAvailable :")) {
                availableMemory = Long.parseLong(line.split("\\s+")[1]);
            }
        }
        reader.close();
        return Double.parseDouble(String.format("%.2f",100 * ((totalMemory - freeMemory-availableMemory)*1.0 / totalMemory)));
    }
}

