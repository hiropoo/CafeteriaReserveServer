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
import main.util.PropertyUtil;

interface CommandExecutor {
    // 共通のDB処理用の定数
    static final String DB_URL = PropertyUtil.getProperty("db_url"); // データベースのURL
    static final String DB_USER = PropertyUtil.getProperty("db_user"); // データベースのユーザー名
    static final String DB_PASS = PropertyUtil.getProperty("db_pass"); // データベースのパスワード

    // 各コマンドに対応した処理を実行するメソッド
    void execute(PrintWriter out, String args);
}

// ユーザーの登録処理
// 登録成功 -> "success userID"
// 登録失敗 -> "failure message"
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
            System.out.println("User " + name + " registered fully.");

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
        }
    }
}

// ユーザーのログイン処理
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
        }

    }
}
