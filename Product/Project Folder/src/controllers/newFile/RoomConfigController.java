package controllers.newFile;

import functional.HandleButton;
import functional.Room;
import controllers.configurations.AddOrDeleteRoomController;
import controllers.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;


public class RoomConfigController implements Initializable {


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
    private ComboBox<String> sexComboBox;

    @FXML
    private Button nextButton;

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
        addButtonClicked(roomTextField, buildingTextField, maxResidentsTextField, sexComboBox, roomsObservableList, roomTableView);
    }

    @FXML
    void deleteClick(ActionEvent event) {
        deleteButtonClicked(roomTableView, roomsObservableList);
    }

    @FXML
    void cancelClick(ActionEvent event) throws IOException {
        DirectoryController.deleteDB(DirectoryController.oldFileName, DirectoryController.oldDirectory);
        HandleButton button = new HandleButton();
        button.handleCancelButton(cancelButton);
    }

    @FXML
    void nextClick(ActionEvent event) throws IOException {
        writeToDB();
        createStudentColumns();
        HandleButton button = new HandleButton();
        button.handleNextButton(nextButton,"/fxmls/newFile/StudentConfig.fxml");
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
        roomsObservableList = populateTableView();
        roomTableView.setItems(roomsObservableList);
    }

    public static ObservableList<Room> populateTableView() {
        ObservableList<Room> roomsObservableList = FXCollections.observableArrayList();
        try {


            Statement stmt = MainController.c.createStatement();

            String sql =  "CREATE TABLE IF NOT EXISTS Rooms " +
                    "(Id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "'Room No./Name'     TEXT                NOT NULL, " +
                    "'Building No./Name' TEXT                NOT NULL, " +
                    "'Max Residents'     INT                 NOT NULL," +
                    "'Boy/Girl'          TEXT                NOT NULL)";
            stmt.executeUpdate(sql);
            MainController.c.commit();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms");
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("Id"));
                room.setRoom(rs.getString("Room No./Name"));
                room.setBuilding(rs.getString("Building No./Name"));
                room.setMaxResidents(rs.getInt("Max Residents"));
                room.setSexRoom(rs.getString("Boy/Girl"));
                roomsObservableList.add(room);

            }
            stmt.close();

            return roomsObservableList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeToDB() {
        try {


            Statement stmt = MainController.c.createStatement();

            String sql1 = "DROP TABLE IF EXISTS Rooms";
            stmt.executeUpdate(sql1);
            MainController.c.commit();
            String sql =  "CREATE TABLE Rooms " +
                    "(Id             INTEGER   PRIMARY KEY   AUTOINCREMENT," +
                    "'Room No./Name'     TEXT                NOT NULL, " +
                    "'Building No./Name' TEXT                NOT NULL, " +
                    "'Max Residents'     INT                 NOT NULL," +
                    "'Boy/Girl'          TEXT                NOT NULL)";
            stmt.executeUpdate(sql);
            MainController.c.commit();
            for (Room room: roomsObservableList) {
                String sql2 = "INSERT INTO Rooms ('Room No./Name','Building No./Name','Max Residents', 'Boy/Girl') " +
                        "VALUES ('" + room.getRoom() + "'" +  "," + "'" + room.getBuilding() + "'" + "," + room.getMaxResidents() +
                        ",'" + room.getSexRoom() + "');";
                stmt.executeUpdate(sql2);
            }
            stmt.close();
            MainController.c.commit();

        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    private void createStudentColumns() {
        try {


            Statement stmt = MainController.c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT \"Max Residents\" FROM Rooms;");
            int maxRoomCapacity = 0 ;
            while (rs.next()) {
                int current = rs.getInt(1);
                if (current > maxRoomCapacity)
                    maxRoomCapacity = current;
            }
            rs.close();
            for (int i = 0; i < maxRoomCapacity; i++) {
                stmt.executeUpdate("ALTER TABLE Rooms ADD COLUMN 'Student " + (i+1) + "' INTEGER;");
                MainController.c.commit();
            }
            stmt.close();
            MainController.c.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Room addButtonClicked(TextField roomTextField, TextField buildingTextField, TextField maxResidentsTextField, ComboBox<String> sexComboBox, ObservableList<Room> roomsObservableList, TableView<Room> roomTableView) {
        Room addRoom = new Room();
        if (roomTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("There is an Error!");
            alert.setContentText("Please Enter the Room No./Name!");
            alert.showAndWait();
            roomTextField.requestFocus();
            return null;
        }
        if (buildingTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("There is an Error!");
            alert.setContentText("Please Enter the Building No./Name!");
            alert.showAndWait();
            buildingTextField.requestFocus();
            return null;
        }
        addRoom.setRoom(roomTextField.getText());
        addRoom.setBuilding(buildingTextField.getText());
        if (!AddOrDeleteRoomController.contains(roomsObservableList, addRoom)) {
            try {
                int maxResidents = Integer.parseInt(maxResidentsTextField.getText());
                addRoom.setMaxResidents(maxResidents);
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("There is an Error!");
                alert.setContentText("Please Enter an INTEGER for Max Residents");
                alert.showAndWait();
                maxResidentsTextField.requestFocus();
                return null;
            }
            try {
                sexComboBox.getValue().isEmpty();
                addRoom.setSexRoom(sexComboBox.getValue());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("There is an Error!");
                alert.setContentText("Please choose boy/girl's dormitory");
                alert.showAndWait();
                sexComboBox.requestFocus();
                return null;
            }
            addRoom.setId(roomsObservableList.size()+1);
            roomsObservableList.add(addRoom);
            roomTableView.setItems(roomsObservableList);
            roomTextField.clear();
            roomTextField.requestFocus();
            return addRoom;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("An Error has Occurred!");
            alert.setContentText("This room is already added in the scheme!");
            alert.showAndWait();
            return null;
        }
    }

    public static ObservableList<Room> deleteButtonClicked(TableView<Room> roomTableView, ObservableList<Room> roomsObservableList) {
        if (roomTableView.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please Choose a Row to Delete!");
            alert.showAndWait();
        } else {
            int selectedIndex = roomTableView.getSelectionModel().getSelectedIndex();
            for (int i = selectedIndex + 1; i < roomsObservableList.size(); i++) {
                roomsObservableList.get(i).setId(i);
            }
            roomsObservableList.remove(selectedIndex);
            roomTableView.setItems(roomsObservableList);
        }
        return roomsObservableList;
    }
}
