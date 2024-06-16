package main.server;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
class SignUpExecutor implements CommandExecutor {
    private static final String COUNT_USER_QUERY = "SELECT COUNT(*) FROM users WHERE username = ?";
    private static final String INSERT_USER_QUERY = "INSERT INTO users (username, password) VALUES (?, ?)";

    @Override
    public void execute(PrintWriter out, String args) {
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
            out.println("Failed: Username and password should contain only alphanumeric characters.");
            return;
        }

        Connection connection = null;
        PreparedStatement countStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet resultSet = null;

        try {
            // データベースに接続
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Connected to DB.");

            // ユーザー名がすでに存在するかどうかを確認 (ユーザーネームにnameを使用しているユーザーの数をカウントするクエリ)
            countStatement = connection.prepareStatement(COUNT_USER_QUERY);
            countStatement.setString(1, name);
            resultSet = countStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                out.println("Failed: Username already exists.");
                return;
            }

            // ユーザーネームに重複がなければユーザーを登録
            insertStatement = connection.prepareStatement(INSERT_USER_QUERY);
            insertStatement.setString(1, name);
            insertStatement.setString(2, password);
            insertStatement.executeUpdate();
            out.println("User " + name + " registered successfully.");

        } catch (SQLException e) {
            out.println("Failed: Failed to register user " + name);
            System.out.println("Failed to register user. " + name);
            e.printStackTrace();

        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (countStatement != null) {
                    countStatement.close();
                }
                if (insertStatement != null) {
                    insertStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                out.println("Failed: Failed to close connection.");
                System.out.println("Failed to close connection.");
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
            out.println("Failed: Username and password should contain only alphanumeric characters.");
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
                out.println("User " + name + " logged in successfully.");
                System.out.println("User " + name + " logged in successfully.");
            } else {
                out.println("Failed: Invalid username or password.");
                System.out.println("Failed: Invalid username or password.");
            }

        } catch (SQLException e) {
            out.println("Failed: Failed to login user " + name);
            System.out.println("Failed to login user. " + name);
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
                out.println("Failed: Failed to close connection.");
                System.out.println("Failed to close connection.");
                e.printStackTrace();
            }
        }

    }
}
