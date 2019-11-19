package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class Main extends Application {

    private static final String FIN_DE_LIGNE = ";" ; // pour l'instant on delimite une ligne au " ; "
    private static ArrayList<String> liste_token  = null ;
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

        bar.getMenus().addAll(menu1,menu2,menu3,menu4);

        liste_token = new ArrayList<>();

        // create a File chooser
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension
        fil_chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("pdf files","*.pdf"),
                new FileChooser.ExtensionFilter("text files", "*.txt"),
                new FileChooser.ExtensionFilter("dot files","*.dot"));
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
                                /*Scanner scan = new Scanner(s);
                                scan.useDelimiter(" "); // on sépare les token grace a l'espace
                                String token ;
                                while(scan.hasNext()){
                                    token = scan.next() ;
                                    //System.out.println(scan.next());
                                    if ( token.toString() == FIN_DE_LIGNE )
                                        consumer.accept("// Fin de ligne.");
                                    else if ( token != FIN_DE_LIGNE ) {
                                        consumer.accept("token : " + token); // affichage du token
                                    }


                                }
                                scan.close();
                                */

                                s = s.trim();
                                if ( !s.equals("") && !s.equals(" ")) {
                                    Set_liste_token(s);
                                    ma_list.add(s);
                                }
                                //consumer.accept(s.toString());
                            });
                            list.forEach(s->lv.getItems().add(s.toString()));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                };

        ListView<String> list_token = new ListView<>();

        lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String token_tmp = lv.getSelectionModel().getSelectedItem();
                System.out.println("clicked on : " + token_tmp);
                liste_token.clear();
                Set_liste_token(token_tmp);
                if (list_token.getItems() != null ){
                    list_token.getItems().clear();
                }
                for (int i = 0; i < liste_token.size() ; i++) {
                    if ( !liste_token.get(i).equals(" "))
                    list_token.getItems().add(liste_token.get(i)); // ajout a la list_view le contenu de la liste contenant les token
                }
            }
        });

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
        pane.setLeft(lv);
        pane.setRight(list_token);
        lv.setMinWidth(500); // fixe la taille min de la liste view
        list_token.setMinWidth(500); // fixe la taille min de la liste view
        root.getChildren().add(pane);
        primaryStage.setScene(new Scene(root, 1200, 375));
        primaryStage.show();
    }

    public void Set_liste_token(String s){
        /**
         * Contenu entre parentheses
         */
        if ( s.contains("(") && s.contains(")")){ // donc du contenu entre parentheses
            int par_ouvrante = s.lastIndexOf("(") ;
            int par_fermante = s.lastIndexOf(")") ;
            Analyse_token(s , par_ouvrante , par_fermante);
        }
        s = s.trim();
        Scanner scan = new Scanner(s);
        scan.useDelimiter(" "); // on sépare les token grace a l'espace
        String token ;
        while(scan.hasNext()){
            token = scan.next() ;
            if (token.length() == 1 && Character.isDigit(token.charAt(0))){
                System.out.println("Chiffre : " + token);
            }
            if ( token.toUpperCase().equals(token) && !token.equals("=") && !token.equals("$$") ) // Mot clé en masjuscule :
                System.out.println("Mot clé : " + token);
            //System.out.println(scan.next());
            if ( token != FIN_DE_LIGNE ) {
                if ( !token.equals(" ") || !token.equals('\n') || token.equals("") || token.equals('\t') ) {
                    liste_token.add(token);
                    //System.out.println("token : " + token); // affichage du token
                }
            }
        }
        scan.close();
    }

    public void Analyse_token(String s ,int ouvr , int ferm){
        String token ;
        s = s.substring(ouvr+1,ferm); // permet d'obtenir le contenue de la parenthese
        System.out.println("Contenu trouvé entre parenthese : "+ s);
        Scanner scan = new Scanner(s);
        scan.useDelimiter(",");
        while(scan.hasNext()){
            token = scan.next();
            Scanner scan_tmp = new Scanner(token);
            scan_tmp.useDelimiter(" ");
            while(scan_tmp.hasNext()) {
                token = scan_tmp.next(); // nom variable
                System.out.println("variable : " + token );
                if ( scan_tmp.hasNext() ){
                    token = scan_tmp.next(); // type de la variable
                    System.out.println("TYPE DE LA VARIABLE : " + token);
                }
            }
        }

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
