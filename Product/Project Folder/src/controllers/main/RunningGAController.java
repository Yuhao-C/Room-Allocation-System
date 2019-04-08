package controllers.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class RunningGAController {
    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    @FXML
    private ProgressIndicator progressIndicator;

    public Label getLabel1() {
        return label1;
    }

    public Label getLabel2() {
        return label2;
    }

    public Label getLabel3() {
        return label3;
    }

    public Button getOkButton() {
        return okButton;
    }

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;

    @FXML
    private Button okButton;

    @FXML
    void okClicked(ActionEvent event) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

}
