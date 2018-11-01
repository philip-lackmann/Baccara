/*  
 *  © Copyright Philip Lackmann
 *
 *  [Project Title]     Baccara
 *  [Description]       The standalone version of Baccara which I originally made for a bigger 
                        group project consisting of a whole casino with 5 games.
 *  [Authors]           Philip Lackmann
 *  [Version]           Version 1.0      
 */

package ch.bbbaden.baccara;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        
        // removes the top bar of the application window. (undecorated stage)
        stage.initStyle(StageStyle.TRANSPARENT);
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/BaccaraMainView.fxml"));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        stage.setTitle("Baccara");
        stage.setScene(scene);
        stage.show();  
    }

    public static Stage getStage() {
        return stage;
    }
}