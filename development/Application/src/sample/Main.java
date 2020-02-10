package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer;

public class Main extends Application {
    public static Consumer<String> consumer = e -> System.out.println(e);
    String fichier = "";
    protected static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        String path = "/home/paul/Documents/M1/Projet Long/temp/cadiou-traore-plong-1920/development/Application/src/dependences.gogol";
        //ParserG parserG = new ParserG(path);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        Parent root = FXMLLoader.load(getClass().getResource("Fenetre.fxml"));
        primaryStage.setTitle("Graph User Interface");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
