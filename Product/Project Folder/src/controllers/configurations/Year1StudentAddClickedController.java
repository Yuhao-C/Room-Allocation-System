package controllers.configurations;

import controllers.main.MainController;
import functional.AutoCompleteComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Year1StudentAddClickedController implements Initializable {

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

    public static void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Please Fill in all Fields!");
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializingContents(continentTF, sexCB, countries, countryCB, continents);
    }

    static void initializingContents(TextField continentTF, ComboBox<String> sexCB, Map<String, String> countries, ComboBox<String> countryCB, Map<String, String> continents) {
        continentTF.setEditable(false);
        sexCB.getItems().addAll("male", "female");
        Locale.setDefault(Locale.US);
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            countries.put(locale.getDisplayCountry(), countryCode.toUpperCase());
            countryCB.getItems().add(locale.getDisplayCountry());
        }
        AutoCompleteComboBox.setAutoComplete(countryCB, (typedText, itemToCompare) -> itemToCompare.toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.equals(typedText));
        continents.put("AS", "Asia");
        continents.put("EU", "Europe");
        continents.put("NA", "North America");
        continents.put("AF", "Africa");
        continents.put("AN", "Antarctica");
        continents.put("SA", "South America");
        continents.put("OC", "Oceania");
        countryCB.setOnHidden(event -> {
            try {
                String countryCode = countries.get(countryCB.getValue());
                InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("country_continent.csv");
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);
                while (br.ready()) {
                    String[] line = br.readLine().split(",");
                    if (line[0].equals(countryCode)) {
                        continentTF.setText(continents.get(line[1]));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

