package sample.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import sample.elements.Tank;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerController {

    @FXML
    public Canvas canvas;
    @FXML
    public Label player1Label;
    @FXML
    private Label player2Label;
    @FXML
    public Label infoLabel;

    public static ServerSocket server;
    public static String name;
    public static Socket clientSocket;

    GraphicsContext gc;

    boolean readyFlag;

    double shotDiameter = 10;
    boolean shotTimeFlag = false;
    boolean shotReadyFlag = false;
    boolean clientCoordsReadyFlag = false;
    boolean serverCoordsReadyFlag = false;
    public ArrayList<Tank> serverTanks = new ArrayList<>();
    public ArrayList<Tank> clientTanks = new ArrayList<>();
    public BufferedReader br;
    public BufferedWriter bw;

    double xx;
    double yy;
    Thread exchanging = new Thread(() -> {
        try {
            while (true) {
                while (shotReadyFlag != true)
                    Thread.sleep(100);
                bw.write(xx + "|" + yy + "\n");
                bw.flush();
                System.out.println("Отправил");
                shotReadyFlag = false;
                setInfoLabel("Ход соперника");
                String[] coords = br.readLine().split("\\|");
                System.out.println("Принял");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                drawOpponentShot(x, y);
                doOpponentShot(x, y);
                shotTimeFlag = true;
                setInfoLabel("Ваш ход");
            }
        } catch (SocketException e) {
            closeApp("Соединение с соперником потеряно");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    });

    Thread waitToStartGame = new Thread(() -> {
       while(clientCoordsReadyFlag == false || serverCoordsReadyFlag == false){
           try {
               Thread.sleep(100);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
       System.out.println("Запускаю exchangeing");
       setInfoLabel("Ваш ход");

        //Отображение танков клиента ЕЩЕ РАЗ ПОТОМУЧТО НАВЕРНОЕ ГДЕ-ТО ВО ВРЕМЯ СЛИПА ЛОВИМ МОМЕНТ
        // И НЕ ПОЛУЧАЕТСЯ ОТОБРАЗИТЬ В ПОТОКЕ doAccept!!!!
        for (int i = 0; i < clientTanks.size(); i++) {
            Double x = clientTanks.get(i).getX();
            Double y = clientTanks.get(i).getY();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.strokeRect(x - 10, y - 10, 20, 20);
            gc.strokeOval(x - 3, y - 3, 6, 6);
            gc.strokeLine(x + 3, y, x + 6, y);
        }

       shotTimeFlag = true;
       exchanging.setDaemon(true);
       exchanging.start();
    });

    Thread doAccept = new Thread(() -> {
        try {
            clientSocket = server.accept();
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            //Чтение имени клиента и координатов его танков
            String clientName = br.readLine();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    player1Label.setText(clientName);
                }
            });
            String[] clientCoords = br.readLine().split("\\|");
            for (int i = 0; i < clientCoords.length; i += 2) {
                Double x = Double.parseDouble(clientCoords[i]);
                Double y = Double.parseDouble(clientCoords[i + 1]);
                clientTanks.add(new Tank(x, y));
            }

            //Отображение танков клиента
            for (int i = 0; i < clientTanks.size(); i++) {
                Double x = clientTanks.get(i).getX();
                Double y = clientTanks.get(i).getY();
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.strokeRect(x - 10, y - 10, 20, 20);
                gc.strokeOval(x - 3, y - 3, 6, 6);
                gc.strokeLine(x + 3, y, x + 6, y);
            }
            clientCoordsReadyFlag = true;
        } catch (SocketException e) {
            closeApp("Соединение с клиентом потеряно");
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    Thread writeServerCoords = new Thread(() -> {
        while(clientSocket == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            try{
                //Отправка клиенту имени сервера и координатов танков сервера
                bw.write(name + "\n");
                bw.flush();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < serverTanks.size(); i++) {
                    sb.append(serverTanks.get(i).getX());
                    sb.append("|");
                    sb.append(serverTanks.get(i).getY());
                    sb.append("|");
                }
                sb.append("\n");
                bw.write(sb.toString());
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        serverCoordsReadyFlag = true;
    });


    public void initialize() throws IOException {
        System.out.println("Server");
        gc = canvas.getGraphicsContext2D();
        infoLabel.setText("Расположите свои танки");
        player2Label.setText(name + "(Вы)");
        readyFlag = false;
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        canvas.setOnMouseClicked(this::onMouseClicked);
        doAccept.setDaemon(true);
        doAccept.start();
        waitToStartGame.setDaemon(true);
        waitToStartGame.start();
    }


    private void onMouseClicked(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        if (readyFlag == false) {
            if (x > canvas.getWidth() / 2 + 10 && x < canvas.getWidth() - 10 && y > 10 && y < canvas.getHeight() - 10) {
                boolean flag = true;
                for (int i = 0; i < serverTanks.size(); i++) {
                    Tank tank = serverTanks.get(i);
                    if (x < tank.getX() + 20 && x > tank.getX() - 20 && y < tank.getY() + 20 && y > tank.getY() - 20) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    gc.strokeRect(x - 10, y - 10, 20, 20);
                    gc.strokeOval(x - 3, y - 3, 6, 6);
                    gc.strokeLine(x - 3, y, x - 6, y);
                    serverTanks.add(new Tank(x, y));
                }
            }
            if (serverTanks.size() == 2) {
                readyFlag = true;
                infoLabel.setText("Ожидание соперника...");
                writeServerCoords.setDaemon(true);
                writeServerCoords.start();
            }
        } else {
            if (x > canvas.getWidth() / 2 + 5 && x < canvas.getWidth() - 5 && y > 5 && y < canvas.getHeight() - 5)
                if (shotTimeFlag) {
                    xx = canvas.getWidth() - x;
                    yy = y;
                    doShot(x, y);
                }
        }
    }

    private void doShot(double x, double y) {

        gc.setFill(Color.BLUE);
        //выстрел на своей половине
        gc.fillOval(x - shotDiameter / 2, y - shotDiameter / 2, shotDiameter, shotDiameter);
        //выстрел на вражеской половине
        gc.fillOval(canvas.getWidth() - (x + shotDiameter / 2), y - shotDiameter / 2, shotDiameter, shotDiameter);
        //отзеркалим x для удобства
        x = canvas.getWidth() - x;
        for (int i = 0; i < clientTanks.size(); i++) {
            double tx = clientTanks.get(i).getX();
            double ty = clientTanks.get(i).getY();
            //проверка на прямое убийство
            if (x > tx - shotDiameter / 2 && x < tx + shotDiameter / 2 &&
                    y > ty - shotDiameter / 2 && y < ty + shotDiameter / 2) {
                //рисуем две красные линии
                drawTwoRedLines(tx, ty);
                //удаляем танк из списка
                clientTanks.remove(i);
            } else { //проверяем на попадание
                boolean injuredFlag = false;
                for (double jX = x - shotDiameter / 2; jX <= x + shotDiameter / 2; jX += 0.1) {
                    double jY1 = Math.sqrt(Math.pow(shotDiameter / 2, 2) - Math.pow(jX - x, 2)) + y;
                    double jY2 = (-1) * Math.sqrt(Math.pow(shotDiameter / 2, 2) - Math.pow(jX - x, 2)) + y;
                    if ((jX > tx - 10 && jX < tx + 10 && jY1 < ty + 10 && jY1 > ty - 10) ||
                            (jX > tx - 10 && jX < tx + 10 && jY2 < ty + 10 && jY2 > ty - 10)) {
                        injuredFlag = true;
                        break;
                    }
                }
                //если попадание
                if (injuredFlag) {
                    //если танк уже был ранен
                    if (clientTanks.get(i).isInjured()) {
                        //рисуем две красные линии
                        drawTwoRedLines(tx, ty);
                        //удаляем танк из списка
                        clientTanks.remove(i);
                    } else { //если танк не был ранен
                        //устанавливаем пометку о ранении
                        clientTanks.get(i).setInjured(true);
                        //рисуем одну красную линию
                        drawOneRedLine(tx, ty);
                    }
                }
            }
        }
        if (clientTanks.size() == 0)
            closeApp("Вы победили");
        shotTimeFlag = false;
        shotReadyFlag = true;

    }

    public void doOpponentShot(double x, double y) {
        for (int i = 0; i < serverTanks.size(); i++) {
            double tx = serverTanks.get(i).getX();
            double ty = serverTanks.get(i).getY();
            //проверка на прямое убийство
            if (x > tx - shotDiameter / 2 && x < tx + shotDiameter / 2 &&
                    y > ty - shotDiameter / 2 && y < ty + shotDiameter / 2) {
                //рисуем две красные линии
                drawTwoRedLines(tx, ty);
                //удаляем танк из списка
                serverTanks.remove(i);
            } else { //проверяем на попадание
                boolean injuredFlag = false;
                for (double jX = x - shotDiameter / 2; jX <= x + shotDiameter / 2; jX += 0.1) {
                    double jY1 = Math.sqrt(Math.pow(shotDiameter / 2, 2) - Math.pow(jX - x, 2)) + y;
                    double jY2 = (-1) * Math.sqrt(Math.pow(shotDiameter / 2, 2) - Math.pow(jX - x, 2)) + y;
                    if ((jX > tx - 10 && jX < tx + 10 && jY1 < ty + 10 && jY1 > ty - 10) ||
                            (jX > tx - 10 && jX < tx + 10 && jY2 < ty + 10 && jY2 > ty - 10)) {
                        injuredFlag = true;
                        break;
                    }
                }
                //если попадание
                if (injuredFlag) {
                    //если танк уже был ранен
                    if (serverTanks.get(i).isInjured()) {
                        //рисуем две красные линии
                        drawTwoRedLines(tx, ty);
                        //удаляем танк из списка
                        serverTanks.remove(i);
                    } else { //если танк не был ранен
                        //устанавливаем пометку о ранении
                        serverTanks.get(i).setInjured(true);
                        //рисуем одну красную линию
                        drawOneRedLine(tx, ty);
                    }
                }
            }
        }
        if (serverTanks.size() == 0)
            closeApp("Вы проиграли");

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private void drawOpponentShot(double x, double y) {
        gc.setFill(Color.RED);
        gc.fillOval(x - shotDiameter / 2, y - shotDiameter / 2, shotDiameter, shotDiameter);
    }

    private void drawTwoRedLines(double x, double y) {
        gc.setStroke(Color.RED);
        gc.strokeLine(x - 10, y + 10, x + 10, y - 10);
        gc.strokeLine(x - 10, y - 10, x + 10, y + 10);
    }

    private void drawOneRedLine(double x, double y) {
        gc.setStroke(Color.RED);
        gc.strokeLine(x - 10, y + 10, x + 10, y - 10);
    }

    public void setInfoLabel(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                infoLabel.setText(text);
            }
        });
    }

    public void closeApp(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(text);
                alert.showAndWait();
                try {
                    server.close();
                    bw.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }
}