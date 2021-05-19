import database.DataBaseWorker;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RuRoom extends Thread {
    User user1;
    User user2;
    Socket socket1;
    Socket socket2;
    BufferedReader br1;
    BufferedReader br2;
    BufferedWriter bw1;
    BufferedWriter bw2;
    DataBaseWorker dataBaseWorker;
    public RuRoom(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        socket1 = user1.getSocket();
        socket2 = user2.getSocket();
        br1 = user1.getBr();
        bw1 = user1.getBw();
        br2 = user2.getBr();
        bw2 = user2.getBw();
        dataBaseWorker = new DataBaseWorker();
        start();
    }

    String[] randomText = new String[3]; //Три строчки текста

    boolean user1LeftFlag = false;
    boolean user2LeftFlag = false;

    boolean user1FinishedFlag = false;
    boolean user2FinishedFlag = false;

    @Override
    public void run() {
        try {
            System.out.println("RuRoom");
            //Отправляем подтверждение игры
            bw1.write("ru\n");
            bw1.flush();
            sleep(100);
            bw2.write("ru\n");
            bw2.flush();
            sleep(100);
            //Отправляем данные обоим пользователям
            bw1.write(user2.getUserName() + "\n");
            bw1.flush();
            bw2.write(user1.getUserName() + "\n");
            bw2.flush();
            socket1.getOutputStream().write(user2.getIcon());
            socket1.getOutputStream().flush();
            socket2.getOutputStream().write(user1.getIcon());
            socket2.getOutputStream().flush();
            randomText = generateRandomText();
            sendText(user1);
            //new Listener1();
            sendText(user2);
            //new Listener2();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("/RuRoom");
    }

    public void sendText(User user) {
        try {
            for (int i = 0; i < 3; i++) {
                user.getBw().write(randomText[i] + "\n");
                user.getBw().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] generateRandomText() {
        String[] result = new String[3];
        try {
            File file = new File("files/ruTexts/texts");
            List<String> lines = Files.readAllLines(file.toPath());
            int linesNum = lines.size();
            int textsNum = linesNum / 4;
            int randomTextNum = (int) (Math.random() * textsNum);
            result[0] = lines.get(randomTextNum * 4);
            result[1] = lines.get(randomTextNum * 4 + 1);
            result[2] = lines.get(randomTextNum * 4 + 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class Listener1 extends Thread {
        BufferedWriter bw;
        BufferedReader br;

        public Listener1() {
            bw = user1.getBw();
            br = user1.getBr();
            start();
        }

        @Override
        public void run() {
            while (true) {
                String message = "";
                //Читаем сообщение от клиента
                try {
                    message = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] params = message.split("\\|");
                //Если информационное сообщение формата info|'число'
                if (params[0].equals("info")) {
                    try {
                        user2.getBw().write(params[1] + "\n");
                        user2.getBw().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                //Если сообщение о выходе
                } else if (params[0].equals("close")) {
                    try {
                        user2.getBw().write("close\n");
                        user2.getBw().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if(params[0].equals("finish")){
                    try {
                        user2.getBw().write("finish\n");
                        user2.getBw().flush();
                        //Принимаем статистику
                        String s = br.readLine();
                        String[] stats = s.split("\\|");
                        dataBaseWorker.updateRuStats(user1.getUserName(),stats[0],stats[1],stats[2]);
                        randomText = generateRandomText();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private class Listener2 extends Thread {
        BufferedWriter bw;
        BufferedReader br;

        public Listener2() {
            bw = user2.getBw();
            br = user2.getBr();
            start();
        }

        @Override
        public void run() {

        }
    }
}
