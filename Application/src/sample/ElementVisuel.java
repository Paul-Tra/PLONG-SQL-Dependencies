package sample;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class ElementVisuel {
    private double oldX, oldY;
    private final int DEFAULT_MARGE_X_NOM_NODE = 10;
    private final int DEFAULT_MARGE_Y_NOM_NODE = 5;
    private final int TAILLE_FLECHE = 10; // taille du bout de la fleche
    private final double ANGLE_FLECHE = 45; // angle d'un cote du bout de la fleche par rapport au noeud d'arrivee
    private final double COEFFICIENT_CONTROLE = 0.3;// coefficent de longueur/hauteur pour placer les points de controles
    private Pane pane; // panneau auquel l'element visuel est asssocié
    private Consumer<String> consumer = e -> System.out.println(e);
    ArrayList<Shape> list_shape; // listes des elements a inserer dans la vue
    public ElementVisuel(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction, Pane p) {
        list_shape = new ArrayList<>();
        pane = p;
        createShape(l_relation,l_transaction);
    }

    private void createShape(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction) {
        if (l_transaction != null) {
            for (Transaction transaction : l_transaction) {
                Rectangle r = createNodeRectange(transaction);
                Text text = new Text(transaction.id);
                text.setManaged(false);
                text.setX(r.getX());
                text.setY(r.getY());
                text.setLayoutX(r.getLayoutX());
                text.setLayoutY(r.getLayoutY());
                // on "colle" le text avec le rectangle
                //text.xProperty().bind(Bindings.add(DEFAULT_MARGE_X_NOM_NODE, r.xProperty()));
                //text.yProperty().bind(Bindings.add((text.getLayoutBounds().getHeight() + DEFAULT_MARGE_Y_NOM_NODE / 2), r.yProperty()));
                text.layoutXProperty().bind(r.layoutXProperty().add(DEFAULT_MARGE_X_NOM_NODE));
                text.layoutYProperty().bind(r.layoutYProperty().add((DEFAULT_MARGE_Y_NOM_NODE/2)+text.getLayoutBounds().getHeight()));
                addHandlerRectangle(r);
                list_shape.add(text);
                list_shape.add(r);
            }
        }else{
            consumer.accept("liste de translation null");
        }
        if (l_relation != null) {
            for (Relation relation : l_relation) {
                // TODO : faire les ligne avec des fleche
                String source = relation.source;
                String destination = relation.destination;
                Rectangle r_source = matchRectangleById(source);
                Rectangle r_dest = matchRectangleById(destination);
                if (r_dest == null || r_source == null) {
                    consumer.accept("source ou destination null");
                    continue;
                }
                //  TODO: recup cote
                int cote = 2;
                // les plus 100 sont la que pour les test pour l'instant
                Path p = createFleche(relation.nom, r_source.getX(), r_source.getY(), r_dest.getX()+100, r_dest.getY()+100, cote);
                list_shape.add(p);
            }
        }else{
            consumer.accept("liste de relation null");
        }
    }
    private Rectangle createNodeRectange(Transaction transaction) {
        Text text = new Text(transaction.id); // text util pour avoir la taille du rectangle en fonction du nom du Node
        Rectangle r = new Rectangle();
        r.setWidth(text.getLayoutBounds().getWidth() + DEFAULT_MARGE_X_NOM_NODE*2);
        r.setHeight(text.getLayoutBounds().getHeight() + DEFAULT_MARGE_Y_NOM_NODE*2);
        r.setArcHeight(10);
        r.setArcWidth(10);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(2);
        r.setX(calculeCoordNode(true));
        r.setY(calculeCoordNode(false));
        consumer.accept(text.getText());
        r.setAccessibleText(text.getText());
        //addHandlerRectangle(r);
        return r;
    }
    private double calculeCoordNode(boolean x) {
        if (x) {// alors coordonne horizontale
            return new Random().nextDouble() * pane.getWidth();
        }else { // alors coordonnée verticale
            return new Random().nextDouble() * pane.getHeight();
        }
    }
    private void addHandlerRectangle(Rectangle rectangle) {
        rectangle.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                oldX = mouseEvent.getX();
                oldY = mouseEvent.getY();
            }
        });
        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                rectangle.setLayoutX(rectangle.getLayoutX() + mouseEvent.getX() - oldX);
                rectangle.setLayoutY(rectangle.getLayoutY() + mouseEvent.getY() - oldY);
            }
        });
    }
    // cree une "fleche" entre un point s source et un point d destination
    private Path createFleche(String nom, double s_x,double s_y, double d_x, double d_y, int cote) {
        Path fleche = new Path();
        fleche.setAccessibleText(nom);
        MoveTo mt = new MoveTo();
        CubicCurveTo cct = new CubicCurveTo();
        createCourbe(mt,cct,s_x,s_y,d_x,d_y);
        //ajout de la courbe a la fleche
        fleche.getElements().add(mt);
        fleche.getElements().add(cct);
        // on ajout le bout de la fleche
        createBoutFleche(fleche, d_x, d_y, cote, true); // |\ <- oui c'est bien un bout de fleche
        createBoutFleche(fleche, d_x, d_y, cote, false);// /| <- ici aussi  /| + |\ = /|\ en gros /\
        fleche.setStroke(Color.BLACK);
        fleche.setStrokeWidth(1);
        return fleche;
    }

    // s'occupe de creer le bout de la fleche au niveau du point de destination
    private void createBoutFleche(Path p, double d_x, double d_y, int cote,boolean premier) {
        if (cote < 0 || cote >= 4) {// 0_BAS , 1_GAUCHE , 2_HAUT , 3_DROITE
            return;
        }
        double angle = ANGLE_FLECHE + (90 * cote);
        if (!premier) {
            angle += 90;
        }
        MoveTo mv = new MoveTo();
        mv.setX(d_x);
        mv.setY(d_y);
        LineTo lineTo = new LineTo();
        double coord_x = d_x + TAILLE_FLECHE;
        double coord_y = d_y;
        double tab[] = calcul_rotation(d_x, d_y, coord_x, coord_y, angle);
        lineTo.setX(tab[0]);
        lineTo.setY(tab[1]);
        p.getElements().add(mv);
        p.getElements().add(lineTo);
    }
    //renvoie les nouvelles coordonné de d : destination apres rotation en fonction de o : origine
    protected double[] calcul_rotation(double o_x, double o_y,double d_x, double d_y,double angle) {
        double xm, ym, x, y;
        double rot = angle * Math.PI / 180;
        xm = d_x - o_x;
        ym = d_y - o_y;
        x = xm * Math.cos(rot) + ym * Math.sin(rot) + o_x;
        y = xm * Math.sin(rot) + ym * Math.cos(rot) + o_y;
        double tab[] = {x, y};
        return tab;
    }

    private void createCourbe(MoveTo mt, CubicCurveTo cct, double s_x, double s_y, double d_x, double d_y) {
        //gestion des points depart/arrivee
        mt.setX(s_x);// depart
        mt.setY(s_y);
        cct.setX(d_x);// arrivee
        cct.setY(d_y);
        //gestion des points de controles
        ArrayList<Double> list_coord = gestionPointControle(s_x, s_y, d_x, d_y);
        cct.setControlX1(list_coord.get(0));
        list_coord.remove(0);
        cct.setControlX2(list_coord.get(0));
        list_coord.remove(0);
        cct.setControlY1(list_coord.get(0));
        list_coord.remove(0);
        cct.setControlY2(list_coord.get(0));
        list_coord.remove(0);
        if (list_coord == null || list_coord.size() != 0) {
            consumer.accept("on a bien utilisé tout les elements de la liste pour les points de controle");
        }

    }

    private ArrayList<Double> gestionPointControle( double s_x, double s_y, double d_x, double d_y) {
        ArrayList<Double> list = new ArrayList<>();
        double diff_X = d_x - s_x;
        double diff_Y = d_y - s_y;
        if (diff_X <= 0) {
            list.add(calculCoordonneeControle(s_x, diff_X, true));// x point de controle 1
            list.add(calculCoordonneeControle(d_x, diff_X, false));// x point de controle 2
        }else{
            list.add(calculCoordonneeControle(s_x, diff_X, false));
            list.add(calculCoordonneeControle(d_x, diff_X, true));
        }
        if (diff_Y >= 0) {
            list.add(calculCoordonneeControle(s_y, diff_Y, true));// y point de controle 1
            list.add(calculCoordonneeControle(d_y, diff_Y, false));// y point de controle 2
        }else{
            list.add(calculCoordonneeControle(s_y, diff_Y, false));
            list.add(calculCoordonneeControle(d_y, diff_Y, true));
        }
        return list;
    }

    // calcule une coordonne d'un point de controle d'une courbe en fonction d'un point de depart d et d'une taille
    // res = d (+;-) taille*coefficient_controle      ( taille *0.3)
    private double calculCoordonneeControle(double d,double taille, boolean addition) {
        double res = 0.0;
        if (addition) {
            res = d + taille * COEFFICIENT_CONTROLE;
        }else{
            res = d - taille * COEFFICIENT_CONTROLE;
        }
        return res;
    }
    private Rectangle matchRectangleById(String id) {
        for (Shape shape : list_shape) {
            if (shape.getClass() == Rectangle.class) { // verification du type
                Rectangle r = (Rectangle) shape;
                if (id.equals(r.getAccessibleText())) { // verification de l'id
                    return r;
                }
            }
        }
        return null;
    }

    private void createRectangle(String id) {
        Rectangle rectangle = new Rectangle();
    }


}
