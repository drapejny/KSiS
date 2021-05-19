package sample.controllers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static sample.controllers.EnterController.bw;
import static sample.controllers.EnterController.br;
import static sample.controllers.EnterController.socket;

public class MainController {
    @FXML
    private TextField textField;
    @FXML
    private Text modeText;
    @FXML
    private Text roomText;
    @FXML
    private Text timerText;
    @FXML
    private Text yourNameText;
    @FXML
    private Text opponentNameText;
    @FXML
    private ImageView yourImageView;
    @FXML
    private ImageView opponentImageView;
    @FXML
    private Text text0;
    @FXML
    private Text text1;
    @FXML
    private Text text2;

    public static String mode;

    String[] textLines = new String[3];
    int lineNum = 0;

    Thread timer = new Thread(() -> {
        for (int i = 5; i > 0; i--) {
            timerText.setText(Integer.toString(i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timerText.setText("Go!");
    });


    public void initialize() {
        //Устанавливаем режим
        modeText.setText(mode);
        //Устанавливаем собственное имя
        yourNameText.setText(AccountController.name);
        //Устанавливаем собственную иконку
        setYourImage(AccountController.icon);
        //Ждём и устанавливаем имя оппонента
        try {
            String opponentName = br.readLine();
            opponentNameText.setText(opponentName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Ждём и устанавливаем иконку оппонента
        try {
            while (socket.getInputStream().available() == 0) ;
            int size = socket.getInputStream().available();
            byte[] buffer = new byte[size];
            socket.getInputStream().read(buffer, 0, size);
            setOpponentImage(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Получаем текст
        try {
            textLines[0] = br.readLine();
            textLines[1] = br.readLine();
            textLines[2] = br.readLine();
            //Устанавливаем текст
            text0.setText(textLines[0]);
            text1.setText(textLines[1]);
            text2.setText(textLines[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.setDaemon(true);
        timer.start();

        //.....................ОБРАБОТЧИКИ ЭЛЕМЕНТОВ.......................

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String line = textLines[lineNum];
            int charactersNum = newValue.length();
            if (charactersNum == line.length() && newValue.equals(line)) {
                if (lineNum == 2) {
//                    try {
//                        bw.write("finish\n");
//                        bw.flush();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    text0.setText("");
                    textField.clear();
                    textField.setDisable(true);
                    lineNum = 0;
                } else if (lineNum == 1) {
                    text0.setText(textLines[2]);
                    text1.setText("");
                    lineNum++;
                    textField.clear();
                } else if (lineNum == 0) {
                    text0.setText(textLines[1]);
                    text1.setText(textLines[2]);
                    text2.setText("");
                    lineNum++;
                    textField.clear();
                }
            } else if (charactersNum > line.length()) {
                textField.setStyle("-fx-background-color: #FFFF00");
            } else if (newValue.equals(textLines[lineNum].substring(0, charactersNum))) {
                textField.setStyle("-fx-background-color: #FFFFFF");
            } else
                textField.setStyle("-fx-background-color: #FFFF00");
        });
    }

    public void setOpponentImage(byte[] icon) {
        try (FileOutputStream fos = new FileOutputStream("files/opponent/" + opponentNameText.getText())) {
            fos.write(icon, 0, icon.length);
            File file = new File("files/opponent/" + opponentNameText.getText());
            String localURL = "";
            try {
                localURL = file.toURI().toURL().toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Image image = new Image(localURL, 50, 50, false, true);
            opponentImageView.setImage(image);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setYourImage(byte[] icon) {
        try (FileOutputStream fos = new FileOutputStream("files/you/" + yourNameText.getText())) {
            fos.write(icon, 0, icon.length);
            File file = new File("files/you/" + yourNameText.getText());
            String localURL = "";
            try {
                localURL = file.toURI().toURL().toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Image image = new Image(localURL, 50, 50, false, true);
            yourImageView.setImage(image);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
