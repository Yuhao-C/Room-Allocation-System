package controllers.main;

import GA.Population;
import com.jfoenix.controls.JFXTreeView;
import com.sun.tools.javac.Main;
import functional.Room;
import functional.StudentString;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public static Connection c;

    ObservableList<StudentString> students = FXCollections.observableArrayList();

    ObservableList<TreeItem<String>> buildingsTreeItems = FXCollections.observableArrayList();

    TreeItem<String> root;

    public static String fileName;
    public static String directory;

    public boolean finishedAllocation;

    @FXML
    private TextField boyGirlTextField;

    @FXML
    private TextField roomCapacityTextField;

    @FXML
    private TextField bedsAvailableTextField;

    @FXML
    private MenuItem fileNew;

    @FXML
    private MenuItem fileOpen;

    @FXML
    private MenuItem fileQuit;

    @FXML
    private TableView<StudentString> roomTableView;

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
    private TableColumn<StudentString, String> yearColumn;

    ContextMenu contextMenu = new ContextMenu();
    Menu addMenu = new Menu("Add");
    MenuItem addYear1Student = new MenuItem("Year 1 Student...");
    MenuItem addYear2Student = new MenuItem("Year 2 Student...");
    MenuItem removeMenuItem = new MenuItem("Remove");
    MenuItem moveMenuItem = new MenuItem("Move to...");
    MenuItem switchMenuItem = new MenuItem("Switch with...");

    @FXML
    private JFXTreeView<String> treeView = new JFXTreeView<>();

    List<String> buildingNames = new ArrayList<>();

    @FXML
    void fileNewClicked(ActionEvent event) throws IOException {
        Parent directoryLayout = FXMLLoader.load(getClass().getResource("/fxmls/newFile/DirectoryView.fxml"));
        Stage fileNewStage = new Stage();
        fileNewStage.setScene(new Scene(directoryLayout));
        fileNewStage.setTitle("newFile Room Allocation");
        fileNewStage.setResizable(false);
        fileNewStage.initModality(Modality.APPLICATION_MODAL);
        fileNewStage.centerOnScreen();
        fileNewStage.showAndWait();
        showTreeView();
    }

    @FXML
    void fileOpenClicked(ActionEvent event) throws SQLException{
        Stage currentStage = (Stage) treeView.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("sqlite Files", "*.sqlite"));
        File selectedFile = fileChooser.showOpenDialog(currentStage);
        if (selectedFile != null) {
            MainController.fileName = selectedFile.getName();
            MainController.directory = selectedFile.getAbsolutePath().replace("\\", "/");
            MainController.directory = MainController.directory.substring(0, MainController.directory.length() - MainController.fileName.length() - 1);
            MainController.fileName = MainController.fileName.substring(0, MainController.fileName.length() - 7);
            connectToDB();
            roomTableView.setItems(null);
            roomCapacityTextField.setText("");
            bedsAvailableTextField.setText("");
            boyGirlTextField.setText("");
        }
        showTreeView();
    }

    @FXML
    void fileQuitClicked(ActionEvent event) {
        writeDirectoryFile();
        System.exit(0);
    }
    @FXML
    void addOrDeleteClicked(ActionEvent event) {
        openWindow("/fxmls/configurations/AddOrDeleteRoom.fxml", "Room Configuration");
        showTreeView();
    }

    @FXML
    void updateRoomInfoClicked(ActionEvent event) {
        openWindow("/fxmls/configurations/UpdateRoomInfo.fxml", "Room Configuration");
        showTreeView();
    }

    @FXML
    void year1ConfigClicked(ActionEvent event) {
        openWindow("/fxmls/configurations/Year1Student.fxml", "Year 1 Student Configuration");
        showTreeView();
    }

    @FXML
    void year2ConfigClicked(ActionEvent event) {
        openWindow("/fxmls/configurations/Year2Student.fxml", "Year 2 Student Configuration");
        showTreeView();
    }

    @FXML
    void clearYear1AllocationClicked(ActionEvent event) {
        clearAllocation(1);
    }

    @FXML
    void clearYear2AllocationClicked(ActionEvent event) {
        clearAllocation(2);
    }


    private void clearAllocation(int year) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Please Confirm...");
        alert.setContentText("By clicking OK, all Year " + year + " allocation information will be deleted.\nAre you sure to proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                List<Integer> studentIds = new ArrayList<>();
                Statement stmt = MainController.c.createStatement();
                ResultSet rs1 = stmt.executeQuery("SELECT id FROM Students WHERE \"Year\" = " + year + ";");
                while (rs1.next()) {
                    studentIds.add(rs1.getInt(1));
                }
                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms");
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnNum = rsmd.getColumnCount();
                while (rs.next()) {
                    for (int i = 6; i <= columnNum; i++) {
                        Statement stmt1 = MainController.c.createStatement();
                        if (studentIds.contains(rs.getInt(i))) {
                            stmt1.executeUpdate("UPDATE Rooms SET \"Student " + (i-5) + "\" = NULL WHERE id = " + rs.getInt(1) + ";");
                        }
                        c.commit();
                        stmt1.close();
                    }
                }
                stmt.close();
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information Dialog");
                alert1.setHeaderText(null);
                alert1.setContentText("Year " + year + " Students Cleared!");
                alert1.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showTreeView();
    }

    @FXML
    void deleteYear1Clicked(ActionEvent event) {
        deleteStudentClicked(1);
    }

    @FXML
    void deleteYear2Clicked(ActionEvent event) {
        deleteStudentClicked(2);
    }

    private void deleteStudentClicked(int year) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Please Confirm...");
        alert.setContentText("By clicking OK, all Year " + year + " students information will be deleted.\nAre you sure to proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                List<Integer> studentIds = new ArrayList<>();
                Statement stmt = MainController.c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Students WHERE \"Year\" = " + year + ";");
                while (rs.next()) {
                    studentIds.add(rs.getInt(1));
                }
                rs.close();
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
                int columnNum = rs1.getMetaData().getColumnCount();
                int rowId = 1;
                while (rs1.next()) {
                    for (int i = 6; i <= columnNum; i++) {
                        if (studentIds.contains(rs1.getInt(i))) {
                            Statement stmt1 = MainController.c.createStatement();
                            stmt1.executeUpdate("UPDATE Rooms SET \"Student " + (i-5) + "\" = NULL WHERE Id = " + rowId + ";");
                        }
                    }
                    rowId++;
                }
                stmt.executeUpdate("DELETE FROM Students WHERE \"Year\" = " + year + ";");
                c.commit();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showTreeView();
    }

    @FXML
    void upgradeClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Please Confirm...");
        alert.setContentText("By clicking OK, the Year 1 Students will be transferred to Year 2 students, and All information of current Year 2 students will be deleted!\n" +
                "Are you sure to proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                Statement stmt = MainController.c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT Id FROM Students WHERE Year = " + 2 + ";");
                List<Integer> year2Ids = new ArrayList<>();
                while (rs.next()) {
                    year2Ids.add(rs.getInt(1));
                }
                for (Integer year2Id: year2Ids) {
                    ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
                    ResultSetMetaData rsmd = rs1.getMetaData();
                    int numberOfColumns = rsmd.getColumnCount();
                    int id = 1;
                    while (rs1.next()) {
                        for (int i = 1; i <= numberOfColumns; i++) {
                            if (year2Ids.contains(rs1.getInt(i))) {
                                Statement stmt1 = c.createStatement();
                                stmt1.executeUpdate("UPDATE Rooms SET \"Student " + (i-5) + "\" = NULL WHERE Id = " + id + ";");
                                c.commit();
                            }
                        }
                        id++;
                    }
                    stmt.executeUpdate("DELETE FROM Students WHERE Id = " + year2Id + ";");
                    MainController.c.commit();
                }
                stmt.executeUpdate("UPDATE Students SET Year = 2 WHERE Year = 1;");
                MainController.c.commit();
                stmt.close();
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information Dialog");
                alert1.setHeaderText(null);
                alert1.setContentText("Year 1 students are now Year 2 students.\nPlease upload the new Year 1 Students.");
                alert1.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            showTreeView();
        }
    }

    @FXML
    void allocateStudentClicked(ActionEvent event) throws IOException {
        if (informationIsNotComplete()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("An Error has Occurred!");
            alert.setContentText("Students' information is not complete!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Confirmation Dialog");
            alert.setContentText("Please choose the group you wish to allocate. All the unallocated students in this group will be automatically allocated to optimize for maximum diversity.");
            ButtonType buttonTypeYear1 = new ButtonType("Year 1");
            ButtonType buttonTypeYear2 = new ButtonType("Year 2");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYear1, buttonTypeYear2, buttonTypeCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeYear1) {
                allocate(1);
            }
            if (result.get() == buttonTypeYear2) {
                allocate(2);
            }
        }
    }

    private void allocate(int year) throws IOException {
        boolean bedsAreEnough = true;
        if (boyBedsNumNotEnough()) {
            Alert alert1 = new Alert(Alert.AlertType.ERROR);
            alert1.setTitle("Error Dialog");
            alert1.setHeaderText("An Error has Occurred!");
            alert1.setContentText("Beds for boys are not enough!");
            alert1.showAndWait();
            bedsAreEnough = false;
        }
        if (girlBedsNumNotEnough()) {
            Alert alert1 = new Alert(Alert.AlertType.ERROR);
            alert1.setTitle("Error Dialog");
            alert1.setHeaderText("An Error has Occurred!");
            alert1.setContentText("Beds for girls are not enough!");
            alert1.showAndWait();
            bedsAreEnough = false;
        }
        if (bedsAreEnough) {
            List<Room> fixedGenes = getFixedGenes();
            Population population = new Population(1000, 0.1, 1000, fixedGenes, year);
            if (population.getUnallocatedStudents().size() != 0) {
                SimpleDoubleProperty progress = new SimpleDoubleProperty(-1.0);
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main/RunningGA.fxml"));
                stage.setScene(new Scene(loader.load()));
                RunningGAController controller = loader.getController();
                controller.getProgressIndicator().setProgress(-1.0f);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Genetic Algorithm Running");
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.setOnCloseRequest(Event::consume);
                stage.show();
                controller.getProgressIndicator().progressProperty().bind(progress);
                Service<Void> backgroundThread = new Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<>() {
                            @Override
                            protected Void call() {
                                runGA(controller, progress, population, fixedGenes);
                                return null;
                            }
                        };
                    }
                };
                backgroundThread.start();
            } else {
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setTitle("Error Dialog");
                alert1.setHeaderText("An Error has Occurred!");
                alert1.setContentText("All students are allocated!");
                alert1.showAndWait();
            }
        }
    }

    private boolean informationIsNotComplete() {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students;");
            while (rs.next()) {
                for (int i = 2; i <= 6; i++) {
                    if (rs.getString(i) == null || rs.getString(i).isEmpty())
                        return true;
                }
                if (rs.getInt(7) == 0)
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    void mouseClicked(MouseEvent event) {
        addMenu.disableProperty().bind(Bindings.createBooleanBinding(this::treeViewValidation));
        removeMenuItem.disableProperty().bind(Bindings.createBooleanBinding(this::tableViewValidation));
        moveMenuItem.disableProperty().bind(Bindings.createBooleanBinding(this::tableViewValidation));
        switchMenuItem.disableProperty().bind(Bindings.createBooleanBinding(this::tableViewValidation));
        MouseButton mouseButton = event.getButton();
        if (mouseButton.equals(MouseButton.SECONDARY)) {
            contextMenu.show(treeView.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            addYear1Student.setOnAction(event1 -> {
                if (bedsAvailableTextField.getText().equals("0")) {
                    showNoSpareCapacityAlert();
                } else {
                    int boyGirl = 0;
                    if (boyGirlTextField.getText().equals("Boy")) boyGirl = 0;
                    if (boyGirlTextField.getText().equals("Girl")) boyGirl = 1;
                    String room = treeView.getSelectionModel().getSelectedItem().getValue();
                    String building = treeView.getSelectionModel().getSelectedItem().getParent().getValue();
                    addToTableView(getStudent(1, boyGirl), room, building, false);
                    bedsAvailableTextField.setText(Integer.toString(Integer.parseInt(roomCapacityTextField.getText()) - roomTableView.getItems().size()));
                }
            });
            addYear2Student.setOnAction(event1 -> {
                if (bedsAvailableTextField.getText().equals("0")) {
                    showNoSpareCapacityAlert();
                } else {
                    int boyGirl = 0;
                    if (boyGirlTextField.getText().equals("Boy")) boyGirl = 0;
                    if (boyGirlTextField.getText().equals("Girl")) boyGirl = 1;
                    String room = treeView.getSelectionModel().getSelectedItem().getValue();
                    String building = treeView.getSelectionModel().getSelectedItem().getParent().getValue();
                    addToTableView(getStudent(2, boyGirl), room, building, false);
                    bedsAvailableTextField.setText(Integer.toString(Integer.parseInt(roomCapacityTextField.getText()) - roomTableView.getItems().size()));
                }
            });
            removeMenuItem.setOnAction(event1 -> removeStudent());
            moveMenuItem.setOnAction(event1 -> {
                StudentString student = roomTableView.getSelectionModel().getSelectedItem();
                List<String> rooms = getRoomsList(false, student);
                ChoiceDialog<String> dialog = new ChoiceDialog<>("",rooms);
                dialog.setTitle("Choice Dialog");
                dialog.setHeaderText("Move Student To...");
                dialog.setContentText("Choose the room:");
                Optional<String> result = dialog.showAndWait();
                String building;
                String room;
                if (result.isPresent()) {
                    building = result.get().split(",")[0];
                    room = result.get().split(",")[1];
                    if (room.equals(treeView.getSelectionModel().getSelectedItem().getValue()) && building.equals(treeView.getSelectionModel().getSelectedItem().getParent().getValue())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Dialog");
                        alert.setHeaderText("An Error has Occurred");
                        alert.setContentText("The selected student is already in this room!");
                        alert.showAndWait();
                    } else {
                        if (hasSpareCapacity(room, building)) {
                            try {

                                Statement stmt = MainController.c.createStatement();

                                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                                rs.next();
                                int roomCapacity = rs.getInt(4);
                                int columnIndex = 0;
                                for (int i = 6; i < 6 + roomCapacity; i++) {
                                    int studentId = rs.getInt(i);
                                    if (studentId == 0) {
                                        columnIndex = i;
                                        break;
                                    }
                                }
                                rs.close();
                                String columnHeader = "Student " + Integer.toString(columnIndex - 5);
                                stmt.executeUpdate("UPDATE Rooms SET '" + columnHeader + "' = " + roomTableView.getSelectionModel().getSelectedItem().getId() + " WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                                MainController.c.commit();
                                stmt.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            removeStudent();
                        } else {
                            showNoSpareCapacityAlert();
                        }
                    }
                }
            });
            switchMenuItem.setOnAction(event1 -> {
                StudentString student = roomTableView.getSelectionModel().getSelectedItem();
                List<String> rooms = getRoomsList(true, student);
                ChoiceDialog<String> dialog = new ChoiceDialog<>("",rooms);
                dialog.setTitle("Choice Dialog");
                dialog.setHeaderText("Switch Students");
                dialog.setContentText("Switch with the student in room:");
                Optional<String> result = dialog.showAndWait();
                String building;
                String room;
                if (result.isPresent()) {
                    building = result.get().split(",")[0];
                    room = result.get().split(",")[1];
                    if (room.equals(treeView.getSelectionModel().getSelectedItem().getValue()) && building.equals(treeView.getSelectionModel().getSelectedItem().getParent().getValue())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Dialog");
                        alert.setHeaderText("An Error has Occurred");
                        alert.setContentText("The selected student is already in this room!");
                        alert.showAndWait();
                    } else {
                        Room room1 = new Room();
                        room1.setRoom(room);
                        room1.setBuilding(building);
                        if (room1.isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error Dialog");
                            alert.setHeaderText("An Error has Occurred");
                            alert.setContentText("There is no student allocated in the selected room!");
                            alert.showAndWait();
                        } else {
                            try {
                                Stage stage = new Stage();
                                stage.setTitle("Switch with...");
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main/SwitchStudents.fxml"));
                                stage.setScene(new Scene(loader.load()));
                                SwitchStudentsController controller = loader.getController();
                                stage.initModality(Modality.APPLICATION_MODAL);
                                stage.setResizable(false);
                                stage.setOnCloseRequest(event2 -> controller.setSelectedItem(null));
                                stage.setOnShowing(event2 -> controller.populateTableView(room, building));
                                stage.setOnHiding(event2 -> {
                                    if (controller.getSelectedItem() != null) {
                                        StudentString student1 = controller.getSelectedItem();
                                        StudentString student2 = roomTableView.getSelectionModel().getSelectedItem();
                                        switchStudentsInDB(student1, student2);
                                    }
                                });
                                stage.showAndWait();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                populateContent(treeView.getSelectionModel().getSelectedItem());
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roomCapacityTextField.setEditable(false);
        bedsAvailableTextField.setEditable(false);
        showTreeView();
        addMenu.getItems().addAll(addYear1Student, addYear2Student);
        contextMenu.getItems().addAll(addMenu, removeMenuItem, new SeparatorMenuItem(), moveMenuItem,switchMenuItem);
        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> populateContent(newValue));
    }

    private void populateContent(TreeItem<String> newValue) {
        if (treeView.getSelectionModel().getSelectedItem() != null) {
            int flag = 1;
            for (int i = 0; i < buildingNames.size(); i++) {
                if (buildingNames.get(i).equals(newValue.getValue())) {
                    flag = 0;
                    break;
                }
            }
            if (flag == 0) {
                bedsAvailableTextField.setText("");
                roomCapacityTextField.setText("");
                boyGirlTextField.setText("");
                roomTableView.setItems(null);
            } else {
                String room = newValue.getValue();
                String building = newValue.getParent().getValue();
                givenNameColumn.setStyle("-fx-alignment: CENTER;");
                familyNameColumn.setStyle("-fx-alignment: CENTER;");
                sexColumn.setStyle("-fx-alignment: CENTER;");
                countryColumn.setStyle("-fx-alignment: CENTER;");
                continentColumn.setStyle("-fx-alignment: CENTER;");
                yearColumn.setStyle("-fx-alignment: CENTER;");
                givenNameColumn.setCellValueFactory(new PropertyValueFactory<>("givenName"));
                familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
                sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
                countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
                continentColumn.setCellValueFactory(new PropertyValueFactory<>("continent"));
                yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
                try {
                    Statement stmt = MainController.c.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    rs.next();
                    int roomCapacity = rs.getInt(4);
                    String boyGirl = rs.getString(5);
                    rs.close();
                    roomCapacityTextField.setText(Integer.toString(roomCapacity));
                    boyGirlTextField.setText(boyGirl);
                    ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    rs1.next();
                    students.clear();
                    for (int i = 6; i < 6 + roomCapacity; i++) {
                        int studentId = rs1.getInt(i);
                        if (studentId == 0) {
                            continue;
                        }
                        Statement stmt1 = MainController.c.createStatement();
                        ResultSet rs2 = stmt1.executeQuery("SELECT * FROM Students WHERE Id = " + studentId + ";");
                        StudentString student = new StudentString();
                        student.setId(rs2.getInt("Id"));
                        student.setContinent(rs2.getString("Continent"));
                        student.setCountry(rs2.getString("Country"));
                        student.setSex(rs2.getString("Sex"));
                        student.setFamilyName(rs2.getString("FamilyName"));
                        student.setGivenName(rs2.getString("GivenName"));
                        student.setYear(rs2.getInt("Year"));
                        students.add(student);
                        stmt1.close();
                    }
                    roomTableView.setItems(students);
                    try {
                        int studentNum = roomTableView.getItems().size();
                        bedsAvailableTextField.setText(Integer.toString(roomCapacity - studentNum));
                    } catch (NullPointerException e) {
                        bedsAvailableTextField.setText(Integer.toString(roomCapacity));
                    }
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean treeViewValidation() {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == (null)) {
            return true;
        } else {
            return (buildingsTreeItems.contains(selectedItem));
        }
    }

    private boolean tableViewValidation() {
        return (roomTableView.getSelectionModel().getSelectedItem() == null);
    }

    private StudentString getStudent(int year, int boyGirl) { // boy = 0, girl = 1;
        try {
            Stage stage = new Stage();
            if (year == 1) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main/AddYear1Student.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Add Year 1 Student");
                AddYear1StudentController controller = loader.getController();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setOnCloseRequest(event -> controller.setSelectedItem(null));
                stage.setOnShowing(event -> {
                    controller.setBoyGirl(boyGirl);
                    controller.populateTableView();
                });
                stage.showAndWait();
                return controller.okClick(new ActionEvent());
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main/AddYear2Student.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Add Year 2 Student");
                AddYear2StudentController controller = loader.getController();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setOnCloseRequest(event -> controller.setSelectedItem(null));
                stage.setOnShowing(event -> {
                    controller.setBoyGirl(boyGirl);
                    controller.populateTableView();
                });
                stage.showAndWait();
                return controller.okClick(new ActionEvent());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addToTableView(StudentString student, String room, String building, boolean isMove) {
        if (student != null) {
            try {
                Statement stmt = MainController.c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                rs.next();
                int roomCapacity = rs.getInt(4);
                int columnIndex = 0;
                for (int i = 6; i < 6 + roomCapacity; i++) {
                    int studentId = rs.getInt(i);
                    if (studentId == 0) {
                        columnIndex = i;
                        break;
                    }
                }
                String columnHeader = "Student " + Integer.toString(columnIndex - 5);
                stmt.executeUpdate("UPDATE Rooms SET '" + columnHeader + "' = " + student.getId() + " WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                MainController.c.commit();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isMove) {
                students.add(student);
                roomTableView.refresh();
            }
        }
    }

    void removeStudent() {
        StudentString studentToRemove = roomTableView.getSelectionModel().getSelectedItem();
        students.remove(studentToRemove);
        roomTableView.setItems(students);
        try {
            String room = treeView.getSelectionModel().getSelectedItem().getValue();
            String building = treeView.getSelectionModel().getSelectedItem().getParent().getValue();

            Statement stmt = MainController.c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
            rs.next();
            int columnIndex = 0;
            for (int i = 6; i < 6 + Integer.parseInt(roomCapacityTextField.getText()); i++) {
                int studentId = rs.getInt(i);
                if (studentId == studentToRemove.getId()) {
                    columnIndex = i;
                    break;
                }
            }
            String columnHeader = "Student " + Integer.toString(columnIndex - 5);
            stmt.executeUpdate("UPDATE Rooms SET \"" + columnHeader + "\" = " + null + " WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
            MainController.c.commit();
            stmt.close();
            bedsAvailableTextField.setText(Integer.toString(Integer.parseInt(roomCapacityTextField.getText()) - roomTableView.getItems().size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoSpareCapacityAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("No Available Bed");
        alert.setContentText("Sorry, there is no available bed in this room!");
        alert.showAndWait();
    }

    private boolean hasSpareCapacity(String room, String building) {
        int spareCapacity = 0;
        try {

            Statement stmt = MainController.c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
            rs.next();
            int roomCapacity = rs.getInt(4);
            for (int i = 6; i < 6 + roomCapacity; i++) {
                if (rs.getInt(i) == 0) spareCapacity++;
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (spareCapacity > 0);
    }

    private void showTreeView() {
        roomTableView.setItems(null);
        bedsAvailableTextField.setText("");
        boyGirlTextField.setText("");
        roomCapacityTextField.setText("");
        File file = new File(MainController.directory + "/" + MainController.fileName + ".sqlite");
        treeView.setRoot(new TreeItem<>());
        if (file.exists()) {
            buildingNames.clear();
            root = new TreeItem<>();
            treeView.setRoot(root);
            try {
                Statement stmt = MainController.c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms");
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) {
                    Room room = new Room();
                    room.setBuilding(rs.getString(3));
                    room.setRoom(rs.getString(2));
                    rooms.add(room);
                }
                rooms.sort(MainController::roomComparator);
                TreeItem<String> currentTreeItem = null;
                for (Room room: rooms) {
                    if (!buildingNames.contains(room.getBuilding())) {
                        buildingNames.add(room.getBuilding());
                        currentTreeItem = new TreeItem<>(room.getBuilding());
                        root.getChildren().add(currentTreeItem);
                    }
                    currentTreeItem.getChildren().add(new TreeItem<>(room.getRoom()));
                }
                buildingsTreeItems = root.getChildren();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeDirectoryFile() {
        try {
            String directory = (MainController.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath().replace("\\", "/");
            directory = directory.substring(0, directory.lastIndexOf("/") + 1)  + "/Directory.txt";
            File file = new File(directory);
            file.createNewFile();
            FileWriter fw = new FileWriter(directory,false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.println(MainController.fileName);
            pw.println(MainController.directory);
            pw.flush();
            pw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxml, String windowTitle) {
        try {
            Parent directoryLayout = FXMLLoader.load(getClass().getResource(fxml));
            Stage fileNewStage = new Stage();
            fileNewStage.setScene(new Scene(directoryLayout));
            fileNewStage.setTitle(windowTitle);
            fileNewStage.setResizable(false);
            fileNewStage.initModality(Modality.APPLICATION_MODAL);
            fileNewStage.centerOnScreen();
            fileNewStage.showAndWait();
            showTreeView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getRoomsList(boolean switchRoom, StudentString student) {
        List<String> rooms = new ArrayList<>();
        try {
            Statement stmt = MainController.c.createStatement();
            for (TreeItem<String> building: root.getChildren()) {
                String currentBuilding = building.getValue();
                for (TreeItem<String> room: building.getChildren()) {
                    ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room.getValue() + "\" AND \"Building No./Name\" = \"" + currentBuilding + "\";");
                    int columnNum = rs.getMetaData().getColumnCount();
                    rs.next();
                    if (rs.getString(5).equals(boyGirlTextField.getText()) && (!rs.getString(2).equals(student.getRoom()) || !rs.getString(3).equals(student.getBuilding()))) {
                        if (switchRoom) {
                            boolean roomIsEmpty = true;
                            for (int i = 6; i<= columnNum; i++) {
                                if (rs.getInt(i) != 0) {
                                    roomIsEmpty = false;
                                    break;
                                }
                            }
                            if (!roomIsEmpty) {
                                rooms.add(currentBuilding + "," + room.getValue());
                            }
                        } else {
                            int maxCapacity = rs.getInt(4);
                            int studentNum = 0;
                            for (int i = 6; i <= columnNum; i++) {
                                if (rs.getInt(i) != 0) studentNum++;
                            }
                            if (studentNum < maxCapacity) {
                                rooms.add(currentBuilding + "," + room.getValue());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private void switchStudentsInDB(StudentString student1, StudentString student2) {
        String student1Room = student1.getRoom();
        String student2Room = student2.getRoom();
        String student1Building = student1.getBuilding();
        String student2Building = student2.getBuilding();
        try {

            Statement stmt = MainController.c.createStatement();

            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + student1Room + "\" AND \"Building No./Name\" = \"" + student1Building + "\";");
            ResultSetMetaData rsmd = rs1.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            int columnNum1 = 0;
            rs1.next();
            for (int i = 6; i <= numberOfColumns; i++) {
                if (student1.getId() == rs1.getInt(i)) {
                    columnNum1 = i;
                    break;
                }
            }
            stmt.executeUpdate("UPDATE Rooms SET \"Student " + (columnNum1-5) + "\" = " + student2.getId() + " WHERE \"Room No./Name\" = \"" + student1Room + "\" AND \"Building No./Name\" = \"" + student1Building + "\";");
            MainController.c.commit();
            ResultSet rs2 = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + student2Room + "\" AND \"Building No./Name\" = \"" + student2Building + "\";");
            int columnNum2 = 0;
            rs2.next();
            for (int i = 6; i <= numberOfColumns; i++) {
                if (student2.getId() == rs2.getInt(i)) {
                    columnNum2 = i;
                    break;
                }
            }
            stmt.executeUpdate("UPDATE Rooms SET \"Student " + (columnNum2-5) + "\" = " + student1.getId() + " WHERE \"Room No./Name\" = \"" + student2Room + "\" AND \"Building No./Name\" = \"" + student2Building + "\";");
            MainController.c.commit();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectToDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            MainController.c = DriverManager.getConnection("jdbc:sqlite:" + MainController.directory + "/" + MainController.fileName + ".sqlite");
            MainController.c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void viewRoomClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/view/Room.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Room Information");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    @FXML
    void viewStudentClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/view/Student.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Room Information");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    private boolean boyBedsNumNotEnough() {
        int boyNum = 0;
        int numBoyBeds = 0;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students;");
            while (rs.next()) {
                if (rs.getString(4).equals("male")) boyNum++;
            }
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
            while (rs1.next()) {
                if (rs1.getString(5).equals("Boy")) {
                    numBoyBeds += rs1.getInt(4);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (numBoyBeds < boyNum);
    }

    private boolean girlBedsNumNotEnough() {
        int girlNum = 0;
        int numGirlBeds = 0;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students;");
            while (rs.next()) {
                if (rs.getString(4).equals("female")) girlNum++;
            }
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
            while (rs1.next()) {
                if (rs1.getString(5).equals("Girl")) {
                    numGirlBeds += rs1.getInt(4);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (numGirlBeds < girlNum);
    }

    private List<Room> getFixedGenes() {
        List<Room> fixedGenes = new ArrayList<>();
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt(1));
                room.setRoom(rs.getString(2));
                room.setBuilding(rs.getString(3));
                room.setMaxResidents(rs.getInt(4));
                room.setSexRoom(rs.getString(5));
                for (int i = 6; i <= numberOfColumns; i++) {
                    int studentId = rs.getInt(i);
                    if (studentId != 0) {
                        Statement stmt1 = MainController.c.createStatement();
                        ResultSet rs1 = stmt1.executeQuery("SELECT * FROM Students WHERE Id = " + studentId + ";");
                        rs1.next();
                        StudentString student = new StudentString();
                        student.setId(studentId);
                        student.setGivenName(rs1.getString("GivenName"));
                        student.setFamilyName(rs1.getString("FamilyName"));
                        student.setSex(rs1.getString("Sex"));
                        student.setCountry(rs1.getString("Country"));
                        student.setContinent(rs1.getString("Continent"));
                        student.setYear(rs1.getInt("Year"));
                        room.getStudents().add(student);
                    }
                }
                fixedGenes.add(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fixedGenes;
    }

    @FXML
    void exportClicked(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main/ShowUnallocatedStudents.fxml"));
        stage.setScene(new Scene(loader.load()));
        ShowUnallocatedStudentsController controller = loader.getController();
        stage.setOnHiding(event1 -> {
            if (controller.isProceed()) {
                String directory = chooseDirectory();
                if (directory != null) {
                    if (exportToExcel(directory)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText("The Excel File is successfully generated!");
                        alert.show();
                    } else  {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText("An Unexpected Error has Occurred! Please retry.");
                        alert.show();
                    }
                }
            }
        });
        stage.setOnShown(event1 -> {
            if (controller.getStudentTableView().getItems().isEmpty()) {
                controller.setProceed(true);
                controller.getStage().close();
            }
        });
        stage.show();
    }

    private void runGA(RunningGAController controller, SimpleDoubleProperty progress, Population population, List<Room> fixedGenes) {
        population.calcFitness();
        do {
            population.naturalSelection();
            population.calcFitness();
        } while (population.evaluate());
        Platform.runLater(() -> {
            progress.set(1.0);
            controller.getLabel1().setVisible(false);
            controller.getLabel2().setVisible(false);
            controller.getLabel3().setVisible(false);
            controller.getOkButton().setDisable(false);
            controller.getOkButton().setVisible(true);
        });
        List<Room> bestAllocation = population.getBestOne().getGenes();
        for (Room room: bestAllocation) {
            try {
                int roomId = room.getId();
                List<StudentString> students = room.getStudents();
                List<StudentString> studentsToRemove = new ArrayList<>();
                for (StudentString student: students) {
                    if (fixedGenes.get(roomId-1).getStudents().contains(student))
                        studentsToRemove.add(student);
                }
                for (StudentString studentToRemove: studentsToRemove) {
                    students.remove(studentToRemove);
                }
                for (StudentString student: students) {
                    Statement stmt = MainController.c.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE Id = " + room.getId() + ";");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int numberOfColumns = rsmd.getColumnCount();
                    rs.next();
                    for (int i = 6; i <= numberOfColumns; i++) {
                        if (rs.getInt(i) == 0) {
                            Statement stmt1 = MainController.c.createStatement();
                            stmt1.executeUpdate("UPDATE Rooms SET \"Student " + (i-5) + "\" = " + student.getId() + " WHERE Id = " + room.getId() + ";");
                            MainController.c.commit();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Platform.runLater(() -> showTreeView());
    }

    @FXML
    void uploadYear1StudentClicked(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Please Confirm...");
        alert.setContentText("The uploaded students will overwrite the existing students.\nAre you sure to proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            openWindow("/fxmls/configurations/UploadYear1Student.fxml", "Year 1 Student Upload");
        }
    }

    @FXML
    void uploadYear2StudentClicked(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Please Confirm...");
        alert.setContentText("The uploaded students will overwrite the existing students.\nAre you sure to proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            openWindow("/fxmls/configurations/UploadYear2Student.fxml", "Year 2 Student Upload");
        }
    }

    private String chooseDirectory() {
        String directory;
        final DirectoryChooser dirChooser = new DirectoryChooser();
        Stage currentStage = (Stage) roomTableView.getScene().getWindow();
        File file = dirChooser.showDialog(currentStage);
        if (file != null) {
            directory = file.getAbsolutePath().replace("\\", "/");
        } else {
            directory = null;
        }
        return directory;
    }

    private boolean exportToExcel(String directory) {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnNum = rsmd.getColumnCount();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(fileName);
            XSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("Building");
            header.createCell(1).setCellValue("Room");
            header.createCell(2).setCellValue("Boy/Girl");
            for (int i = 6; i <= columnNum; i++) {
                header.createCell(i-3).setCellValue("Student " + (i-5));
            }
            List<Room> rooms = new ArrayList<>();
            while (rs.next()) {
                Room room = new Room();
                room.setRoom(rs.getString(2));
                room.setBuilding(rs.getString(3));
                room.setSexRoom(rs.getString(5));
                for (int j = 6; j <= columnNum; j++) {
                    int studentId = rs.getInt(j);
                    if (studentId != 0) {
                        Statement stmt1 = MainController.c.createStatement();
                        ResultSet rs1 = stmt1.executeQuery("SELECT * FROM Students WHERE Id = " + studentId + ";");
                        rs1.next();
                        StudentString student = new StudentString();
                        student.setFamilyName(rs1.getString(3));
                        student.setGivenName(rs1.getString(2));
                        room.getStudents().add(student);
                    }
                }
                rooms.add(room);
            }
            rooms.sort(MainController::roomComparator);
            int i = 1;
            for (Room room: rooms) {
                XSSFRow row = sheet.createRow(i);
                row.createCell(0).setCellValue(room.getBuilding());
                row.createCell(1).setCellValue(room.getRoom());
                row.createCell(2).setCellValue(room.getSexRoom());
                int k = 3;
                for (StudentString student: room.getStudents()) {
                    row.createCell(k).setCellValue(student.getGivenName() + " " + student.getFamilyName());
                    k++;
                }
                i++;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + fileName + ".xlsx");
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (NoClassDefFoundError | IOException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int roomComparator(Room o1, Room o2) {
        String building1 = o1.getBuilding();
        String building2 = o2.getBuilding();
        int result = building1.compareTo(building2);
        if (result != 0) {
            return result;
        } else {
            String room1 = o1.getRoom();
            String room2 = o2.getRoom();
            if (room1.length() < room2.length()) return -1;
            else if (room1.length() > room2.length()) return 1;
            else return room1.compareTo(room2);
        }
    }
}
