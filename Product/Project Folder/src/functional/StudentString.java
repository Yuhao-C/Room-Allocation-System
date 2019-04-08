package functional;

import controllers.main.MainController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class StudentString {
    private int id;
    private SimpleStringProperty givenName;
    private SimpleStringProperty familyName;
    private SimpleIntegerProperty year;
    private SimpleStringProperty sex;
    private SimpleStringProperty country;
    private SimpleStringProperty continent;
    private CheckBox allocated = new CheckBox();

    public StudentString() {
    }

    public StudentString(StudentString student) {
        this.id = student.getId();
        this.givenName = new SimpleStringProperty(student.getGivenName());
        this.familyName = new SimpleStringProperty(student.getFamilyName());
        this.year = new SimpleIntegerProperty(student.getYear());
        this.sex = new SimpleStringProperty(student.getSex());
        this.country = new SimpleStringProperty(student.getCountry());
        this.continent = new SimpleStringProperty(student.getContinent());
        this.allocated = new CheckBox();
    }

    public CheckBox getAllocated() {
        return allocated;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGivenName() {
        return givenName.get();
    }

    public void setGivenName(String givenName) {
        this.givenName = new SimpleStringProperty(givenName);
    }

    public String getFamilyName() {
        return familyName.get();
    }

    public void setFamilyName(String familyName) {
        this.familyName = new SimpleStringProperty(familyName);
    }

    public void setYear(int year) {
        this.year = new SimpleIntegerProperty(year);
    }

    public int getYear() {
        return year.get();
    }

    public String getSex() {
        return sex.get();
    }

    public void setSex(String sex) {
        this.sex = new SimpleStringProperty(sex);
    }

    public String getCountry() {
        return country.get();
    }

    public void setCountry(String country) {
        this.country = new SimpleStringProperty(country);
    }

    public String getContinent() {
        return continent.get();
    }

    public void setContinent(String continent) {
        this.continent = new SimpleStringProperty(continent);
    }

    public String getRoom() {
        String room = null;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 6; i <= numberOfColumns; i++) {
                    if (rs.getInt(i) == this.id) {
                        room = rs.getString(2);
                    }
                }
            }
            stmt.close();
            return room;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBuilding() {
        String building = null;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 6; i <= numberOfColumns; i++) {
                    if (rs.getInt(i) == this.id) {
                        building = rs.getString(3);
                    }
                }
            }
            stmt.close();

            return building;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAllocated(int id) {
        return Student.isAllocated(id);
    }
}
