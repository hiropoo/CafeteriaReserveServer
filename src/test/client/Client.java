package test.client;

import test.util.User;

public class Client {
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    // クライアントを起動
    public void start() {
        // 新規登録のテスト
        User.setUserName("testUser4");
        User.setPassword("testPassword4");
        User.setStudentID(1234567890);
        // ログインのテスト
        new ServerHandler().login();
    }

}
