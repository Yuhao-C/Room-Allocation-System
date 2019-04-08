package controllers.newFile;


import com.sun.tools.javac.Main;
import functional.HandleButton;
import controllers.main.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;

public class DirectoryController {

    static String oldFileName;
    static String oldDirectory;

    @FXML
    private TextField scheduleNameInput;

    @FXML
    private TextField scheduleDirectoryInput;

    @FXML
    private Button selectDirectoryButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label warningLabel;

    @FXML AnchorPane ap;

    @FXML
    void cancelClick(ActionEvent event) throws IOException {
        HandleButton button = new HandleButton();
        button.handleCancelButton(cancelButton);
    }

    @FXML
    void nextClick(ActionEvent event) throws IOException {
        if (scheduleNameInput.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Enter a file name to create a new room allocation");
            alert.showAndWait();
        } else if (scheduleDirectoryInput.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Choose a directory to create a new room allocation");
            alert.showAndWait();
        } else {
            oldFileName = MainController.fileName;
            oldDirectory = MainController.directory;
            MainController.fileName = scheduleNameInput.getText();
            MainController.directory = scheduleDirectoryInput.getText().replace("\\", "/");
            File file = new File(MainController.directory + "/" + MainController.fileName +".sqlite");
            if (file.exists()) {
                MainController.fileName = oldFileName;
                MainController.directory = oldDirectory;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error has Occurred!");
                alert.setContentText("File Already Exists!\n" +
                        "Open the file by clicking File -> Open in the main page.");
                alert.showAndWait();
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
            } else {
                MainController.connectToDB();
                Parent layout = FXMLLoader.load(getClass().getResource("/fxmls/newFile/RoomConfig.fxml"));
                Stage stage = (Stage) nextButton.getScene().getWindow();
                stage.setScene(new Scene(layout));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.setOnCloseRequest(e -> deleteDB(DirectoryController.oldFileName, DirectoryController.oldDirectory));
                stage.show();
            }
        }
    }

    public static void deleteDB(String oldFileName, String oldDirectory) {
        try {
            MainController.c.close();
            File file = new File(MainController.directory + "/" + MainController.fileName +".sqlite");
            file.delete();
            MainController.fileName = oldFileName;
            MainController.directory = oldDirectory;
            MainController.connectToDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    void selectDirectoryClick(ActionEvent event) {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        Stage currentStage = (Stage) nextButton.getScene().getWindow();
        File file = dirChooser.showDialog(currentStage);
        if (file != null) {
            scheduleDirectoryInput.setText(file.getAbsolutePath());
        }
    }
}


