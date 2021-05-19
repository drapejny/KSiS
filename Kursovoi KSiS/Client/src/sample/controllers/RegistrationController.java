package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAction;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import sample.Main;

import javax.swing.text.FlowView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static sample.controllers.EnterController.*;

public class RegistrationController {
    @FXML
    private TextField loginTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button registrationButton;
    @FXML
    private ImageView imageView;
    @FXML
    private Button chooseButton;

    File file; //картинка

    public void initialize() {
        System.out.println("777");
        registrationButton.setOnAction(actionEvent -> {
            String login = loginTextField.getText().trim();
            String password = passwordTextField.getText().trim();
            if (login.equals("") || password.equals("") || imageView.getImage() == null) {
                showAlert("Неверный ввод");
                return;
            }
            try {
                //Отправляем данные о новом пользователе
                bw.write("reg" + "|" + login + "|" + password + "\n");
                bw.flush();
                //Отправляем иконку
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = fis.readAllBytes();
                fis.close();
                socket.getOutputStream().write(buffer,0,buffer.length);
                socket.getOutputStream().flush();
                //Читаем ответ от сервера
                String response = br.readLine();
                if(response.equals("UserAlreadyExist")){
                    showAlert("Пользователь с данным именем уже существует");
                    return;

                } else {
                    String[] params  = response.split("\\|");
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
                    //Сохраняем иконку
                    FileOutputStream fos = new FileOutputStream("files/you/" + AccountController.name);
                    fos.write(buffer,0,buffer.length);
                    fos.close();
                    AccountController.icon = buffer;
                    //Открываем новое окно с информацией об аккаунте
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
        chooseButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
            file = fileChooser.showOpenDialog(chooseButton.getScene().getWindow());
            String localURL = "";
            try {
                localURL = file.toURI().toURL().toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Image image = new Image(localURL, 50, 50, false, true);
            imageView.setImage(image);

        });
    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(text);
        alert.showAndWait();
    }
}
