package main.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import properties.PropertyUtil;

public class Server {
    private static Map<String, CommandExecutor> commandExecutors = new HashMap<>(); // コマンドとそれに対応するクラスのマッピング

    private static ServerSocket serverSocket = null; // サーバーソケット
    private static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    // コマンドマップの初期化
    static {
        commandExecutors.put("signUp", new SignUpExecutor());
        commandExecutors.put("login", new LoginExecutor());
    }

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT); // サーバーソケットの作成
        System.out.println("Server is running...\n");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                UpdateArrivedExecutor.checkArrival();
            }
        };
        long initialDelay = calculateInitialDelay();
        long period = 20;   //実行間隔
        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES); //定期的にtaskを実行

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
                System.out.println("Received request: " + request);

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
            System.out.println();
        }
    }

    private static long calculateInitialDelay() {   //次の実行時間までの分数を返すメソッド
        LocalDateTime now = LocalDateTime.now();
        int minute = now.getMinute();

        int nextRunMinute = ((minute / 20) * 20 + 6) % 60;
        if (minute >= nextRunMinute) {
            nextRunMinute += 20;
        }

        LocalDateTime nextRunTime = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(nextRunMinute);
        long delay = ChronoUnit.MINUTES.between(now, nextRunTime);
        return delay;
    }
}
