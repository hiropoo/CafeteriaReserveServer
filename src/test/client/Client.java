package test.client;

import test.util.User;

public class Client {
    public static void main(String[] args) {
        System.out.println("Client is running...\n");

        Client client = new Client();
        client.start();
    }

    // クライアントを起動
    public void start() {
        User.setUserName("testUser4");
        User.setPassword("testPassword4");
        User.setStudentID(1234567890);

        // 新規登録のテスト
        new ServerHandler().register();

        // ログインのテスト
        new ServerHandler().login();
    }

}
