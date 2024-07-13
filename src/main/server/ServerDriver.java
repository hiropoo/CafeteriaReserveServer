package main.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerDriver {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("localhost", 12345);) {
            System.out.println("Testing Server class");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //ユーザ登録テスト
            System.out.println("\nTesting sign-up. Sending: signUp userName userPass");
            out.println("signUp userName userPass");
            System.out.println("received: " + in.readLine());

            //ログインテスト
            System.out.println("\nTesting login. Sending: login userName userPass");
            out.println("login userName userPass");
            System.out.println("received: " + in.readLine());

            //フレンド取得テスト
            System.out.println("\nTesting fetch friend. Sending: fetchFriend userID");
            out.println("fetchFriend userID");
            System.out.println("received: " + in.readLine());

            //フレンド登録テスト
            System.out.println("\nTesting add friend. Sending: addFriend userID friendID");
            out.println("addFriend userID friendID");
            System.out.println("received: " + in.readLine());

            //フレンド削除テスト
            System.out.println("\nTesting remove friend. Sending: removeFriend userID friendID");
            out.println("removeFriend userID friendID");
            System.out.println("received: " + in.readLine());

            //予約取得テスト
            System.out.println("\nTesting fetch reservation. Sending: fetchReservation userID");
            out.println("fetchReservation userID");
            System.out.println("received: " + in.readLine());

            //予約追加テスト
            System.out.println("\nTesting add reservation. Sending: addReservation userID1,userID2 cafeNum seatNum startTime endTime");
            out.println("addReservation userID1,userID2 cafeNum seatNum startTime endTime");
            System.out.println("received: " + in.readLine());

            //予約削除テスト
            System.out.println("\nTesting remove reservation. Sending: removeReservation userID1,userID2");
            out.println("removeReservation userID1,userID2");
            System.out.println("received: " + in.readLine());

            //空席情報取得テスト
            System.out.println("\nTesting fetch available seats. Sending: fetchAvailableSeats cafeNum startTime endTime");
            out.println("fetchAvailableSeats cafeNum startTime endTime");
            System.out.println("received: " + in.readLine());

            //到着報告テスト
            System.out.println("\nTesting update arrived. Sending: updateArrived userID cafeNum");
            out.println("updateArrived userID cafeNum");
            System.out.println("received: " + in.readLine());

            //予約履歴取得テスト
            System.out.println("\nTesting fetch reservation history. Sending: fetchReservationHistory userID");
            out.println("fetchReservationHistory userID");
            System.out.println("received: " + in.readLine());

            //存在しないコマンドを送信した場合
            System.out.println("\nTesting invalid command. Sending: invalidCommand argument");
            out.println("invalidCommand argument");
            System.out.println("received: " + in.readLine());

            //空Stringを送信した場合
            System.out.println("\nTesting empty input. Sending: ");
            out.println("");
            System.out.println("received: " + in.readLine());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
