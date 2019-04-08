package controllers.configurations;


import controllers.newFile.StudentConfig2Controller;
import controllers.newFile.StudentConfigController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class UploadYear1StudentController {

    private int id = 1;

    @FXML
    Button uploadButton;

    @FXML
    void uploadClicked(ActionEvent event) {
        Stage mainStage = null;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            try {
                StudentConfigController.writeToDB(selectedFile, 1, this.id);
                this.id++;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("The file has been successfully uploaded!");
                alert.showAndWait();
                Stage stage =  (Stage) uploadButton.getScene().getWindow();
                stage.close();
                showNextStage();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("An Error Occurred!");
                alert.setContentText("Please make sure the format of the CSV file and upload again");
            }
        }
    }

    private void showNextStage() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/newFile/StudentConfig2.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Student Configuration");
            StudentConfig2Controller controller = loader.getController();
            stage.setOnShown(event1 -> {
                controller.setDeleteDB(false);
                controller.getCancelButton().setDisable(true);
            });
            stage.setOnCloseRequest(event1 -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning Dialog");
                alert.setHeaderText("Warning Dialog");
                alert.setContentText("You must complete the table before exiting!");
                alert.showAndWait();
                event1.consume();
            });
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
