package controllers.configurations;

import functional.AutoCompleteComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static controllers.configurations.Year1StudentAddClickedController.showAlert;

public class Year2StudentAddClickedController implements Initializable {
    public boolean isOkButtonClicked() {
        return okButtonClicked;
    }

    private boolean okButtonClicked = false;

    private Map<String,String> countries = new HashMap<>();

    public TextField getFamilyNameTF() {
        return familyNameTF;
    }

    public TextField getGivenNameTF() {
        return givenNameTF;
    }

    public ComboBox<String> getSexCB() {
        return sexCB;
    }

    public ComboBox<String> getCountryCB() {
        return countryCB;
    }

    public TextField getContinentTF() {
        return continentTF;
    }

    private Map<String,String> continents = new HashMap<>();

    @FXML
    private TextField familyNameTF;

    @FXML
    private TextField givenNameTF;

    @FXML
    private ComboBox<String> sexCB;

    @FXML
    private ComboBox<String> countryCB;

    @FXML
    private TextField continentTF;

    @FXML
    void okClicked(ActionEvent event) {
        if (givenNameTF.getText().isEmpty() || familyNameTF.getText().isEmpty()) {
            showAlert();
        } else {
            try {
                sexCB.getValue().isEmpty();
                AutoCompleteComboBox.getComboBoxValue(sexCB).isEmpty();
                AutoCompleteComboBox.getComboBoxValue(countryCB).isEmpty();
                okButtonClicked = true;
                Stage currentStage = (Stage) continentTF.getScene().getWindow();
                currentStage.close();
            } catch (Exception e) {
                showAlert();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Year1StudentAddClickedController.initializingContents(continentTF, sexCB, countries, countryCB, continents);
    }
}
