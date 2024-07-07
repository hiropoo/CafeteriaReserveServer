package test.client;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import test.util.Reservation;
import test.util.User;

public class Client {
    static {
        // ユーザ情報の設定
        User.setUserName("user1");
        User.setPassword("pass1");
        User.setStudentID(1234567890);

        // 予約情報の設定
        Reservation.setStartTime(LocalDateTime.of(2024, 7, 11, 12, 00, 00));
        Reservation.setEndTime(LocalDateTime.of(2024, 7, 11, 12, 20, 00));
        Reservation.setCafeNum(1);
        Reservation.setSeatNums(Arrays.asList(1, 2, 3));
        Reservation.setMembers(Arrays.asList("user1", "user2", "user3"));
        Reservation.setArrived(false);
    }

    public static void main(String[] args) {
        System.out.println("Client is running...\n");

        Client client = new Client();
        client.start();
    }

    // クライアントを起動
    public void start() {

        /* 新規登録のテスト */
        // ServerHandler.register();

        // ログインのテスト
        ServerHandler.login();

        /* 友達リスト取得のテスト */
        // ServerHandler.fetchFriend();
        // for (String friendID : User.getFriendList().keySet()) {
        // System.out.println("Friend ID: " + friendID + ", Friend Name: " +
        // User.getFriendList().get(friendID));
        // }

        /* 友達追加のテスト */
        // ServerHandler.addFriend("DEF456");
        // System.out.println("--- Friend List ---");
        // for (String friendID : User.getFriendList().keySet()) {
        // System.out.println("Friend ID: " + friendID + ", Friend Name: " +
        // User.getFriendList().get(friendID));
        // }
        // System.out.println();

        /* 友達削除のテスト */
        // ServerHandler.removeFriend("DEF456");
        // System.out.println("--- Friend List ---");
        // for (String friendID : User.getFriendList().keySet()) {
        // System.out.println("Friend ID: " + friendID + ", Friend Name: " +
        // User.getFriendList().get(friendID));
        // }
        // System.out.println();

        /* 予約情報取得のテスト */
        // ServerHandler.fetchReservation();
        // System.out.println("--- Reservation ---");
        // System.out.println("Reservation: " +
        // Reservation.getStartTime().toString() + " ~ " +
        // Reservation.getEndTime().toString());

        /* 予約追加のテスト */
        // ServerHandler.addReservation();
        // System.out.println("--- Reservation ---");
        // System.out.println("Reservation: " +
        // Reservation.getStartTime().toString() + " ~ "
        // + Reservation.getEndTime().toString());

        /* 予約削除のテスト */
        // ServerHandler.removeReservation();
        // System.out.println("--- Reservation ---");
        // System.out.println(Reservation.getStartTime() == null ? "Reservation does not exist" : "Reservation: " +
        //         Reservation.getStartTime().toString() + " ~ "
        //         + Reservation.getEndTime().toString());

        /* 空席情報取得のテスト */
        // List<Integer> availableSeats = ServerHandler.fetchAvailableSeats(1, LocalDateTime.of(2024, 7, 11, 12, 00, 00), 
        //         LocalDateTime.of(2024, 7, 11, 12, 00, 00));
        // System.out.println("--- Available Seats ---");
        // for (int seatNum : availableSeats) {
        //     System.out.println("Seat Number: " + seatNum);
        // }
        // System.out.println();
    }

}
