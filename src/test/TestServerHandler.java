package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import main.util.PropertyUtil;

/*
 * テスト用のサーバーハンドラ
 * クライアントがサーバにリクエストを送信したり、サーバからのレスポンスを受け取るためのハンドラ
 */
public class TestServerHandler {
    static final String SERVER_IP = PropertyUtil.getProperty("ip"); // サーバーのIPアドレス
    static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    /* インスタンス変数 */
    private Socket clientSocket;
    private BufferedReader in; // サーバからの入力ストリーム
    private PrintWriter out; // サーバへの出力ストリーム

    // Userインスタンスを生成（未実装）


    // コンストラクタでサーバに接続
    public TestServerHandler() {
        System.out.println("Connecting to the server...");
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the server.");
        }
    }





}
