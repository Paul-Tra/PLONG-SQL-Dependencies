package sample;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Graphic {
    private final static int rangeMin = 0; // pour RGB
    private final static int rangeMax = 1;// pour RGB
    private static int width_win = 600;
    private static int height_win = 600;
    private Stage ma_stage;
    private Scene ma_scene;
    private Pane mon_pane;

    public Graphic() throws InterruptedException {
        create_stage(1,rand(), rand(), rand());
    }


    private void create_stage(int indice, double r, double g, double b) {
        Stage stage = new Stage();
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.color(r,g,b,1), CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(pane,width_win,height_win);
        stage.setScene(scene);
        stage.setTitle("Fenetre_"+indice);
        stage.show();
        ma_stage = stage;
        ma_scene = scene;
        mon_pane = pane;
        //tab_rect[indice] = new Rectangle();
    }

    private double rand() throws InterruptedException {
        Random r = new Random();
        sleep(100);
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue;
    }
}
