package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import sample.Controller.Controller;
import sample.Controller.SelectionController;

import java.io.IOException;
import java.util.ArrayList;

public class Selection {
    private Stage stage;
    private Scene scene;

    private ArrayList<RadioButton> radioButtons;
    private final boolean initialSelection = true;

    public Selection(ArrayList<Transaction> transactions){
        if (this.radioButtons != null) {
            this.radioButtons.clear();
        }else{
            this.radioButtons = new ArrayList<>();
        }
        fillRadioButtons(transactions);
        setSelectionOnAllRadios(this.initialSelection);
    }

    /**
     * fill the radios button list from Transactions
     *
     * @param transactions the list of Transaction needed to fill the radio Buttons
     */
    private void fillRadioButtons(ArrayList<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            this.radioButtons.add(new RadioButton(transaction.getId()));
        }
    }

    /**
     * set a boolean selection to all radio Buttons
     *
     * @param selected boolean value for the applying selection
     */
    private void setSelectionOnAllRadios(boolean selected) {
        this.radioButtons.forEach(radioButton -> radioButton.setSelected(selected));
    }


    /**
     * opens and configures the setting of the selection window
     *
     * @param controller controller from which we want to launch the window
     */
    public void stageConfiguration(Controller controller) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("./View/selection.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            SelectionController selection = loader.getController();
            selection.setController(controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stage = new Stage();
        this.stage.setTitle("Selection transaction window");
        this.scene = new Scene(root, 600, 400);
        this.stage.setScene(this.scene);
        this.stage.show();
    }

    /**
     * closes the stage corresponding the Selection window
     */
    public void closeStage() {
        this.stage.close();
    }

    public ArrayList<RadioButton> getRadioButtons() {
        return radioButtons;
    }
}
