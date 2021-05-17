package sample.controllers;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import sample.elements.Tank;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;

public class ClientController {

    @FXML
    private Canvas canvas;
    @FXML
    private Label player1Label;
    @FXML
    private Label player2Label;
    @FXML
    private Label infoLabel;

    public static Socket socket;
    public static String name;

    GraphicsContext gc;
    boolean readyFlag = false;

    public ArrayList<Tank> serverTanks = new ArrayList<>();
    public ArrayList<Tank> clientTanks = new ArrayList<>();

    double shotDiameter = 10;

    boolean shotTimeFlag = false;
    boolean shotReadyFlag = false;

    boolean serverCoordsReadyFlag = false;
    boolean clientCoordsReadyFlag = false;

    BufferedWriter bw;
    BufferedReader br;

    double xx;
    double yy;
    Thread exchanging = new Thread(() -> {
        try {
            while (true) {
                setInfoLabel("Ход соперника");
                String[] coords = br.readLine().split("\\|");
                System.out.println("Принял");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                drawOpponentShot(x, y);
                doOpponentShot(x, y);
                shotTimeFlag = true;
                setInfoLabel("Ваш ход");
                while (shotReadyFlag != true) {
                    drawOpponentShot(x, y);
                    Thread.sleep(100);
                }
                bw.write(xx + "|" + yy + "\n");
                bw.flush();
                System.out.println("Отправил");
                shotReadyFlag = false;
                shotTimeFlag = false;

            }
        } catch (SocketException e) {
            closeApp("Соединение с соперником потеряно");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    });

    Thread waitToStartGame = new Thread(() -> {
        while(serverCoordsReadyFlag == false || clientCoordsReadyFlag == false) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        exchanging.setDaemon(true);
        exchanging.start();
    });

    Thread readStartCoords = new Thread(() -> {
        try {
            //Чтение имени сервера и координатов его танков
            String serverName = br.readLine();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    player2Label.setText(serverName);
                }
            });
            String[] serverCoords = br.readLine().split("\\|");
            for (int i = 0; i < serverCoords.length; i += 2) {
                Double x = Double.parseDouble(serverCoords[i]);
                Double y = Double.parseDouble(serverCoords[i + 1]);
                serverTanks.add(new Tank(x, y));
            }
            //Отображение танков сервера
            for (int i = 0; i < serverTanks.size(); i++) {
                Double x = serverTanks.get(i).getX();
                Double y = serverTanks.get(i).getY();
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.strokeRect(x - 10, y - 10, 20, 20);
                gc.strokeOval(x - 3, y - 3, 6, 6);
                gc.strokeLine(x - 3, y, x - 6, y);
            }
        } catch (SocketException e) {
            closeApp("Соединение с соперником потеряно");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverCoordsReadyFlag = true;
    });
    Thread writeStartsCoords = new Thread(() -> {
        try {
            //Отправка имени клиента и координатов танков клиента на сервер
            bw.write(name + "\n");
            bw.flush();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < clientTanks.size(); i++) {
                sb.append(clientTanks.get(i).getX());
                sb.append("|");
                sb.append(clientTanks.get(i).getY());
                sb.append("|");
            }
            sb.append("\n");
            bw.write(sb.toString());
            bw.flush();

        } catch (SocketException e) {
            closeApp("Соединение с соперником потеряно");
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientCoordsReadyFlag = true;
    });

    boolean isDuoConnectionFlag = false;

    Thread checkDuoConnection = new Thread(() ->{
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    Thread waitPingByServer = new Thread(() -> {
        try {
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isDuoConnectionFlag = true;
    });
    public void initialize() throws IOException, InterruptedException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        readStartCoords.setDaemon(true);
        readStartCoords.start();
        waitToStartGame.setDaemon(true);
        waitToStartGame.start();
        gc = canvas.getGraphicsContext2D();
        System.out.println("Client");
        infoLabel.setText("Расположите свои танки");
        player1Label.setText(name + "(Вы)");
        readyFlag = false;
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        canvas.setOnMouseClicked(this::onMouseClicked);
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
    }

    private void onMouseClicked(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        if (readyFlag == false) {
            if (x > 10 && x < canvas.getWidth() / 2 - 10 && y > 10 && y < canvas.getHeight() - 10) {
                boolean flag = true;
                for (int i = 0; i < clientTanks.size(); i++) {
                    Tank tank = clientTanks.get(i);
                    if (x < tank.getX() + 20 && x > tank.getX() - 20 && y < tank.getY() + 20 && y > tank.getY() - 20) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    gc.strokeRect(x - 10, y - 10, 20, 20);
                    gc.strokeOval(x - 3, y - 3, 6, 6);
                    gc.strokeLine(x + 3, y, x + 6, y);
                    clientTanks.add(new Tank(x, y));
                }
            }
            if (clientTanks.size() == 2) {
                setInfoLabel("Ожидание соперника");
                readyFlag = true;
                writeStartsCoords.start();
            }
        } else {
            if (x > shotDiameter / 2 && x < canvas.getWidth() / 2 - shotDiameter / 2 && y > shotDiameter / 2 && y < canvas.getHeight() - shotDiameter / 2)
                if (shotTimeFlag) {
                    xx = canvas.getWidth() - x;
                    yy = y;
                    shotReadyFlag = true;
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
            closeApp("Вы победили");
        shotTimeFlag = false;

    }

    public void doOpponentShot(double x, double y) {
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
            closeApp("Вы проиграли");
        shotTimeFlag = false;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void drawOpponentShot(double x, double y) {
        gc.setFill(Color.RED);
        gc.fillOval(x - shotDiameter / 2, y - shotDiameter / 2, shotDiameter, shotDiameter);
    }

    public void closeApp(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(text);
                alert.showAndWait();
                try {
                    socket.close();
                    bw.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
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
}

