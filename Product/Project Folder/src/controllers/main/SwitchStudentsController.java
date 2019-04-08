package controllers.main;

import functional.HandleButton;
import functional.StudentString;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SwitchStudentsController implements Initializable {

    private ObservableList<StudentString> studentObservableList = FXCollections.observableArrayList();

    public StudentString getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(StudentString selectedItem) {
        this.selectedItem = selectedItem;
    }

    private StudentString selectedItem;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

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
    void keyTyped(KeyEvent event) {
        selectedItem = studentTableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    void mouseClicked(MouseEvent event) {
        selectedItem = studentTableView.getSelectionModel().getSelectedItem();
    }


    @FXML
    void cancelClick(ActionEvent event) {
        selectedItem = null;
        HandleButton button = new HandleButton();
        button.handleCancelButton(cancelButton);
    }

    @FXML
    void okClick(ActionEvent event) {
        Stage currentStage = (Stage) okButton.getScene().getWindow();
        currentStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AddYear1StudentController.initializingContents(okButton, studentTableView, sexColumn, countryColumn, continentColumn, givenNameColumn, familyNameColumn);
    }

    public void populateTableView(String room, String building) {
        try {


            Statement stmt = MainController.c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            rs.next();
            List<Integer> studentIds = new ArrayList<>();
            for (int i = 6; i <= numberOfColumns; i++) {
                int studentId = rs.getInt(i);
                if (studentId != 0) {
                    studentIds.add(studentId);
                }
            }
            rs.close();
            for (Integer studentId: studentIds) {
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM Students WHERE Id = " + studentId + ";");
                StudentString student = new StudentString();
                student.setId(studentId);
                student.setGivenName(rs1.getString("GivenName"));
                student.setFamilyName(rs1.getString("FamilyName"));
                student.setSex(rs1.getString("Sex"));
                student.setCountry(rs1.getString("Country"));
                student.setContinent(rs1.getString("Continent"));
                student.setYear(rs1.getInt("Year"));
                studentObservableList.add(student);
            }
            stmt.close();

            studentTableView.setItems(studentObservableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
