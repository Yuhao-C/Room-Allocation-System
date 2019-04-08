package controllers.main;

import GA.Population;
import functional.StudentString;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShowUnallocatedStudentsController implements Initializable {

    public boolean isProceed() {
        return proceed;
    }

    public void setProceed(boolean proceed) {
        this.proceed = proceed;
    }

    private boolean proceed;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    public TableView<StudentString> getStudentTableView() {
        return studentTableView;
    }

    @FXML
    private TableView<StudentString> studentTableView;

    @FXML
    private TableColumn<StudentString, String> givenNameColumn;

    @FXML
    private TableColumn<StudentString, String> familyNameColumn;

    @FXML
    private TableColumn<StudentString, String> sexColumn;

    @FXML
    private TableColumn<StudentString, String> countryColumn;

    @FXML
    private TableColumn<StudentString, String> continentColumn;

    @FXML
    void cancelClick(ActionEvent event) {
        proceed = false;
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void okClick(ActionEvent event) {
        proceed = true;
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentTableView.setEditable(false);
        sexColumn.setStyle("-fx-alignment: CENTER;");
        countryColumn.setStyle("-fx-alignment: CENTER;");
        continentColumn.setStyle("-fx-alignment: CENTER;");
        givenNameColumn.setCellValueFactory(new PropertyValueFactory<>("givenName"));
        familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        continentColumn.setCellValueFactory(new PropertyValueFactory<>("continent"));
        List<StudentString> unallocatedStudents = Population.findUnallocatedStudents(3);
        ObservableList<StudentString> students = FXCollections.observableArrayList();
        students.addAll(unallocatedStudents);
        studentTableView.setItems(students);
    }

    Stage getStage() {
        return (Stage) studentTableView.getScene().getWindow();
    }
}
