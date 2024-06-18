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
        User user = new User("user1", "pass1", 0000001);
        ServerHandler serverHandler = new ServerHandler(user);
        serverHandler.register();
    }

}
