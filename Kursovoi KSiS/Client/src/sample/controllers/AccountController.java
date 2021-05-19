package sample.controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sample.Main;
import sample.dataclasses.RuExchanger;

import java.io.*;
import java.net.MalformedURLException;

import static sample.controllers.EnterController.br;
import static sample.controllers.EnterController.bw;
import static sample.controllers.EnterController.socket;

public class AccountController {
    @FXML
    private ImageView imageView;
    @FXML
    private Label nameText;
    @FXML
    private Button changeButton;
    @FXML
    private Text ruPlaysText;
    @FXML
    private Text enSpeedCText;
    @FXML
    private Text enSpeedWText;
    @FXML
    private Text enMistakesText;
    @FXML
    private Button exitButton;
    @FXML
    private ToggleButton ruSearchButton;
    @FXML
    private ToggleButton enSearchButton;
    @FXML
    private Text ruSpeedCText;
    @FXML
    private Text ruSpeedWText;
    @FXML
    private Text ruMistakesText;
    @FXML
    private Text enPlaysText;

    public static String name;
    public static int ruPlays;
    public static float ruSpeedC;
    public static float ruSpeedW;
    public static float ruMistakes;
    public static int enPlays;
    public static float enSpeedC;
    public static float enSpeedW;
    public static float enMistakes;

    public static byte[] icon;

    Thread waitForGame = new Thread(() -> {
        try {
            //Ждём приглашения в игру
            String game = br.readLine();
            if (game.equals("ru"))
                MainController.mode = "ru";
            else
                MainController.mode = "en";
            System.out.println("Приняли " + game);
            // Отправляем стоп-сигнал для потока SearchWaiter
            bw.write("StopSearchWaiter\n");
            bw.flush();
            //Открываем главное окно
            showMainWindow();


        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    public void initialize() {

        //Отображаем элементы окна
        nameText.setText(name);
        ruPlaysText.setText(Integer.toString(ruPlays));
        ruSpeedCText.setText(Float.toString(ruSpeedC));
        ruSpeedWText.setText(Float.toString(ruSpeedW));
        ruMistakesText.setText(Float.toString(ruMistakes));
        enPlaysText.setText(Integer.toString(enPlays));
        enSpeedCText.setText(Float.toString(enSpeedC));
        enSpeedWText.setText(Float.toString(enSpeedW));
        enMistakesText.setText(Float.toString(enMistakes));

        //Отображаем иконку
        setImage();

        //Запускаем поток, который ждёт приглашения в игру
        waitForGame.setDaemon(true);
        waitForGame.start();


        //...........ОБРАБОТЧИКИ КНОПОК.................

        ruSearchButton.setOnAction(actionEvent -> {
            if (ruSearchButton.isSelected()) {
                ruSearchButton.setText("Поиск");
                enSearchButton.setVisible(false);
                try {
                    bw.write("StartRuSearch\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ruSearchButton.setText("Искать");
                enSearchButton.setVisible(true);
                try {
                    bw.write("StopRuSearch\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        enSearchButton.setOnAction(actionEvent -> {
            if (enSearchButton.isSelected()) {
                enSearchButton.setText("Поиск");
                ruSearchButton.setVisible(false);
                try {
                    bw.write("StartEnSearch\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                enSearchButton.setText("Искать");
                ruSearchButton.setVisible(true);
                try {
                    bw.write("StopEnSearch\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        changeButton.setOnAction(actionEvent -> {
//            TranslateTransition translateTransition = new TranslateTransition();
//            translateTransition.setDuration(Duration.millis(1000));
//            translateTransition.setNode(imageView);
//            translateTransition.setByX(300);
//            translateTransition.setAutoReverse(false);
//            translateTransition.play();
        });
    }

    public void setImage() {
        try {
            FileOutputStream fos = new FileOutputStream("files/you/" + name);
            fos.write(icon, 0, icon.length);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File icon = new File("files/you/" + name);
        String localURL = "";
        try {
            localURL = icon.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Image image = new Image(localURL, 200, 200, false, true);
        imageView.setImage(image);
    }

    public void showMainWindow() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/sample/forms/MainWindow.fxml"));
                Parent parent = null;
                try {
                    parent = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Scene scene = new Scene(parent);
                Main.stage.setTitle("Игра");
                Main.stage.setScene(scene);
            }
        });

    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(text);
        alert.showAndWait();
    }
}