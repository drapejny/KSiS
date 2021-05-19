package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("forms/EnterWindow.fxml"));
        primaryStage.setScene(new Scene(root));
        stage = primaryStage;
        primaryStage.show();

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        launch(args);
    }
}
