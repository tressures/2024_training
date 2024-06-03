package com.cl.collector.service.impl;

import com.cl.collector.service.StatusCollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service("statusCollectorService")
@Slf4j
public class StatusCollectorServiceImpl implements StatusCollectorService {


    @Override
    public Double getCpuUsage() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
        long idlePart = 0,totalPart = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("cpu ")) {
                String[] parts = line.trim().split(" ");
                long user = Long.parseLong(parts[1]);
                long nice = Long.parseLong(parts[2]);
                long sys = Long.parseLong(parts[3]);
                long idle = Long.parseLong(parts[4]);
                long total = user + nice + sys + idle;
                idlePart=idle;
                totalPart=total;
                break;
            }
        }
        reader.close();
        return Double.parseDouble(String.format("%.2f",100 * idlePart*1.0 / totalPart));
    }

    @Override
    public Double getMemUsage() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
        String line;
        long totalMemory = 0,freeMemory = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("MemTotal:")) {
                totalMemory = Long.parseLong(line.split("\\s+")[1]);
            } else if (line.startsWith("MemFree:")) {
                freeMemory = Long.parseLong(line.split("\\s+")[1]);
            }
        }
        reader.close();
        return Double.parseDouble(String.format("%.2f",100 * (totalMemory - freeMemory)*1.0 / totalMemory));
    }
}

