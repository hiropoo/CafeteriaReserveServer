package main.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* テスト用のユーザの情報を保存するクラス
 */
public class User {
    /* インスタンス変数 */
    private String userName;
    private String password;
    private int studentID;
    private String userID;
    private Map<String, String> friendList = new HashMap<>();
    private boolean hasReservation;
    // private Reservation reservation;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Map<String, String> getFriendList() {
        return friendList;
    }

    public void setFriendList(Map<String, String> friendList) {
        this.friendList = friendList;
    }

    public boolean isHasReservation() {
        return hasReservation;
    }

    public void setHasReservation(boolean hasReservation) {
        this.hasReservation = hasReservation;
    }

    // 新規登録時に呼び出されるコンストラクタ
    public User(String userName, String password, int studentID) {
        this.userName = userName;
        this.password = password;
        this.studentID = studentID;
        // this.userID = uuid.getID(); // 未実装
        this.userID = generateUserID(); // 仮の実装
        this.friendList = null;
        this.hasReservation = false;
    }

    // フレンドの削除を行うメソッド
    public void deleteFriend(String friendID) {
        friendList.remove(friendID);
    }

    // userIDを生成するメソッド（仮）本来はUUIDを使用
    public String generateUserID() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1000000000));
    }
}
