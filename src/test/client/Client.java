package test.client;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import test.util.Reservation;
import test.util.User;

public class Client {

    public static void main(String[] args) {
        System.out.println("Client is running...\n");

        Client client = new Client();
        client.start();
    }

    // クライアントを起動
    public void start() {
        // ユーザ情報の設定
        User.setUserName("user1");
        User.setPassword("pass1");
        User.setStudentID(1234567890);

        // 予約情報の設定
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
        Reservation.setStartTime(LocalDateTime.of(2024, 7, 11, 12, 00, 00));
        Reservation.setEndTime(LocalDateTime.of(2024, 7, 11, 12, 20, 00));

        

        // // 新規登録のテスト
        // ServerHandler.register();

        // ログインのテスト
        ServerHandler.login();

        // 友達リスト取得のテスト
        // ServerHandler.fetchFriend();
        // for (String friendID : User.getFriendList().keySet()) {
        // System.out.println("Friend ID: " + friendID + ", Friend Name: " +
        // User.getFriendList().get(friendID));
        // }

        // // 友達追加のテスト
        // ServerHandler.addFriend("DEF456");
        // System.out.println("--- Friend List ---");
        // for (String friendID : User.getFriendList().keySet()) {
        //     System.out.println("Friend ID: " + friendID + ", Friend Name: " + User.getFriendList().get(friendID));
        // }
        // System.out.println();

        // // 友達削除のテスト
        // ServerHandler.removeFriend("DEF456");
        // System.out.println("--- Friend List ---");
        // for (String friendID : User.getFriendList().keySet()) {
        //     System.out.println("Friend ID: " + friendID + ", Friend Name: " + User.getFriendList().get(friendID));
        // }
        // System.out.println();

        // // 予約リスト取得のテスト

        ServerHandler.fetchReservation();
        System.out.println("Reservation: " + User.getReservation().getStartTime().toString() + " ~ " + User.getReservation().getEndTime().toString());

    }

}
