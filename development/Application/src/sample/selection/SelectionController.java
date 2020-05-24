package sample.selection;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import sample.control.Controller;
import sample.Model.Relation;
import sample.Model.Transaction;

import java.util.ArrayList;

public class SelectionController {
    private Controller controller;
    private ArrayList<RadioButton> radios = new ArrayList<>();

    @FXML
    private ListView listView;

    @FXML
    private void onClickButtonUpdate(){
        updateSelectionVisibility();
        this.controller.selection.closeStage();
    }

    @FXML
    private void onClickListView(){

    }

    /**
     * updates the visibility champ of the selected, or not, Transactions in the view
     */
    private void updateSelectionVisibility(){
       show();
       hide();
    }

    /**
     * manages the visibility of Transactions when the corresponding radio Button is selected
     */
    private void show(){
        for (RadioButton radioButton : this.controller.selection.getRadioButtons()) {
            if (radioButton.isSelected()) {
                manageVisibilityRelation(radioButton.getText(), radioButton.isSelected());
                manageVisibilityTransaction(radioButton.getText(), radioButton.isSelected());
            }
        }
    }

    /**
     * manages the visibility of the Transactions <hen the corresponding radio Button
     * is not selected
     */
    private void hide() {
        for (RadioButton radioButton : this.controller.selection.getRadioButtons()) {
            if (!radioButton.isSelected()) {
                manageVisibilityTransaction(radioButton.getText(), radioButton.isSelected());
                manageVisibilityRelation(radioButton.getText(), radioButton.isSelected());
            }
        }
    }

    /**
     * manages the visibility of all visual parts of a Relation
     *
     * @param id  the id of the Transaction which give us the visibility following
     *            if it is selected or not
     * @param selection if the id's Transaction is selected or not
     */
    private void manageVisibilityRelation(String id, boolean selection){
        for (Relation relation : this.controller.relations) {
            if (relation.getSource().getId().equals(id) || relation.getTarget().getId().equals(id)) {
                changeVisibilityRelationElement(relation, selection);
            }
        }
    }

    /**
     * finds and manages the visibility of all visual parts of a Transaction
     *
     * @param id  the id of the Transaction which give us the visibility following
     *            if it is selected or not
     * @param selection if the id's Transaction is selected or not
     */
    private void manageVisibilityTransaction(String id, boolean selection){
        for (Transaction transaction : this.controller.transactions) {
            if (transaction.getId().equals(id)) {
                changeVisibilityTransactionElement(transaction, selection );
            }
        }
    }

    /**
     * manages the visibility of all visual parts of a Relation
     *
     * @param relation targeted relation that we have to manages its visual parts
     * @param visibility visibility that we have to apply to the visual part
     *                     of the relation
     */
    private void changeVisibilityRelationElement(Relation relation,
                                                  boolean visibility) {
        if (!visibility) {
            relation.getControl1().setVisible(visibility);
            relation.getControl2().setVisible(visibility);
        }
        relation.getArrow().setVisible(visibility);
        relation.getEndArrow().setVisible(visibility);
    }

    /**
     *  manages the visibility of all visual parts of a Transaction
     *
     * @param transaction targeted transaction that we have to manages its visual parts
     * @param visibility visibility that we have to apply to the visual part
     *                   of the relation
     */
    private void changeVisibilityTransactionElement(Transaction transaction,
                                                    boolean visibility) {
        transaction.getRectangle().setVisible(visibility);
        transaction.getText().setVisible(visibility);
    }

    /**
     * fill the listView from the list of radio button
     */
    protected void fillListView() {
        this.listView.getItems().addAll(this.controller.selection.getRadioButtons());
    }

    public void setController(Controller controller) {
        this.controller = controller;
        fillListView();
    }
}
