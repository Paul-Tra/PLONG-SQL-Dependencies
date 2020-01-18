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
    // 0_BAS , 1_GAUCHE , 2_HAUT , 3_DROITE
    private static final int BAS = 0;
    private static final int  GAUCHE = 1;
    private static final int  HAUT = 2;
    private static final int  DROITE = 3;
    private final int NB_COTE=4;
    private double oldX, oldY;
    private final int DEFAULT_MARGE_X_NOM_NODE = 10;
    private final int DEFAULT_MARGE_Y_NOM_NODE = 5;
    private final int TAILLE_FLECHE = 8; // taille du bout de la fleche
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
                consumer.accept("depart coord x,y" + r_source.getX() + ";" + r_source.getY());
                consumer.accept("depart layout x,y" + r_source.getLayoutX() + ";" + r_source.getLayoutY());
                int cote_arrivee = getCoteNodeArrivee(r_source, r_dest);
                double tab_coord_arrivee[] = getCoordPointFleche(r_dest,cote_arrivee);
                double tab_coord_depart[] = getCoordPointFleche(r_source, (cote_arrivee + 2) % 4);
                Path p = createFleche(relation.nom, tab_coord_depart[0], tab_coord_depart[1], tab_coord_arrivee[0], tab_coord_arrivee[1],r_source,r_dest, cote_arrivee);
                list_shape.add(p);
            }
        }else{
            consumer.accept("liste de relation null");
        }
    }
    // renvoie les coordonnée de depart/arrivée d'une fleche sur un Node
    private double[] getCoordPointFleche(Rectangle r, int cote) {
        double rand = randPointCote(r, cote);
        if (cote == BAS) {
            return new double[]{r.getX() + rand, r.getY() + r.getHeight()};
        } else if (cote == HAUT) {
            return new double[]{r.getX() + rand, r.getY()};
        } else if (cote == GAUCHE) {
            return new double[]{r.getX() , r.getY() + rand};
        }else {
            return new double[]{r.getX() + r.getWidth() , r.getY() + rand};
        }
    }
    // renvoie une coord d'un point aleatoire sur une cote d'un node en fonction du cote donné entree
    private double randPointCote(Rectangle r, int cote) {
        double taille=0;
        if (cote == GAUCHE || cote == DROITE) {
            taille = r.getHeight();
        } else if(cote == HAUT || cote == BAS) {
            taille = r.getWidth();
        }
        return new Random().nextDouble() * taille*0.9;
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
        if (Math.abs(diff_X) < Math.abs(diff_Y)) {
            return true;
        } else {
            return false;
        }
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
    private Path createFleche(String nom,double s_x,double s_y,double d_x,double d_y,Rectangle r_source,Rectangle r_dest, int cote) {
        Path fleche = new Path();
        fleche.setAccessibleText(nom);
        MoveTo mt = new MoveTo();
        mt.xProperty().bind(r_source.layoutXProperty().add(s_x));
        mt.yProperty().bind(r_source.layoutYProperty().add(s_y));
        CubicCurveTo cct = new CubicCurveTo();
        cct.xProperty().bind(r_dest.layoutXProperty().add(d_x));
        cct.yProperty().bind(r_dest.layoutYProperty().add(d_y));
        createCourbe(cct,s_x,s_y,d_x,d_y);
        //ajout de la courbe a la fleche
        fleche.getElements().add(mt);
        fleche.getElements().add(cct);
        // on ajout le bout de la fleche
        createBoutFleche(fleche,r_dest, d_x, d_y, cote, true); // |\ <- oui c'est bien un bout de fleche
        createBoutFleche(fleche,r_dest, d_x, d_y, cote, false);// /| <- ici aussi  /| + |\ = /|\ en gros /\
        fleche.setStroke(Color.BLACK);
        fleche.setStrokeWidth(1);
        return fleche;
    }
    // s'occupe de creer le bout de la fleche au niveau du point de destination
    private void createBoutFleche(Path p,Rectangle r_dest, double d_x, double d_y, int cote,boolean premier) {
        if (cote < 0 || cote >= 4) {// 0_BAS , 1_GAUCHE , 2_HAUT , 3_DROITE
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

    private void createCourbe(CubicCurveTo cct, double s_x, double s_y, double d_x, double d_y) {
        ArrayList<Double> list_coord = gestionPointControle(s_x, s_y, d_x, d_y);
        if (list_coord == null || list_coord.size() < NB_COTE) {
            consumer.accept("liste de coordonnee null ou partielle");
        }/*
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
        }*/
        Circle c1 =createCircleControle(list_coord.get(0),list_coord.get(2));
        cct.controlX1Property().bind(c1.layoutXProperty().add(c1.getCenterX()));
        cct.controlY1Property().bind(c1.layoutYProperty().add(c1.getCenterY()));
        Circle c2 =createCircleControle(list_coord.get(1),list_coord.get(3));
        cct.controlX2Property().bind(c2.layoutXProperty().add(c2.getCenterX()));
        cct.controlY2Property().bind(c2.layoutYProperty().add(c2.getCenterY()));
    }

    private Circle createCircleControle(double x, double y) {
        Circle c = new Circle();
        c.setCenterX(x);
        c.setCenterY(y);
        c.setFill(Color.BLACK);
        c.setRadius(5);
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
    private ArrayList<Double> gestionPointControle(double s_x, double s_y, double d_x, double d_y) {
        ArrayList<Double> list = new ArrayList<>();
        boolean verticale = estDirectionVerticale((d_x - s_x), (d_y - s_y));
        if (verticale) {
            list.add(s_x);
            list.add(d_x);
            list.add(calculCoordonneeControle(s_y, (d_y - s_y), true));// y point de controle 1
            list.add(calculCoordonneeControle(d_y, (d_y - s_y), false));// y point de controle 2
        }else{
            list.add(calculCoordonneeControle(s_x, (d_x - s_x), true));
            list.add(calculCoordonneeControle(d_x, (d_x - s_x), false));
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
