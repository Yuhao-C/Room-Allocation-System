package functional;

import controllers.main.MainController;
import javafx.beans.property.SimpleStringProperty;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private int id;
    private SimpleStringProperty room;
    private SimpleStringProperty building;
    private int maxResidents;
    private SimpleStringProperty sexRoom;
    private List<StudentString> students = new ArrayList<>();

    public Room() {

    }

    public Room(Room room) {
        this.id = room.getId();
        this.room = new SimpleStringProperty(room.getRoom());
        this.building = new SimpleStringProperty(room.getBuilding());
        this.maxResidents = room.getMaxResidents();
        this.sexRoom = new SimpleStringProperty(room.getSexRoom());
        this.students = new ArrayList<>(room.getStudents());
    }

    public List<StudentString> getStudents() {
        return students;
    }

    public boolean isEqualTo(Room room) {
        return (this.getRoom().equals(room.getRoom()) && this.getBuilding().equals(room.getBuilding()));
    }

    public String getSexRoom() {
        return sexRoom.get();
    }

    public void setSexRoom(String sexRoom) {
        this.sexRoom = new SimpleStringProperty(sexRoom);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoom() {
        return room.get();
    }

    public void setRoom(String room) {
        this.room = new SimpleStringProperty(room);
    }

    public String getBuilding() {
        return building.get();
    }

    public void setBuilding(String building) {
        this.building = new SimpleStringProperty(building);
    }

    public int getMaxResidents() {
        return maxResidents;
    }

    public void setMaxResidents(int maxResidents) {
        this.maxResidents = maxResidents;
    }

    public boolean isEmpty() {
        boolean isEmpty = true;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms WHERE \"Room No./Name\" = \"" + room.get() + "\" AND \"Building No./Name\" = \"" + building.get() + "\";");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            rs.next();
            for (int i = 6; i <= numberOfColumns; i++) {
                if (rs.getInt(i) != 0) {
                    isEmpty = false;
                    break;
                }
                if (!isEmpty) break;
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEmpty;
    }
}
