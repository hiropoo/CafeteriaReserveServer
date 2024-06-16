package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import main.util.PropertyUtil;

public class TestClient {
    private static final String SERVER_IP = PropertyUtil.getProperty("ip");
    private static final int PORT = Integer.parseInt(PropertyUtil.getProperty("port"));

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String[] args) {

        TestClient client = new TestClient();
        client.start();
    }

    // クライアントを起動
    public void start() {
        System.out.println("Client started on port " + PORT);
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("login user1 pass1");
            System.out.println("Received: " + in.readLine());

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to close the client socket.");
            }
        }
    }

}
