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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * response -> "Success userID" or "failure message"
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
        System.out.println(
                "userID: " + userID + ", username: " + name + ", password: " + password + ", studentID: " + studentID);

        // ユーザー名とパスワードが半角英数字以外の文字を含むかをチェック
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher nameMatcher = pattern.matcher(name);
        Matcher passwordMatcher = pattern.matcher(password);

        if (nameMatcher.find() || passwordMatcher.find()) {
            out.println("failure Username and password should contain only alphanumeric characters.");
            System.out.println("failure Username and password should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement checkStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet resultSet = null;

        try {
            // データベースに接続
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            // ユーザー名がすでに存在するかどうかを確認 (ユーザーネームにnameを使用しているユーザーの数をカウントするクエリ)
            checkStatement = connection.prepareStatement(CHECK_USER_QUERY);
            checkStatement.setString(1, name);
            resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                out.println("failure Username already exists.");
                System.out.println("failure Username already exists.");
                return;
            }

            // ユーザーネームに重複がなければユーザーを登録
            insertStatement = connection.prepareStatement(INSERT_USER_QUERY);
            insertStatement.setString(1, userID);
            insertStatement.setString(2, name);
            insertStatement.setString(3, password);
            insertStatement.setInt(4, studentID);
            insertStatement.executeUpdate();
            out.println("success " + userID);
            System.out.println("User " + name + " registered successfully.");

        } catch (SQLException e) {
            out.println("failure Failed to register user " + name);
            System.out.println("failure Failed to register user " + name);
            e.printStackTrace();

        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (checkStatement != null) {
                    checkStatement.close();
                }
                if (insertStatement != null) {
                    insertStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
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
        System.out.println("username: " + name);
        System.out.println("password: " + password);

        // ユーザー名とパスワードが半角英数字以外の文字を含むかをチェック
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher nameMatcher = pattern.matcher(name);
        Matcher passwordMatcher = pattern.matcher(password);

        if (nameMatcher.find() || passwordMatcher.find()) {
            out.println("failure Username and password should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        // データベースに接続
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            // ユーザー名とパスワードが一致するか確認
            statement = connection.prepareStatement(SELECT_USER_QUERY);
            statement.setString(1, name);
            statement.setString(2, password);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                out.println("success " + resultSet.getString("user_id") + " " + resultSet.getInt("student_id"));
                System.out.println("User " + name + " logged in successfully");
            } else {
                out.println("failure Invalid username or password.");
                System.out.println("failure Invalid username or password.");
            }

        } catch (SQLException e) {
            out.println("failure Failed to login user " + name);
            System.out.println("failure Failed to login user " + name);
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
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

        String thisUserID= args;

        String filteredArgs= args.replaceAll("[" + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, resultSet2= null;
        try{
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_FRIEND_QUERY);
            statement.setString(1, thisUserID);     //このユーザが含まれるデータを探す
            statement.setString(2, thisUserID);
            resultSet = statement.executeQuery();

            String user1, user2, friendName= "", friendList= "";
            if (resultSet.next()) {
                do {
                    user1= resultSet.getString("user_id1");
                    user2= resultSet.getString("user_id2");
                    statement = connection.prepareStatement(SELECT_USER_QUERY);
                    if(user1.equals(thisUserID)){
                        statement.setString(1, user2);      //このユーザでない方（友達）のユーザ名を取得する
                        friendList= friendList + " " + user2;       //返送するfriendListに友達のユーザIDを追加
                    }else if(user2.equals(thisUserID)){
                        statement.setString(1, user1);
                        friendList= friendList + " " + user1;
                    }
                    resultSet2 = statement.executeQuery();
                    resultSet2.next();
                    friendName= resultSet2.getString("username");
                    friendList= friendList + ":" + friendName;       //friendListに友達のユーザ名を追加
                } while (resultSet.next());
                out.println("success" + friendList);
                System.out.println("FriendList:'" + friendList + "' fetched successfully");
            }else {
                out.println("success "); 
                System.out.println("success This user has no friends currently.");      /*エラーと友達いないのを区別できない*/
            }
        } catch (SQLException e) {
            out.println("failure Failed to fetch friend.");  
            System.out.println("failure Failed to fetch friend.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (resultSet2 != null) {
                    resultSet2.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
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

        String userID = args.split(" ")[0];    //引数をスペースで分割し、ユーザーIDと追加する友達のIDを取得
        String friendID = args.split(" ")[1];

        String filteredArgs= args.replaceAll("[" + " " + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, userResult = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);     //データベースに接続
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(ADD_FRIEND_QUERY);   //このフレンド関係をfriendsテーブルに追加
            statement.setString(1, userID);
            statement.setString(2, friendID);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                statement = connection.prepareStatement(SELECT_USER_QUERY);
                statement.setString(1, friendID);     //ユーザ名を得るためにユーザ情報を取得
                userResult = statement.executeQuery();
                userResult.next();

                out.println("success " + friendID + ":" + userResult.getString("username")); 
                System.out.println("success '" + friendID +":"+ userResult.getString("username")+ "' added successfully"); 
            } else {
                out.println("failure Failed to add friend.");
                System.out.println("failure Failed to add friend.");
            }
        } catch (SQLException e) {
            out.println("failure Failed to add friend.");
            System.out.println("failure Failed to add friend.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (userResult != null) {
                    userResult.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

/*
 * 友達削除処理
 * request -> "removeFriend userID friendID"
 * response -> "success" or "failure message"
 */
class RemoveFriendExecutor implements CommandExecutor {
    static final String REMOVE_FRIEND_QUERY = "DELETE FROM friends WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Remove friend executor called.");

        String userID = args.split(" ")[0];     //argsからユーザIDとフレンドIDを得る
        String friendID = args.split(" ")[1];

        String filteredArgs= args.replaceAll("[" + " " + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); //データベースに接続
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(REMOVE_FRIEND_QUERY);
            statement.setString(1, userID);
            statement.setString(2, friendID);
            statement.setString(3, friendID);
            statement.setString(4, userID);
            int rowsAffected = statement.executeUpdate(); //このフレンド関係を削除する

            if (rowsAffected > 0) {
                out.println("success ");
                System.out.println("success Friend removed successfully.");
            } else {
                out.println("failure Failed to remove friend.");
                System.out.println("failure Failed to remove friend.");
            }
        } catch (SQLException e) {
            out.println("failure Failed to remove friend.");
            System.out.println("failure Failed to remove friend.");
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
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

        String userID= args;

        String filteredArgs= args.replaceAll("[" + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, membersResultSet = null, userInfoResultSet= null;
        try{
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setString(1, userID);     //このユーザが含まれるデータを探す
            resultSet = statement.executeQuery();

            String reservationInfo = "";
            if (resultSet.next()) {                
                LocalDateTime currentTime = LocalDateTime.now();       //過去の予約なら履歴に移動させて終了
                if(!resultSet.getTimestamp("end_time").toLocalDateTime().isAfter(currentTime)){
                    if(!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out))
                            return;     //移動に失敗したらreturn
                    out.println("success");
                    System.out.println("success This user has no reservations currently."); 
                    return;
                }
                statement = connection.prepareStatement(SELECT_USER_QUERY);
                statement.setString(1, resultSet.getString("user_id"));     //このユーザのIDと名前をreservationInfoに格納
                userInfoResultSet = statement.executeQuery();
                userInfoResultSet.next();
                reservationInfo= resultSet.getString("user_id") + ":" + userInfoResultSet.getString("username");

                statement = connection.prepareStatement(SELECT_MEMBERS_QUERY);
                statement.setString(1, resultSet.getString("reservation_id"));     //予約IDが一致する（＝一緒に予約した）メンバの予約を探す
                membersResultSet = statement.executeQuery();
                
                String membersSeatNums= "";
                while(membersResultSet.next()){         //メンバのユーザIDと名前をカンマで繋げて格納
                    statement = connection.prepareStatement(SELECT_USER_QUERY);
                    statement.setString(1, membersResultSet.getString("user_id"));     //ユーザ名を得るためにユーザ情報を取得
                    userInfoResultSet = statement.executeQuery();
                    userInfoResultSet.next();

                    if(!membersResultSet.getString("user_id").equals(userID)){    //二重に追加しないように、リクエストを送信したユーザでない時のみ追加
                        reservationInfo = reservationInfo + "," + membersResultSet.getString("user_id") + ":" + userInfoResultSet.getString("username");
                        membersSeatNums = membersSeatNums + "," + membersResultSet.getString("seat_num");
                    }
                }
                LocalDateTime localStartTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime localEndTime = resultSet.getTimestamp("end_time").toLocalDateTime();

                //Stringに変換しつつ残りの予約情報をすべて取得
                reservationInfo =  reservationInfo +" "+ Integer.toString(resultSet.getInt("cafe_num")) +" "
                                 + Integer.toString(resultSet.getInt("seat_num")) + membersSeatNums +" "+ localStartTime.format(formatter) 
                                 +" "+ localEndTime.format(formatter) +" "+ resultSet.getBoolean("arrived");

                out.println("success " + reservationInfo);
                System.out.println("success '" + reservationInfo + "' fetched successfully");
            }else {
                out.println("success");
                System.out.println("success This user has no reservations currently.");
            }
        } catch (SQLException e) {
            out.println("failure Failed to fetch reservations.");
            System.out.println("failure Failed to fetch reservations.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (membersResultSet != null) {
                    membersResultSet.close();
                }
                if (userInfoResultSet != null) {
                    userInfoResultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

/*
 * 予約追加処理
 * request ->
 * "addReservation userID1,userID2,... cafeNum seatNum startTime endTime"
 * response -> "success" or "failure message"
 * 既に予約がある時 -> "failure userID:username seatNum"
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

        int membersNum= args.split(" ")[0].split(",").length;      //予約人数を取得
        List<String> userIDs= new ArrayList<>();
        List<Integer> seatNums= new ArrayList<>();

        for(int i=0; i< membersNum; i++){       //メンバ全員のユーザIDと席番号を配列に追加
            userIDs.add(args.split(" ")[0].split(",")[i]);
            seatNums.add(Integer.parseInt(args.split(" ")[2].split(",")[i]));
        }
        String reservationID= MyUUID.getUUID();     //予約IDを設定
        int cafeNum = Integer.parseInt(args.split(" ")[1]);     //その他の情報を取得
        LocalDateTime startTime = LocalDateTime.parse(args.split(" ")[3], formatter);
        LocalDateTime endTime = LocalDateTime.parse(args.split(" ")[4], formatter);
        boolean arrived = false;

        String filteredArgs= args.replaceAll("[" + "," + " " + "-" + "_" + ":" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, userResult = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);     //データベースに接続
            System.out.println("Connected to DB.");

            for(int i=0; i< membersNum; i++){       //メンバの内、一人でも予約やペナルティが無いかを確認
                statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
                statement.setString(1, userIDs.get(i));    //メンバのユーザIDでの予約を探す
                resultSet = statement.executeQuery();

                if(resultSet.next()){
                    LocalDateTime currentTime = LocalDateTime.now();       //過去の予約かどうかを判別
                    if(resultSet.getTimestamp("end_time").toLocalDateTime().isAfter(currentTime)){
                        statement = connection.prepareStatement(SELECT_USER_QUERY);     //未来の予約ならこの予約を拒否
                        statement.setString(1, userIDs.get(i));
                        userResult = statement.executeQuery();
                        userResult.next();

                        out.println("failure "+ userIDs.get(i) +":"+ userResult.getString("username") +" "+ resultSet.getInt("seat_num"));
                        System.out.println("failure "+ userIDs.get(i) +":"+ userResult.getString("username") +" already has a reservation.");
                        return;
                    } else {
                        if(!FetchReservationHistoryExecutor.exportToHistory(resultSet, connection, out))     //過去の予約なら履歴テーブルに移動させ、現在の予約を続行
                            return;     //移動に失敗したらreturn
                    }
                }
            }

            for(int i=0; i<seatNums.size(); i++){   //予約が重複しないように空席であることを確認
                statement = connection.prepareStatement(CHECK_AVAILABLE_QUERY);
                statement.setInt(1, cafeNum);
                statement.setInt(2, seatNums.get(i));
                resultSet = statement.executeQuery();
                if(resultSet.next()){
                    out.println("failure seatNum: "+ seatNums.get(i) + " is not available");
                    System.out.println("failure seatNum: "+ seatNums.get(i) + " is not available");
                    return;
                }
            }

            for(int i=0; i< membersNum; i++){       //メンバごとにreservationsテーブルに予約を保存
                statement = connection.prepareStatement(ADD_RESERVATION_QUERY);
                statement.setString(1, userIDs.get(i));
                statement.setString(2, reservationID);
                statement.setInt(3, cafeNum);
                statement.setInt(4, seatNums.get(i));
                statement.setTimestamp(5, Timestamp.valueOf(startTime));
                statement.setTimestamp(6, Timestamp.valueOf(endTime));
                statement.setBoolean(7, arrived);
                statement.executeUpdate();

                statement = connection.prepareStatement("SELECT * FROM reservations WHERE user_id = ?");
                statement.setString(1, userIDs.get(i));
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String reservationsUser= resultSet.getString("user_id");   //データの記録に成功したかを確認
                    Timestamp reservationsTime= resultSet.getTimestamp("start_time");
                    if(reservationsUser.equals(userIDs.get(i)) && reservationsTime.equals(Timestamp.valueOf(startTime))){
                        //正常に保存できているならこの段階では何もしない
                    }else {
                        out.println("failure Failed to add reservation.");
                        System.out.println("failure Failed to add reservation.");
                        return;
                    }
                } else {
                    out.println("failure Failed to add reservation.");
                    System.out.println("failure Failed to add reservation.");
                    return;
                }
            }
            out.println("success ");       //全メンバの予約を正常に保存できたらクライアントに通知   
            System.out.println("success Added reservation successfully.");
        } catch (SQLException e) {
            out.println("failure Failed to add reservation.");
            System.out.println("failure Failed to add reservation.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (userResult != null) {
                    userResult.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

/*
 * 予約キャンセル処理
 * request -> "removeReservation userID1,userID2,..."
 * response -> "success" or "failure message"
 */
class RemoveReservationExecutor implements CommandExecutor {
    String REMOVE_RESERVATION_QUERY = "DELETE FROM reservations WHERE user_id = ?";
        
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Remove reservation executor called.");

        int membersNum= args.split(",").length;      //キャンセルする人数を取得
        List<String> userIDs= new ArrayList<>();

        for(int i=0; i< membersNum; i++){       //メンバ全員のユーザIDを配列に追加
            userIDs.add(args.split(" ")[0].split(",")[i]);
        }
        
        String filteredArgs= args.replaceAll("[" + "," + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }
    
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);     //データベースに接続
            System.out.println("Connected to DB.");
    
            for(int i=0; i< membersNum; i++){       //メンバごとに予約を削除
                statement = connection.prepareStatement(REMOVE_RESERVATION_QUERY);
                statement.setString(1, userIDs.get(i));
                int rowsAffected = statement.executeUpdate();   //予約を削除
        
                if (rowsAffected > 0) {
                    //正常に削除できているならこの段階では何もしない
                } else {
                    out.println("failure Failed to remove reservations.");
                    System.out.println("failure Failed to remove reservations."); 
                }
            }
            out.println("success ");        //すべて削除できたら通知
            System.out.println("success Removed reservation successfully.");
        } catch (SQLException e) {
            out.println("failure Failed to remove reservations.");
            System.out.println("failure Failed to remove reservations."); 
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
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
    static final int MAX_SEATS_1 = 30, MAX_SEATS_2 = 30;    //第一、第二食堂の最大座席数

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch Available seats executor called.");

        int cafeNum = Integer.parseInt(args.split(" ")[0]);     //引数から食堂と時間を取得
        LocalDateTime startTime = LocalDateTime.parse(args.split(" ")[1], formatter);

        String filteredArgs= args.replaceAll("[" + " " + "-" + ":" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            List<Integer> availableSeats = new ArrayList<>();     //空席を保存する配列を初期化
            if(cafeNum == 1){
                for (int i=1; i <= MAX_SEATS_1; i++){
                    availableSeats.add(i);
                }
            } else if (cafeNum == 2){
                for (int i=1; i <= MAX_SEATS_2; i++){       /*最大席数が同じなら削除 */
                    availableSeats.add(i);
                }
            }
            
            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setInt(1, cafeNum);     //指定された食堂・時間の予約を探す
            statement.setTimestamp(2, Timestamp.valueOf(startTime));
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {                
                availableSeats.remove(Integer.valueOf(resultSet.getInt("seat_num")));
            }
            
            String availableSeat = "";
            for (int i=0; i<availableSeats.size(); i++) {
                if(i > 0) {
                    availableSeat = availableSeat + ",";
                }
                availableSeat = availableSeat + availableSeats.get(i).toString();
            }
            out.println("success " + availableSeat);
            System.out.println("success '" + availableSeat + "' fetched successfully.");
        } catch (SQLException e) {
            out.println("failure Failed to fetch available seats.");
            System.out.println("failure Failed to fetch available seats.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

/*
 * 位置情報から学食にきたかどうかを更新する処理
 * request -> "updateArrived userID cafeNum"
 * response -> "success" or "failure message"
 */
class UpdateArrivedExecutor implements CommandExecutor {
    static final String UPDATE_ARRIVED_QUERY = "UPDATE reservations SET arrived = ? WHERE user_id = ?";

    @Override
    public void execute(PrintWriter out, String args) {        
        System.out.println("Update arrival executor called.");

        String userID = args.split(" ")[0];
        
        String filteredArgs= args.replaceAll("[" + " " + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(UPDATE_ARRIVED_QUERY);
            statement.setBoolean(1, true);     //指定された食堂・時間の予約を探す
            statement.setString(2, userID);
            int rowsUpdated = statement.executeUpdate();
            
            if(rowsUpdated > 0){
                out.println("success");
                System.out.println("success Updated 'arrived' successfully.");
            }
        } catch (SQLException e) {
            out.println("failure Failed to update arrived.");
            System.out.println("failure Failed to update 'arrived'.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

/*
* 予約履歴取得処理
* request -> "fetchReservationHistory userID"
* response -> "success members cafeNum seatNum startTime endTime went, members cafeNum seatNum..." or "failure message"
* （members は自分を含まない、一緒に予約した人）
*/
class FetchReservationHistoryExecutor implements CommandExecutor{
    static final String EXPORT_RESERVATION_QUERY = "INSERT INTO reserv_history (user_id, members, cafe_num, seat_num, start_time, end_time, arrived) VALUES (?, ?, ?, ?, ? , ?, ?)";
    static final String SELECT_RESERVATION_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    static final String SELECT_RESERV_HISTORY_QUERY = "SELECT * FROM reserv_history WHERE user_id = ?";
    static final String REMOVE_RESERVATION_QUERY = "DELETE FROM reservations WHERE user_id = ?";
    static final String SELECT_MEMBERS_QUERY = "SELECT * FROM reservations WHERE reservation_id = ?";
    static final String SELECT_USER_QUERY = "SELECT * FROM users WHERE user_id = ?";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    @Override
    public void execute(PrintWriter out, String args) {     
        System.out.println("Fetch reservation history executor called.");

        String userID= args;
        String filteredArgs= args.replaceAll("[" + "-" + "_" + "]", "");
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //必要な特殊文字を取り除いた引数が半角英数字以外の文字を含むかをチェック
        Matcher argsMatcher = pattern.matcher(filteredArgs);
        if (argsMatcher.find()) {
            out.println("failure Requests should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null, historyResultSet = null;
        try{
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(SELECT_RESERVATION_QUERY);
            statement.setString(1, userID);     //このユーザが含まれる予約を探す
            resultSet = statement.executeQuery();
            if(resultSet.next()){
                LocalDateTime currentTime = LocalDateTime.now();        //過去の予約がある場合は履歴に移動
                if(!resultSet.getTimestamp("end_time").toLocalDateTime().isAfter(currentTime)){
                    if(!exportToHistory(resultSet, connection, out))
                        return;
                }
            }

            statement = connection.prepareStatement(SELECT_RESERV_HISTORY_QUERY);
            statement.setString(1, userID);     //このユーザが含まれる予約履歴を探す
            historyResultSet = statement.executeQuery();
            if(historyResultSet.next()){
                String reservHistory = "";
                do {
                    if(!reservHistory.equals("")){  //先頭でなければカンマを追加
                        reservHistory = reservHistory + ",";
                    }
                    LocalDateTime localStartTime = historyResultSet.getTimestamp("start_time").toLocalDateTime();
                    LocalDateTime localEndTime = historyResultSet.getTimestamp("end_time").toLocalDateTime();

                    reservHistory = reservHistory +" "+ historyResultSet.getString("members") +" "
                                    + Integer.toString(historyResultSet.getInt("cafe_num")) +" "
                                    + Integer.toString(historyResultSet.getInt("seat_num")) +" "+ localStartTime.format(formatter) 
                                    +" "+ localEndTime.format(formatter) +" "+ historyResultSet.getBoolean("arrived");
                } while (historyResultSet.next());

                out.println("success" + reservHistory);
                System.out.println("success '" + reservHistory + "' fetched successfully");
            } else {
                out.println("success");
                System.out.println("success This user has no reservations history.");
            }
        } catch (SQLException e) {
            out.println("failure Failed to fetch reservations.");
            System.out.println("failure Failed to fetch reservations.");
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (historyResultSet != null) {
                    historyResultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    static boolean exportToHistory(ResultSet resultSet, Connection connection, PrintWriter out){
        PreparedStatement statement = null, usersStatement = null;
        ResultSet membersResultSet = null, userInfoResultSet = null;
        try{
            statement = connection.prepareStatement(SELECT_MEMBERS_QUERY);
            statement.setString(1, resultSet.getString("reservation_id"));     //予約IDが一致するメンバの予約を探す
            membersResultSet = statement.executeQuery();
            
            String membersInfo= "";
            while(membersResultSet.next()){         //メンバのユーザIDと名前をカンマで繋げて記録
                usersStatement = connection.prepareStatement(SELECT_USER_QUERY);
                usersStatement.setString(1, membersResultSet.getString("user_id"));     //ユーザ名を得るためにユーザ情報を取得
                userInfoResultSet = usersStatement.executeQuery();
                userInfoResultSet.next();

                if(!membersInfo.equals("")) {
                    membersInfo = membersInfo + ",";
                }
                membersInfo = membersInfo + membersResultSet.getString("user_id") + ":" + userInfoResultSet.getString("username");
            }
            membersResultSet = statement.executeQuery();
           
            String membersWithoutUser = "";
            while(membersResultSet.next()){         //リクエストを送信したユーザを含め、全員の予約を履歴に移動させ、予約テーブルからは削除
                membersWithoutUser = membersInfo;
                String entryToRemove = "^" + membersResultSet.getString("user_id") + "[^,]*,";
                membersWithoutUser = membersWithoutUser.replaceFirst(entryToRemove, "");     //メンバ一覧から本人を削除
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
                int rowsAffected = statement.executeUpdate();

                statement = connection.prepareStatement(REMOVE_RESERVATION_QUERY);
                statement.setString(1, membersResultSet.getString("user_id"));
                int rowsAffected_2 = statement.executeUpdate();
                if(rowsAffected == 0 || rowsAffected_2 == 0) {
                    out.println("failure Failed to export reservation info.");
                    System.out.println("failure Failed to export reservation info.");
                    return false;
                }
            }
            System.out.println("success Exported reservation info successfully.");
            return true;
        } catch (SQLException e) {
            out.println("failure Failed to export reservation info.");
            System.out.println("failure Failed to export reservation info.");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (membersResultSet != null) {
                    membersResultSet.close();
                }
                if (userInfoResultSet != null) {
                    userInfoResultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (usersStatement != null) {
                    usersStatement.close();
                }
            } catch (SQLException e) {
                out.println("failure Failed to close connection.");
                System.out.println("failure Failed to close connection.");
                e.printStackTrace();
            }
        }
    }
}