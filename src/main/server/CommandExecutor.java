package main.server;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import main.util.MyUUID;
import properties.PropertyUtil;

interface CommandExecutor {
    // 共通のDB処理用の定数
    static final String DB_NAME = PropertyUtil.getProperty("db_name"); // データベースの名前
    static final String DB_URL = PropertyUtil.getProperty("db_url") + DB_NAME; // データベースのURL
    static final String DB_USER = PropertyUtil.getProperty("db_user"); // データベースのユーザー名
    static final String DB_PASS = PropertyUtil.getProperty("db_pass"); // データベースのパスワード

    // 各コマンドに対応した処理を実行するメソッド
    void execute(PrintWriter out, String args);
}

/*
 * ユーザーの登録処理
 * request -> "signUp userName password studentID"
 * response -> "success userID" or "failure message"
 */
class SignUpExecutor implements CommandExecutor {
    private static final String CHECK_USER_QUERY = "SELECT * FROM users WHERE username = ?";
    private static final String INSERT_USER_QUERY = "INSERT INTO users (user_id, username, password, student_id) VALUES (?, ?, ?, ?)";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Sign up executor called.");

        // 受け取った引数をスペースで分割し、ユーザー名とパスワードを取得
        String userID = MyUUID.getUUID();
        String name = args.split(" ")[0];
        String password = args.split(" ")[1];
        int studentID = Integer.parseInt(args.split(" ")[2]);

        if (!args.matches("^[a-zA-Z0-9 ]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement checkStatement = null, insertStatement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); // データベースに接続
            System.out.println("Connected to DB.");

            // ユーザー名がすでに存在するかどうかを確認 (ユーザーネームにnameを使用しているユーザーの数をカウントするクエリ)
            checkStatement = connection.prepareStatement(CHECK_USER_QUERY);
            checkStatement.setString(1, name);
            resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                GeneralMethods.outAndPrint(out, "failure Username already exists.", "");
                return;
            }

            insertStatement = connection.prepareStatement(INSERT_USER_QUERY);
            insertStatement.setString(1, userID); // ユーザーネームに重複がなければユーザーを登録
            insertStatement.setString(2, name);
            insertStatement.setString(3, password);
            insertStatement.setInt(4, studentID);
            insertStatement.executeUpdate();
            GeneralMethods.outAndPrint(out, "success " + userID, " " + name + " registered successfully.");
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to register user " + name, "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, null, null, checkStatement, insertStatement);
            System.out.println();
        }
    }
}

/*
 * ログイン処理
 * request -> "login userName password"
 * response -> "success userID studentID" or "failure message"
 */
class LoginExecutor implements CommandExecutor {
    private static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE username = ? AND password = ?";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Login executor called.");

        // 受け取った引数をスペースで分割し、ユーザー名とパスワードを取得
        String name = args.split(" ")[0];
        String password = args.split(" ")[1];
        if (!args.matches("^[a-zA-Z0-9 ]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            // ユーザー名とパスワードが一致するか確認
            statement = connection.prepareStatement(SELECT_USER_QUERY);
            statement.setString(1, name);
            statement.setString(2, password);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                GeneralMethods.outAndPrint(out,
                        "success " + resultSet.getString("user_id") + " " + resultSet.getInt("student_id"),
                        " logged in successfully");
            } else {
                GeneralMethods.outAndPrint(out, "failure Invalid username or password.", "");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to login user " + name, "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, null, null, null, statement);
            System.out.println();
        }

    }
}

/*
 * 友達リスト取得処理
 * request -> "fetchFriend userID"
 * response -> "success userID1:userName1 userID2:userName2 ..." or
 * "failure message"
 */
class FetchFriendExecutor implements CommandExecutor {
    static final String SELECT_FRIEND_QUERY = "SELECT * FROM friends WHERE user_id1 = ? OR user_id2 = ?";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch friend executor called.");

        String thisUserID = args;
        if (!args.matches("^[a-zA-Z0-9-_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, resultSet2 = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_FRIEND_QUERY);
            statement.setString(1, thisUserID); // このユーザが含まれるデータを探す
            statement.setString(2, thisUserID);
            resultSet = statement.executeQuery();

            String user1, user2, friendName = "", friendList = "";
            if (resultSet.next()) {
                do {
                    user1 = resultSet.getString("user_id1");
                    user2 = resultSet.getString("user_id2");
                    statement = connection.prepareStatement(SELECT_USER_QUERY);
                    if (user1.equals(thisUserID)) {
                        statement.setString(1, user2); // このユーザでない方（友達）のユーザ名を取得する
                        friendList = friendList + " " + user2; // 返送するfriendListに友達のユーザIDを追加
                    } else if (user2.equals(thisUserID)) {
                        statement.setString(1, user1);
                        friendList = friendList + " " + user1;
                    }
                    resultSet2 = statement.executeQuery();
                    resultSet2.next();
                    friendName = resultSet2.getString("username");
                    friendList = friendList + ":" + friendName; // friendListに友達のユーザ名を追加
                } while (resultSet.next());
                GeneralMethods.outAndPrint(out, "success" + friendList, " fetched successfully");
            } else {
                GeneralMethods.outAndPrint(out, "failure noData",
                        " An error occurred or this user has no reservations currently.");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to fetch friend.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, resultSet2, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 友達追加処理
 * request -> "addFriend userID friendID"
 * response -> "success friendID:friendName" or "failure message"
 */
class AddFriendExecutor implements CommandExecutor {
    static final String ADD_FRIEND_QUERY = "INSERT INTO friends (user_id1, user_id2) VALUES (?, ?)";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Add friend executor called.");

        String userID = args.split(" ")[0]; // 引数をスペースで分割し、ユーザーIDと追加する友達のIDを取得
        String friendID = args.split(" ")[1];
        if (!args.matches("^[a-zA-Z0-9 -_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, userResult = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(ADD_FRIEND_QUERY); // このフレンド関係をfriendsテーブルに追加
            statement.setString(1, userID);
            statement.setString(2, friendID);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                statement = connection.prepareStatement(SELECT_USER_QUERY);
                statement.setString(1, friendID); // ユーザ名を得るためにユーザ情報を取得
                userResult = statement.executeQuery();
                userResult.next();

                GeneralMethods.outAndPrint(out, "success " + friendID + ":" + userResult.getString("username"),
                        " added successfully");
            } else {
                GeneralMethods.outAndPrint(out, "failure Failed to add friend.", "");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to add friend.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, userResult, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 友達削除処理
 * request -> "removeFriend userID friendID"
 * response -> "success " or "failure message"
 */
class RemoveFriendExecutor implements CommandExecutor {
    static final String REMOVE_FRIEND_QUERY = "DELETE FROM friends WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Remove friend executor called.");

        String userID = args.split(" ")[0]; // argsからユーザIDとフレンドIDを得る
        String friendID = args.split(" ")[1];
        if (!args.matches("^[a-zA-Z0-9 -_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(REMOVE_FRIEND_QUERY);
            statement.setString(1, userID);
            statement.setString(2, friendID);
            statement.setString(3, friendID);
            statement.setString(4, userID);
            int rowsAffected = statement.executeUpdate(); // このフレンド関係を削除する

            if (rowsAffected > 0) {
                GeneralMethods.outAndPrint(out, "success ", " Friend removed successfully.");
            } else {
                GeneralMethods.outAndPrint(out, "failure Failed to remove friend.", "");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to remove friend.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, null, null, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 予約情報取得処理
 * request -> "fetchReservation userID"
 * response ->
 * "success userID:userName,userID:userName,... cafeNum seatNum startTime endTime went"
 */
class FetchReservationExecutor implements CommandExecutor {
    static final String SELECT_RESERVATION_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    static final String SELECT_MEMBERS_QUERY = "SELECT * FROM reservations WHERE reservation_id = ?";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch reservation executor called.");

        String userID = args;
        if (!args.matches("^[a-zA-Z0-9-_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, membersResultSet = null, userInfoResultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setString(1, userID); // このユーザが含まれるデータを探す
            resultSet = statement.executeQuery();

            String reservationInfo = "";
            if (resultSet.next()) {
                LocalDateTime currentTime = LocalDateTime.now();
                if (!resultSet.getTimestamp("end_time").toLocalDateTime().plusMinutes(6).isAfter(currentTime)) {
                    if (!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out)) // 過去の予約なら履歴に移動させて終了
                        return; // 移動に失敗したらreturn
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) { // 予約がもうない場合
                        GeneralMethods.outAndPrint(out, "failure noData",
                                " An error occurred or this user has no reservations currently.");
                        return;
                    }
                }
                statement = connection.prepareStatement(SELECT_USER_QUERY);
                statement.setString(1, resultSet.getString("user_id")); // このユーザのIDと名前をreservationInfoに格納
                userInfoResultSet = statement.executeQuery();
                userInfoResultSet.next();
                reservationInfo = resultSet.getString("user_id") + ":" + userInfoResultSet.getString("username");

                statement = connection.prepareStatement(SELECT_MEMBERS_QUERY);
                statement.setString(1, resultSet.getString("reservation_id")); // 予約IDが一致する（＝一緒に予約した）メンバの予約を探す
                membersResultSet = statement.executeQuery();

                String membersSeatNums = "";
                while (membersResultSet.next()) { // メンバのユーザIDと名前をカンマで繋げて格納
                    statement = connection.prepareStatement(SELECT_USER_QUERY);
                    statement.setString(1, membersResultSet.getString("user_id")); // ユーザ名を得るためにユーザ情報を取得
                    userInfoResultSet = statement.executeQuery();
                    userInfoResultSet.next();

                    if (!membersResultSet.getString("user_id").equals(userID)) { // 二重に追加しないように、リクエストを送信したユーザでない時のみ追加
                        reservationInfo = reservationInfo + "," + membersResultSet.getString("user_id") + ":"
                                + userInfoResultSet.getString("username");
                        membersSeatNums = membersSeatNums + "," + membersResultSet.getString("seat_num");
                    }
                }
                LocalDateTime localStartTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime localEndTime = resultSet.getTimestamp("end_time").toLocalDateTime();

                // Stringに変換しつつ残りの予約情報をすべて取得
                reservationInfo = reservationInfo + " " + Integer.toString(resultSet.getInt("cafe_num")) + " "
                        + Integer.toString(resultSet.getInt("seat_num")) + membersSeatNums + " "
                        + localStartTime.format(formatter)
                        + " " + localEndTime.format(formatter) + " " + resultSet.getBoolean("arrived");

                GeneralMethods.outAndPrint(out, "success " + reservationInfo, " fetched successfully");
            } else {
                GeneralMethods.outAndPrint(out, "failure noData",
                        " An error occurred or this user has no reservations currently.");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to fetch reservations.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, membersResultSet, userInfoResultSet, null, statement);
            System.out.println();
        }
    }
}

/*
 * 予約追加処理
 * request ->
 * "addReservation userID1,userID2,... cafeNum seatNum startTime endTime"
 * response -> "success " or "failure message"
 * メンバーの誰かが既に予約をしている時 -> "failure userID:username seatNum"
 * 座席が空いていない時 -> "failure seatNum is not available"
 */
class AddReservationExecutor implements CommandExecutor {
    static final String ADD_RESERVATION_QUERY = "INSERT INTO reservations (user_id, reservation_id, cafe_num, seat_num, start_time, end_time, arrived) VALUES (?, ?, ?, ?, ? , ?, ?)";
    static final String SELECT_RESERVATION_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    static final String CHECK_AVAILABLE_QUERY = "SELECT * FROM reservations WHERE cafe_num = ? AND seat_num = ?";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Add reservation executor called.");

        int membersNum = args.split(" ")[0].split(",").length; // 予約人数を取得
        List<String> userIDs = new ArrayList<>();
        List<Integer> seatNums = new ArrayList<>();

        for (int i = 0; i < membersNum; i++) { // メンバ全員のユーザIDと席番号を配列に追加
            userIDs.add(args.split(" ")[0].split(",")[i]);
            seatNums.add(Integer.parseInt(args.split(" ")[2].split(",")[i]));
        }
        String reservationID = MyUUID.getUUID(); // 予約IDを設定
        int cafeNum = Integer.parseInt(args.split(" ")[1]); // その他の情報を取得
        LocalDateTime startTime = LocalDateTime.parse(args.split(" ")[3], formatter);
        LocalDateTime endTime = LocalDateTime.parse(args.split(" ")[4], formatter);
        boolean arrived = false;

        if (!args.matches("^[a-zA-Z0-9 ,-_:]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, userResult = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            for (int i = 0; i < membersNum; i++) { // メンバの内、一人でも予約やペナルティが無いかを確認
                statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
                statement.setString(1, userIDs.get(i)); // メンバのユーザIDでの予約を探す
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    LocalDateTime currentTime = LocalDateTime.now(); // 過去の予約かどうかを判別
                    LocalDateTime arrivalDeadline = resultSet.getTimestamp("end_time").toLocalDateTime().plusMinutes(6);
                    if (arrivalDeadline.isAfter(currentTime) || !resultSet.getBoolean("arrived")) { // 終了していない予約があるならこの予約を拒否
                        int seatNum = resultSet.getInt("seat_num");
                        if (currentTime.isAfter(arrivalDeadline) && !resultSet.getBoolean("arrived")) {
                            seatNum = -1; // 行かなかった過去の予約があるならペナルティを付与してその予約を履歴に移行
                            if (!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out))
                                return; // 移動に失敗したらreturn
                        }
                        statement = connection.prepareStatement(SELECT_USER_QUERY);
                        statement.setString(1, userIDs.get(i));
                        userResult = statement.executeQuery();
                        userResult.next();

                        GeneralMethods.outAndPrint(out,
                                "failure " + userIDs.get(i) + ":" + userResult.getString("username") + " " + seatNum,
                                " already has a reservation.");
                        return;
                    } else {
                        if (!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out)) // 過去の予約なら履歴テーブルに移動させ、現在の予約を続行
                            return; // 移動に失敗したらreturn
                    }
                }
            }

            synchronized (this) {
                for (int i = 0; i < seatNums.size(); i++) { // 予約が重複しないように空席であることを確認
                    statement = connection.prepareStatement(CHECK_AVAILABLE_QUERY);
                    statement.setInt(1, cafeNum);
                    statement.setInt(2, seatNums.get(i));
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        GeneralMethods.outAndPrint(out, "failure seatNum: " + seatNums.get(i) + " is not available", "");
                        return;
                    }
                }

                for (int i = 0; i < membersNum; i++) { // メンバごとにreservationsテーブルに予約を保存
                    statement = connection.prepareStatement(ADD_RESERVATION_QUERY);
                    statement.setString(1, userIDs.get(i));
                    statement.setString(2, reservationID);
                    statement.setInt(3, cafeNum);
                    statement.setInt(4, seatNums.get(i));
                    statement.setTimestamp(5, Timestamp.valueOf(startTime));
                    statement.setTimestamp(6, Timestamp.valueOf(endTime));
                    statement.setBoolean(7, arrived);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected <= 0) {
                        GeneralMethods.outAndPrint(out, "failure Failed to add reservation.", "");
                        return;
                    }
                }
            }
            GeneralMethods.outAndPrint(out, "success ", " Added reservation successfully."); // 全メンバの予約を正常に保存できたらクライアントに通知
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to add reservation.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, userResult, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 予約キャンセル処理
 * request -> "removeReservation userID1,userID2,..."
 * response -> "success " or "failure message"
 */
class RemoveReservationExecutor implements CommandExecutor {
    String REMOVE_RESERVATION_QUERY = "DELETE FROM reservations WHERE user_id = ?";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Remove reservation executor called.");

        int membersNum = args.split(",").length; // キャンセルする人数を取得
        List<String> userIDs = new ArrayList<>();

        for (int i = 0; i < membersNum; i++) { // メンバ全員のユーザIDを配列に追加
            userIDs.add(args.split(" ")[0].split(",")[i]);
        }
        if (!args.matches("^[a-zA-Z0-9,-_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            for (int i = 0; i < membersNum; i++) { // メンバごとに予約を削除
                statement = connection.prepareStatement(REMOVE_RESERVATION_QUERY);
                statement.setString(1, userIDs.get(i));
                int rowsAffected = statement.executeUpdate(); // 予約を削除

                if (rowsAffected <= 0) {
                    GeneralMethods.outAndPrint(out, "failure Failed to remove reservations.", "");
                }
            }
            GeneralMethods.outAndPrint(out, "success ", " Removed reservation successfully."); // すべて削除できたら通知
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to remove reservations.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, null, null, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 座席空き情報取得処理
 * request -> "fetchAvailableSeats cafeNum startTime endTime"
 * response -> "success seatNum1,seatNum2,..." or "failure message"
 */
class FetchAvailableSeatsExecutor implements CommandExecutor {
    static final String SELECT_RESERVATION_QUERY = "SELECT * FROM reservations WHERE cafe_num = ? AND start_time = ?";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    static final int MAX_SEATS_1 = 54, MAX_SEATS_2 = 60; // 第一、第二食堂の最大座席数

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch Available seats executor called.");

        int cafeNum = Integer.parseInt(args.split(" ")[0]); // 引数から食堂と時間を取得
        LocalDateTime startTime = LocalDateTime.parse(args.split(" ")[1], formatter);

        if (!args.matches("^[a-zA-Z0-9 -_:]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            List<Integer> availableSeats = new ArrayList<>(); // 空席を保存する配列を初期化
            if (cafeNum == 1) {
                for (int i = 1; i <= MAX_SEATS_1; i++) {
                    availableSeats.add(i);
                }
            } else if (cafeNum == 2) {
                for (int i = 1; i <= MAX_SEATS_2; i++) {
                    availableSeats.add(i);
                }
            }

            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setInt(1, cafeNum); // 指定された食堂・時間の予約を探す
            statement.setTimestamp(2, Timestamp.valueOf(startTime));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                availableSeats.remove(Integer.valueOf(resultSet.getInt("seat_num")));
            }

            String availableSeat = "";
            for (int i = 0; i < availableSeats.size(); i++) {
                if (i > 0) {
                    availableSeat = availableSeat + ",";
                }
                availableSeat = availableSeat + availableSeats.get(i).toString();
            }
            GeneralMethods.outAndPrint(out, "success " + availableSeat, " fetched successfully.");
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to fetch available seats.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, null, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 位置情報から学食にきたかどうかを更新する処理
 * request -> "updateArrived userID cafeNum"
 * response -> "success " or "failure message"
 */
class UpdateArrivedExecutor implements CommandExecutor {
    static final String SELECT_RESERVATION_ID_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    static final String UPDATE_ARRIVED_QUERY = "UPDATE reservations SET arrived = ? WHERE user_id = ?";
    static final String SELECT_RESERVATION_END_TIME_QUERY = "SELECT * FROM reservations WHERE end_time < ?";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Update arrival executor called.");

        String userID = args.split(" ")[0];

        if (!args.matches("^[a-zA-Z0-9 -_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement_1 = null, statement_2 = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement_1 = connection.prepareStatement(SELECT_RESERVATION_ID_QUERY);
            statement_1.setString(1, userID);
            resultSet = statement_1.executeQuery();

            if (resultSet.next()) {
                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                if (startTime.plusMinutes(26).isAfter(currentTime) && currentTime.isAfter(startTime.minusMinutes(5))) {
                } else { // 現在、到着報告可能な時間か確認
                    GeneralMethods.outAndPrint(out, "failure This reservation cannot be updated now", "");
                    return;
                }
            } else {
                GeneralMethods.outAndPrint(out, "failure Failed to update arrived.", "");
                return;
            }

            statement_2 = connection.prepareStatement(UPDATE_ARRIVED_QUERY);
            statement_2.setBoolean(1, true); // 指定されたユーザの予約のarrivedを更新
            statement_2.setString(2, userID);
            int rowsUpdated = statement_2.executeUpdate();

            resultSet = statement_1.executeQuery(); // 更新した後のデータを取得
            resultSet.next();
            if (!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out)) { // 予約を履歴に移動
                return;
            }

            if (rowsUpdated > 0) {
                GeneralMethods.outAndPrint(out, "success ", " Updated 'arrived' successfully.");
            } else {
                GeneralMethods.outAndPrint(out, "failure Failed to update arrived.", "");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to update arrived.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, null, null, statement_1, statement_2);
            System.out.println();
        }
    }

    static void checkArrival() { // 予約終了時間を過ぎた予約の到着を確認する
        System.out.println("Check Arrival called");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_RESERVATION_END_TIME_QUERY);
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); // 終了している予約を探す
            resultSet = statement.executeQuery();

            PrintWriter stdOut = new PrintWriter(System.out);
            while (resultSet.next()) {
                if (!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, stdOut)) // 予約を履歴テーブルに移動させる
                    return; // 移動に失敗したらreturn
                resultSet = statement.executeQuery(); // 二重に履歴に追加しないようにresultSetをリセット
            }
            System.out.println("success Check arrival done.");
        } catch (SQLException e) {
            System.out.println("failure Failed to check arrival.");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, null, null, null, statement);
            System.out.println();
        }
    }
}

/*
 * 予約履歴取得処理
 * request -> "fetchReservationHistory userID"
 * response ->
 * "success membersID:membersName,membersID:membersName... cafeNum seatNum startTime endTime went,next membersID:membersName,membersID:membersName..."
 * or "failure message"
 * （members は自分を含まない、一緒に予約した人、nextは予約の区切り）
 */
class FetchReservationHistoryExecutor implements CommandExecutor {
    static final String EXPORT_RESERVATION_QUERY = "INSERT INTO reserv_history (user_id, members, cafe_num, seat_num, start_time, end_time, arrived) VALUES (?, ?, ?, ?, ? , ?, ?)";
    static final String ADD_RESERVATION_QUERY = "INSERT INTO reservations (user_id, reservation_id, cafe_num, seat_num, start_time, end_time, arrived) VALUES (?, ?, ?, ?, ? , ?, ?)";
    static final String SELECT_RESERVATION_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    static final String SELECT_RESERV_HISTORY_QUERY = "SELECT * FROM reserv_history WHERE user_id = ?";
    static final String REMOVE_RESERVATION_QUERY = "DELETE FROM reservations WHERE user_id = ?";
    static final String SELECT_MEMBERS_QUERY = "SELECT * FROM reservations WHERE reservation_id = ?";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch reservation history executor called.");

        String userID = args;
        if (!args.matches("^[a-zA-Z0-9-_]+$")) { // 引数に不正な文字が含まれていないかをチェック
            GeneralMethods.outAndPrint(out, "failure Requests should contain only alphanumeric characters.", "");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, historyResultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setString(1, userID); // このユーザが含まれる予約を探す
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                LocalDateTime currentTime = LocalDateTime.now(); // 過去の予約がある場合は履歴に移動
                if (!resultSet.getTimestamp("end_time").toLocalDateTime().plusMinutes(6).isAfter(currentTime)) {
                    if (!exportToHistory(resultSet, connection, out))
                        return;
                }
            }

            statement = connection.prepareStatement(SELECT_RESERV_HISTORY_QUERY);
            statement.setString(1, userID); // このユーザが含まれる予約履歴を探す
            historyResultSet = statement.executeQuery();
            if (historyResultSet.next()) {
                String reservHistory = "";
                do {
                    if (!reservHistory.equals("")) { // 先頭でなければカンマを追加
                        reservHistory = reservHistory + ",next";
                    }
                    LocalDateTime localStartTime = historyResultSet.getTimestamp("start_time").toLocalDateTime();
                    LocalDateTime localEndTime = historyResultSet.getTimestamp("end_time").toLocalDateTime();

                    reservHistory = reservHistory + " " + historyResultSet.getString("members") + " " // 予約履歴の詳細を格納
                            + Integer.toString(historyResultSet.getInt("cafe_num")) + " "
                            + Integer.toString(historyResultSet.getInt("seat_num")) + " "
                            + localStartTime.format(formatter)
                            + " " + localEndTime.format(formatter) + " " + historyResultSet.getBoolean("arrived");
                } while (historyResultSet.next());

                out.println("success " + reservHistory);
                System.out.println("success '" + reservHistory + "' fetched successfully");
            } else {
                GeneralMethods.outAndPrint(out, "failure noData",
                        " An Error ocurred or this user has no reservations history.");
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to fetch reservations.", "");
            e.printStackTrace();
        } finally {
            GeneralMethods.closeAll(connection, resultSet, historyResultSet, null, null, statement);
            System.out.println();
        }
    }

    // 予約を履歴に移動させる処理
    static boolean exportToHistory(ResultSet resultSet, Connection connection, PrintWriter out) {
        PreparedStatement statement = null, usersStatement = null;
        ResultSet membersResultSet = null, userInfoResultSet = null;
        try {
            statement = connection.prepareStatement(SELECT_MEMBERS_QUERY);
            statement.setString(1, resultSet.getString("reservation_id")); // 予約IDが一致するメンバの予約を探す
            membersResultSet = statement.executeQuery();

            String membersInfo = "";
            while (membersResultSet.next()) { // メンバのユーザIDと名前をカンマで繋げて格納
                usersStatement = connection.prepareStatement(SELECT_USER_QUERY);
                usersStatement.setString(1, membersResultSet.getString("user_id")); // ユーザ名を得るためにユーザ情報を取得
                userInfoResultSet = usersStatement.executeQuery();
                userInfoResultSet.next();

                if (!membersInfo.equals("")) {
                    membersInfo = membersInfo + ",";
                }
                membersInfo = membersInfo + membersResultSet.getString("user_id") + ":"
                        + userInfoResultSet.getString("username");
            }
            membersResultSet = statement.executeQuery();

            String membersWithoutUser = "";
            while (membersResultSet.next()) { // リクエストを送信したユーザを含め、全員の予約を履歴に移動させ、予約テーブルからは削除
                statement = connection.prepareStatement(REMOVE_RESERVATION_QUERY);
                statement.setString(1, membersResultSet.getString("user_id"));
                int rowsAffected = statement.executeUpdate(); // 予約テーブルから削除

                if (!membersResultSet.getBoolean("arrived") && membersResultSet.getInt("seat_num") != -1) {
                    if (!createPenaltyReservation(membersResultSet, connection, out))
                        return false; // 失敗したらreturn
                }

                membersWithoutUser = membersInfo;
                String entryToRemove = "^" + membersResultSet.getString("user_id") + "[^,]*,";
                membersWithoutUser = membersWithoutUser.replaceFirst(entryToRemove, ""); // メンバ一覧から本人を削除
                entryToRemove = "," + membersResultSet.getString("user_id") + ":[^,]*";
                membersWithoutUser = membersWithoutUser.replaceFirst(entryToRemove, "");

                statement = connection.prepareStatement(EXPORT_RESERVATION_QUERY);
                statement.setString(1, membersResultSet.getString("user_id"));
                statement.setString(2, membersWithoutUser);
                statement.setInt(3, membersResultSet.getInt("cafe_num"));
                statement.setInt(4, membersResultSet.getInt("seat_num"));
                statement.setTimestamp(5, membersResultSet.getTimestamp("start_time"));
                statement.setTimestamp(6, membersResultSet.getTimestamp("end_time"));
                statement.setBoolean(7, membersResultSet.getBoolean("arrived"));
                int rowsAffected_2 = statement.executeUpdate();

                if (rowsAffected == 0 || rowsAffected_2 == 0) {
                    GeneralMethods.outAndPrint(out, "failure Failed to export reservation info.", "");
                    return false;
                }
            }
            System.out.println("success Exported reservation info successfully.");
            return true;
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to export reservation info.", "");
            e.printStackTrace();
            return false;
        } finally {
            GeneralMethods.closeAll(null, membersResultSet, userInfoResultSet, null, statement, usersStatement);
        }
    }

    // ペナルティを与える予約を作成する処理
    static boolean createPenaltyReservation(ResultSet resultSet, Connection connection, PrintWriter out) {
        PreparedStatement statement = null;
        String reservationID = MyUUID.getUUID();
        try {
            statement = connection.prepareStatement(ADD_RESERVATION_QUERY);
            statement.setString(1, resultSet.getString("user_id"));
            statement.setString(2, reservationID);
            statement.setInt(3, resultSet.getInt("cafe_num"));
            statement.setInt(4, -1);
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().plusWeeks(2)));
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().plusWeeks(2).plusMinutes(20)));
            statement.setBoolean(7, false);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("success Gave penalty successfully.");
                return true;
            } else {
                GeneralMethods.outAndPrint(out, "failure Failed to give penalty.", "");
                return false;
            }
        } catch (SQLException e) {
            GeneralMethods.outAndPrint(out, "failure Failed to give penalty.", "");
            e.printStackTrace();
            return false;
        } finally {
            GeneralMethods.closeAll(null, null, null, null, null, statement);
        }
    }
}

class GeneralMethods {
    // リソースをクローズする処理
    static void closeAll(Connection cnt, ResultSet rst1, ResultSet rst2, ResultSet rst3, PreparedStatement stmt1,
            PreparedStatement stmt2) {
        try {
            if (cnt != null) {
                cnt.close();
            }
            if (rst1 != null) {
                rst1.close();
            }
            if (rst2 != null) {
                rst2.close();
            }
            if (rst3 != null) {
                rst3.close();
            }
            if (stmt1 != null) {
                stmt1.close();
            }
            if (stmt2 != null) {
                stmt2.close();
            }
        } catch (SQLException e) {
            System.out.println("failure Failed to close connection.");
            e.printStackTrace();
        }
    }

    // クライアントにメッセージを送信し、コンソールにも出力する処理
    static void outAndPrint(PrintWriter out, String message, String onlyPrintMsg) {
        out.println(message);
        System.out.println(message + onlyPrintMsg);
    }
}