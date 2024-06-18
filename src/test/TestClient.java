package test;

import main.util.User;

public class TestClient {
    public static void main(String[] args) {

        TestClient client = new TestClient();
        client.start();
    }

    // クライアントを起動
    public void start() {
        // 新規登録のテスト
        User user = new User("user1", "pass1", 0000001);
        TestServerHandler serverHandler = new TestServerHandler(user);
        boolean isRegisterSuccess = serverHandler.register();
    }

}
