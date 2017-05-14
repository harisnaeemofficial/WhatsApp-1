package whatsapp.web;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LoginController implements Initializable{
    
    @FXML private TextField tfLogInEmail;
    @FXML private PasswordField tfLogInPass;
    @FXML private TextField tfSignUpName;
    @FXML private TextField tfSignUpEmail;
    @FXML private PasswordField tfSignUpPass;
    @FXML private PasswordField tfSignUpPassConf;
    @FXML private Label labelGoToSignUpPage;
    @FXML private Label labelGoToLogInPage;
    @FXML private Label labelInform;
    
    private Parent parent;
    private HomePageController homePageController;
    private final FXMLLoader fxmlLoader = new FXMLLoader();
    private final Database database = new Database(this, homePageController);
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fxmlLoader.setLocation(getClass().getResource("layouts/HomePage.fxml"));
            parent = fxmlLoader.load();
            homePageController = fxmlLoader.getController();
        } catch (IOException ex) {notify(ex.getMessage());}
    }

    @FXML
    private void logIn() {
        String email = tfLogInEmail.getText();
        String pass = tfLogInPass.getText();
        if (isValid(email, pass)) {
            if (database.logIn(email, pass)) {
                homePageController.setEmail(email);
                homePageController.setDatabase(database);
                getAccess();
            }
        }
    }
    
    @FXML
    private void signUp() {
        String name = tfSignUpName.getText();
        String email = tfSignUpEmail.getText();
        String pass = tfSignUpPass.getText();
        String passConf = tfSignUpPassConf.getText();
        if (isValid(name, email, pass, passConf)) {
            if (database.signUp(name, email, pass)) {
                homePageController.setEmail(email);
                homePageController.setDatabase(database);
                getAccess();
            }
        }
    }
    
    @FXML
    private void goToLogInPage() {
        tfSignUpName.setVisible(false);
        tfSignUpEmail.setVisible(false);
        tfSignUpPass.setVisible(false);
        tfSignUpPassConf.setVisible(false);
        labelGoToLogInPage.setVisible(false);
        
        tfSignUpName.clear();
        tfSignUpEmail.clear();
        tfSignUpPass.clear();
        tfSignUpPassConf.clear();       
        labelInform.setText(""); 
        
        tfLogInEmail.setVisible(true);
        tfLogInPass.setVisible(true);
        labelGoToSignUpPage.setVisible(true);
    }
    
    @FXML
    private void goToSignUpPage() {
        tfLogInEmail.setVisible(false);
        tfLogInPass.setVisible(false);
        
        tfLogInEmail.clear();
        tfLogInPass.clear();
        labelGoToSignUpPage.setVisible(false);
        labelInform.setText("");
        
        tfSignUpName.setVisible(true);
        tfSignUpEmail.setVisible(true);
        tfSignUpPass.setVisible(true);
        tfSignUpPassConf.setVisible(true);
        labelGoToLogInPage.setVisible(true);
    }
    
    private boolean isValid(String name, String email, String pass, String passConf) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || passConf.isEmpty()) {
            notify("Not all fields are filled");
            return false;
        } else {
            if (!pass.equals(passConf)) {
                notify("Passwords do not match !!!");
                tfSignUpPass.clear();
                tfSignUpPassConf.clear();
                return false;
            } else {
                boolean access = true;
                for (int i = 0; i < name.length(); i++) {
                    if (name.charAt(i) == ':' ||name.charAt(i) == '%'|| name.charAt(i) == '@') {
                        access = false;
                        break;
                    }
                }
                if (access) {
                    boolean isEmail = true;
                    for (int i = 0; i < email.length(); i++) {
                        //TODO
                        if (email.charAt(0) == '@') {
                            isEmail = false;
                            break;
                        }
                    }
                    if (isEmail) {
                        tfSignUpName.clear();
                        tfSignUpEmail.clear();
                        tfSignUpPass.clear();
                        tfSignUpPassConf.clear();
                        return true;
                    } else {
                        notify("example of email: example@gmail.com");
                        return false; 
                    }
                } else {
                    notify("Name contains '@', ':', '%'");
                    return false;
                }
            }
        }
    }
    
    private boolean isValid(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            notify("Not all fields are filled");
            return false;
        } else {
            tfLogInEmail.clear();
            tfLogInPass.clear();
            return true;
        }
    }
    
    private void getAccess() {
        Stage parentStage = (Stage) labelInform.getScene().getWindow();
        parentStage.close();
        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("WhatsApp Web");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/logo.png")));
        stage.setWidth(1050);
        stage.setHeight(650);
        stage.setMinWidth(900);
        stage.setMinHeight(500);      
        stage.show();
        homePageController.start();
    }
    
    protected void notify (String notification) {
        labelInform.setText(notification);
    }
        
}