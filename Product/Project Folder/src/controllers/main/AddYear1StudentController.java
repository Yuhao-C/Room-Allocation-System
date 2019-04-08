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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddYear1StudentController implements Initializable {

    private ObservableList<StudentString> studentObservableList = FXCollections.observableArrayList();

    public void setSelectedItem(StudentString selectedItem) {
        this.selectedItem = selectedItem;
    }

    private StudentString selectedItem;

    public void setBoyGirl(int boyGirl) {
        this.boyGirl = boyGirl;
    }

    private int boyGirl; // boy = 0, girl = 1;

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
    void cancelClicked(ActionEvent event) {
        selectedItem = null;
        HandleButton button = new HandleButton();
        button.handleCancelButton(cancelButton);
    }

    @FXML
    StudentString okClick(ActionEvent event) {
        Stage currentStage = (Stage) okButton.getScene().getWindow();
        currentStage.close();
        return selectedItem;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        initializingContents(okButton, studentTableView, sexColumn, countryColumn, continentColumn, givenNameColumn, familyNameColumn);
    }

    static void initializingContents(Button okButton, TableView<StudentString> studentTableView, TableColumn<StudentString, String> sexColumn, TableColumn<StudentString, String> countryColumn, TableColumn<StudentString, String> continentColumn, TableColumn<StudentString, String> givenNameColumn, TableColumn<StudentString, String> familyNameColumn) {
        okButton.setDisable(true);
        studentTableView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    okButton.setDisable(false);
                });
        sexColumn.setStyle("-fx-alignment: CENTER;");
        countryColumn.setStyle("-fx-alignment: CENTER;");
        continentColumn.setStyle("-fx-alignment: CENTER;");
        givenNameColumn.setCellValueFactory(new PropertyValueFactory<>("givenName"));
        familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        continentColumn.setCellValueFactory(new PropertyValueFactory<>("continent"));
    }

    public void populateTableView() {
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs1.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            List<Integer> allocatedStudentIds = new ArrayList<>();
            while (rs1.next()) {
                for (int i = 6; i <= numberOfColumns; i++) {
                    int studentId = rs1.getInt(i);
                    if (studentId != 0) {
                        allocatedStudentIds.add(studentId);
                    }
                }
            }
            ResultSet rs;
            if (boyGirl == 0) {
                rs = stmt.executeQuery("SELECT * FROM Students Where Year = " + 1 + " AND \"Sex\" = \"male\";");
            } else {
                rs = stmt.executeQuery("SELECT * FROM Students Where Year = " + 1 + " AND \"Sex\" = \"female\";");
            }
            addStudentToList(rs, studentObservableList, allocatedStudentIds);
            stmt.close();

            studentTableView.setItems(studentObservableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addStudentToList(ResultSet rs, ObservableList<StudentString> studentObservableList, List<Integer> allocatedStudentIds) throws SQLException {
        while (rs.next()) {
            Integer studentId = rs.getInt("Id");
            if (!allocatedStudentIds.contains(studentId)) {
                StudentString student = new StudentString();
                student.setId(studentId);
                student.setGivenName(rs.getString("GivenName"));
                student.setFamilyName(rs.getString("FamilyName"));
                student.setSex(rs.getString("Sex"));
                student.setCountry(rs.getString("Country"));
                student.setContinent(rs.getString("Continent"));
                student.setYear(rs.getInt("Year"));
                studentObservableList.add(student);
            }
        }
    }
}
