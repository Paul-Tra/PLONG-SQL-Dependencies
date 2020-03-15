package sample;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import static sample.Controller.*;

public class ElementVisuel {
    // 0_BOTTOM , 1_LEFT , 2_TOP , 3_RIGHT
    private final double DEFAULT_DISTANCE_BOUCLE = 30;
    private static final int BAS = 0;
    private static final int  GAUCHE = 1;
    private static final int  HAUT = 2;
    private static final int  DROITE = 3;
    private final int NB_COTE=4;
    private double oldX, oldY;
    private final int DEFAULT_MARGE_X_NOM_NODE = 20;
    private final int DEFAULT_MARGE_Y_NOM_NODE = 10;
    private final int TAILLE_FLECHE = 12; // taille du bout de la fleche
    private final double ANGLE_FLECHE = 45; // angle d'un cote du bout de la fleche par rapport au noeud d'arrivee
    private final double COEFFICIENT_CONTROLE = 0.3;// coefficent de longueur/hauteur pour placer les points de controles
    private Pane pane; // panneau auquel l'element visuel est asssocié
    private Group group;
    private Consumer<String> consumer = e -> System.out.println(e);
    ArrayList<Shape> list_shape; // listes des elements a inserer dans la vue
    ArrayList<Transaction> list_transaction;
    ArrayList<Relation> list_relation;

    public ElementVisuel(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction, Pane p) {
        list_shape = new ArrayList<>();
        pane = p;
        list_relation = l_relation;
        list_transaction = l_transaction;
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
		// wee bind the text to the rectangle
                text.layoutXProperty().bind(r.layoutXProperty().add(DEFAULT_MARGE_X_NOM_NODE));
                text.layoutYProperty().bind(r.layoutYProperty().add((DEFAULT_MARGE_Y_NOM_NODE/2)+text.getLayoutBounds().getHeight()));
                addHandlerRectangle(r);
                list_shape.add(text);
                list_shape.add(r);
            }
        }else{
            consumer.accept("transaction's list null");
        }
        if (l_relation != null) {
            for (Relation relation : l_relation) {
                String source = relation.source;
                String destination = relation.destination;
                Rectangle r_source = matchRectangleById(source);
                Rectangle r_dest = matchRectangleById(destination);
                if (r_dest == null || r_source == null) {
                    consumer.accept("source ou destination null");
                    continue;
                }
                int cote_arrivee;
                double tab_coord_arrivee[];
                double tab_coord_depart[];
                Path curve;
                Circle circle;
                if (r_source.getAccessibleText().equals(r_dest.getAccessibleText())) {
                    //if the relation draw a loop from the same  transaction (node)
                    cote_arrivee = new Random().nextInt(NB_COTE);
                    tab_coord_depart = getCoordPointFleche(r_source, cote_arrivee, true, true);
                    tab_coord_arrivee = getCoordPointFleche(r_dest, cote_arrivee, true, false);
                } else {
                    cote_arrivee = getCoteNodeArrivee(r_source, r_dest);
                    tab_coord_depart = getCoordPointFleche(r_source, (cote_arrivee + 2) % 4, false, false);
                    tab_coord_arrivee = getCoordPointFleche(r_dest, cote_arrivee, false, false);
                }
                curve = createCurve(relation.nom, tab_coord_depart[0], tab_coord_depart[1], tab_coord_arrivee[0],
                        tab_coord_arrivee[1], r_source, r_dest, cote_arrivee);
                circle = createCircleArrow(r_dest, tab_coord_arrivee[0], tab_coord_arrivee[1]);
                // addition to the Arrow two control circles
                if (relation.key.equals("d2")) {
                    curve.getStrokeDashArray().addAll(3.0,7.0,3.0,7.0);
                }
                if (isRWRelation(relation)) {
                    curve.setStroke(Color.CORAL);
                    circle.setFill(Color.CORAL);
                }
                list_shape.addAll(Arrays.asList(curve, circle));
                addHandlerCurve(curve);
            }
        }else{
            consumer.accept("relation's list null");
        }
    }
    private boolean isRWRelation(Relation relation) {
        return relation.nom.contains("rw,");
    }
// get the departure/arrival coord of an arrow on a Node
    private double[] getCoordPointFleche(Rectangle r, int cote, boolean boucle, boolean depart) {
        double rand = 0;
        if (!boucle) {
            rand = randPointCote(r, cote);
        }else {
            if (!depart) {
                if (cote == BAS || cote == HAUT) rand += r.getWidth();
                else rand += r.getHeight();
            }
        }
        if (cote == BAS) return new double[]{r.getX() + rand, r.getY() + r.getHeight()};
        else if (cote == HAUT) return new double[]{r.getX() + rand, r.getY()};
        else if (cote == GAUCHE) return new double[]{r.getX(), r.getY() + rand};
        else return new double[]{r.getX() + r.getWidth(), r.getY() + rand};
    }

    // renvoie une coord d'un point aleatoire sur une cote d'un node en fonction du cote donné entree
    private double randPointCote(Rectangle r, int cote) {
        double taille = 0;

        if (cote == GAUCHE || cote == DROITE) taille = r.getHeight();
        else if(cote == HAUT || cote == BAS) taille = r.getWidth();

        return new Random().nextDouble() * taille*0.8+(taille*0.1);
    }

    private int getCoteNodeArrivee(Rectangle r_depart, Rectangle r_arrivee) {
        boolean verticale = estDirectionVerticale((r_arrivee.getX() - r_depart.getX()), (r_arrivee.getY() - r_depart.getY()));
        if (verticale) { // alors on gere en fonction des positions verticales
            if (r_depart.getY() < r_arrivee.getY()) { // alors le Node le plus haut est le Node de depart
                return HAUT;
            } else { // sinon c'est celui d'arrivee
                return BAS;
            }
        } else { // alors on gere en fonction des positions horizontales
            if (r_depart.getX() < r_arrivee.getX()) { // alors le Node le plus a gauche est le Node de depart
                return GAUCHE;
            } else { // sinon c'est celui d'arrivee
                return DROITE;
            }
        }
    }
    // renvoie "direction" suivie par la fleche en fonction des diff de position de 2 Node
    private boolean estDirectionVerticale(double diff_X, double diff_Y) {
        if (Math.abs(diff_X) <= Math.abs(diff_Y)) {
            return true;
        } else {
            return false;
        }
    }
    // renvoie la "direction" suivie par la fleche en fonctio du cote passé en parametre
    private boolean estDirectionVerticale(int cote) {
        if (cote == HAUT || cote == BAS) {
            return true;
        }
        return false;
    }
    private Rectangle createNodeRectange(Transaction transaction) {
        Text text = new Text(transaction.id); // text utile pour avoir la taille du rectangle en fonction du nom du Node
        Rectangle r = new Rectangle();
        r.setWidth(text.getLayoutBounds().getWidth() + DEFAULT_MARGE_X_NOM_NODE*2);
        r.setHeight(text.getLayoutBounds().getHeight() + DEFAULT_MARGE_Y_NOM_NODE*2);
        r.setArcHeight(10);
        r.setArcWidth(10);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(2);
        r.setX(calculeCoordNode(true,r.getWidth()));
        r.setY(calculeCoordNode(false,r.getHeight()));
        r.setAccessibleText(text.getText());
        return r;
    }
    private double calculeCoordNode(boolean x, double margin) {
        if (x) {// alors coordonne horizontale
            return new Random().nextDouble() * (pane.getWidth() - margin);
        }else { // alors coordonnée verticale
            return new Random().nextDouble() * (pane.getHeight() - margin);
        }
    }
    private void addHandlerRectangle(Rectangle rectangle) {
        rectangle.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                oldX = mouseEvent.getX();
                oldY = mouseEvent.getY();
                // update the position Node data (the clicked Node)
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
    //recovering of the Node's data and manage them into the GUI 
    private void infoNode(Rectangle rectangle) {
        Transaction transaction = matchTransaction(rectangle);
        Controller c = null;
        try {
            c = getControllerFenetre();
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.fillData(transaction);
    }
    private Transaction matchTransaction(Rectangle rectangle) {
        for (Transaction transaction : list_transaction) {
            if (rectangle.getAccessibleText().equals(transaction.id)) { // on a trouve la transcation correspondant au rectangle
                return transaction;
            }
        }
        return null;
    }
    // create the curve representing the Relation between source : s and destination : d
    private Path createCurve(String nom, double s_x, double s_y, double d_x, double d_y, Rectangle r_source, Rectangle r_dest,int cote) {
        Path curve = new Path();
        curve.setAccessibleText(nom);
        MoveTo mt = new MoveTo();
        mt.xProperty().bind(r_source.layoutXProperty().add(s_x));
        mt.yProperty().bind(r_source.layoutYProperty().add(s_y));
        CubicCurveTo cct = new CubicCurveTo();
        cct.xProperty().bind(r_dest.layoutXProperty().add(d_x));
        cct.yProperty().bind(r_dest.layoutYProperty().add(d_y));
        boolean loop = (r_source.getAccessibleText().equals(r_dest.getAccessibleText()));
        drawCurve(mt,cct,s_x,s_y,d_x,d_y,loop,cote,nom);
        curve.getElements().add(mt);
        curve.getElements().add(cct);
        curve.setStroke(Color.BLACK); // default color
        curve.setStrokeWidth(3);
        return curve;
    }

    // manage the way of the curve
    private void drawCurve(MoveTo mt,CubicCurveTo cct, double s_x, double s_y, double d_x, double d_y,boolean boucle,int cote,String nom) {
        ArrayList<Double> list_coord = gestionPointControle(s_x, s_y, d_x, d_y,boucle,cote);
        if (list_coord == null || list_coord.size() < NB_COTE) {
            consumer.accept("liste de coordonnee null ou partielle");
        }
        Circle c1 = createCircleControle(list_coord.get(0), list_coord.get(2), nom);
        Circle c2 = createCircleControle(list_coord.get(1), list_coord.get(3), nom);
        c1.centerXProperty().bind(mt.xProperty().add((list_coord.get(0)-mt.getX())));
        c1.centerYProperty().bind(mt.yProperty().add((list_coord.get(2)-mt.getY())));
        c2.centerXProperty().bind(cct.xProperty().add((list_coord.get(1)-cct.getX())));
        c2.centerYProperty().bind(cct.yProperty().add((list_coord.get(3)-cct.getY())));
        cct.controlX1Property().bind(c1.layoutXProperty().add(c1.centerXProperty()));
        cct.controlY1Property().bind(c1.layoutYProperty().add(c1.centerYProperty()));
        cct.controlX2Property().bind(c2.layoutXProperty().add(c2.centerXProperty()));
        cct.controlY2Property().bind(c2.layoutYProperty().add(c2.centerYProperty()));
        c1.setVisible(false);// cercle de congtrole invisible au depart
        c2.setVisible(false);
        if (estDirectionVerticale(cote)) {
            c1.setFill(Color.BLUE);
            c2.setFill(Color.LIGHTGREEN);
        } else {
            c1.setFill(Color.RED);
            c2.setFill(Color.SALMON);
        }
    }
    // manage the event actions of the arrow
    private void addHandlerCurve(Path curve) {
        curve.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // set invisible all of the control-circles
                setInvisbleAllCircleControle();
                // addition of only the arrow's control-circles in the view
                setVisibleCircleControleFleche(curve);
            }
        });
    }
    private Controller getControllerFenetre() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Fenetre.fxml"));
        Parent calcRoot = loader.load();
        Controller controller = loader.getController();
        return controller;
    }
    private Relation matchRelation(Path path) {
        for (Relation relation : list_relation) {
            if (path.getAccessibleText().equals(relation.nom)) { // on a trouve la transcation correspondant au rectangle
                return relation;
            }
        }
        return null;
    }
    // set the 2 arrow's control-circles visible  
    private void setVisibleCircleControleFleche(Path curve) {
        int cpt=0;
        for (int i = 0; i < list_shape.size(); i++) {
            if (cpt >= 2) { 
            // if we have found our 2 circles
                return;
            }
            if (list_shape.get(i).getClass() == Circle.class) { // si c'est un cercle
                Circle c = (Circle) list_shape.get(i);
                if( curve == null || c == null) return;
                try {
                    if (c.getAccessibleText().equals(curve.getAccessibleText())) {
                        // match between c.text and fleche.text
                        cpt++;
                        if (!c.isVisible()) {
                            c.setVisible(true);
                        }
                    }
                } catch (Exception e) {
                    // case when the circle corresponds to a end of 'arrow'
                }
            }
        }
    }

    private void setInvisbleAllCircleControle() {
        for (Shape shape : list_shape) {
            if (shape.getClass() == Circle.class) {
                Circle c = (Circle) shape;
                try {
                    if (!c.getAccessibleText().isEmpty()) {
                        c.setVisible(false);
                    }
                } catch (NullPointerException e) {
                }
            }
        }
    }

    // create a circle on the destination's node of an 'arrow'
    private Circle createCircleArrow(Rectangle r_dest, double d_x, double d_y) {
        Circle c = new Circle();
        c.setRadius(4);
        c.centerXProperty().bind(r_dest.layoutXProperty().add(d_x));
        c.centerYProperty().bind(r_dest.layoutYProperty().add(d_y));
        return c ;
    }
    // takes care of the creation of the arrow's apex from the destination point
    private void createBoutFleche(Path p,Rectangle r_dest, double d_x, double d_y, int cote,boolean premier) {
        if (cote < 0 || cote >= 4) {
        // 0_BOTTOM , 1_LEFT , 2_TOP , 3_RIGHT
            return;
        }
        double angle = ANGLE_FLECHE + (90 * cote);
        if (!premier) {
            angle += 90;
        }
        MoveTo mv = new MoveTo();
        mv.xProperty().bind(r_dest.layoutXProperty().add(d_x));
        mv.yProperty().bind(r_dest.layoutYProperty().add(d_y));
        LineTo lineTo = new LineTo();
        double coord_x = d_x + TAILLE_FLECHE;
        double coord_y = d_y;
        double tab[] = calcul_rotation(d_x, d_y, coord_x, coord_y, angle);
        lineTo.xProperty().bind(r_dest.layoutXProperty().add(tab[0]));
        lineTo.yProperty().bind(r_dest.layoutYProperty().add(tab[1]));
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



    private Circle createCircleControle(double x, double y,String nom) {
        Circle c = new Circle();
        c.setCenterX(x);
        c.setCenterY(y);
        c.setFill(Color.BLACK);
        c.setRadius(5);
        c.setAccessibleText(nom);
        c.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                oldX = mouseEvent.getX();
                oldY = mouseEvent.getY();
            }
        });
        c.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                c.setLayoutX(c.getLayoutX() + mouseEvent.getX() - oldX);
                c.setLayoutY(c.getLayoutY() + mouseEvent.getY() - oldY);
            }
        });
        list_shape.add(c);
        return c;
    }
    // calcule et renvoie les point de controle d'une fleche
    private ArrayList<Double> gestionPointControle(double s_x, double s_y, double d_x, double d_y,boolean boucle,int cote) {
        ArrayList<Double> list = new ArrayList<>();
        double distance_boucle = 0; // sert donner a la fleche une fore de boucle si elle partet arrive sur le meme Node
        if (boucle) {
            distance_boucle = DEFAULT_DISTANCE_BOUCLE*3;
            if( cote == GAUCHE || cote == HAUT) distance_boucle = -distance_boucle;
        }
        if (estDirectionVerticale(cote)) {
            list.add(s_x);
            list.add(d_x);
            if (d_y < s_y) { // arrivee au dessus du depart
                list.add(calculCoordonneeControle(s_y, (d_y - s_y), false) + distance_boucle);// y point de controle 1
                list.add(calculCoordonneeControle(d_y, (d_y - s_y), true) + distance_boucle);
            }else{
                list.add(calculCoordonneeControle(s_y, (d_y - s_y), true) + distance_boucle);// y point de controle 1
                list.add(calculCoordonneeControle(d_y, (d_y - s_y), false) + distance_boucle);// y point de controle 2
            }
        }else{
            if (d_x < s_x) {
                list.add(calculCoordonneeControle(s_x, (d_x - s_x), false) + distance_boucle);
                list.add(calculCoordonneeControle(d_x, (d_x - s_x), true) + distance_boucle);
            } else {
                list.add(calculCoordonneeControle(s_x, (d_x - s_x), true) + distance_boucle);
                list.add(calculCoordonneeControle(d_x, (d_x - s_x), false) + distance_boucle);
            }
            list.add(s_y);
            list.add(d_y);
        }
        return list;
    }

    // calcule une coordonne d'un point de controle d'une courbe en fonction d'un point de depart d et d'une taille
    // res = d (+;-) taille*coefficient_controle      ( taille *0.3)
    private double calculCoordonneeControle(double d,double taille, boolean addition) {
        double res = 0.0;
        if (addition) {
            res = d + Math.abs(taille) * COEFFICIENT_CONTROLE;
        }else{
            res = d - Math.abs(taille) * COEFFICIENT_CONTROLE;
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
