package whatsapp.web;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class WhatsAppWeb extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent authorization = FXMLLoader.load(getClass().getResource("layouts/Login.fxml"));
        Scene authorizationScene = new Scene(authorization); 
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/logo.png")));
        stage.setResizable(false);
        stage.setScene(authorizationScene);
        stage.setTitle("Welcome to WhatsApp");
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}