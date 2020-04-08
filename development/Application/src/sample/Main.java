package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Parser.GogolParser;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        /*GogolParser gogolParser = new GogolParser("./src/dependencies.gogol");*/
        Parent root = FXMLLoader.load(getClass().getResource("View/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();

    }


    public static void main(String[] args) { launch(args);
    }
}
