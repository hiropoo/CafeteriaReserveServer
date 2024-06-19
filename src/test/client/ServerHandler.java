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

    // コンストラクタでサーバに接続
    public ServerHandler() {
        System.out.println("Connecting to the server...");
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the server.\n");
        }
    }

    // ユーザ情報を送信し、新規登録が成功したかどうかを返す
    // 新規登録成功時には、サーバからユーザIDを取得してUserクラスに保存
    // request -> "signUp userName password studentID"
    // response -> "Success userID" or "Failure message"
    public boolean register() {
        String userName = User.getUserName();
        String password = User.getPassword();
        int studentID = User.getStudentID();
        boolean isRegisterSuccess = false;
        String request = "signUp " + userName + " " + password + " " + studentID;
        String response = "";

        try {
            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                System.out.println("Registration success");
                User.setUserID(args); // ユーザIDをUserクラスに保存
                isRegisterSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Registration failed.");
                System.out.println("message: " + args);
            } else {
                System.out.println("Invalid response: " + response);
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
            System.out.println();
        }

        return isRegisterSuccess;
    }

    // ログイン情報を送信し、ログインが成功したかどうかを返す
    // ログイン成功時には、ユーザーIDと学籍番号を取得してUserクラスに保存
    // request -> "login userName password"
    // response -> "success userID studentID" or "Failure message"
    public boolean login() {
        String userName = User.getUserName();
        String password = User.getPassword();
        boolean isLoginSuccess = false;
        String request = "login " + userName + " " + password;
        String response = "";

        try {
            out.println(request);
            System.out.println("Sent: login " + userName + " " + password);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                System.out.println("Login success.");
                User.setUserID(args.split(" ")[0]); // ユーザIDをUserクラスに保存
                User.setStudentID(Integer.parseInt(args.split(" ")[1])); // 学籍番号をUserクラスに保存
                isLoginSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Login failed.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
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
            System.out.println();
        }

        return isLoginSuccess;
    }

}
