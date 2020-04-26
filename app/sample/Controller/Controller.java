package sample.Controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.*;
import sample.Parser.GogolParser;
import sample.Parser.GraphmlParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class Controller implements Initializable {
    private final String GOGOLPATH = "dependencies.gogol";
    private final String DOTPATH = "graph.dot";
    public final double BOUND = 10;
    private String currentPath = "";
    public ArrayList<Transaction> transactions = new ArrayList<>();
    public ArrayList<Relation> relations = new ArrayList<>();
    public Style style = new Style();
    private GogolParser gogolParser;
    @FXML
    public AnchorPane anchorPane1, anchorPane2, anchorPane3;
    @FXML
    public MenuBar menuBar;
    @FXML
    private MenuItem menuItemExport, menuItemClearLaunch, menuItemAppearance;

    @FXML
    BorderPane borderPane1;
    @FXML
    public Label labelElement, labelElement2, labelSource, labelSource2,
            labelTarget, labelTarget2, labelDependencies, labelName,labelFileSource,
            labelFileSource2,labelFileTarget, labelFileTarget2;
    @FXML
    ListView listViewDependencies,listViewSourceLines,listViewTargetLines;
    @FXML
    Button buttonHide;


    @FXML
    private void onMenuItemAppearance() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/Style.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            StyleController styleController = loader.getController();
            styleController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Appearance settings");
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        this.menuItemAppearance.setDisable(true);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> menuItemAppearance.setDisable(false));
    }

    @FXML
    private void onMenuItemExport() {
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension
        fil_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("pdf files", ".pdf"),
                new FileChooser.ExtensionFilter("xdot files", ".xdot"),
                new FileChooser.ExtensionFilter("dot files", ".dot"),
                new FileChooser.ExtensionFilter("PostScript files", ".ps"),
                new FileChooser.ExtensionFilter("XFIG files", ".fig"),
                new FileChooser.ExtensionFilter("png files", ".png"),
                new FileChooser.ExtensionFilter("gif files", ".gif"),
                new FileChooser.ExtensionFilter("jpeg files", ".jpeg"),
                new FileChooser.ExtensionFilter("jpg files", ".jpg"),
                new FileChooser.ExtensionFilter("json files", ".json"),
                new FileChooser.ExtensionFilter("svg files", ".svg"));

        File file = fil_chooser.showSaveDialog(this.anchorPane1.getScene().getWindow());
        String filePath;
        String fileExtension;
        if (file == null) {
            System.out.println(" chosen file null");
            return;
        }
        filePath = file.getAbsolutePath();
        fileExtension = fil_chooser.getSelectedExtensionFilter().getExtensions().get(0);
        filePath += fileExtension;
        DotWriter dotWriter = new DotWriter(this.DOTPATH, this.relations, this.style.getPattern());
        conversion(fileExtension, filePath);
    }

    /**
     * looks after the conversion of the generated .dot file following an extension
     *
     * @param extension required extension for the conversion
     * @param filePath  path of the file whose we want to convert
     */
    private void conversion(String extension, String filePath) {
        String option = "-T";
        option += extension.replace(".", "");
        try {
            /*command line ex: dot -Tpdf graph.dot -o filepath.pdf  */
            Process p = Runtime.getRuntime().exec("dot " + option + " " + this.DOTPATH
                    + " -o " + filePath);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void clickButtonHide() {
        this.anchorPane2.setVisible(false);
        this.anchorPane3.setVisible(false);
    }
    @FXML
    private void clickListViewDependencies() {
        String dependency = (String)
                this.listViewDependencies.getSelectionModel().getSelectedItem();
        String source = this.labelSource2.getText();
        String target = this.labelTarget2.getText();
        ArrayList<String> sourceLines = new ArrayList<>();
        ArrayList<String> targetLines = new ArrayList<>();
        boolean conditional = false;
        if (this.labelDependencies.getText().contains("Conditional")) {
            conditional = true;
        }

        this.gogolParser.getDependencyLines(dependency, source, target, conditional,
                sourceLines, targetLines);

        fillPopUp(sourceLines, targetLines);
    }

    /**
     * fills the elements of the Pop-up by source and target files information
     *
     * @param sourceLines   lines causing a dependency in the source file
     * @param targetLines   lines causing a dependency in the target file
     */
    private void fillPopUp(ArrayList<String> sourceLines, ArrayList<String> targetLines) {
        clearPopUp();

        this.labelFileSource2.setText(this.labelSource2.getText() );
        this.labelFileTarget2.setText(this.labelTarget2.getText() );
        this.listViewSourceLines.getItems().addAll(sourceLines);
        this.listViewTargetLines.getItems().addAll(targetLines);

        this.anchorPane3.toFront();
        this.anchorPane3.setVisible(true);
    }

    /**
     * clear all listViews of the pop-up window which shows the dependency
     * lines from the source and target files
     */
    private void clearPopUp() {
        this.listViewSourceLines.getItems().clear();
        this.listViewTargetLines.getItems().clear();
    }
    /**
     * manages the event when a Path is pressed in the anchorPane1
     */
    public void pressAnchorPane1Path(Relation relation) {
        this.anchorPane3.setVisible(false);
        this.anchorPane2.setVisible(true);
        this.anchorPane2.toFront();
        this.labelElement2.setText("Relation");
        this.labelSource2.setText(relation.getSource().getId());
        this.labelTarget2.setText(relation.getTarget().getId());
        if (relation.getArrow().getStrokeDashArray().isEmpty()) {
            this.labelDependencies.setText("Dependencies :");
        } else {
            this.labelDependencies.setText("Conditional Dependencies :");
        }
        ArrayList<String> dependencies = relation.getDependenciesLinesFromName();
        this.listViewDependencies.getItems().clear();
        this.listViewDependencies.getItems().addAll(dependencies);
    }

    @FXML
    private void onMenuItemClearLaunch() {
        if (this.currentPath.isEmpty() || this.currentPath.isBlank()) {
            return;
        }
        this.menuItemClearLaunch.setDisable(false);
        this.menuItemExport.setDisable(false);
        this.anchorPane2.setVisible(false);
        this.anchorPane1.getChildren().clear();

        // we put back the rampant elements after the clearing of anchorPane1
        this.anchorPane1.getChildren().addAll(this.labelName, this.anchorPane3);
        this.anchorPane3.setVisible(false);
        this.relations.clear();
        this.transactions.clear();
        generateGraph();
    }

    @FXML
    private void onMenuItemFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select your folder");
        File defaultDirectory = new File("../../");
        chooser.setInitialDirectory(defaultDirectory);
        File s = chooser.showDialog(this.anchorPane1.getScene().getWindow());
        String c_dir = System.getProperty("user.dir");
        try {
           Process p = Runtime.getRuntime().exec("python3.7 " + s.getParent() + "/Parser.py " + s + "/");
            // wait until p finished
            p.waitFor() ;

           Process q =Runtime.getRuntime().exec("cp " + s.getParent() + "/graphs/Mygraphml.graphml " + c_dir);
            // wait until p finished
            q.waitFor() ;
            Process r = Runtime.getRuntime().exec("cp " + s.getParent() + "/graphs/dependencies.gogol " + c_dir);
            r.waitFor();
        } catch (Exception e) {
            System.out.println("issue causing by python3.7 execution" + e );
        }
        this.currentPath = "Mygraphml.graphml";
        onMenuItemClearLaunch();
    }

    @FXML
    private void onMenuItemFile() {
        //Node node = (Node) event.getSource();
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension+
        fil_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("graphml files", "*.graphml"));
        File file = fil_chooser.showOpenDialog(this.anchorPane1.getScene().getWindow());
        if (file != null) {
            this.currentPath = file.getAbsolutePath();
            onMenuItemClearLaunch();
        }
    }

    /**
     * looks after the generation of a Dependency graph from a graphml file
     * representing SQL queries
     */
    private void generateGraph() {
        if (this.currentPath.isEmpty() || this.currentPath.isBlank()) {
            return;
        }
        GraphmlParser graphmlParser = new GraphmlParser(this.currentPath);
        HashMap<Integer, String> map = graphmlParser.getTransactionMap();
        HashMap<Integer, String[]> map2 = graphmlParser.getRelationMap();

        fillListTransactionFromMap(map, this.transactions);
        fillListRelationFromMap(map2, this.relations, this.transactions);

        double boundWidth = this.anchorPane1.getWidth() -
                getMaxWidthTransactions(this.transactions) - Relation.loopSize;
        double boundHeight = this.anchorPane1.getHeight() - Relation.loopSize;
        Placement placement = new Placement(this.transactions, this.relations,
                boundWidth, boundHeight);
        placement.placementTransaction(this.transactions);

        this.relations.forEach(Relation::buildRelationShape);


        colorRelations();
        colorTransactions();

        addRelationsToPane(this.relations);
        addTransactionsToPane(this.transactions);

        this.transactions.forEach((t) -> t.setController(this));
        this.relations.forEach((r) -> r.setController(this));

        this.gogolParser = new GogolParser(GOGOLPATH, this.relations);
    }

    /**
     * looks after the shape of the entrant/ outgoing
     *
     * @param transaction Transaction on which we released the mouse
     */
    public void manageReleaseRectangle(Transaction transaction){
        this.relations.forEach(relation -> {
            if (relation.getSource().getId().equals(transaction.getId())
                    || relation.getTarget().getId().equals(transaction.getId())) {
                manageRelationArrowUpdate(relation);
            }
        });
        colorRelations();
    }

    /**
     * manages the update of the Relation's arrow
     * @param relation
     */
    private void manageRelationArrowUpdate(Relation relation){
        if (relation.isLoop()) {
            /* do not take care about loop arrows now */
            /* TODO: manages the decreasing of translations */
            return;
        }
        boolean curveFind = false;
        boolean endPointFind = false;
        Path path;
        Circle circle;
        if ((path = findRelationCurve(relation)) != null) {
            curveFind = true;
        } if ((circle = findRelationEndPoint(relation)) != null) {
            endPointFind = true;
        }
        if (curveFind && endPointFind) {
            if (removeRelationControlCircles(relation)) {
                activateRelationBuildingArrow(relation, path, circle);
            }
        }
    }

    /**
     * looks after the recovering of the Relation's arrow curve into pane view
     *
     * @param relation relation which we want to find the arrow in the view
     * @return the element of the view corresponding to  the Relation's arrow curve
     */
    private Path findRelationCurve(Relation relation) {
        for (Node child : this.anchorPane1.getChildren()) {
            if (child.getClass() == Path.class) {
                Path curve = (Path) child;
                if (curve == relation.getArrow()) {
                    return curve;
                }
            }
        }
        return null;
    }

    /**
     * looks after the recovering of the Relation's endPoint circle into pane view
     *
     * @param relation relation which we want to find the endPoint in the view
     * @return the element of the view corresponding to the Relation's endPoint circle
     */
    private Circle findRelationEndPoint(Relation relation) {
        for (Node child : this.anchorPane1.getChildren()) {
            if (child.getClass() == Circle.class) {
                Circle endPoint = (Circle) child;
                if (endPoint == relation.getEndArrow() ) {
                    return endPoint;
                }
            }
        }
        return null;
    }

    /**
     * Find and remove the the control circles of a Relation in the view
     * @param relation Relation which we want to find and remove the control
     *                  circle into the view
     * @return if the two control circles have been deleted from the view or not
     */
    private boolean removeRelationControlCircles(Relation relation) {
        boolean cc1find = false;
        boolean cc2find = false;
        Circle cc1 = new Circle();
        Circle cc2 = new Circle();
        for (Node child : this.anchorPane1.getChildren()) {
            if (child.getClass() == Circle.class) {
                Circle c = (Circle) child;
                if (c == relation.getControl1() ) {
                    cc1 = c;
                    cc1find = true;
                } else if (c == relation.getControl2()) {
                    cc2 = c;
                    cc2find = true;
                }
            }
        }
        if (cc1find && cc2find) {
            this.anchorPane1.getChildren().removeAll(cc1, cc2);
            return true;
        }
        return false;
    }

    /**
     * manages the substitution of the Relation's arrow in the view
     *
     * @param relation  relation which we have to update the arrow in the view
     * @param path former arrow of the relation
     * @param circle former endPoint circle of the relation
     */
    public void activateRelationBuildingArrow(Relation relation, Path path,
                                              Circle circle) {
        this.anchorPane1.getChildren().remove(path);
        this.anchorPane1.getChildren().remove(circle);
        relation.buildRelationShape();
        this.anchorPane1.getChildren().addAll(relation.getEndArrow(),
                relation.getArrow(), relation.getControl1(), relation.getControl2());
        relation.getControl1().toBack();
        relation.getControl2().toBack();
        relation.getArrow().toBack();
        relation.getEndArrow().toBack();
    }

    /**
     * manage the position of the labelName when we click on a Relation's arrow
     */
    public void positionLabelName(MouseEvent mouseEvent) {
        if ((mouseEvent.getX() + this.labelName.getWidth()) >
                this.anchorPane1.getWidth()-this.BOUND ) {
            this.labelName.setLayoutX(mouseEvent.getX() - this.labelName.getWidth());
        }else{
            this.labelName.setLayoutX(mouseEvent.getX());
        }
        if ((mouseEvent.getY() + this.labelName.getHeight()) >
                this.anchorPane1.getHeight() - this.BOUND) {
            this.labelName.setLayoutY(mouseEvent.getY() - this.labelName.getHeight());
        } else {
            this.labelName.setLayoutY(mouseEvent.getY());
        }

        this.labelName.setTranslateX(this.BOUND);
        this.labelName.setTranslateY(this.BOUND);

    }



    /**
     * manages the coloration of the Transactions of the generated graph
     */
    public void colorTransactions() {
        this.transactions.forEach(transaction -> {
            transaction.getRectangle().setStroke(this.style.getStrokeColor());
            transaction.getRectangle().setFill(this.style.getBackgroundColor());
            transaction.getText().setFill(this.style.getTextColor());
        });
    }

    /**
     * manages the coloration of the Relation of the generated graph
     */
    public void colorRelations() {
        this.relations.forEach(relation -> {
            if (this.style.getPattern().isEmpty()) {
                relation.manageColorRelation(this.style.getClassicDependencyColor());
            } else if (relation.isSelectedDependencyRelation(this.style.getPattern())) {
                relation.manageColorRelation(this.style.getSelectedDependencyColor());
            } else {
                relation.manageColorRelation(this.style.getClassicDependencyColor());
            }
        });
    }

    /**
     * Hides all control circle of the the arrows excepted
     * the relation's control circles
     *
     * @param relation relation whose do not want to hide the control circles
     */
    public void hideControlCircles(Relation relation) {
        if (relation == null) {
            relations.forEach(relation1 -> {
                relation1.getControl1().setVisible(false);
                relation1.getControl2().setVisible(false);
            });
        } else {
            relations.forEach(relation1 -> {
                if (relation1 != relation) {
                    relation1.getControl1().setVisible(false);
                    relation1.getControl2().setVisible(false);
                }else{
                    relation.getControl1().setVisible(true);
                    relation.getControl2().setVisible(true);
                    relation.getControl1().toFront();
                    relation.getControl2().toFront();
                }
            });
        }
    }


    /**
     * identifies the bigger width of all Transaction's rectangle
     *
     * @param transactions all Transactions
     * @return the max value of Transaction's rectangle's width
     */
    private double getMaxWidthTransactions(ArrayList<Transaction> transactions) {
        double max = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getRectangle().getWidth() > max) {
                max = transaction.getRectangle().getWidth();
            }
        }
        return max;
    }

    /**
     * Put Relations into the main Pane
     *
     * @param relations list of Relations that we want tu put into the main pane
     */
    private void addRelationsToPane(ArrayList<Relation> relations) {
        for (Relation relation : relations) {
            anchorPane1.getChildren().addAll(relation.getArrow(), relation.getEndArrow(),
                    relation.getControl1(), relation.getControl2());
        }
    }

    /**
     * Put Transactions into the main Pane
     *
     * @param transactions list of Transactions that we want tu put into the main pane
     */
    private void addTransactionsToPane(ArrayList<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            anchorPane1.getChildren().addAll(transaction.getRectangle(),
                    transaction.getText());
        }
    }

    /**
     * Fills a list of Relations from a map
     *
     * @param map          map which contains all Relations information
     * @param relations    list of Relations that we want to fill
     * @param transactions list of Transaction use to the Transactions of a Relation
     */
    private void fillListRelationFromMap(HashMap<Integer, String[]> map,
                                         ArrayList<Relation> relations,
                                         ArrayList<Transaction> transactions) {
        for (int i = 0; i < map.size(); i++) {
            String content[] = map.get(i);

            Relation r = new Relation(getTransactionFromId(transactions, content[0]),
                    getTransactionFromId(transactions, content[1]), content[2], content[3]);
            relations.add(r);
        }
    }

    /**
     * Finds a Transaction into a list of Transaction by its id
     *
     * @param transactions list of Transactions where we searched a Transaction
     * @param id           id of the searched Transaction
     * @return the transaction with the same id as id parameter
     */
    private Transaction getTransactionFromId(ArrayList<Transaction> transactions,
                                             String id) {
        for (Transaction transaction : transactions) {
            if (transaction.getId().equals(id)) {
                return transaction;
            }
        }
        return null;
    }

    /**
     * Fills a list of Transactions from a map
     *
     * @param map          map which contains all Transactions information
     * @param transactions list of Transactions that we want to fill
     */
    private void fillListTransactionFromMap(HashMap<Integer, String> map,
                                            ArrayList<Transaction> transactions) {
        for (int i = 0; i < map.size(); i++) {
            String content = map.get(i);
            Transaction t = new Transaction(content);
            transactions.add(t);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPane2.setVisible(false);
        labelElement2.setText("no element selected");
        labelSource.setText("Source :");
        labelSource2.setText("no source found");
        labelTarget.setText("Target :");
        labelTarget2.setText("no target found");
        labelDependencies.setText("Dependencies");
        listViewDependencies.setStyle("-fx-font-size : 11");
        labelName.setVisible(false);
        this.listViewSourceLines.setStyle("-fx-font-size : 11");

        this.listViewTargetLines.setStyle("-fx-font-size : 11");
        this.anchorPane3.setVisible(false);
    }
}
