package controllers.view;

import controllers.main.MainController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class RoomController implements Initializable {
    @FXML
    private AnchorPane pane;

    @FXML
    private TextField totalRoomsTextField;

    @FXML
    private TextField girlRoomsTextField;

    @FXML
    private TextField boyRoomsTextField;

    @FXML
    private TextField girlBedsTextField;

    @FXML
    private TextField boyBedsTextField;


    @FXML
    private TextField totalBedsTextField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int numRows = 0;
        int numBoyRows = 0;
        int numGirlRows = 0;
        int numBoyBeds = 0;
        int numGirlBeds = 0;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            while (rs.next()) {
                numRows++;
                if (rs.getString(5).equals("Boy")) {
                    numBoyRows++;
                    numBoyBeds += rs.getInt(4);
                }
                if (rs.getString(5).equals("Girl")) {
                    numGirlRows++;
                    numGirlBeds += rs.getInt(4);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        totalBedsTextField.setText((numBoyBeds + numGirlBeds) + "");
        totalRoomsTextField.setText(numRows + "");
        boyBedsTextField.setText(numBoyBeds + "");
        boyRoomsTextField.setText(numBoyRows + "");
        girlBedsTextField.setText(numGirlBeds + "");
        girlRoomsTextField.setText(numGirlRows + "");
    }
}
