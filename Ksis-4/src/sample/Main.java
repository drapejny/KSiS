package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;
    public static Scene scene;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample/forms/ConnectWindow.fxml"));
        primaryStage.setScene(new Scene(root));
        stage = primaryStage;
        scene = primaryStage.getScene();
        Main.stage.setTitle("Окно подключения");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
