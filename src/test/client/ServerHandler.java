package test.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private static void connect() throws ConnectionException {
        System.out.println("Connecting to the server...");
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Failed to connect to the server.\n");
            throw new ConnectionException("Failed to connect to the server.");
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
                User.setUserID(args); // ユーザIDをUserクラスに保存

                System.out.println("Registration success");
                isRegisterSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Registration failed.");
                System.out.println("message: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while registering.");
            e.printStackTrace();
        } finally {
            disconnect();
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
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                User.setUserID(args.split(" ")[0]); // ユーザIDをUserクラスに保存
                User.setStudentID(Integer.parseInt(args.split(" ")[1])); // 学籍番号をUserクラスに保存

                System.out.println("Login success.");
                isLoginSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Login failed.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while logging in.");
            e.printStackTrace();
        } finally {
            disconnect();
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
        String userId = User.getUserID();
        boolean isFetchSuccess = false;
        String request = "fetchFriend " + userId;
        String response = "";

        System.out.println("Fetching friend list for user: " + userId);

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                String[] friends = args.split(" ");
                for (String friend : friends) {
                    String friendId = friend.split(":")[0];
                    String friendName = friend.split(":")[1];
                    User.getFriendList().put(friendId, friendName); // 友達のユーザIDとユーザ名をUserクラスに保存
                }

                System.out.println("Friend list fetched successfully.");
                isFetchSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to fetch friend list.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while fetching friend list.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isFetchSuccess;
    }

    /*
     * ユーザーIDと友達のユーザーIDを送信し、友達追加が成功したかどうかを返す
     * 成功時には、友達のユーザーIDとユーザー名をUserクラスに保存
     * request -> "addFriend userID friendID"
     * response -> "success friendID:friendName" or "failure message"
     */
    public static boolean addFriend(String friendID) {
        String userId = User.getUserID();
        Map<String, String> friendList = User.getFriendList();
        boolean isAddSuccess = false;
        String request = "addFriend " + userId + " " + friendID;
        String response = "";

        System.out.println("Adding friend: " + userId + " " + friendID);

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                String friendName = args.split(":")[1];
                if (friendList.containsKey(friendID)) {
                    System.out.println("Friend already exists.");
                } else {
                    // 友達のユーザIDとユーザ名をUserクラスに保存
                    friendList.put(friendID, friendName);
                    System.out.println("Friend added successfully.");
                }

                isAddSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to add friend.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while adding friend.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isAddSuccess;
    }

    private static void disconnect() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.out.println("Failed to close the client socket.");
        }
        System.out.println();
    }

    /*
     * ユーザーIDと友達のユーザーIDを送信し、友達削除が成功したかどうかを返す
     * 成功時には、友達のユーザーIDとユーザー名をUserクラスから削除
     * request -> "removeFriend userID friendID"
     * response -> "success" or "failure message"
     */
    public static boolean removeFriend(String friendID) {
        String userId = User.getUserID();
        Map<String, String> friendList = User.getFriendList();
        boolean isRemoveSuccess = false;
        String request = "removeFriend " + userId + " " + friendID;

        System.out.println("Removing friend: " + userId + " " + friendID);

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            String response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                if (friendList.containsKey(friendID)) {
                    // 友達のユーザIDとユーザ名をUserクラスから削除
                    friendList.remove(friendID);
                    System.out.println("Friend removed successfully.");
                } else {
                    System.out.println("Friend does not exist.");
                }

                isRemoveSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to remove friend.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while removing friend.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isRemoveSuccess;
    }

    /*
     * ユーザーIDを送信し、予約情報を取得できたかどうかを返す
     * 予約情報取得成功時には、予約情報をUserクラスに保存
     * request -> "fetchReservation userID"
     * response ->
     * "success userID,userID,... cafeNum seatNum startTime endTime went"
     */
    public static boolean fetchReservation() {
        String userId = User.getUserID();
        boolean isFetchSuccess = false;
        String request = "fetchReservation " + userId;
        String response = "";

        System.out.println("Fetching reservation for user: " + userId);

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                // 予約情報をUserクラスに保存
                // エラー時はParseExceptionをthrow
                User.getReservation().fromResponse(args);

                System.out.println("Reservation fetched successfully.");
                isFetchSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to fetch reservation.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ParseException e) {
            System.out.println("Failed to set reservation data.");
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while fetching reservation.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isFetchSuccess;
    }

    /*
     * ユーザーID、学食番号、座席番号、予約開始時間、予約終了時間を送信し、予約が成功したかどうかを返す
     * 予約成功時には、予約情報をUserクラスに保存
     * request ->
     * "addReservation userID1,userID2,... cafeNum seatNum,seatNum startTime endTime"
     * response -> "success" or "failure message"
     */
    public static boolean addReservation() {
        List<String> members = User.getReservation().getMembers();
        int cafeNum = User.getReservation().getCafeNum();
        List<String> seatNums = User.getReservation().getSeatNums().stream().map(seatNum -> String.valueOf(seatNum))
                .toList();
        Date startTime = User.getReservation().getStartTime();
        Date endTime = User.getReservation().getEndTime();
        boolean isAddSuccess = false;

        String request = "addReservation " + String.join(",", members) + " " + cafeNum + " "
                + String.join(",", seatNums) + " " + startTime.getTime() + " " + endTime.getTime();
        String response = "";

        System.out.println("Adding reservation");

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                // 予約情報をUserクラスに保存
                User.getReservation().setArrived(false);

                System.out.println("Reservation added successfully.");
                isAddSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to add reservation.");
                User.getReservation().clear();
                System.out.println("Cleared reservation data.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }

        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while adding reservation.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isAddSuccess;
    }

    /*
     * ユーザーIDを送信し、予約をキャンセルできたかどうかを返す
     * キャンセル成功時には、予約情報をUserクラスから削除
     * request -> "removeReservation userID1,userID2,..."
     * response -> "success" or "failure message"
     */
    public static boolean removeReservation() {
        List<String> members = User.getReservation().getMembers();
        boolean isRemoveSuccess = false;
        String request = "removeReservation " + String.join(",", members);
        String response = "";

        System.out.println("Removing reservation");

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                // 予約情報をUserクラスから削除
                User.getReservation().clear();

                System.out.println("Reservation removed successfully.");
                isRemoveSuccess = true;
            } else if (command.equals("failure")) {
                System.out.println("Failed to remove reservation.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while removing reservation.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return isRemoveSuccess;
    }

    /*
     * 学食番号と予約の開始時間と終了時間を送信し、その時間に空いている座席を取得する
     * request -> "fetchAvailableSeats startTime endTime"
     * response -> "success seatNum1,seatNum2,seatNum3..." or "failure message"
     */
    public static List<Integer> fetchAvailableSeats(int cafeNum, Date startTime, Date endTime) {
        List<Integer> availableSeats = new java.util.ArrayList<>();
        String request = "fetchAvailableSeats " + cafeNum + " " + startTime.getTime() + " " + endTime.getTime();
        String response = "";

        System.out.println("Fetching available seats");

        try {
            connect();

            out.println(request);
            System.out.println("Sent: " + request);

            response = in.readLine();
            String command = response.split(" ", 2)[0];
            String args = response.split(" ", 2).length > 1 ? response.split(" ", 2)[1] : "";
            System.out.println("Received: " + command + " " + args);

            if (command.equals("success")) {
                availableSeats = List.of(args.split(",")).stream().map(seatNum -> Integer.parseInt(seatNum)).toList();
                System.out.println("Available seats fetched successfully.");
            } else if (command.equals("failure")) {
                System.out.println("Failed to fetch available seats.");
                System.out.println("Response: " + args);
            } else {
                System.out.println("Invalid response: " + response);
            }
        } catch (ConnectionException e) {
        } catch (Exception e) {
            System.out.println("Error occurred while fetching available seats.");
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return availableSeats;
    }

}

// 接続エラー時の例外
class ConnectionException extends RuntimeException {
    public ConnectionException(String message) {
        super(message);
    }
}
