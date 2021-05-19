import database.DataBaseWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static ArrayList<User> userList = new ArrayList<>();


    public static void main(String args[]) {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            System.out.println("Server started!");
            //Запускаем компановщик игр
            RuPlayer ruPlayer = new RuPlayer();
            ruPlayer.setDaemon(true);
            ruPlayer.start();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Someone connected");
                User user = new User(socket);
                userList.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}