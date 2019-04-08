package controllers.configurations;

import functional.Room;
import controllers.main.MainController;
import controllers.newFile.RoomConfigController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateRoomInfoController implements Initializable {

    private ObservableList<Room> roomsObservableList = FXCollections.observableArrayList();

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
    public void changeRoomSexEvent(TableColumn.CellEditEvent editedCell) {
        Room roomSelected = roomTableView.getSelectionModel().getSelectedItem();
        if (!editedCell.getNewValue().toString().equals(editedCell.getOldValue().toString())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Confirmation Dialog");
            alert.setContentText("Changing the room to " + editedCell.getNewValue().toString().toLowerCase() + "'s room will cause the original students allocated in this room deleted!\n" +
                    "Are you sure to change it?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    Statement stmt = MainController.c.createStatement();
                    String room = roomSelected.getRoom();
                    String building = roomSelected.getBuilding();
                    for (int i = 1; i < 1 + roomSelected.getMaxResidents(); i++) {
                        stmt.executeUpdate("UPDATE Rooms SET \"Student " + i + "\" = NULL WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                        MainController.c.commit();
                    }
                    String sexRoom = "";
                    if (roomSelected.getSexRoom().equals("Boy")) sexRoom = "Girl";
                    if (roomSelected.getSexRoom().equals("Girl")) sexRoom = "Boy";
                    stmt.executeUpdate("UPDATE Rooms SET \"Boy/Girl\" = \"" + sexRoom + "\" WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    MainController.c.commit();
                    stmt.close();

                    roomSelected.setSexRoom(editedCell.getNewValue().toString());
                    roomTableView.refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                roomSelected.setSexRoom(editedCell.getOldValue().toString());
                roomTableView.refresh();
            }
        }
    }


    @FXML
    public void changeMaxCapacityEvent(TableColumn.CellEditEvent editedCell) {
        Room roomSelected = roomTableView.getSelectionModel().getSelectedItem();
        int studentCount = 0;
        int oldMaxCapacity = 0;
        try {
            Statement stmt = MainController.c.createStatement();
            String room = roomSelected.getRoom();
            String building = roomSelected.getBuilding();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
            rs.next();
            for (int i = 6; i < 6 + roomSelected.getMaxResidents(); i++) {
                int studentId = rs.getInt(i);
                if (studentId != 0 ) {
                    studentCount++;
                }
            }
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs1.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            oldMaxCapacity = numberOfColumns - 5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            int changedCapacity = Integer.parseInt(editedCell.getNewValue().toString());
            if (studentCount > changedCapacity) {
                roomSelected.setMaxResidents(Integer.parseInt(editedCell.getOldValue().toString()));
                roomTableView.refresh();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error has Occurred!");
                alert.setContentText(studentCount + " students have been allocated in this room!\n" +
                        "Delete " + (studentCount - changedCapacity) + " student(s) to reduce the maximum residents to " + changedCapacity + "!");
                alert.showAndWait();
            } else {
                try {
                    Statement stmt = MainController.c.createStatement();
                    String room = roomSelected.getRoom();
                    String building = roomSelected.getBuilding();
                    stmt.executeUpdate("UPDATE Rooms SET 'Max Residents' = " + changedCapacity + " WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    MainController.c.commit();
                    if (changedCapacity > oldMaxCapacity) {
                        for (int i = oldMaxCapacity; i < changedCapacity; i++) {
                            stmt.executeUpdate("ALTER TABLE Rooms ADD COLUMN 'Student " + (i+1) + "' INTEGER;");
                            MainController.c.commit();
                        }
                    }
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                roomSelected.setMaxResidents(changedCapacity);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("An Error has Occurred!");
            alert.setContentText("Please Enter an INTEGER for Maximum Residents!");
            alert.showAndWait();
        }
    }

    @FXML
    void changeRoomCellEvent(TableColumn.CellEditEvent editedCell) {
        Room roomSelected = roomTableView.getSelectionModel().getSelectedItem();
        String newRoomName = editedCell.getNewValue().toString();
        String oldRoomName = editedCell.getOldValue().toString();
        if (!newRoomName.equals(oldRoomName)) {
            roomSelected.setRoom(newRoomName);
            roomTableView.refresh();
            if (AddOrDeleteRoomController.contains(roomsObservableList, roomSelected)) {
                roomSelected.setRoom(oldRoomName);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error has Occurred!");
                alert.setContentText("This room already exists in the scheme!");
                alert.showAndWait();
            } else {
                try {


                    Statement stmt = MainController.c.createStatement();

                    String building = roomSelected.getBuilding();
                    stmt.executeUpdate("UPDATE Rooms SET \"Room No./Name\" = \"" + newRoomName + "\" WHERE \"Room No./Name\" = \"" + oldRoomName + "\" AND \"Building No./Name\" = \"" + building + "\";");
                    MainController.c.commit();
                    stmt.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @FXML
    void changeBuildingCellEvent(TableColumn.CellEditEvent editedCell) {
        Room roomSelected = roomTableView.getSelectionModel().getSelectedItem();
        String newBuildingName = editedCell.getNewValue().toString();
        String oldBuildingName = editedCell.getOldValue().toString();
        if (!newBuildingName.equals(oldBuildingName)) {
            roomSelected.setBuilding(newBuildingName);
            if (AddOrDeleteRoomController.contains(roomsObservableList, roomSelected)) {
                roomSelected.setBuilding(oldBuildingName);
                roomTableView.refresh();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error has Occurred!");
                alert.setContentText("This room already exists in the scheme!");
                alert.showAndWait();
            } else {
                try {
                    Statement stmt = MainController.c.createStatement();
                    String room = roomSelected.getRoom();
                    stmt.executeUpdate("UPDATE Rooms SET \"Building No./Name\" = \"" + newBuildingName + "\" WHERE \"Room No./Name\" = \"" + room + "\" AND \"Building No./Name\" = \"" + oldBuildingName + "\";");
                    MainController.c.commit();
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @FXML
    void finishClick(ActionEvent event) {
        Stage currentStage = (Stage) roomTableView.getScene().getWindow();
        currentStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildingColumn.setSortable(true);
        sexRoomColumn.setSortable(true);
        roomTableView.setEditable(true);
        roomColumn.setEditable(true);
        buildingColumn.setEditable(true);
        maxResidentsColumn.setEditable(true);
        sexRoomColumn.setEditable(true);
        idColumn.setEditable(false);
        roomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        buildingColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        maxResidentsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        sexRoomColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Girl", "Boy"));
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

}