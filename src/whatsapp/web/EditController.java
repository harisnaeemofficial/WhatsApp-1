package whatsapp.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class EditController {  
    
    @FXML private Label label;
    @FXML private Label fileName;
    @FXML private Circle circle;
    @FXML private TextField curPass;
    @FXML private TextField newPass;
    @FXML private TextField confirmPass;
    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    
    private Database database;
    private String email, name;
    private Stage stage;
    private ImagePattern photo;
    private File file;
    private HomePageController hpc;
    
    protected void setInfo(String name, String email, Stage stage, Database database, HomePageController hpc) {
        this.name = name;
        this.email = email;
        this.stage = stage;
        this.database = database;
        this.hpc = hpc;
        tfName.setPromptText(name);
        tfEmail.setPromptText(email);
        photo = new ImagePattern(database.getImage(email));
        circle.setFill(photo);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF0000;");
    }
    
    @FXML
    private void chooseFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
            file = fileChooser.showOpenDialog(stage);
            if (file != null) { //TODO
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                photo = new ImagePattern(image);
                circle.setFill(photo);
                fileName.setText(file.getName());
            }
        } catch (IOException ex) {label.setText(ex.getMessage());}
    }
    
    @FXML
    private void submit() {
        String nName = tfName.getText();
        String nCurPass = curPass.getText();
        String nNewPass = newPass.getText();
        String nConfirmPass = confirmPass.getText();
        boolean access = true;
        for (int i = 0; i < nName.length(); i++) {
            if (nName.charAt(i) == ':' ||nName.charAt(i) == '%'|| nName.charAt(i) == '@') {
                access = false;
                break;
            }
        }
        if (access) {
            if (file != null) {
                database.updateImage(email, file);
            }
            if (!nName.isEmpty()) {
                database.updateName(nName, email);
            } else {nName = name;}
            if (!nCurPass.isEmpty() || !nNewPass.isEmpty() || !nConfirmPass.isEmpty()) {
                if (nCurPass.isEmpty() || nNewPass.isEmpty() || nConfirmPass.isEmpty()) {
                    label.setText("Not all fields are filled");
                } else {
                    if (!nNewPass.equals(nConfirmPass)) {
                       label.setText("Passwords do not match"); 
                       newPass.clear();
                       confirmPass.clear();
                    } else {
                        if (!database.updatePassword(email, nCurPass, nNewPass)) {
                            label.setText("Invalid current password !!!"); 
                            curPass.clear();
                            newPass.clear();
                            confirmPass.clear();
                        } else {
                            hpc.update();
                            hpc.updateContactInfo(nName, email, "ME");
                            cancel();
                        }
                    }
                }
            } else {
                hpc.update();
                hpc.updateContactInfo(nName, email, "ME");
                cancel(); 
            }
        } else {
            label.setText("Name contains '@', ':', '%'");
        }
    }
    
    @FXML
    private void cancel() {
       stage.close();
    }
    
    @FXML 
    private void tryUpdateEmail() {
        label.setText("Email can not be changed");
    }
      
}