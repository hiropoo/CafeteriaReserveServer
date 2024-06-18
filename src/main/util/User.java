package main.util;

import java.util.HashMap;
import java.util.Map;

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

    // 新規登録時に呼び出されるコンストラクタ
    public User(String userName, String password, int studentID) {
        this.userName = userName;
        this.password = password;
        this.studentID = studentID;
        // this.userID = uuid.getID(); // 未実装
        this.friendList = null;
        this.hasReservation = false;
    }

    // フレンドの削除を行うメソッド
    public void deleteFriend(String friendID) {
        friendList.remove(friendID);
    }
}
