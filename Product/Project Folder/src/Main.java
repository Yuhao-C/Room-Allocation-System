import controllers.main.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.Locale;

import static controllers.main.MainController.connectToDB;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale.setDefault(Locale.US);
        readDirectoryFile();
        connectToDB();
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Login.fxml"));
        primaryStage.setTitle("Room Allocation System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> MainController.writeDirectoryFile());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void readDirectoryFile() {
        try {
            String directory = (MainController.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath().replace("\\", "/");
            directory = directory.substring(0, directory.lastIndexOf("/") + 1)  + "/Directory.txt";
            File file = new File(directory);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                MainController.fileName = br.readLine();
                MainController.directory = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
