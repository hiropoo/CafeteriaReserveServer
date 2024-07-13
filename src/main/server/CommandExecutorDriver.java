package main.server;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CommandExecutorDriver {
    public static void main(String[] args) {
        StringWriter capturedOut = new StringWriter();
        PrintWriter out = new PrintWriter(capturedOut); //クライアントに送信するはずの出力を記憶する変数

        //ユーザ登録テスト
        SignUpExecutor signUp = new SignUpExecutor();
        signUp.execute(out, "userName userPass 2264000");
        String userID = capturedOut.toString().split(" ")[1];   //ユーザID取得
        userID = userID.replace("\n", "").replace("\r", "");
        capturedOut.getBuffer().setLength(0);

        SignUpExecutor signUpFriend = new SignUpExecutor();
        signUpFriend.execute(out, "friendName friendPass 2264001");
        String friendID = capturedOut.toString().split(" ")[1];   //ユーザID取得
        friendID = friendID.replace("\n", "").replace("\r", "");        

        //ユーザ登録テスト
        LoginExecutor login = new LoginExecutor();
        login.execute(out, "userName userPass");

        //友達追加テスト
        AddFriendExecutor addFriend = new AddFriendExecutor();
        addFriend.execute(out, userID +" "+ friendID);

        //友達取得テスト
        FetchFriendExecutor fetchFriend = new FetchFriendExecutor();
        fetchFriend.execute(out, userID);

        //友達削除テスト
        RemoveFriendExecutor removeFriend = new RemoveFriendExecutor();
        removeFriend.execute(out, userID +" "+ friendID);

        //予約追加テスト
        AddReservationExecutor addReservation = new AddReservationExecutor();
        addReservation.execute(out, userID +","+ friendID + " 1 10,11 2024-07-12-20:40:00 2024-07-12-21:00:00");

        //予約取得テスト
        FetchReservationExecutor fetchReservation = new FetchReservationExecutor();
        fetchReservation.execute(out, userID);

        //空席情報取得テスト
        FetchAvailableSeatsExecutor fetchAvailableSeats = new FetchAvailableSeatsExecutor();
        fetchAvailableSeats.execute(out, "1 2024-07-12-19:20:00 2024-07-12-19:40:00");

        //到着報告テスト
        UpdateArrivedExecutor updateArrived = new UpdateArrivedExecutor();
        updateArrived.execute(out, userID + " 1");

        //予約削除テスト
        RemoveReservationExecutor removeReservation = new RemoveReservationExecutor();
        removeReservation.execute(out, userID +","+ friendID);

        //予約履歴を作るために過去の予約を追加
        AddReservationExecutor addOldReservation = new AddReservationExecutor();
        addOldReservation.execute(out, userID +","+ friendID + " 1 10,11 2024-07-02-18:40:00 2024-07-02-19:00:00");

        //予約履歴取得テスト
        FetchReservationHistoryExecutor fetchReservationHistory = new FetchReservationHistoryExecutor();
        fetchReservationHistory.execute(out, userID);

        capturedOut.getBuffer().setLength(0);
        //有害な記号を含んだリクエストの場合
        LoginExecutor toxicLogin = new LoginExecutor();
        toxicLogin.execute(out, "userName' OR 1=1 --");
        System.out.println(capturedOut);
    }
}
