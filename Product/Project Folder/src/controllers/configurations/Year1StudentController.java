package controllers.configurations;

import functional.AutoCompleteComboBox;
import functional.Student;
import controllers.main.MainController;
import controllers.newFile.StudentConfig2Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

import static controllers.configurations.Year2StudentController.searchGivenName;

public class Year1StudentController implements Initializable {

    private Map<String,String> countries = new HashMap<>();
    private Map<String,String> continents = new HashMap<>();

    private List<Student> editedStudents = new ArrayList<>();

    private ObservableList<Student> studentObservableList = FXCollections.observableArrayList();
    private List<Integer> deletedStudentsIds = new ArrayList<>();
    private List<Student> addedStudents = new ArrayList<>();

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

    @FXML
    void checkClicked(ActionEvent event) {
        Student selectedStudent = studentTableView.getSelectionModel().getSelectedItem();
        checkClickedContents(selectedStudent);
    }

    static void checkClickedContents(Student selectedStudent) {
        if (selectedStudent.getAllocated().isSelected()) {
            String room = "";
            String building = "";
            try {
                int studentId = selectedStudent.getId();
                Statement stmt = MainController.c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
                ResultSetMetaData rsmd = rs.getMetaData();
                int columNum = rsmd.getColumnCount();
                while (rs.next()) {
                    for (int i = 6; i <= columNum; i++) {
                        if (rs.getInt(i) == studentId) {
                            room = rs.getString(2);
                            building = rs.getString(3);
                            break;
                        }
                    }
                    if (!room.equals("") && !building.equals("")) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("The selected student is allocated in...\nBuilding: " + building + "\nRoom: " + room);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("The selected student is not allocated in any room!");
            alert.showAndWait();
        }
    }

    @FXML
    void addClicked(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/configurations/Year1StudentAddClicked.fxml"));
        stage.setScene(new Scene(loader.load()));
        Year1StudentAddClickedController controller = loader.getController();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnHiding(event1 -> {
            if (controller.isOkButtonClicked()) {
                Student addedStudent = new Student();
                addedStudent.setGivenName(controller.getGivenNameTF().getText());
                addedStudent.setFamilyName(controller.getFamilyNameTF().getText());
                addedStudent.setSex(controller.getSexCB().getValue());
                addedStudent.setCountryValue(controller.getCountryCB().getValue());
                addedStudent.setContinent(controller.getContinentTF().getText());
                addedStudent.setYear(1);
                studentObservableList.add(addedStudent);
                addedStudents.add(addedStudent);
                searchGivenName(searchTextField, studentTableView, studentObservableList);
            }
        });
        stage.showAndWait();
    }

    @FXML
    void cancelClick(ActionEvent event) {
        cancelClickContents(studentTableView);
    }

    static void cancelClickContents(TableView<Student> studentTableView) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Confirmation Dialog");
        alert.setContentText("By clicking OK, you will lose all changes you just made!\n" +
                "Are you sure to continue?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Stage stage = (Stage) studentTableView.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void deleteClicked(ActionEvent event) {
        Student studentToDelete = studentTableView.getSelectionModel().getSelectedItem();
        deleteClickedContents(studentToDelete, addedStudents, studentObservableList, studentTableView, deletedStudentsIds);
        searchGivenName(searchTextField, studentTableView, studentObservableList);
    }

    static void deleteClickedContents(Student studentToDelete, List<Student> addedStudents, ObservableList<Student> studentObservableList, TableView<Student> studentTableView, List<Integer> deletedStudentsIds) {
        if (addedStudents.contains(studentToDelete)) {
            addedStudents.remove(studentToDelete);
            studentObservableList.remove(studentToDelete);
            studentTableView.refresh();
        } else {
            deletedStudentsIds.add(studentToDelete.getId());
            studentObservableList.remove(studentToDelete);
            studentTableView.refresh();
        }
    }

    @FXML
    void okClick(ActionEvent event) {
        updateStudents(editedStudents);
        deleteStudents(deletedStudentsIds);
        addStudents(addedStudents);
        Stage stage = (Stage) studentTableView.getScene().getWindow();
        stage.close();
    }

    public static void updateStudents(List<Student> editedStudents) {
        for (Student student: editedStudents) {
            try {
                Statement stmt = MainController.c.createStatement();
                stmt.executeUpdate("UPDATE Students SET " +
                        "GivenName = \"" + student.getGivenName() + "\", " +
                        "FamilyName = \"" + student.getFamilyName() + "\", " +
                        "Sex = \"" + student.getSex() + "\", " +
                        "Country = \"" + AutoCompleteComboBox.getComboBoxValue(student.getCountryCB()) + "\", " +
                        "Continent = \"" + student.getContinent() + "\" WHERE " +
                        "Id = " + student.getId() + ";");
                MainController.c.commit();
                stmt.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteStudents(List<Integer> deletedStudentIds) {
        for (Integer studentId : deletedStudentIds) {
            try {
                Statement stmt = MainController.c.createStatement();
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
                ResultSetMetaData rsmd = rs1.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                int roomId = 0;
                int columnNum = 0;
                while (rs1.next()) {
                    for (int i = 6; i <= numberOfColumns; i++) {
                        if (rs1.getInt(i) == studentId) {
                            roomId = rs1.getInt(1);
                            columnNum = i;
                            break;
                        }
                    }
                    if (columnNum != 0) break;
                }
                rs1.close();
                if (roomId != 0) {
                    stmt.executeUpdate("UPDATE Rooms SET 'Student " + (columnNum - 5) + "' = NULL WHERE Id = " + roomId + ";");
                    MainController.c.commit();
                }
                stmt.executeUpdate("DELETE FROM Students WHERE Id = " + studentId + ";");
                MainController.c.commit();
                stmt.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addStudents(List<Student> addedStudents) {
        try {
            Statement stmt = MainController.c.createStatement();
            for (Student addedStudent: addedStudents) {
                stmt.executeUpdate("INSERT INTO Students (GivenName, FamilyName, Sex, Country, Continent, 'Year') VALUES " +
                        "('" + addedStudent.getGivenName()
                        + "', '" + addedStudent.getFamilyName()
                        + "', '" + addedStudent.getSex()
                        + "', '" + AutoCompleteComboBox.getComboBoxValue(addedStudent.getCountryCB())
                        + "', '" + addedStudent.getContinent()
                        + "', " + addedStudent.getYear() + ")");
                MainController.c.commit();
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializingContents(1, studentTableView, deleteButton, countries, continents, sexColumn, countryColumn, continentColumn, givenNameColumn, familyNameColumn, allocatedColumn, studentObservableList, editedStudents, checkButton);
    }

    static void initializingContents(int year, TableView<Student> studentTableView, Button deleteButton, Map<String, String> countries, Map<String, String> continents, TableColumn<Student, String> sexColumn, TableColumn<Student, String> countryColumn, TableColumn<Student, String> continentColumn, TableColumn<Student, String> givenNameColumn, TableColumn<Student, String> familyNameColumn, TableColumn<Student, CheckBox> allocatedColumn, ObservableList<Student> studentObservableList, List<Student> editedStudents, Button checkButton) {
        studentTableView.setEditable(true);
        deleteButton.setDisable(true);
        checkButton.setDisable(true);
        Locale.setDefault(Locale.US);
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            countries.put(locale.getDisplayCountry(), countryCode.toUpperCase());
        }
        continents.put("AS", "Asia");
        continents.put("EU", "Europe");
        continents.put("NA", "North America");
        continents.put("AF", "Africa");
        continents.put("AN", "Antarctica");
        continents.put("SA", "South America");
        continents.put("OC", "Oceania");
        sexColumn.setStyle("-fx-alignment: CENTER;");
        countryColumn.setStyle("-fx-alignment: CENTER;");
        continentColumn.setStyle("-fx-alignment: CENTER;");
        allocatedColumn.setStyle("-fx-alignment: CENTER;");
        givenNameColumn.setCellValueFactory(new PropertyValueFactory<>("givenName"));
        givenNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        familyNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        sexColumn.setCellFactory(ComboBoxTableCell.forTableColumn("male", "female"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("countryCB"));
        continentColumn.setCellValueFactory(new PropertyValueFactory<>("continent"));
        allocatedColumn.setCellValueFactory(new PropertyValueFactory<>("allocated"));
        populateTableView(year, studentObservableList);
        for (Student student: studentObservableList) {
            student.getCountryCB().setOnHidden(e -> {
                StudentConfig2Controller.showContinent(student, countries, continents, studentTableView);
                editedStudents.add(student);
            });
        }
        studentTableView.setItems(studentObservableList);
        studentTableView.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            deleteButton.setDisable(false);
            checkButton.setDisable(false);
        } );
    }

    public static void populateTableView(int year, ObservableList<Student> studentObservableList) {
        try {
            studentObservableList.clear();
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students WHERE Year = " + year + ";");
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("Id"));
                student.setGivenName(rs.getString("GivenName"));
                student.setFamilyName(rs.getString("FamilyName"));
                student.setSex(rs.getString("Sex"));
                student.setCountryValue(rs.getString("Country"));
                student.setContinent(rs.getString("Continent"));
                if (student.isAllocated(rs.getInt("Id"))) {
                    student.getAllocated().setSelected(true);
                } else {
                    student.getAllocated().setSelected(false);
                }
                studentObservableList.add(student);
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    populateTableView(1, studentObservableList);
                    studentTableView.refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                editingStudent.setSex(editedCell.getOldValue().toString());
                studentTableView.refresh();
            }
        }
    }

    public static void addToEditedStudents(Student student, List<Student> editedStudents, List<Student> addedStudents) {
        if (!editedStudents.contains(student) && !addedStudents.contains(student)) {
            editedStudents.add(student);
        }
    }
}
