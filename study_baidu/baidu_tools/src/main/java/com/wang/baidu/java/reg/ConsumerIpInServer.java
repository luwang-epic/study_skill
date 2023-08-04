package com.wang.baidu.java.reg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: wanglu51
 * @Date: 2023/8/3 20:45
 */
public class ConsumerIpInServer {

    private static final String CONSUMER_IP = "alarm_traces    19         3703318271      3703318271      0               consumer-3-15223c4d-7442-4fed-a08d-c6434e369793 /10.93.141.144  consumer-3\n" +
            "alarm_traces    12         3687455584      3687455584      0               consumer-1-95f42a3d-9455-4315-8655-2caa51988daa /10.209.189.24  consumer-1\n" +
            "alarm_traces    7          4328636831      4328636831      0               consumer-1-6dc0dda6-35e2-44ca-8a8c-22336036d82f /10.188.238.148 consumer-1\n" +
            "alarm_traces    2          4284689585      4284689585      0               consumer-1-2e81acf6-09ac-41fe-bdab-215eb107b2e0 /10.209.188.82  consumer-1\n" +
            "alarm_traces    4          4258302019      4258302019      0               consumer-1-33f0d037-afac-4a2e-805f-7c61db44906e /10.209.188.92  consumer-1\n" +
            "alarm_traces    5          4296370235      4296370235      0               consumer-1-39439e22-3ce6-4504-953a-91a9a72cdd0f /10.188.232.141 consumer-1\n" +
            "alarm_traces    18         3638476107      3638476107      0               consumer-3-04501142-f8d7-4a1a-bb7e-688bf6537031 /10.93.141.156  consumer-3\n" +
            "alarm_traces    13         3586011290      3586011290      0               consumer-1-99c7524a-8a6f-48b5-8ce1-ee7ae20d7c15 /10.174.228.36  consumer-1\n" +
            "alarm_traces    15         3664454067      3664454067      0               consumer-1-d2091319-bb61-4cbf-a5d7-75b1c49f04a9 /10.174.224.14  consumer-1\n" +
            "alarm_traces    1          4257679390      4257679390      0               consumer-1-2c46a100-4fdc-4783-83d6-220de67f67c8 /10.174.223.42  consumer-1\n" +
            "alarm_traces    9          3693784026      3693784026      0               consumer-1-7d0281eb-ea07-432e-9100-a9ee8f292af1 /10.174.240.26  consumer-1\n" +
            "alarm_traces    8          3712954956      3712954956      0               consumer-1-7cfb0781-6cd0-4ef3-8e98-8cde03c839ad /10.174.230.155 consumer-1\n" +
            "alarm_traces    11         3687061309      3687061309      0               consumer-1-8e5accee-0a2f-4c40-aca3-8b79e636c22f /10.174.206.150 consumer-1\n" +
            "alarm_traces    14         3645497099      3645497099      0               consumer-1-a6c798e7-58e3-4ee5-8eec-6d8a98ae7782 /10.174.237.14  consumer-1\n" +
            "alarm_traces    0          4222775624      4222775624      0               consumer-1-1799a1ed-fb09-47d8-9e05-8200f9c26c60 /10.209.189.26  consumer-1\n" +
            "alarm_traces    3          4303386220      4303386220      0               consumer-1-31fcf5c3-3262-4379-8646-f1f22266ec0a /10.209.189.25  consumer-1\n" +
            "alarm_traces    10         3656932313      3656932313      0               consumer-1-837dc7f9-65a6-4b80-bf51-a651777a60f9 /10.209.187.220 consumer-1\n" +
            "alarm_traces    17         3624575529      3624575529      0               consumer-1-fe89a538-e76d-4270-864a-9d8dd3a8c6db /10.209.186.146 consumer-1\n" +
            "alarm_traces    6          4253269081      4253269081      0               consumer-1-555acd81-895b-4364-bcd6-57ca73c584b4 /10.209.188.145 consumer-1\n" +
            "alarm_traces    16         3728682131      3728682131      0               consumer-1-d8f1a306-9a26-492e-929a-f5f8b05dfd1e /10.209.188.102 consumer-1";


    private static final List<String> SERVER_IPS = Arrays.asList(
            "10.209.186.146:8855",
            "10.209.187.220:8855",
            "10.209.188.82:8855",
            "10.209.188.92:8855",
            "10.209.188.102:8855",
            "10.209.188.145:8855",
            "10.209.189.24:8855",
            "10.209.189.25:8855",
            "10.209.189.26:8855",
            "10.174.206.150:8855",
            "10.174.223.42:8855",
            "10.174.224.14:8855",
            "10.174.228.36:8855",
            "10.174.230.155:8855",
            "10.174.237.14:8855",
            "10.174.240.26:8855",
            "10.188.232.141:8855",
            "10.188.238.148:8855",
            "10.209.187.219:8855",
            "10.209.188.17:8855",
            "10.209.188.91:8855",
            "10.209.188.101:8855",
            "10.211.83.139:8855",
            "10.211.83.140:8855",
            "10.211.83.141:8855",
            "10.211.83.142:8855",
            "10.211.83.143:8855",
            "10.211.83.144:8855",
            "10.211.83.145:8855",
            "10.211.83.146:8855",
            "10.211.83.147:8855",
            "10.211.83.148:8855",
            "10.211.83.150:8855",
            "10.211.83.151:8855",
            "10.211.83.152:8855",
            "10.211.83.154:8855",
            "10.211.83.155:8855",
            "10.211.83.156:8855",
            "10.211.83.157:8855",
            "10.211.83.158:8855",
            "10.211.83.159:8855",
            "10.93.140.15:8855",
            "10.93.140.16:8855",
            "10.93.140.79:8855",
            "10.93.140.80:8855",
            "10.93.140.93:8855",
            "10.93.140.94:8855",
            "10.93.140.143:8855",
            "10.93.140.144:8855",
            "10.93.140.157:8855",
            "10.93.140.158:8855",
            "10.93.141.142:8855",
            "10.93.141.143:8855",
            "10.93.141.144:8855",
            "10.93.141.156:8855",
            "10.93.141.157:8855",
            "10.93.141.158:8855",
            "10.93.141.206:8855",
            "10.93.141.207:8855",
            "10.93.141.208:8855",
            "10.93.141.221:8855",
            "10.93.141.222:8855",
            "10.93.142.14:8855",
            "10.93.142.15:8855",
            "10.93.142.16:8855",
            "10.93.152.139:8855",
            "10.93.152.140:8855",
            "10.93.152.141:8855",
            "10.93.152.142:8855",
            "10.93.152.143:8855",
            "10.93.152.144:8855",
            "10.93.152.208:8855",
            "10.93.152.209:8855",
            "10.93.152.210:8855",
            "10.93.152.211:8855",
            "10.174.195.26:8855",
            "10.212.174.14:8855",
            "10.212.175.87:8855"
    );

    public static List<String> getConsumerIps() {
        Pattern pattern = Pattern.compile("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}");
        Matcher matcher = pattern.matcher(CONSUMER_IP);
        List<String> consumerIps = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(0);
            consumerIps.add(group + ":8855");
        }
        return consumerIps;
    }


    public static void main(String[] args) {
        List<String> consumerIps = getConsumerIps();
        consumerIps.forEach(System.out::println);
        System.out.println("===================== consumer ip size: " + consumerIps.size() + " =======================");

        List<String> notInServerIps = new ArrayList<>();
        for (String ip : consumerIps) {
            if (!SERVER_IPS.contains(ip)) {
                notInServerIps.add(ip);
            }
        }
        System.out.println("================== not in server ips ==========================");
        notInServerIps.forEach(System.out::println);
    }



}
