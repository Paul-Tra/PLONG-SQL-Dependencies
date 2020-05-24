package sample.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import sample.control.Controller;

public class Transaction {
    // dragging attributes
    private double oldX;
    private double oldY;
    private boolean drag = false;
    // identifications
    private String id = "";
    // positions
    private DoubleProperty centerX = new SimpleDoubleProperty();
    private DoubleProperty centerY = new SimpleDoubleProperty();
    // relations
    public static final int NB_sides = 4;
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;
    private int tabSideRelations[] = new int[NB_sides];
    // shape of Transaction
    private static final int WHITE_GAP = 20;
    private Text text = new Text();
    private Rectangle rectangle = new Rectangle();
    private Controller controller;

    public Transaction(String id) {
        this.id = id;
        this.text.setText(this.id);
        buildTransactionShape();
    }

    /**
     * looks after the building of the Transaction shape so building of the rectangle and its text
     */
    private void buildTransactionShape() {
        rectangle.setWidth(text.getLayoutBounds().getWidth() + WHITE_GAP);
        rectangle.setHeight(text.getLayoutBounds().getHeight() + WHITE_GAP);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        rectangle.setStrokeWidth(2);
        rectangle.setLayoutX(0);
        rectangle.setLayoutY(0);
        actionRectangle();
        bindText();
    }

    /**
     * looks after the text behaviour in front of the rectangle
     */
    private void bindText() {
        text.setManaged(false);
        text.layoutXProperty().bind(rectangle.layoutXProperty().add(WHITE_GAP/2));
        text.layoutYProperty().bind(rectangle.layoutYProperty().add((WHITE_GAP) ));
        text.setOnMousePressed(this::mouseEventSimulation);
        text.setOnMouseDragged(this::mouseEventSimulation);
        text.setOnMouseReleased(this::mouseEventSimulation);
    }

// action methods --------------

    /**
     * manages all the events of the rectangle
     */
    private void actionRectangle() {
        pressRectangle();
        dragRectangle();
        releaseRectangle();
    }

    private void releaseRectangle() {
      rectangle.setOnMouseReleased(mouseEvent -> {
          if (!this.drag) {
              return;
          }
          this.controller.manageReleaseRectangle(this);
          this.drag = false;
      });
    }
    /**
     * manages the mouse press event of the rectangle
     */
    private void pressRectangle() {
        rectangle.setOnMousePressed(mouseEvent -> {
            oldX = mouseEvent.getX();
            oldY = mouseEvent.getY();
            this.controller.hideControlCircles(null);
        });
    }

    /**
     * manages the mouse drag event on the rectangle
     */
    private void dragRectangle() {
        rectangle.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getSceneX() > controller.BOUND &&
                    mouseEvent.getSceneX() < controller.anchorPane1.getScene().getWidth() - controller.BOUND) {
                rectangle.setLayoutX(rectangle.getLayoutX() + mouseEvent.getX() - oldX);
                this.drag = true;
            }
            if (mouseEvent.getSceneY() > controller.menuBar.getHeight() + controller.BOUND &&
                    mouseEvent.getSceneY() < controller.anchorPane1.getScene().getHeight() - controller.BOUND) {
                rectangle.setLayoutY(rectangle.getLayoutY() + mouseEvent.getY() - oldY);
                this.drag = true;
            }
        });
    }

    /**
     * Simulate a mouse event on the rectangle
     * @param mouseEvent mouse event whose we want simulate
     */
    private void mouseEventSimulation(MouseEvent mouseEvent) {
        Event.fireEvent(rectangle, mouseEvent);
    }
// action methods --------------


    /**
     * Finds the number of Relations on the specified side
     * @param side side whose we want the number of relation
     * @return number of relation on the side
     */
    public int getSideRelationBySide(int side) {
        if (!isCorrectSide(side)) {
            return -1;
        }
        return this.tabSideRelations[side];
    }

    /**
     * increase by one the number of relations on side
     * @param side side whose we want to increase the number of relations
     */
    public void increaseSide(int side) {
        if (!isCorrectSide(side)) {
            return;
        }
        this.tabSideRelations[side]++;
    }

    /**
     * decrease by one the number of relations on side
     * @param side side whose we want to decrease the number of relations
     */
    public void decreaseSide(int side) {
        if (!isCorrectSide(side)) {
            return;
        }
        this.tabSideRelations[side]--;
    }

    /**
     * checks if a side corresponds to a correct side value
     * @param side side that we want to check
     * @return if the side is correct or not
     */
    private boolean isCorrectSide(int side) {
        return (side >= TOP && side <= RIGHT);
    }

    public Text getText() {
        return text;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public String getId() {
        return id;
    }

    public void setController(Controller c) {
        this.controller = c;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public boolean isDrag() {
        return drag;
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }
}
