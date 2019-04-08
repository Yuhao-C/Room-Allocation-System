package controllers.newFile;

import functional.HandleButton;
import controllers.main.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Statement;

public class StudentConfigController {

    private int id = 1;

    private boolean isUpload1Clicked = false;
    private boolean isUpload2Clicked = false;

    @FXML
    private Button previousButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button nextButton;

    @FXML
    void cancelClick(ActionEvent event) throws IOException {
        DirectoryController.deleteDB(DirectoryController.oldFileName, DirectoryController.oldDirectory);
        HandleButton button = new HandleButton();
        button.handleCancelButton(cancelButton);
    }

    @FXML
    void previousClick(ActionEvent event) throws IOException {
        HandleButton button = new HandleButton();
        button.handlePreviousButton(previousButton, "/fxmls/newFile/RoomConfig.fxml");
    }

    @FXML
    void nextClick(ActionEvent event) throws IOException {
        HandleButton button = new HandleButton();
        button.handleNextButton(nextButton, "/fxmls/newFile/StudentConfig2.fxml");
    }

    @FXML
    void year1UploadButtonClick(ActionEvent event) {
       upload(1);
    }

    @FXML
    void year2UploadButtonClick(ActionEvent event) {
        upload(2);
    }

    public void upload(int year) {
        Stage mainStage = null;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            try {
                writeToDB(selectedFile, year, this.id);
                this.id++;
                if (year == 1) isUpload1Clicked = true;
                if (year == 2) isUpload2Clicked = true;
                if (isUpload1Clicked && isUpload2Clicked) {
                    nextButton.setDisable(false);
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("The file has been successfully uploaded!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error Occurred!");
                alert.setContentText("Please make sure the format of the CSV file and upload again");
            }
        }
    }

    public static void writeToDB(File selectedFile, int year, int id) {
        try {
            Statement stmt = MainController.c.createStatement();
            if (id == 1) {
                String sql2 = "CREATE TABLE IF NOT EXISTS Students" +
                        "(Id INTEGER PRIMARY KEY   AUTOINCREMENT," +
                        " GivenName      TEXT      NOT NULL, " +
                        " FamilyName     TEXT      NOT NULL," +
                        " Sex            TEXT," +
                        " Country        TEXT," +
                        " Continent      TEXT," +
                        " Year           INTEGER   NOT NULL);";
                stmt.executeUpdate(sql2);
                MainController.c.commit();
                stmt.executeUpdate("DELETE FROM Students WHERE \"Year\" = " + year + ";");
                MainController.c.commit();
            }
            BufferedReader br = new BufferedReader(new FileReader(selectedFile));
            while (br.ready()) {
                String[] record = br.readLine().split(",");
                if (record[2].equals("m")) record[2] = "male";
                if (record[2].equals("f")) record[2] = "female";
                String sql3 = "INSERT INTO Students (GivenName, FamilyName, 'Year', 'Sex') VALUES" +
                        "('" + record[0] + "','" + record[1] + "'," + year + ", '" + record[2] + "');";
                stmt.executeUpdate(sql3);
                MainController.c.commit();
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
