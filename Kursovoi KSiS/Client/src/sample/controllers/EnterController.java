package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import sample.Main;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class EnterController {
    @FXML
    private TextField loginTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button enterButton;
    @FXML
    private Button registrationButton;

    public static Socket socket;
    public static BufferedReader br;
    public static BufferedWriter bw;

    public void initialize() {
        connectToServer("localhost", 5555); //подключение к серверу и инициализация потоков ввода/вывода

        registrationButton.setOnAction(actionEvent -> {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/sample/forms/RegistrationWindow.fxml"));
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                Main.stage.setTitle("Регистрация");
                Main.stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        enterButton.setOnAction(actionEvent -> {
            String login = loginTextField.getText().trim();
            String password = passwordTextField.getText().trim();
            if (login.equals("") || password.equals("||")) {
                showAlert("Данные не введены");
                return;
            }
            try {
                bw.write("log" + "|" + login + "|" + password + "\n");
                bw.flush();
                String response = br.readLine();
                if (response.equals("UserNotFound")) {
                    showAlert("Введены неверные данные");
                    return;
                } else if (response.equals("UserAlreadyLogged")) {
                    showAlert("Данный пользователь уже вошёл");
                    return;
                } else {
                    System.out.println(response);
                    String[] params = response.split("\\|");
                    //Устанавливаем статистику пользователя в окне аккаунта
                    AccountController.name = params[0];
                    AccountController.ruPlays = Integer.parseInt(params[1]);
                    AccountController.ruSpeedC = Float.parseFloat(params[2]);
                    AccountController.ruSpeedW = Float.parseFloat(params[3]);
                    AccountController.ruMistakes = Float.parseFloat(params[4]);
                    AccountController.enPlays = Integer.parseInt(params[5]);
                    AccountController.enSpeedC = Float.parseFloat(params[6]);
                    AccountController.enSpeedW = Float.parseFloat(params[7]);
                    AccountController.enMistakes = Float.parseFloat(params[8]);
                    //Получаем иконку пользователя
                    while (socket.getInputStream().available() == 0) ;
                    int size = socket.getInputStream().available();
                    System.out.println(size);
                    AccountController.icon = new byte[size];
                    socket.getInputStream().read(AccountController.icon, 0, size);
                    FileOutputStream fos = new FileOutputStream("files/you/" + AccountController.name);
                    fos.write(AccountController.icon);
                    fos.close();

                    //Открываем окно аккаунта
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/sample/forms/AccountWindow.fxml"));
                    Parent parent = loader.load();
                    Scene scene = new Scene(parent);
                    Main.stage.setTitle("Аккаунт");
                    Main.stage.setScene(scene);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }

    private void connectToServer(String host, int port) {
        if (socket == null) {
            try {
                socket = new Socket(host, port);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (ConnectException e) {
                showAlert("Не удалось подключиться к серверу");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSocket() {
        try {
            br.close();
            bw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(text);
        alert.showAndWait();
    }
}
