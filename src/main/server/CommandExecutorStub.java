package main.server;

import java.io.PrintWriter;

interface CommandExecutorStub {
    void execute(PrintWriter out, String args);    // 各コマンドに対応した処理を実行するメソッド
}

/*
 * ユーザーの登録処理
 * request -> "signUp userName password studentID"
 * response -> "Success userID" or "failure message"
 */
class SignUpExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Sign up executor called.");
        out.println("success userID");
        System.out.println("Sending: success userID\n");
    }
}

/*
 * ログイン処理
 * request -> "login userName password"
 * response -> "success userID studentID" or "failure message"
 */
class LoginExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Login executor called.");
        out.println("success userID studentID");
        System.out.println("Sending: success userID studentID\n");
    }
}

/*
 * 友達リスト取得処理
 * request -> "fetchFriend userID"
 * response -> "success userID1:userName1 userID2:userName2 ..." or
 * "failure message"
 */
class FetchFriendExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch friend executor called.");
        out.println("success userID:userName,userID:userName");
        System.out.println("Sending: success userID:userName,userID:userName\n");
    }
}

/*
 * 友達追加処理
 * request -> "addFriend userID friendID"
 * response -> "success friendID:friendName" or "failure message"
 */
class AddFriendExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Add friend executor called.");
        out.println("success friendID:friendName");
        System.out.println("Sending: success friendID:friendName\n");
    }
}

/*
 * 友達削除処理
 * request -> "removeFriend userID friendID"
 * response -> "success" or "failure message"
 */
class RemoveFriendExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Remove friend executor called.");
        out.println("success");
        System.out.println("Sending: success\n");
    }
}
 

/*
 * 予約情報取得処理
 * request -> "fetchReservation userID"
 * response ->
 * "success userID:userName,userID:userName,... cafeNum seatNum startTime endTime went"
 */
class FetchReservationExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch reservation executor called.");
        out.println("success userID:userName,userID:userName cafeNum seatNum startTime endTime went");
        System.out.println("Sending: success userID:userName,userID:userName cafeNum seatNum startTime endTime went\n");
    }
}

/*
 * 予約追加処理
 * request ->
 * "addReservation userID1,userID2,... cafeNum seatNum startTime endTime"
 * response -> "success" or "failure message"
 * メンバーの誰かが既に予約をしている時 -> "failure userID:username seatNum"
 * 座席が空いていない時 -> "failure seatNum is not available"
 */
class AddReservationExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Add reservation executor called.");
        out.println("success userID:userName,userID:userName cafeNum seatNum startTime endTime went");
        System.out.println("Sending: success userID:userName,userID:userName cafeNum seatNum startTime endTime went\n");
    }
}

/*
 * 予約キャンセル処理
 * request -> "removeReservation userID1,userID2,..."
 * response -> "success" or "failure message"
 */
class RemoveReservationExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("remove reservation executor called.");
        out.println("success");
        System.out.println("Sending: success\n");
    }
}

/*
 * 座席空き情報取得処理
 * request -> "fetchAvailableSeats cafeNum startTime endTime"
 * response -> "success seatNum1,seatNum2,..." or "failure message"
 */
class FetchAvailableSeatsExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch available seats executor called.");
        out.println("success seatNum1,seatNum2");
        System.out.println("Sending: success seatNum1,seatNum2\n");
    }
}

/*
 * 位置情報から学食にきたかどうかを更新する処理
 * request -> "updateArrived userID cafeNum"
 * response -> "success" or "failure message"
 */
class UpdateArrivedExecutorStub implements CommandExecutorStub {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Update arrived executor called.");
        out.println("success");
        System.out.println("Sending: success\n");
    }

    static void checkArrival(){
        System.out.println("Check Arrival called");
        System.out.println("done\n");
    }
}

/*
* 予約履歴取得処理
* request -> "fetchReservationHistory userID"
* response -> "success members cafeNum seatNum startTime endTime went, members cafeNum seatNum..." or "failure message"
* （members は自分を含まない、一緒に予約した人）
*/
class FetchReservationHistoryExecutorStub implements CommandExecutorStub{
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("Fetch reservation history executor called.");
        out.println("success members cafeNum seatNum startTime endTime went");
        System.out.println("Sending: success members cafeNum seatNum startTime endTime went\n");
    }
}