package main.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import properties.PropertyUtil;

public class ServerStub {
    private static ServerSocket serverSocket = null; // サーバーソケット
    private static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port")); // サーバーのポート番号

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT); // サーバーソケットの作成
        System.out.println("Server is running...\n");

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 接続待ち
                
                // ストリームの作成
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Received request: " + request);

                    String[] parts = request.split(" ", 2); //requestをからコマンドを取得
                    String command = parts[0];

                    switch (command) {
                        case "signUp":
                            out.println("success userID");
                            break;
                        case "login":
                            out.println("success userID 2264000");
                            break;
                        case "fetchFriend":
                            out.println("success friendID:friendName");
                            break;
                        case "addFriend":
                            out.println("success friendID:friendName");
                            break;
                        case "removeFriend":
                            out.println("success");
                            break;
                        case "fetchReservation":
                            out.println("success userID:userName,friendID:friendName 1 1 2024-07-13-10:20:00 2024-07-13-10:40:00 false");
                            break;
                        case "addReservation":
                            out.println("success");
                            break;
                        case "removeReservation":
                            out.println("success");
                            break;
                        case "fetchAvailableSeats":
                            out.println("success 3,4,5,6,7,8,9,10");
                            break;
                        case "updateArrived":
                            out.println("success");
                            break;
                        case "fetchReservationHistory":
                            out.println("success friendID:friendName 1,2 2024-07-13-10:20:00 2024-07-13-10:40:00 false");
                            break;
                        
                        default:
                            out.println("Unknown command: " + command);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error accepting client connection: " + e.getMessage());
        } finally {
            if (serverSocket != null)
                serverSocket.close();
        }
    }
}