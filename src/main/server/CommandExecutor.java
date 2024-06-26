package main.server;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //ユーザーIDが半角英数字以外の文字を含むかをチェック
        Matcher userIDMatcher = pattern.matcher(thisUserID);
        if (userIDMatcher.find()) {
            out.println("failure UserID should contain only alphanumeric characters."); 
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
                    if(user1.equals(thisUserID)){           //このユーザでない方（友達）のユーザ名を取得する
                        statement.setString(1, user2);  
                        friendList= friendList + " " + user2;       //返送する友達リストにユーザIDを追加
                    }else if(user2.equals(thisUserID)){
                        statement.setString(1, user1);
                        friendList= friendList + " " + user1;
                    }
                    resultSet2 = statement.executeQuery();
                    if (resultSet2.next()) {
                        friendName= resultSet2.getString("username");
                        friendList= friendList + ":" + friendName;       //返送する友達リストにユーザ名を追加
                    }
                }while (resultSet.next());
                out.println("success" + friendList);
                System.out.println("FriendList:" + friendList + " fetched successfully");
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

    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Add friend executor called.");

        String userID = args.split(" ")[0];    //引数をスペースで分割し、ユーザーIDと追加する友達のIDを取得
        String friendID = args.split(" ")[1];

        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //ユーザーIDとフレンドIDが半角英数字以外の文字を含むかをチェック
        Matcher userIDMatcher = pattern.matcher(userID);
        Matcher friendIDMatcher = pattern.matcher(friendID);
        if (userIDMatcher.find() || friendIDMatcher.find()) {
            out.println("failure UserID should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);     //データベースに接続
            System.out.println("Connected to DB.");

            statement = connection.prepareStatement(ADD_FRIEND_QUERY);   //このフレンド関係をfriendsテーブルに追加
            statement.setString(1, userID);
            statement.setString(2, friendID);
            statement.executeUpdate();

            statement = connection.prepareStatement("SELECT * FROM friends WHERE user_id1 = ? OR user_id2 = ?");
            statement.setString(1, userID);
            statement.setString(2, friendID);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String thisUser= resultSet.getString("user_id1");   //データの記録に成功したかを確認
                String friend= resultSet.getString("user_id2");     /*長いからexecuteUpdate()の返り値が１ならokに変更する？*/
                if(thisUser.equals(userID) && friend.equals(friendID)){
                    out.println("success friendID: " + friend); 
                    System.out.println("FriendID: " + friend + " added successfully");
                }else {
                    out.println("failure Failed to add friend.");
                    System.out.println("failure Failed to add friend.");
                }
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

        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");    //ユーザーIDが半角英数字以外の文字を含むかをチェック
        Matcher userIDMatcher = pattern.matcher(userID);
        Matcher friendIDMatcher = pattern.matcher(friendID);
        if (userIDMatcher.find() || friendIDMatcher.find()) {
            out.println("failure UserID and friendID should contain only alphanumeric characters."); 
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
                out.println("success Friend removed successfully.");
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
    
    @Override
    public void execute(PrintWriter out, String args) {
    }
}

/*
 * 予約追加処理
 * request ->
 * "addReservation userID1,userID2,... cafeNum seatNum startTime endTime"
 * response -> "success" or "failure message"
 */
class AddReservationExecutor implements CommandExecutor {

    @Override
    public void execute(PrintWriter out, String args) {
    }
}

/*
 * 予約キャンセル処理
 * request -> "removeReservation userID1,userID2,..."
 * response -> "success" or "failure message"
 */
class RemoveReservationExecutor implements CommandExecutor {

    @Override
    public void execute(PrintWriter out, String args) {
    }
}

/*
 * 座席空き情報取得処理
 * request -> "fetchAvailableSeats cafeNum startTime endTime"
 * response -> "success seatNum1,seatNum2,..." or "failure message"
 */
class FetchAvailableSeatsExecutor implements CommandExecutor {

    @Override
    public void execute(PrintWriter out, String args) {
    }
}

/*
 * 位置情報から学食にきたかどうかを更新する処理
 * request -> "updateArrived userID cafeNum"
 * response -> "success" or "failure message"
 */
class UpdateArrivedExecutor implements CommandExecutor {

    @Override
    public void execute(PrintWriter out, String args) {
    }
}
