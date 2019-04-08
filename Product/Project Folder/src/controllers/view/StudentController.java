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

public class StudentController implements Initializable {

    @FXML
    private AnchorPane pane;

    @FXML
    private TextField totalNumTextField;

    @FXML
    private TextField girlNumTextField;

    @FXML
    private TextField boyNumTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int totalNum = 0;
        int boyNum = 0;
        int girlNum = 0;
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students;");
            while (rs.next()) {
                totalNum++;
                if (rs.getString(4).equals("male")) boyNum++;
                if (rs.getString(4).equals("female")) girlNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        totalNumTextField.setText(totalNum + "");
        boyNumTextField.setText(boyNum + "");
        girlNumTextField.setText(girlNum + "");
    }
}
