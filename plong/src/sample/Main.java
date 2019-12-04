package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("PLONG");
        Pane root = new Pane();
        BorderPane pane = new BorderPane();
        //menuBar
        MenuBar bar = new MenuBar();
        //menu
        Menu menu1 = new Menu("File");
        Menu menu3 = new Menu("View");
        Menu menu4 = new Menu("Naviate");
        Menu menu_test = new Menu("Test");
        MenuItem item5 = new MenuItem("fenetre");
        item5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    new Graphic();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        menu_test.getItems().addAll(item5);
        //itemMenu
        MenuItem item1 = new MenuItem("Open");
        MenuItem item3 = new MenuItem("Save as");
        MenuItem item2 = new MenuItem("Quit");
        item2.setOnAction(e-> Platform.exit());//gere le Quit : crtl-Q
        menu1.getItems().addAll(item1,item2,item3);

        Menu menu2 = new Menu("Edit");
        RadioMenuItem rmi = new RadioMenuItem("Edit");
        MenuItem itemE1 = new MenuItem("Add");
        itemE1.setDisable(true);
        itemE1.disableProperty().bind(rmi.selectedProperty().not());
        menu2.getItems().addAll(rmi,itemE1);

        bar.getMenus().addAll(menu1,menu2,menu3,menu4,menu_test);


        // create a File chooser
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension
        fil_chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("dot files","*.dot"),
                new FileChooser.ExtensionFilter("pdf files","*.pdf"),
                new FileChooser.ExtensionFilter("text files", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        // create a Label
        Label label = new Label("no files selected");

        List<String> ma_list = new ArrayList<String>();
        ObservableList<String> list = FXCollections.observableList(ma_list);
        ListView<String> lv = new ListView();
        // Add the CellFactory to the ListView
        lv.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new TextFieldListCell<String>(new DefaultStringConverter());
            }
        });
        EventHandler<ActionEvent> event2 = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fil_chooser2 = new FileChooser();
                File file = fil_chooser2.showSaveDialog(primaryStage);
                fil_chooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Lst files","*.lst"));
                if (file != null) {
                    String s = "";
                    for (String item : lv.getItems()) {
                        s+=item+'\n';
                    }
                    writer(file,s);
                }
            }
        };
        // create an Event Handler
        EventHandler<ActionEvent> event1 =
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        // get the file selected
                        File file = fil_chooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            label.setText(file.getAbsolutePath()
                                    + "  selected");
                        }
                        //read file into stream, try-with-resources
                        Path path = Paths.get(file.getAbsolutePath());
                        //consumer appelé lors de .accept plus tard
                        Consumer<String> consumer = i -> System.out.println(i);
                        try (Stream<String> stream = Files.lines(path)) {
                            stream.forEach(s->{
                                ma_list.add(s);
                                consumer.accept(s.toString());
                            });
                            list.forEach(s->lv.getItems().add(s.toString()));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                };
        item1.setOnAction(event1);
        item3.setOnAction(event2);
        //gestion de l'editabilité des eltss de lv
        lv.editableProperty().bind(rmi.selectedProperty());
        //le double click est gerer de base pour l'edition dans lv
        /*rmi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });*/
        //gestion du add
        itemE1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String s = new String();
                s = " ";
                lv.getItems().add(s);
            }
        });
        pane.setTop(bar);
        pane.setCenter(lv);
        root.getChildren().add(pane);
        primaryStage.setScene(new Scene(root, 600, 275));
        primaryStage.show();
    }

    public void writer(File fichier, String data) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fichier);
            writer.println(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
