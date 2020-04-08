package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import sample.Controller.Controller;

import java.util.ArrayList;
import java.util.Random;

public class Relation {
    // dragging attributes
    private double oldX;
    private double oldY;
    private Controller controller;
    // identification
    private String name;
    private String key;
    private Transaction source;
    private Transaction target;
    // Shape of Relation
    boolean loop;
    public static final double loopSize = 90;
    Path arrow;
    CubicCurveTo curve = new CubicCurveTo();
    Circle endArrow = new Circle();
    Circle control1 = new Circle();
    Circle control2 = new Circle();


    public Relation(Transaction source, Transaction target, String name, String key) {
        this.name = name;
        this.key = key;
        this.source = source;
        this.target = target;
        this.loop = (this.target.getId().equals(this.source.getId()));
        //buildRelationShape();
    }

    /**
     * looks after the building of the Relation shape so the arrow with all it needs
     * (end Point, curve, control points, ...)
     */
    public void buildRelationShape() {
        int side2 = getAppropriateSide(this.source.getRectangle(),
                this.target.getRectangle());
        // way we choose the opposite side following side2
        int side1 = loop ? side2 : (side2 + 2) % 4;
        // System.out.println("loop : " + loop);
        this.source.increaseSide(side1);
        this.target.increaseSide(side2);
        double[] sourceCoordinates = getPointsArrow(this.source.getRectangle(),
                side1, true);
        double[] targetCoordinates = getPointsArrow(this.target.getRectangle(),
                side2, false);
        arrow = createCurve(sourceCoordinates, targetCoordinates, side2);
        endArrow = createCircleArrow(targetCoordinates);
        arrowColorManagement();
        eventArrow();
    }

    /**
     * manages all the event about the arrow of the Relation
     */
    private void eventArrow() {
        pressArrow();
        enterArrow();
        exitArrow();
        clickArrow();
    }

    /**
     * manages the behavior of the Relation when we press on its arrow
     */
    private void pressArrow() {
        this.arrow.setOnMousePressed(mouseEvent -> {
            this.control1.setVisible(true);
            this.control2.setVisible(true);
        });

    }

    /**
     * manages the behavior of the Relation when we exit to its arrow
     */
    private void exitArrow() {
        this.arrow.setOnMouseExited(mouseEvent -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.controller.labelName.setVisible(false);
        });
    }

    /**
     * manages the behavior of the Relation when we enter in its arrow
     */
    private void enterArrow() {
        this.arrow.setOnMouseEntered(mouseEvent -> {
            this.controller.labelName.setText(this.name);
            this.controller.labelName.setVisible(true);
            this.controller.labelName.setLayoutX(mouseEvent.getX());
            this.controller.labelName.setLayoutY(mouseEvent.getY());
        });
    }

    /**
     * manages the behavior of the Relation when we click on its arrow
     */
    private void clickArrow() {
        this.arrow.setOnMouseClicked(mouseEvent -> {
            this.controller.hideControlCircles(this);
            this.controller.pressAnchorPane1Path(this);
        });
    }

    /**
     * looks after the color and aspect of the arrow following the facts that
     * the relation can be a conditionnal relation or contains a Read-Write
     * dependency
     */
    private void arrowColorManagement() {
        if (this.key.equals("d2")) {
            arrow.getStrokeDashArray().addAll(3.0, 7.0, 3.0, 7.0);
            //arrow.setStrokeDashOffset(10);
        }
        if (isRWRelation()) {
            arrow.setStroke(Color.CORAL);
            endArrow.setFill(Color.CORAL);
        }
    }

    /**
     * checks if the current Relation have READ - WRITE dependency
     *
     * @return if this is RW relation or not
     */
    private boolean isRWRelation() {
        return this.name.contains("rw,");
    }

    /**
     * @param targetCoordinates corresponds to the coordinates in the target
     *                          rectangle where the arrow ends
     * @return the circle that we put at the end of our arrow
     */
    private Circle createCircleArrow(double[] targetCoordinates) {
        Circle c = new Circle();
        c.setRadius(4);
        c.centerXProperty().bind(
                this.target.getRectangle().layoutXProperty().add(targetCoordinates[0]));
        c.centerYProperty().bind(
                this.target.getRectangle().layoutYProperty().add(targetCoordinates[1]));
        return c;
    }

    /**
     * creates the arrow's curve
     *
     * @param sourceCoordinates coordinates of the point where the arrow starts
     * @param targetCoordinates coordinates of the point where the arrow ends
     * @param side2             side where the arrow ends on the target Transaction's rectangle
     * @return the path of the arrow made up of the curve and the moveTo
     */
    private Path createCurve(double[] sourceCoordinates, double[] targetCoordinates, int side2) {
        Path curve = new Path();
        MoveTo mt = new MoveTo();
        mt.xProperty().bind(
                this.source.getRectangle().layoutXProperty().add(sourceCoordinates[0]));
        mt.yProperty().bind(
                this.source.getRectangle().layoutYProperty().add(sourceCoordinates[1]));
        CubicCurveTo cct = new CubicCurveTo();
        cct.xProperty().bind(
                this.target.getRectangle().layoutXProperty().add(targetCoordinates[0]));
        cct.yProperty().bind(
                this.target.getRectangle().layoutYProperty().add(targetCoordinates[1]));
        drawCurve(mt, cct, new double[]{mt.getX(), mt.getY()}, new double[]{cct.getX(),
                cct.getY()}, side2);
        curve.getElements().add(mt);
        curve.getElements().add(cct);
        curve.setStroke(Color.BLACK); // initial default color
        curve.setStrokeWidth(3);
        return curve;
    }

    /**
     * looks after the shape of the curve
     *
     * @param mt                start point of the curve
     * @param cct               body of the curve
     * @param sourceCoordinates start coordinates of the curve
     * @param targetCoordinates end coordinates of the curve
     * @param side              side of the arrival Transaction's rectangle
     */
    private void drawCurve(MoveTo mt, CubicCurveTo cct, double[] sourceCoordinates,
                           double[] targetCoordinates, int side) {
        ArrayList<Double> coordinates = controlPointStartPosition(sourceCoordinates,
                targetCoordinates, side);
        if (coordinates == null || coordinates.size() < Transaction.NB_sides) {
            System.out.println("coordinates list issue");
            // TODO : add an error condition (break the function with return | exception ...)
        }
        this.control1 = createControlCircle();
        this.control2 = createControlCircle();
        this.control1.centerXProperty().bind(mt.xProperty().add((coordinates.get(0))));
        this.control1.centerYProperty().bind(mt.yProperty().add((coordinates.get(2))));
        this.control2.centerXProperty().bind(cct.xProperty().add((coordinates.get(1))));
        this.control2.centerYProperty().bind(cct.yProperty().add((coordinates.get(3))));
        cct.controlX1Property().bind(this.control1.layoutXProperty().add(this.control1.centerXProperty()));
        cct.controlY1Property().bind(this.control1.layoutYProperty().add(this.control1.centerYProperty()));
        cct.controlX2Property().bind(this.control2.layoutXProperty().add(this.control2.centerXProperty()));
        cct.controlY2Property().bind(this.control2.layoutYProperty().add(this.control2.centerYProperty()));
        // start visibility of control circles
        this.control1.setVisible(false);
        this.control2.setVisible(false);
        this.control1.setFill(Color.RED);
        this.control2.setFill(Color.SALMON);
    }

    /**
     * Determines the control points positions from the start, the end of the curve and the side
     *
     * @param sourceCoordinates x,y positions of a source point corresponding to the start of
     *                          the arrow's curve
     * @param targetCoordinates x,x positions of a target point corresponding to the end of
     *                          the arrow's curve
     * @param side              chosen side that determines the sign of loop size
     * @return the coordinates x1,x2,y1,y2 of the two control points of the arrow's curve
     */
    private ArrayList<Double> controlPointStartPosition(double[] sourceCoordinates,
                                                        double[] targetCoordinates, int side) {
        ArrayList<Double> list = new ArrayList<>();
        double currentLoopSize = 0; // value if the arrow doesn't make a loop
        if (loop) {
            currentLoopSize = loopSize;
            if (side == Transaction.LEFT || side == Transaction.TOP) {
                currentLoopSize = -loopSize;
            }
        }
        double size;
        boolean addition;
        if (side == Transaction.TOP || side == Transaction.BOTTOM) {
            list.add(0.0);
            list.add(0.0);
            size = targetCoordinates[1] - sourceCoordinates[1];
            addition = !(targetCoordinates[1] < sourceCoordinates[1]); // depends if source is
            // higher than target
            list.add(controlCoordinate(size, addition) + currentLoopSize);
            list.add(controlCoordinate(size, !addition) + currentLoopSize);
        } else {
            size = targetCoordinates[0] - sourceCoordinates[0];
            addition = !(targetCoordinates[0] < sourceCoordinates[0]); // depends if source is
            // at the left of target
            list.add(controlCoordinate(size, addition) + currentLoopSize);
            list.add(controlCoordinate(size, !addition) + currentLoopSize);
            list.add(0.0);
            list.add(0.0);
        }
        return list;
    }

    /**
     * looks after the position of control circle between the two Transaction's
     * rectangle following if we have to add it or subtract it and following a coefficient
     *
     * @param size     size between the two Transaction's rectangle of the Relation
     * @param addition if we have to add it or subtract it
     * @return
     */
    private double controlCoordinate(double size, boolean addition) {
        double res = Math.abs(size) * 0.5;
        return addition ? res : -res;
    }

    /**
     * create the shape and behaviour of the control circles
     *
     * @return a control circle
     */
    private Circle createControlCircle() {
        Circle c = new Circle();
        c.setFill(Color.BLACK);
        c.setRadius(5);
        c.setOnMousePressed(mouseEvent -> {
            oldX = mouseEvent.getX();
            oldY = mouseEvent.getY();
        });
        c.setOnMouseDragged(mouseEvent -> {
            c.setLayoutX(c.getLayoutX() + mouseEvent.getX() - oldX);
            c.setLayoutY(c.getLayoutY() + mouseEvent.getY() - oldY);
        });
        return c;
    }


    /**
     * Looks after the finding of the start/end coordinates of the arrow
     *
     * @param r         source/target rectangle where the arrow have to come/go
     * @param side      side of the rectangle where the arrow starts/ends
     * @param departure if we talk about the source or target rectangle
     * @return x, y coordinates
     */
    private double[] getPointsArrow(Rectangle r, int side, boolean departure) {
        double rand;
        if (loop) {
            if (!departure) {
                if (side == Transaction.BOTTOM || side == Transaction.TOP)
                    rand = r.getWidth();
                else rand = r.getHeight();
            } else {
                rand = 0;
            }
        } else {
            rand = randSidePoint(r, side);
        }
        if (side == Transaction.BOTTOM)
            return new double[]{r.getX() + rand, r.getY() + r.getHeight()};
        else if (side == Transaction.TOP)
            return new double[]{r.getX() + rand, r.getY()};
        else if (side == Transaction.LEFT)
            return new double[]{r.getX(), r.getY() + rand};
        else return new double[]{r.getX() + r.getWidth(), r.getY() + rand};
    }

    /**
     * Find a random point on a side of the Rectangle
     *
     * @param r    Rectangle on which we want to find a point
     * @param side side of the Rectangle where we want to find a point
     * @return x or y position following the side of the Rectangle
     */
    private double randSidePoint(Rectangle r, int side) {
        if (side == Transaction.TOP || side == Transaction.BOTTOM) {
            return new Random().nextDouble() * r.getWidth();
        } else {
            return new Random().nextDouble() * r.getHeight();
        }
    }

    /**
     * Find the best side of 1st rectangle to receive/send an arrow
     * following the 2nd rectangle position
     *
     * @param rectangleFrom departure rectangle
     * @param rectangleTo   arrival rectangle
     * @return the appropriate side
     */
    private int getAppropriateSide(Rectangle rectangleFrom, Rectangle rectangleTo) {
        if (loop) {
            return getAppropriateSideLoop();
        }
        boolean vertical = isVerticalDirection((rectangleTo.getLayoutX() -
                        rectangleFrom.getLayoutX()),
                (rectangleTo.getLayoutY() - rectangleFrom.getLayoutY()));
        if (vertical) { // vertical positions
            if (rectangleFrom.getLayoutY() < rectangleTo.getLayoutY()) {
                // rectangleFrom is higher than rectangleTo
                return Transaction.TOP;
            } else {
                return Transaction.BOTTOM;
            }
        } else { // horizontal positions
            if (rectangleFrom.getLayoutX() < rectangleTo.getLayoutX()) {
                // rectangleFrom is a the left of rectangleTo
                return Transaction.LEFT;
            } else {
                return Transaction.RIGHT;
            }
        }
    }

    /**
     * Find the best side to put of the unique Transaction to put a loop Relation
     *
     * @return the side of the Transaction with less Relations
     */
    private int getAppropriateSideLoop() {
        int index = 0;
        int min = this.source.getSideRelationBySide(index);
        for (int i = 1; i < Transaction.NB_sides; i++) {
            if (this.source.getSideRelationBySide(i) < min) {
                min = this.source.getSideRelationBySide(i);
                index = i;
            }
        }
        return index;
    }

    /**
     * divides the names of the relation in several dependencies
     *
     * @return the separated lines of the Relation's name
     */
    public ArrayList<String> getDependenciesLinesFromName() {
        ArrayList<String> res = new ArrayList<>();
        String[] lines = name.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isBlank() || lines[i].isBlank()) {
                continue;
            }
            if (lines[i].contains("rw,") || lines[i].contains("ww,") || lines[i].contains("wr,")) {
                res.add(lines[i]);
            }
        }
        return res;
    }

    /**
     * Checks if the difference between 2 point is more vertical than horizontal
     *
     * @param diff_X delta between 2 points on x axis
     * @param diff_Y delta between 2 points on y axis
     * @return if the delta between 2 points corresponds to a vertical vector or not
     */
    private boolean isVerticalDirection(double diff_X, double diff_Y) {
        return Math.abs(diff_X) <= Math.abs(diff_Y);
    }


    public Transaction getSource() {
        return source;
    }

    public void setSource(Transaction source) {
        this.source = source;
    }

    public Transaction getTarget() {
        return target;
    }

    public void setTarget(Transaction target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Path getArrow() {
        return arrow;
    }

    public Circle getEndArrow() {
        return endArrow;
    }

    public Circle getControl1() {
        return control1;
    }

    public Circle getControl2() {
        return control2;
    }

    public void setController(Controller c) {
        this.controller = c;
    }

}
