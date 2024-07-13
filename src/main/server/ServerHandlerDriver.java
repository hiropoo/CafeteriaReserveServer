package main.server;

import java.time.LocalDateTime;
import java.util.Arrays;

import test.util.Reservation;
import test.util.User;
import test.client.ServerHandler;

public class ServerHandlerDriver {
    static {
        // 予約情報の設定
        Reservation.setStartTime(LocalDateTime.of(2024, 7, 13, 10, 20, 00));
        Reservation.setEndTime(LocalDateTime.of(2024, 7, 13, 10, 40, 00));
        Reservation.setCafeNum(1);
        Reservation.setSeatNums(Arrays.asList(1, 2));
        Reservation.setMembers(Arrays.asList("userName", "friendName"));
        Reservation.setArrived(false);
    }

    public static void main(String[] args) {
        System.out.println("Testing ServerHandler\n");

        //フレンドを登録
        User.setUserName("friendName");
        User.setPassword("friendPass");
        User.setStudentID(2264001);
        ServerHandler.register();
        String friendID = User.getUserID();

        //ユーザ登録テスト
        User.setUserName("userName");
        User.setPassword("userPass");
        User.setStudentID(2264000);
        ServerHandler.register();

        //ログインテスト
        ServerHandler.login();

        //フレンド追加テスト
        ServerHandler.addFriend(friendID);

        //フレンド取得テスト
        ServerHandler.fetchFriend();

        //フレンド削除テスト
        ServerHandler.removeFriend(friendID);

        //予約追加テスト
        ServerHandler.addReservation();

        //予約取得テスト
        ServerHandler.fetchReservation();

        //空席情報取得テスト
        ServerHandler.fetchAvailableSeats(1, LocalDateTime.of(2024, 7, 11, 12, 00, 00), 
                LocalDateTime.of(2024, 7, 11, 12, 00, 00));

        //到着報告テスト
        ServerHandler.updateArrived();

        //予約削除テスト
        ServerHandler.removeReservation();
        
        //予約履歴取得テスト
        ServerHandler.fetchReservationHistory();
    }
}