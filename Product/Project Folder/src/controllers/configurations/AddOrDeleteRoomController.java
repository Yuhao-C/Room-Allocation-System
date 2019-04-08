package controllers.configurations;

import functional.HandleButton;
import functional.Room;
import controllers.main.MainController;
import controllers.newFile.RoomConfigController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddOrDeleteRoomController implements Initializable {

    private ObservableList<Room> roomsObservableList = FXCollections.observableArrayList();

    private List<Room> addedRooms = new ArrayList<>();
    private List<Room> deletedRooms = new ArrayList<>();

    @FXML
    private TableView<Room> roomTableView;

    @FXML
    private TableColumn<Room, Integer> idColumn;

    @FXML
    private TableColumn<Room, String> roomColumn;

    @FXML
    private TableColumn<Room, String> buildingColumn;

    @FXML
    private TableColumn<Room, Integer> maxResidentsColumn;

    @FXML
    private TableColumn<Room, String> sexRoomColumn;

    @FXML
    private ComboBox<String> sexComboBox;

    @FXML
    private Button previousButton;

    @FXML
    private Button finishButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField roomTextField;

    @FXML
    private TextField buildingTextField;

    @FXML
    private TextField maxResidentsTextField;

    @FXML
    void addClick(ActionEvent event) {
        Room addedRoom = RoomConfigController.addButtonClicked(roomTextField, buildingTextField, maxResidentsTextField, sexComboBox, roomsObservableList, roomTableView);
        if (addedRoom != null) addedRooms.add(addedRoom);
        roomTextField.clear();
        buildingTextField.clear();
        maxResidentsTextField.clear();
        sexComboBox.setValue("");
    }

    @FXML
    void cancelClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Confirmation Dialog");
        alert.setContentText("If you cancel, all the changes will be lost.\nAre you sure to cancel?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            HandleButton button = new HandleButton();
            button.handleCancelButton(cancelButton);
        }
    }

    @FXML
    void deleteClick(ActionEvent event) {
        if (roomTableView.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please Choose a Row to Delete!");
            alert.showAndWait();
        } else {
            Room roomToDelete = roomTableView.getSelectionModel().getSelectedItem();
            if (addedRooms.contains(roomToDelete)) {
                addedRooms.remove(roomToDelete);
                roomsObservableList.remove(roomToDelete);
                roomTableView.refresh();
            } else {
                String room = roomToDelete.getRoom();
                String building = roomToDelete.getBuilding();
                try {
                    Statement stmt = MainController.c.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    rs.next();
                    int studentCount = 0;
                    for (int i = 6; i < 6 + roomToDelete.getMaxResidents(); i++) {
                        int studentId = rs.getInt(i);
                        if (studentId != 0 ) {
                            studentCount++;
                        }
                    }
                    stmt.close();
                    if (studentCount != 0) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Confirmation Dialog");
                        alert.setContentText("There is/are " + studentCount + " student(s) allocated in this room.\n" +
                                "If you delete, the students will be unallocated.\n" +
                                "Are you sure to delete the room?");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            roomsObservableList = RoomConfigController.deleteButtonClicked(roomTableView, roomsObservableList);
                            deletedRooms.add(roomToDelete);
                        }
                    } else {
                        roomsObservableList = RoomConfigController.deleteButtonClicked(roomTableView, roomsObservableList);
                        deletedRooms.add(roomToDelete);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void finishClick(ActionEvent event) {
        try {
            if (deletedRooms.size() != 0 || addedRooms.size() != 0) {


                Statement stmt = MainController.c.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
                ResultSetMetaData rsmd = rs.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                rs.close();
                for (Room roomToDelete: deletedRooms) {
                    stmt.executeUpdate("DELETE FROM Rooms WHERE \"Room No./Name\" = \"" + roomToDelete.getRoom() + "\" AND \"Building No./Name\" = \"" + roomToDelete.getBuilding() + "\";");
                    MainController.c.commit();
                }
                for (Room roomToAdd: addedRooms) {
                    stmt.executeUpdate("INSERT INTO Rooms ('Room No./Name', 'Building No./Name', 'Max Residents', 'Boy/girl') VALUES (\"" + roomToAdd.getRoom() + "\", \"" + roomToAdd.getBuilding() + "\", " + roomToAdd.getMaxResidents() + ", \"" + roomToAdd.getSexRoom() + "\");");
                    MainController.c.commit();
                }
                int maxRoomCapacity = 0 ;
                for (Room room: roomsObservableList) {
                    if (room.getMaxResidents() > maxRoomCapacity) {
                        maxRoomCapacity = room.getMaxResidents();
                    }
                }
                if (maxRoomCapacity > (numberOfColumns - 5)) {
                    for (int i = (numberOfColumns - 5); i < maxRoomCapacity; i++) {
                        stmt.executeUpdate("ALTER TABLE Rooms ADD COLUMN 'Student " + (i + 1) + "' INTEGER;");
                        MainController.c.commit();
                    }
                }
                stmt.close();

            }
            Stage currentStage = (Stage) roomTableView.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setSortable(false);
        roomColumn.setSortable(false);
        buildingColumn.setSortable(false);
        maxResidentsColumn.setSortable(false);
        sexRoomColumn.setSortable(false);
        sexComboBox.getItems().addAll("Boy", "Girl");
        idColumn.setStyle("-fx-alignment: CENTER;");
        roomColumn.setStyle("-fx-alignment: CENTER;");
        buildingColumn.setStyle("-fx-alignment: CENTER;");
        maxResidentsColumn.setStyle("-fx-alignment: CENTER;");
        sexRoomColumn.setStyle("-fx-alignment: CENTER;");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
        buildingColumn.setCellValueFactory(new PropertyValueFactory<>("building"));
        maxResidentsColumn.setCellValueFactory(new PropertyValueFactory<>("maxResidents"));
        sexRoomColumn.setCellValueFactory(new PropertyValueFactory<>("sexRoom"));
        roomsObservableList = RoomConfigController.populateTableView();
        roomsObservableList.sort(MainController::roomComparator);
        roomTableView.setItems(roomsObservableList);
    }

    public static boolean contains(List<Room> rooms, Room room) {
        if (room.getId() == 0) {
            boolean flag = false;
            for (Room room1: rooms) {
                if (room.isEqualTo(room1)) {
                    flag = true;
                    break;
                }
            }
            return flag;
        } else {
            int occurrences = 0;
            for (Room room1: rooms) {
                if (room.isEqualTo(room1)) {
                    occurrences++;
                }
            }
            return occurrences > 1;
        }
    }
}
