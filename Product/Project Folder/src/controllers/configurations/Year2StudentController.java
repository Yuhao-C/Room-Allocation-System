package controllers.configurations;

import functional.Student;
import controllers.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

import static controllers.configurations.Year1StudentController.*;

public class Year2StudentController implements Initializable {

    private Map<String,String> countries = new HashMap<>();
    private Map<String,String> continents = new HashMap<>();

    private List<Student> editedStudents = new ArrayList<>();

    private ObservableList<Student> studentObservableList = FXCollections.observableArrayList();
    private List<Integer> deletedStudentsIds = new ArrayList<>();
    private List<Student> addedStudents = new ArrayList<>();

    private List<String> givenNames = new ArrayList<>();


    @FXML
    private Button checkButton;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TableView<Student> studentTableView;

    @FXML
    private TableColumn<Student, String> givenNameColumn;

    @FXML
    private TableColumn<Student, String> familyNameColumn;

    @FXML
    private TableColumn<Student, String> sexColumn;

    @FXML
    private TableColumn<Student, String> countryColumn;

    @FXML
    private TableColumn<Student, String> continentColumn;

    @FXML
    private TableColumn<Student, CheckBox> allocatedColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField searchTextField;

    @FXML
    void searchTyped(KeyEvent event) {
        searchGivenName(searchTextField, studentTableView, studentObservableList);
    }

    public static void searchGivenName(TextField searchTextField, TableView<Student> studentTableView, ObservableList<Student> studentObservableList) {
        if (searchTextField.getText().isEmpty()) {
            studentTableView.setItems(studentObservableList);
            studentTableView.refresh();
        } else {
            ObservableList<Student> newList = FXCollections.observableArrayList();
            for (Student student: studentObservableList) {
                if (student.getGivenName().toLowerCase().contains(searchTextField.getText().toLowerCase())) newList.add(student);
            }
            studentTableView.setItems(newList);
            studentTableView.refresh();
        }
    }

    @FXML
    void checkClicked(ActionEvent event) {
        Student selectedStudent = studentTableView.getSelectionModel().getSelectedItem();
        Year1StudentController.checkClickedContents(selectedStudent);
    }

    @FXML
    void addClicked(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/configurations/Year2StudentAddClicked.fxml"));
        stage.setScene(new Scene(loader.load()));
        Year2StudentAddClickedController controller = loader.getController();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnHiding(event1 -> {
            if (controller.isOkButtonClicked()) {
                Student addedStudent = new Student();
                addedStudent.setGivenName(controller.getGivenNameTF().getText());
                addedStudent.setFamilyName(controller.getFamilyNameTF().getText());
                addedStudent.setSex(controller.getSexCB().getValue());
                addedStudent.setCountryValue(controller.getCountryCB().getValue());
                addedStudent.setContinent(controller.getContinentTF().getText());
                addedStudent.setYear(2);
                studentObservableList.add(addedStudent);
                addedStudents.add(addedStudent);
                searchGivenName(searchTextField, studentTableView, studentObservableList);
            }
        });
        stage.showAndWait();
    }

    @FXML
    void cancelClick(ActionEvent event) {
        Year1StudentController.cancelClickContents(studentTableView);
    }

    @FXML
    void deleteClicked(ActionEvent event) {
        Student studentToDelete = studentTableView.getSelectionModel().getSelectedItem();
        Year1StudentController.deleteClickedContents(studentToDelete, addedStudents, studentObservableList, studentTableView, deletedStudentsIds);
        searchGivenName(searchTextField, studentTableView, studentObservableList);
    }


    @FXML
    void okClick(ActionEvent event) {
        updateStudents(editedStudents);
        deleteStudents(deletedStudentsIds);
        addStudents(addedStudents);
        Stage stage = (Stage) studentTableView.getScene().getWindow();
        stage.close();
    }

    @FXML
    void changeContinent(TableColumn.CellEditEvent editedCell) {
        Student editingStudent = studentTableView.getSelectionModel().getSelectedItem();
        if (!editedCell.getNewValue().toString().equals(editedCell.getOldValue().toString())) {
            editingStudent.setContinent(editedCell.getNewValue().toString());
            addToEditedStudents(editingStudent, editedStudents, addedStudents);
        }
    }

    @FXML
    void changeFamilyName(TableColumn.CellEditEvent editedCell) {
        Student editingStudent = studentTableView.getSelectionModel().getSelectedItem();
        if (!editedCell.getNewValue().toString().equals(editedCell.getOldValue().toString())) {
            editingStudent.setFamilyName(editedCell.getNewValue().toString());
            addToEditedStudents(editingStudent, editedStudents, addedStudents);
        }
    }

    @FXML
    void changeGivenName(TableColumn.CellEditEvent editedCell) {
        Student editingStudent = studentTableView.getSelectionModel().getSelectedItem();
        if (!editedCell.getNewValue().toString().equals(editedCell.getOldValue().toString())) {
            editingStudent.setGivenName(editedCell.getNewValue().toString());
            addToEditedStudents(editingStudent, editedStudents, addedStudents);
        }
    }

    @FXML
    void changeSex(TableColumn.CellEditEvent editedCell) {
        if (!editedCell.getNewValue().toString().equals(editedCell.getOldValue().toString())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Please Confirm...");
            alert.setContentText("Changing the student's sex will cause the student to be deleted from current room.\n Are you sure to proceed?");
            Optional<ButtonType> result = alert.showAndWait();
            Student editingStudent = studentTableView.getSelectionModel().getSelectedItem();
            if (result.get() == ButtonType.OK) {
                String newSex = editedCell.getNewValue().toString();
                editingStudent.setSex(newSex);
                int studentId = editingStudent.getId();
                try {
                    Statement stmt = MainController.c.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnNum = rsmd.getColumnCount();
                    int a = 0;
                    while (rs.next()) {
                        for (int i = 6; i <= columnNum; i++) {
                            if (rs.getInt(i) == studentId) {
                                stmt.executeUpdate("UPDATE Rooms SET \"Student " + (i-5) + "\" = NULL WHERE \"Room No./Name\" = \"" + rs.getString(2) + "\" AND \"Building No./Name\" = \"" + rs.getString(3) + "\";");
                                MainController.c.commit();
                                a = 1;
                                break;
                            }
                        }
                        if (a == 1) break;
                    }
                    stmt.executeUpdate("UPDATE Students SET Sex = \"" + newSex + "\" WHERE Id = " + studentId + ";");
                    MainController.c.commit();
                    populateTableView(2, studentObservableList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                editingStudent.setSex(editedCell.getOldValue().toString());
                studentTableView.refresh();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Year1StudentController.initializingContents(2, studentTableView, deleteButton, countries, continents, sexColumn, countryColumn, continentColumn, givenNameColumn, familyNameColumn, allocatedColumn, studentObservableList, editedStudents, checkButton);
    }
}
