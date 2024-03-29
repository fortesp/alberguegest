package application;

import controller.Controller;
import db.DBSession;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import db.DBImport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;


public class Start extends Application {

    public static Map<Class<?>, ?> loaders = new HashMap<>();

    public static Stage personStage;
    public static Stage reportStage;
    public static Stage optionStage;

    public static ResourceBundle labels;

    private final Image windowLogoImage = new Image(getClass().getResourceAsStream("/images/alberguegest_icon.png"));

    protected static final String MANUALFILENAME = "AlbergueGest-Manual.pdf";

    @Override
    public void start(Stage mainStage) {

        try {
            // First run
            if (!new File("db").exists()) {
                System.out.println("Running for the first time? Installing DB...");
                // Import
                DBImport imp = new DBImport("/db/schema.sql");
                imp.fire();

                InputStream is = getClass().getResourceAsStream("/" + MANUALFILENAME);
                File f = new File(MANUALFILENAME);
                if(!f.exists()) Files.copy(is, Paths.get(MANUALFILENAME));
            }
            // --------


            Locale.setDefault(new Locale("es", "PA"));

            labels = ResourceBundle.getBundle("application.locale.LabelsBundle", Locale.getDefault());

            // Main stage
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/fxml/main.fxml"), labels);
            Parent root = (Parent) fxmlLoader.load();
            mainStage.setTitle(labels.getString("mainStageTitle"));
            mainStage.setScene(new Scene(root, 1024, 600));
            mainStage.centerOnScreen();
            mainStage.getIcons().add(windowLogoImage);
            loaders.put(fxmlLoader.getController().getClass(), fxmlLoader.getController());
            mainStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    if(t.getCode()== KeyCode.ESCAPE && Helper.confirmMessage(labels.getString("message.areyousure"), labels.getString("message.areyousure"))) {
                        Stage s = (Stage) mainStage.getScene().getWindow();
                        s.close();
                        System.exit(0);
                    }
                }
            });
            // --

            loadFXMLStage(mainStage, "personStageTitle", "period.fxml", false);
            loadFXMLStage(mainStage, "reportStageTitle", "report.fxml", true);
            loadFXMLStage(mainStage, "optionStageTitle", "option.fxml", false);
            loadFXMLStage(mainStage, "mainStageTitle", "about.fxml", false);

            mainStage.setOnCloseRequest(event -> {
                System.exit(0);
            });

            mainStage.show();

        } catch (Exception e) {

            Helper.showExceptionMessage(e);
        }


        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

    }

    private void loadFXMLStage(Stage parentStage, String bundleTitleKey, String fxml, boolean windowResizable) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/fxml/" + fxml), labels);
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle(labels.getString(bundleTitleKey));
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(windowResizable);
        stage.getIcons().add(windowLogoImage);
        stage.setScene(new Scene(root));

        stage.getScene().getWindow().setOnCloseRequest(event -> {
            ((Controller) fxmlLoader.getController()).onCloseRequest();
        });

        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode()== KeyCode.ESCAPE) {
                    Stage s = (Stage) stage.getScene().getWindow();
                    s.close();
                }
            }
        });

        loaders.put(fxmlLoader.getController().getClass(), fxmlLoader.getController());
    }


    public static void main(String[] args) throws UnknownHostException {  launch(args);  }
}
