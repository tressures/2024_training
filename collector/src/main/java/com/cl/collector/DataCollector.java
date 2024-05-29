package com.cl.collector;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class DataCollector {
    private static final String TO_SERVER_URL = "http://localhost:8080/api/metric/upload";
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    collectAndReport();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 60000);
    }

    private static void collectAndReport() throws IOException {

        InetAddress localHost = InetAddress.getLocalHost();
        String hostName = localHost.getHostName();
        Long timeStamp = System.currentTimeMillis()/1000;
        Double cpuUsage = getCpuUsage();
        Double memUsage = getMemUsage();

        JSONArray Status = new JSONArray();

        JSONObject cpuStatue = new JSONObject();
        cpuStatue.put("metric", "cpu.used.percent");
        cpuStatue.put("endpoint", hostName);
        cpuStatue.put("timestamp", timeStamp);
        cpuStatue.put("step", 60);
        cpuStatue.put("value", cpuUsage);

        JSONObject memStatue = new JSONObject();
        memStatue.put("metric", "mem.used.percent");
        memStatue.put("endpoint", hostName);
        memStatue.put("timestamp", timeStamp);
        memStatue.put("step", 60);
        memStatue.put("value", memUsage);

        Status.add(cpuStatue);
        Status.add(memStatue);
        System.out.println("CpuUsage: "+cpuUsage+",MemUsage: "+memUsage);
        report(Status);

    }

    private static void report(JSONArray jsonArray) throws IOException{

        URL url = new URL(TO_SERVER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] inputBytes = jsonArray.toString().getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
            os.flush();
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response: " + response.toString());
            }
        } else {
            System.out.println("Error: " + responseCode+";Message: "+conn.getResponseMessage());
        }
    }

    private static double getCpuUsage() throws IOException {
        FileReader fileReader = new FileReader("/proc/stat");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        long totalTime = 0;
        long idleTime = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("cpu ")) {
                String[] tokens = line.split("\\s+");
                totalTime = Arrays.stream(tokens)
                        .skip(1)
                        .mapToLong(Long::parseLong)
                        .sum();
                idleTime = Long.parseLong(tokens[4]);
            }
        }
        return 100 * (totalTime - idleTime)*1.0 / totalTime;

    }

    private static Double getMemUsage() throws IOException {
        FileReader fileReader = new FileReader("/proc/meminfo");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        long totalMemory = 0;
        long freeMemory = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("MemTotal:")) {
                totalMemory = Long.parseLong(line.split("\\s+")[1]);
            } else if (line.startsWith("MemFree:")) {
                freeMemory = Long.parseLong(line.split("\\s+")[1]);
            }
        }
        return 100 * (totalMemory - freeMemory)*1.0 / totalMemory;
    }
}

