package sample;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        List<String> list = getParameters().getRaw();

        if (list.size() == 2) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root = loader.load();
            Controller controller = loader.getController();
            primaryStage.setScene(new Scene(root, 600, 700));
            primaryStage.setTitle("File Sharing");
            primaryStage.show();

            controller.init(list.get(0), list.get(1));
        } else {
            System.out.println("Need two parameters: <computerName> <localDirectory>");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}