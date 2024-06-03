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
                long user = Long.parseLong(parts[0]);
                long nice = Long.parseLong(parts[1]);
                long sys = Long.parseLong(parts[2]);
                long idle = Long.parseLong(parts[3]);
                long total = user + nice + sys + idle;
                idlePart=idle;
                totalPart=total;
                break;
            }
        }
        return Double.parseDouble(String.format("%.2f",100 * idlePart*1.0 / totalPart));
    }

    @Override
    public Double getMemUsage() throws IOException {
        String line;
        double memoryUtilization = 0;
        BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("MemTotal: ")) {
                String[] parts = line.trim().split(": ");
                if (parts.length > 1) {
                    long totalMemory = Long.parseLong(parts[1].trim());
                    reader.readLine(); // Skip the next line
                    line = reader.readLine();
                    parts = line.trim().split(": ");
                    if (parts.length > 1) {
                        long freeMemory = Long.parseLong(parts[1].trim());
                        memoryUtilization = (1 - (freeMemory / (double) totalMemory)) * 100;
                        break;
                    }
                }
            }
        }
        return Double.parseDouble(String.format("%.2f",memoryUtilization));
    }
}
