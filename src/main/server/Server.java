package main.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import main.util.PropertyUtil;

public class Server {
    private static Map<String, CommandExecutor> commandExecutors = new HashMap<>(); // コマンドとそれに対応するクラスのマッピング

    private static ServerSocket serverSocket = null;  // サーバーソケット
    private static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    // コマンドマップの初期化
    static {
        commandExecutors.put("signUp", new SignUpExecutor());
    }

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT); // サーバーソケットの作成
        System.out.println("Server is running...");

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 接続待ち

                // ストリームの作成
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // クライアントのリクエスト処理は別スレッドにフォワード
                new Thread(() -> handleClient(clientSocket, writer, reader)).start();
            }
        } catch (IOException e) {
            System.out.println("Error accepting client connection: " + e.getMessage());
        } finally {
            if (serverSocket != null)
                serverSocket.close();

        }
    }

    // クライアントからのリクエストを受け取り、その内容に基づいて適切なアクションを実行
    private static void handleClient(Socket clientSocket, PrintWriter out, BufferedReader in) {
        try {
            // クライアントからの入力読み取り
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received message: " + request);
                
                // request(command args) -> "command" と "args" に分割
                String[] parts = request.split(" ", 2);
                String command = parts[0];
                String args = parts.length > 1 ? parts[1] : "";

                // コマンドの実行
                CommandExecutor cmdExecutor = commandExecutors.get(command);
                if (cmdExecutor != null) {
                    cmdExecutor.execute(out, args);
                } else {
                    out.println("Unknown command: " + command);
                }

            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                // クライアントソケットをクローズ
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to close client socket: " + e.getMessage());
            }
        }
    }
}
