package test.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import properties.PropertyUtil;
import test.util.User;

/*
 * テスト用のサーバーハンドラ
 * クライアントがサーバにリクエストを送信したり、サーバからのレスポンスを受け取るためのハンドラ
 */
public class ServerHandler {
    static final String SERVER_IP = PropertyUtil.getProperty("ip"); // サーバーのIPアドレス
    static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    /* インスタンス変数 */
    private static Socket clientSocket;
    private static BufferedReader in; // サーバからの入力ストリーム
    private static PrintWriter out; // サーバへの出力ストリーム

    // サーバに接続するメソッド
    private static void connect() throws RuntimeException {
        System.out.println("Connecting to the server...");
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Failed to connect to the server.\n");
            throw new RuntimeException("Failed to connect to the server.");
        }
    }

    /*
     * ユーザ情報を送信し、新規登録が成功したかどうかを返す
     * 新規登録成功時には、サーバからユーザIDを取得してUserクラスに保存
     * request -> "signUp userName password studentID"
     * response -> "Success userID" or "failure message"
     */
    public static boolean register() {
        String userName = User.getUserName();
        String password = User.getPassword();
        int studentID = User.getStudentID();
        boolean isRegisterSuccess = false;
        String request = "signUp " + userName + " " + password + " " + studentID;
        String response = "";

        System.out.println("Registering user: " + userName + " " + password + " " + studentID);

        try {
            connect();

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
        } catch (RuntimeException e) {
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

    /*
     * ログイン情報を送信し、ログインが成功したかどうかを返す
     * ログイン成功時には、ユーザーIDと学籍番号を取得してUserクラスに保存
     * request -> "login userName password"
     * response -> "success userID studentID" or "failure message"
     */
    public static boolean login() {
        String userName = User.getUserName();
        String password = User.getPassword();
        boolean isLoginSuccess = false;
        String request = "login " + userName + " " + password;
        String response = "";

        System.out.println("Logging in: " + userName + " " + password);

        try {
            connect();

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
        } catch (RuntimeException e) {
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

    /*
     * ユーザーIDを送信し、友達リストを取得できたかどうかを返す
     * 友達リスト取得成功時には、友達のユーザIDとユーザー名をUserクラスに保存
     * request -> "fetchFriend userID"
     * response -> "success userID1:userName1 userID2:userName2 ..." or
     * "failure message"
     */
    public static boolean fetchFriend() {
        return false;
    }

    /*
     * ユーザーIDと友達のユーザーIDを送信し、友達追加が成功したかどうかを返す
     * 成功時には、友達のユーザーIDとユーザー名をUserクラスに保存
     * request -> "addFriend userID friendID"
     * response -> "success friendID:friendName" or "failure message"
     */
    public static boolean addFriend() {
        return false;
    }

    /*
     * ユーザーIDと友達のユーザーIDを送信し、友達削除が成功したかどうかを返す
     * 成功時には、友達のユーザーIDとユーザー名をUserクラスから削除
     * request -> "removeFriend userID friendID"
     * response -> "success" or "failure message"
     */
    public static boolean removeFriend() {
        return false;
    }

    /*
     * ユーザーIDを送信し、予約情報を取得できたかどうかを返す
     * 予約情報取得成功時には、予約情報をUserクラスに保存
     * request -> "fetchReservation userID"
     * response ->
     * "success userID:userName,userID:userName,... cafeNum seatNum startTime endTime went"
     */
    public static boolean fetchReservation() {
        return false;
    }

    /*
     * ユーザーID、学食番号、座席番号、予約開始時間、予約終了時間を送信し、予約が成功したかどうかを返す
     * 予約成功時には、予約情報をUserクラスに保存
     * request ->
     * "addReservation userID1,userID2,... cafeNum seatNum startTime endTime"
     * response -> "success" or "failure message"
     */
    public static boolean addReservation() {
        return false;
    }

    /*
     * ユーザーIDを送信し、予約をキャンセルできたかどうかを返す
     * キャンセル成功時には、予約情報をUserクラスから削除
     * request -> "removeReservation userID1,userID2,..."
     * response -> "success" or "failure message"
     */
    public static boolean removeReservation() {
        return false;
    }

    /*
     * 学食番号と予約の開始時間と終了時間を送信し、その時間に空いている座席を取得する
     * request -> "fetchAvailableSeats startTime endTime"
     * response -> "success seatNum1,seatNum2,seatNum3..." or "failure message"
     */
    public static int[] fetchAvailableSeats(int cafeNum, Date startTime, Date endTime) {
        return new int[0];
    }

}
