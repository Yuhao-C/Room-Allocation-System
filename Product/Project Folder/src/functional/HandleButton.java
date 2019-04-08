package functional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HandleButton {

    public void handleCancelButton(Button cancelButton) {
        Stage currentStage = (Stage) cancelButton.getScene().getWindow();
        currentStage.close();
    }

    public void handleNextButton(Button button, String fxml) throws IOException {
        Parent layout = FXMLLoader.load(getClass().getResource(fxml));
        Stage currentStage = (Stage) button.getScene().getWindow();
        currentStage.setScene(new Scene(layout));
        currentStage.setResizable(false);
        currentStage.centerOnScreen();
        currentStage.show();
    }

    public void handlePreviousButton(Button previousButton, String fxml) throws IOException{
        handleNextButton(previousButton,fxml);
    }
}
