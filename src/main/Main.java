package main;

import main.util.PropertyUtil;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        String ip = PropertyUtil.getProperty("server_ip");
        System.out.println("Server IP: " + ip);
    }
}
