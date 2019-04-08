package functional;

import controllers.main.MainController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Locale;

public class Student {

    private int id;
    private SimpleStringProperty givenName;
    private SimpleStringProperty familyName;
    private SimpleIntegerProperty year;
    private SimpleStringProperty sex;
    private ComboBox<String> countryCB;
    private SimpleStringProperty continent;
    private CheckBox allocated = new CheckBox();

    public CheckBox getAllocated() {
        return allocated;
    }

    public Student() {
        Locale.setDefault(Locale.US);
        allocated.setOnAction(event -> {
            if (allocated.isSelected()) allocated.setSelected(false);
            else allocated.setSelected(true);
        });
        ObservableList<String> countries = FXCollections.observableArrayList();
        String[] countryCodes = Locale.getISOCountries();
        for (String countryCode: countryCodes) {
            Locale locale = new Locale("", countryCode);
            countries.add(locale.getDisplayCountry());
        }
        countryCB = new ComboBox<>();
        countryCB.setItems(countries);
        AutoCompleteComboBox.setAutoComplete(countryCB, (typedText, itemToCompare) -> itemToCompare.toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.equals(typedText));
    }


    public Integer getYear() {
        return year.get();
    }

    public void setYear(Integer year) {
        this.year = new SimpleIntegerProperty(year);
    }

    public String getSex() {
        return sex.get();
    }

    public void setCountryValue(String countryValue) {
        countryCB.setValue(countryValue);
    }

    public void setSex(String sex) {
        this.sex = new SimpleStringProperty(sex);
    }

    public ComboBox<String> getCountryCB() {
        return countryCB;
    }

    public void setCountryCB(ComboBox<String> countryCB) {
        this.countryCB = countryCB;
    }

    public String getContinent() {
        return continent.get();
    }

    public void setContinent(String continent) {
        this.continent = new SimpleStringProperty(continent);
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

    public static boolean isAllocated(int id) {
        boolean isAllocated = false;
        try {

            Statement stmt = MainController.c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 6; i <= numberOfColumns; i++) {
                    if (rs.getInt(i) == id) {
                        isAllocated = true;
                        break;
                    }
                }
                if (isAllocated) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAllocated;
    }
}
