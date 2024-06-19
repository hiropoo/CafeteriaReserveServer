package test.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* テスト用のユーザの情報を保存するクラス
 */
public class User {
    /* インスタンス変数 */
    static private String userName;
    static private String password;
    static private int studentID;
    static private String userID;
    static private Map<String, String> friendList = new HashMap<>();
    static private boolean hasReservation = false;
    static private Reservation reservation;


    public static Reservation getReservation() {
        return reservation;
    }

    public static void setReservation(Reservation reservation) {
        User.reservation = reservation;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        User.userName = userName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        User.password = password;
    }

    public static int getStudentID() {
        return studentID;
    }

    public static void setStudentID(int studentID) {
        User.studentID = studentID;
    }

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        User.userID = userID;
    }

    public static Map<String, String> getFriendList() {
        return friendList;
    }

    public static void setFriendList(Map<String, String> friendList) {
        User.friendList = friendList;
    }

    public static boolean isHasReservation() {
        return hasReservation;
    }

    public static void setHasReservation(boolean hasReservation) {
        User.hasReservation = hasReservation;
    }

    // フレンドの削除を行うメソッド
    public static void deleteFriend(String friendID) {
        friendList.remove(friendID);
    }


}
