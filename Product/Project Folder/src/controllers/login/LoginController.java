package controllers.login;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private JFXTextField usernameInput;

    @FXML
    private JFXPasswordField passwordInput;

    @FXML
    private JFXButton cancelButton;

    @FXML
    private Label wrongPasswordMessage;

    @FXML
    void cancel(ActionEvent event) {
        Stage currentStage = (Stage)cancelButton.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void login(ActionEvent event) throws Exception{
        if (usernameInput.getText().equals("MUWCI") && passwordInput.getText().equals("muwci2018")) {
            Parent mainScreen = FXMLLoader.load(getClass().getResource("/fxmls/main/Main.fxml"));
            Stage currentStage = (Stage)cancelButton.getScene().getWindow();
            currentStage.setScene(new Scene(mainScreen));
            currentStage.setMaximized(true);
            currentStage.setResizable(true);
            currentStage.show();
        } else {
            wrongPasswordMessage.visibleProperty().set(true);
            passwordInput.clear();
        }
    }
}