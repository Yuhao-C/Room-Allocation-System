package controllers.newFile;

import com.sun.tools.javac.Main;
import functional.AutoCompleteComboBox;
import functional.HandleButton;
import functional.Student;
import controllers.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class StudentConfig2Controller implements Initializable {

    private Map<String,String> countryToCountryCode = new HashMap<>();
    private Map<String,String> continentCodeToContinent = new HashMap<>();

    public void setDeleteDB(boolean deleteDB) {
        this.deleteDB = deleteDB;
    }

    private boolean deleteDB = true;


    private ObservableList<Student> studentsObservableList = FXCollections.observableArrayList();


    @FXML
    private TableView<Student> studentTableView;

    @FXML
    private TableColumn<Student, Integer> idColumn;

    @FXML
    private TableColumn<Student, String> givenNameColumn;

    @FXML
    private TableColumn<Student, String> familyNameColumn;

    @FXML
    private TableColumn<Student, Integer> yearColumn;

    @FXML
    private TableColumn<Student, String> sexColumn;

    @FXML
    private TableColumn<Student, ComboBox<String>> nationalityColumn;

    @FXML
    private TableColumn<Student, String> continentColumn;

    @FXML
    private Button finishButton;

    public Button getCancelButton() {
        return cancelButton;
    }

    @FXML
    private Button cancelButton;

    @FXML
    void cancelClick(ActionEvent event) {
        if (deleteDB) {
            DirectoryController.deleteDB(DirectoryController.oldFileName, DirectoryController.oldDirectory);
            HandleButton button = new HandleButton();
            button.handleCancelButton(cancelButton);
        }
    }

    @FXML
    void finishClick(ActionEvent event) throws IOException {
        saveAndSwitchScene();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Locale.setDefault(Locale.US);
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            countryToCountryCode.put(locale.getDisplayCountry(), countryCode.toUpperCase());
        }
        continentCodeToContinent.put("AS", "Asia");
        continentCodeToContinent.put("EU", "Europe");
        continentCodeToContinent.put("NA", "North America");
        continentCodeToContinent.put("AF", "Africa");
        continentCodeToContinent.put("AN", "Antarctica");
        continentCodeToContinent.put("SA", "South America");
        continentCodeToContinent.put("OC", "Oceania");
        yearColumn.setStyle("-fx-alignment: CENTER;");
        idColumn.setStyle("-fx-alignment: CENTER;");
        sexColumn.setStyle("-fx-alignment: CENTER;");
        nationalityColumn.setStyle("-fx-alignment: CENTER;");
        continentColumn.setStyle("-fx-alignment: CENTER;");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        givenNameColumn.setCellValueFactory(new PropertyValueFactory<>("givenName"));
        familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        nationalityColumn.setCellValueFactory(new PropertyValueFactory<>("countryCB"));
        continentColumn.setCellValueFactory(new PropertyValueFactory<>("continent"));
        populateTableView(1);
        populateTableView(2);
        for (Student student: studentsObservableList) {
            student.getCountryCB().setOnHidden(e -> showContinent(student, countryToCountryCode, continentCodeToContinent, studentTableView));
        }
        studentTableView.setItems(studentsObservableList);
    }


    public static void showContinent(Student student, Map<String, String> countryToCountryCode, Map<String, String> continentCodeToContinent, TableView<Student> studentTableView) {
        try {
            String countryCode = countryToCountryCode.get(student.getCountryCB().getValue());
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("country_continent.csv");
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            while (br.ready()) {
                String[] line = br.readLine().split(",");
                if (line[0].equals(countryCode)) {
                    student.setContinent(continentCodeToContinent.get(line[1]));
                    break;
                }
            }
            studentTableView.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populateTableView(int year) {
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students WHERE Year = " + year + ";");
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("Id"));
                student.setGivenName(rs.getString("GivenName"));
                student.setFamilyName(rs.getString("FamilyName"));
                student.setYear(year);
                student.setSex(rs.getString("Sex"));
                student.setCountryValue(rs.getString("Country"));
                student.setContinent(rs.getString("Continent"));
                studentsObservableList.add(student);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getText(ComboBox<String> comboBox) {
        return AutoCompleteComboBox.getComboBoxValue(comboBox);
    }

    public void saveAndSwitchScene() {
        try {
            for (Student student: studentsObservableList) {
                getText(student.getCountryCB()).isEmpty();
            }
            try {
                Statement stmt = MainController.c.createStatement();
                int id = 1;
                for (Student student: studentsObservableList) {
                    String country = getText(student.getCountryCB());
                    String sql = "UPDATE Students SET Country = '"+ country +"', Continent = \"" + student.getContinent() + "\" WHERE Id = "+ id +";";
                    stmt.executeUpdate(sql);
                    MainController.c.commit();
                    id++;
                }
                HandleButton button = new HandleButton();
                button.handleCancelButton(cancelButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all details of students");
            alert.showAndWait();
        }
    }
}


