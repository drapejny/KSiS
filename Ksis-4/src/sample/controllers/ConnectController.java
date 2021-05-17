package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import sample.Main;

import java.io.IOException;
import java.net.*;

public class ConnectController {
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Button createButton;

    public void initialize() {

        createButton.setOnAction(actionEvent -> {
            try {
                if(nameTextField.getText().trim().equals("")) {
                    showAlert("Не введен никнейм");
                    return;
                }
                int port = Integer.parseInt(portTextField.getText().trim());

                ServerController.server = new ServerSocket(port);

                ServerController.name = nameTextField.getText().trim();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/sample/forms/ServerWindow.fxml"));
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                Main.scene = scene;
                Main.stage.setTitle("Сервер");
                Main.stage.setScene(scene);

            } catch (NumberFormatException e) {
                showAlert("Некорректный ввод порта");
            } catch (BindException e) {
                showAlert("Введенный порт уже используется");
            } catch (IllegalArgumentException e) {
                showAlert("Неверный ввод порта");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        connectButton.setOnAction(actionEvent -> {
            try {
                if(nameTextField.getText().trim().equals("")) {
                    showAlert("Не введен никнейм");
                    return;
                }
                String ip = ipTextField.getText().trim();
                InetAddress inetAddress  = InetAddress.getByName(ip);
                int port = Integer.parseInt(portTextField.getText().trim());

                ClientController.socket = new Socket(inetAddress,port);

                ClientController.name = nameTextField.getText().trim();


                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/sample/forms/ClientWindow.fxml"));
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                Main.scene = scene;
                Main.stage.setTitle("Клиент");
                Main.stage.setScene(scene);

            } catch(ConnectException e){
                showAlert("Сервер еще не запущен");
            }
            catch (UnknownHostException e){
                showAlert("Неверный ввод IP");
            }
            catch (NumberFormatException e) {
                showAlert("Некорректный ввод порта");
            } catch (IllegalArgumentException e) {
                showAlert("Неверный ввод порта");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void showAlert(String textOfError) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(textOfError);
        alert.showAndWait();
    }
}
