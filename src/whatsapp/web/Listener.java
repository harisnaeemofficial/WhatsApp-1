package whatsapp.web;

import java.io.BufferedReader;
import java.io.IOException;
import javafx.application.Platform;

public class Listener implements Runnable {  
    
    private final HomePageController homePageController;
    private final BufferedReader reader;
    private String message;

    public Listener(HomePageController homePageController, BufferedReader reader) {
        this.homePageController = homePageController;
        this.reader = reader;
    }

    @Override
    public void run() {
        try { 
            String data, to, buffer;
            while ((data = reader.readLine()) != null){
                if (data.charAt(0) != '@') {
                    message = data.substring(data.indexOf(":") + 2, data.length());
                    to = data.substring(0, data.indexOf(":"));
                } else {
                    message = data.substring(data.indexOf("%") + 2, data.length());
                    to = data.substring(0, data.indexOf("%"));
                }
                buffer = homePageController.getBuffer();
                if (to.equals(buffer)) {
                    Platform.runLater(() -> {
                        homePageController.receiveMessage(message);
                    });
                }
            }
        } catch (IOException ex){ex.getMessage();}
    }
}