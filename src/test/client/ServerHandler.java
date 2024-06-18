package test.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import main.util.PropertyUtil;
import test.util.User;

/*
 * テスト用のサーバーハンドラ
 * クライアントがサーバにリクエストを送信したり、サーバからのレスポンスを受け取るためのハンドラ
 */
public class ServerHandler {
    static final String SERVER_IP = PropertyUtil.getProperty("ip"); // サーバーのIPアドレス
    static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    /* インスタンス変数 */
    private Socket clientSocket;
    private BufferedReader in; // サーバからの入力ストリーム
    private PrintWriter out; // サーバへの出力ストリーム
    private User user; // Userインスタンス

    // コンストラクタでサーバに接続
    public ServerHandler(User user) {
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

        // Userインスタンスを生成（仮）実際は新規登録時に生成
        this.user = user; // <- 実際のコード
    }

    // ユーザ情報を送信し、新規登録が成功したかどうかを返す
    public boolean register() {
        String userID = user.getUserID();
        String userName = user.getUserName();
        String password = user.getPassword();
        int studentID = user.getStudentID();
        boolean isRegisterSuccess = false;

        try {
            out.println("signUp " + userID + " " + userName + " " + password + " " + studentID);
            System.out.println("Sent: signUp " + userID + " " + userName + " " + password + " " + studentID);

            String response = in.readLine();
            if (response.equals("success")) {
                System.out.println("Registration success");
                isRegisterSuccess = true;
            } else {
                System.out.println("Registration failed.");
                System.out.println("Response: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error occurred while registering.");
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Failed to close the client socket.");
            }
        }

        return isRegisterSuccess;
    }

    // ログイン情報を送信し、ログインが成功したかどうかを返す
    public boolean login() {
        String userName = user.getUserName();
        String password = user.getPassword();
        boolean isLoginSuccess = false;

        try {
            out.println("login " + userName + " " + password);
            System.out.println("Sent: login " + userName + " " + password);

            String response = in.readLine();
            if (response.equals("success")) {
                System.out.println("Login success.");
                isLoginSuccess = true;
            } else {
                System.out.println("Login failed.");
                System.out.println("Response: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error occurred while logging in.");
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Failed to close the client socket.");
            }
        }

        return isLoginSuccess;
    }

}
