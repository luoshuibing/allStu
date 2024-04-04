package com.heima;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static Socket s;
    static PrintWriter writer;
    static BufferedReader reader;

    public static void main(String[] args) throws IOException {
        // 1.定义连接参数
        String host = "192.168.88.102";
        int port = 6379;
        // 2.连接 Redis
        s = new Socket(host, port);
        // 2.1.获取输入流
        writer = new PrintWriter(s.getOutputStream());
        // 2.2.获取输出流
        reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        //发送请求
        sendRequest("auth", "123456");
        //接收响应
        Object result1 = handleResponse();
        System.out.println(result1);
        sendRequest("set", "name", "虎哥");
        //接收响应
        Object result2 = handleResponse();
        System.out.println(result2);

        sendRequest("get", "name");
        //接收响应
        Object result3 = handleResponse();
        System.out.println(result3);
        // 5.关闭连接
        if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (s != null) s.close();
    }

    private static Object handleResponse(String... args) {
        try {
            // 当前前缀
            char prefix = (char) reader.read();
            switch (prefix) {
                case '+': // 单行字符串，直接返回
                    return reader.readLine();
                case '-': // 异常，直接抛出
                    throw new RuntimeException(reader.readLine());
                case ':': // 数值，转为 int 返回
                    return Integer.valueOf(reader.readLine());
                case '$': // 多行字符串，先读长度
                    int length = Integer.parseInt(reader.readLine());
                    // 如果为空，直接返回
                    if (length == 0 || length == -1) return "";
                    // 不为空，则读取下一行
                    return reader.readLine();
                case '*': // 数组，遍历读取
                    return readBulkString();
                default:
                    return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private static List<Object> readBulkString() throws IOException {
        // 当前数组大小
        int size = Integer.parseInt(reader.readLine());
        // 数组为空，直接返回 null
        if (size == 0 || size == -1) {
            return null;
        }
        List<Object> rs = new ArrayList<>(size);
        for (int i = size; i > 0; i--) {
            try { // 递归读取
                rs.add(handleResponse());
            } catch (Exception e) {
                rs.add(e);
            }
        }
        return rs;
    }

    private static void sendRequest(String... args) {
//        writer.println("*3");
//        writer.println("$3");
//        writer.println("set");
//        writer.println("$4");
//        writer.println("name");
//        writer.println("$6");
//        writer.println("虎哥");
//        writer.flush();

        // 元素个数
        writer.println("*" + args.length);
        // 参数
        for (String arg : args) {
            writer.println("$" + arg.getBytes(StandardCharsets.UTF_8).length);
            writer.println(arg);
        }
        // 刷新
        writer.flush();
    }


}
